package org.patientview.api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.patientview.api.model.NhsIndicators;
import org.patientview.api.service.NhsIndicatorsService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.NhsIndicatorsData;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.NhsIndicatorsRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 13/09/2016
 */
@Service
public class NhsIndicatorsServiceImpl extends AbstractServiceImpl<NhsIndicatorsServiceImpl>
        implements NhsIndicatorsService {

    @Inject
    private CodeRepository codeRepository;

    @Inject
    @Named("patientView")
    private DataSource dataSource;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private LookupRepository lookupRepository;

    @Inject
    private NhsIndicatorsRepository nhsIndicatorsRepository;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Inject
    private UserRepository userRepository;

    @Override
    public List<NhsIndicators> getAllNhsIndicatorsAndStore(boolean store)
            throws ResourceNotFoundException, FhirResourceException, JsonProcessingException {
        LOG.info("Get NHS indicators running (UNIT groups only), with store data: " + store);
        // only get groups of type UNIT
        Lookup lookup = lookupRepository.findByTypeAndValue(LookupTypes.GROUP, GroupTypes.UNIT.toString());

        if (lookup == null) {
            throw new ResourceNotFoundException("Cannot get lookup");
        }

        Page<Group> unitGroups
                = groupRepository.findAllByGroupType("%%", Collections.singletonList(lookup.getId()),
                new PageRequest(0, Integer.MAX_VALUE));

        if (CollectionUtils.isEmpty(unitGroups.getContent())) {
            throw new ResourceNotFoundException("Cannot get groups");
        }

        LOG.info("Get NHS indicators, found " + unitGroups.getContent().size() + " groups");

        // get mapping between display codes and real entity code values, for performance
        Map<String, List<String>> typeCodeMap = getTypeCodeMap();
        List<String> allCodeStrings = new ArrayList<>();
        for (String key : typeCodeMap.keySet()) {
            allCodeStrings.addAll(typeCodeMap.get(key));
        }
        List<Code> codes = codeRepository.findAllByCodes(allCodeStrings);
        Map<String, Code> codeMap = new LinkedHashMap<>();
        for (Code code : codes) {
            codeMap.put(code.getCode(), code);
        }

        // get fhirlink resource id of patients where last_login or current_login in last 3 months
        Date threeMonthsAgo = new DateTime(new Date()).minusMonths(3).toDate();

        LOG.info("Get NHS indicators, date for inactive users: " + threeMonthsAgo.toString());

        List<NhsIndicators> nhsIndicatorList = new ArrayList<>();
        for (Group group : unitGroups.getContent()) {
            if (group.getVisible().equals(true)) {
                nhsIndicatorList.add(getNhsIndicators(group, typeCodeMap, codeMap, threeMonthsAgo));
            }
        }

        if (store) {
            LOG.info("Get NHS indicators, storing data");
            storeNhsIndicators(nhsIndicatorList);
        }

        LOG.info("Get NHS indicators done");

        return nhsIndicatorList;
    }

    private Map<String, List<String>> getTypeCodeMap() {
        // group codes by type of treatment
        Map<String, List<String>> typeCodeMap = new LinkedHashMap<>();
        typeCodeMap.put("GEN", Arrays.asList("GEN", "PRE"));
        typeCodeMap.put("HD", Arrays.asList("HD"));
        typeCodeMap.put("PD", Arrays.asList("PD"));
        typeCodeMap.put("Transplant", Arrays.asList("TP"));
        typeCodeMap.put("Total on RRT", Arrays.asList("HD", "PD", "TP"));
        return typeCodeMap;
    }

    @Override
    public NhsIndicators getNhsIndicators(Long groupId) throws ResourceNotFoundException, FhirResourceException {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        Map<String, List<String>> typeCodeMap = getTypeCodeMap();

        // get map of code to entities, for performance
        List<String> allCodeStrings = new ArrayList<>();
        for (String key : typeCodeMap.keySet()) {
            allCodeStrings.addAll(typeCodeMap.get(key));
        }
        List<Code> codes = codeRepository.findAllByCodes(allCodeStrings);
        Map<String, Code> codeMap = new HashMap<>();
        for (Code code : codes) {
            codeMap.put(code.getCode(), code);
        }

        Date threeMonthsAgo = new DateTime(new Date()).minusMonths(3).toDate();

        return getNhsIndicators(group, typeCodeMap, codeMap, threeMonthsAgo);
    }

    /**
     * Get NHS indicators for a specific group.
     * NOTE: there is a hard limit on parameters of 32767 (2 byte value) passed to a JPA prepared statement using
     * Postgres so any searches for fhir links with a large number of patients will throw an exception.
     * see: https://github.com/pgjdbc/pgjdbc/issues/90
     *
     * @param group         Group to get NHS indicators for
     * @param typeCodeMap   Map of String to List of String containing type code map
     * @param entityCodeMap Map of String to Code containing Code entities used for performance
     * @param loginAfter    Date after which a user must have logged in to be considered active
     * @return NhsIndicators
     * @throws ResourceNotFoundException if Group not found
     * @throws FhirResourceException     if FHIR throws exception
     */
    private NhsIndicators getNhsIndicators(Group group, Map<String, List<String>> typeCodeMap,
                                           Map<String, Code> entityCodeMap, Date loginAfter)
            throws ResourceNotFoundException, FhirResourceException {
        if (group == null) {
            throw new ResourceNotFoundException("Group is null");
        }

        LOG.info("Get NHS indicators, running for group ID: " + group.getId()
                + ", short name: " + group.getShortName());

        // if specialty get child groups
        if (group.getGroupType() != null && group.getGroupType().getValue().equals(GroupTypes.SPECIALTY.toString())) {
            LOG.info("Get NHS indicators (group " + group.getId()
                    + "), is SPECIALTY, returning empty NHSIndicators as only applies to non SPECIALTY");
            return new NhsIndicators(group.getId());
        }

        // create object to return results
        NhsIndicators nhsIndicators = new NhsIndicators(group.getId());
        nhsIndicators.setCodeMap(entityCodeMap);

        // get current users in group (do not include those who moved or were deleted)
        List<Long> userIds = userRepository.findPatientUserIds(group.getId());
        List<Long> userIdsLoginAfter = userRepository.findPatientUserIdsByRecentLogin(group.getId(), loginAfter);

        LOG.info("Get NHS indicators (group " + group.getId() + "), found "
                + userIds.size() + " total patient user IDs, " + userIdsLoginAfter.size()
                + " patient user IDs logged in after " + loginAfter.toString());

        // get resource IDs of users in group
        List<UUID> uuids = getFhirLinkResourceIds(userIds, group.getId());
        List<UUID> uuidsLoginAfter = getFhirLinkResourceIds(userIdsLoginAfter, group.getId());

        LOG.info("Get NHS indicators (group " + group.getId() + "), found "
                + uuids.size() + " total Resource IDs, " + uuidsLoginAfter.size()
                + " Resource IDs logged in after " + loginAfter.toString());

        // used when doing NOT IN for encounters that are not in code list
        Set<String> codesSearched = new HashSet<>();

        // iterate through indicators, e.g. GEN, HD, PD, Transplant, Total on RRT
        for (String indicator : typeCodeMap.keySet()) {
            List<String> codesToSearch = typeCodeMap.get(indicator);
            nhsIndicators.getData().getIndicatorCount().put(indicator,
                    fhirResource.getCountEncounterBySubjectIdsAndCodes(uuids, codesToSearch));
            nhsIndicators.getData().getIndicatorCountLoginAfter().put(indicator,
                    fhirResource.getCountEncounterBySubjectIdsAndCodes(uuidsLoginAfter, codesToSearch));
            nhsIndicators.getData().getIndicatorCodeMap().put(indicator, codesToSearch);
            codesSearched.addAll(codesToSearch);
        }

        // get "other"
        nhsIndicators.getData().getIndicatorCount().put("Other Treatment",
                fhirResource.getCountEncounterBySubjectIdsAndNotCodes(uuids, new ArrayList<>(codesSearched)));
        nhsIndicators.getData().getIndicatorCountLoginAfter().put("Other Treatment",
                fhirResource.getCountEncounterBySubjectIdsAndNotCodes(uuidsLoginAfter, new ArrayList<>(codesSearched)));

        // get no treatment
        nhsIndicators.getData().getIndicatorCount().put("No Treatment Data",
                userIds.size() - fhirResource.getCountEncounterTreatmentBySubjectIds(uuids));
        nhsIndicators.getData().getIndicatorCountLoginAfter().put("No Treatment Data",
                userIdsLoginAfter.size() - fhirResource.getCountEncounterTreatmentBySubjectIds(uuidsLoginAfter));

        LOG.info("Get NHS indicators (group " + group.getId() + "), completed.");

        return nhsIndicators;
    }

    @Override
    public NhsIndicators getNhsIndicatorsByGroupAndDate(Long groupId, Long date)
            throws ResourceNotFoundException, IOException {
        if (!groupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("Group not found");
        }

        org.patientview.persistence.model.NhsIndicators nhsIndicators
                = nhsIndicatorsRepository.findByGroupIdAndDate(groupId, new Date(date));

        if (nhsIndicators == null) {
            throw new ResourceNotFoundException("Could not find NHS Indicators data");
        }

        return convertNhsIndicators(nhsIndicators);
    }

    private NhsIndicators convertNhsIndicators(org.patientview.persistence.model.NhsIndicators nhsIndicators)
            throws IOException {
        NhsIndicators toReturn = new NhsIndicators(nhsIndicators.getGroupId());
        toReturn.setData(OBJECT_MAPPER.readValue(nhsIndicators.getData(), NhsIndicatorsData.class));

        // set codes
        if (toReturn.getData() == null) {
            throw new IOException("No data");
        }
        if (toReturn.getData().getIndicatorCodeMap() == null) {
            throw new IOException("No indicator to code mapping");
        }

        for (String indicator : toReturn.getData().getIndicatorCodeMap().keySet()) {
            for (String code : toReturn.getData().getIndicatorCodeMap().get(indicator)) {
                toReturn.getCodeMap().put(code, codeRepository.findOneByCode(code));
            }
        }

        toReturn.setCreated(nhsIndicators.getCreated());

        return toReturn;
    }

    @Override
    public List<Date> getNhsIndicatorsDates() {
        return nhsIndicatorsRepository.getDates();
    }

    /**
     * Native call to get distinct resource ids from fhir link table given user ids and group.
     *
     * @param userIds List of user IDs
     * @param groupId Long ID of group
     * @return List of UUID resource IDs
     * @throws FhirResourceException thrown if issue querying patientview database
     */
    private List<UUID> getFhirLinkResourceIds(List<Long> userIds, Long groupId) throws FhirResourceException {
        if (userIds.isEmpty()) {
            return new ArrayList<>();
        }

        String sql = "SELECT DISTINCT (resource_id) FROM pv_fhir_link WHERE user_id IN ("
                + StringUtils.join(userIds, ",") + ") AND group_id = " + groupId;

        Connection connection = null;
        List<UUID> resourceIds = new ArrayList<>();

        try {
            connection = dataSource.getConnection();
            java.sql.Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(sql);

            while ((results.next())) {
                resourceIds.add((UUID) results.getObject(1));
            }

            connection.close();
        } catch (SQLException e) {
            // try and close the open connection
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e2) {
                LOG.error("Cannot close connection {}", e2);
                throw new FhirResourceException(e2.getMessage());
            }

            throw new FhirResourceException(e.getMessage());
        }

        return resourceIds;
    }

    private void storeNhsIndicators(List<NhsIndicators> nhsIndicatorList) throws JsonProcessingException {
        Date now = new Date();
        List<org.patientview.persistence.model.NhsIndicators> toSave = new ArrayList<>();
        for (NhsIndicators nhsIndicators : nhsIndicatorList) {
            toSave.add(new org.patientview.persistence.model.NhsIndicators(nhsIndicators.getGroupId(),
                    OBJECT_MAPPER.writeValueAsString(nhsIndicators.getData()), now));

        }

        nhsIndicatorsRepository.saveAll(toSave);
    }
}
