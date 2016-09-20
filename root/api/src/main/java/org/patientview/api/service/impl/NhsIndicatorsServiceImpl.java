package org.patientview.api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.TransformerUtils;
import org.joda.time.DateTime;
import org.patientview.api.model.NhsIndicators;
import org.patientview.api.service.NhsIndicatorsService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.NhsIndicatorsData;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.NhsIndicatorsRepository;
import org.patientview.persistence.resource.FhirResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
    private FhirLinkRepository fhirLinkRepository;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private LookupRepository lookupRepository;

    @Inject
    private NhsIndicatorsRepository nhsIndicatorsRepository;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public List<NhsIndicators> getAllNhsIndicatorsAndStore(boolean store)
            throws ResourceNotFoundException, FhirResourceException, JsonProcessingException {
        // only get groups of type UNIT
        List<Long> groupTypes = new ArrayList<>();
        Lookup lookup = lookupRepository.findByTypeAndValue(LookupTypes.GROUP, GroupTypes.UNIT.toString());

        if (lookup == null) {
            throw new ResourceNotFoundException("Cannot get lookup");
        }

        groupTypes.add(lookup.getId());
        Page<Group> unitGroups
                = groupRepository.findAllByGroupType("%%", groupTypes, new PageRequest(0, Integer.MAX_VALUE));
        if (CollectionUtils.isEmpty(unitGroups.getContent())) {
            throw new ResourceNotFoundException("Cannot get groups");
        }
        List<Group> groups = unitGroups.getContent();

        List<NhsIndicators> nhsIndicatorList = new ArrayList<>();
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

        // get fhirlink resource id of patients where last_login or current_login in last 3 months
        Date threeMonthsAgo = new DateTime(new Date()).minusMonths(3).toDate();

        for (Group group : groups) {
            if (group.getVisible().equals(true)) {
                nhsIndicatorList.add(getNhsIndicators(group, typeCodeMap, codeMap, threeMonthsAgo));
            }
        }

        if (store) {
            storeNhsIndicators(nhsIndicatorList);
        }

        return nhsIndicatorList;
    }

    private Map<String, List<String>> getTypeCodeMap() {
        // group codes by type of treatment
        Map<String, List<String>> typeCodeMap = new HashMap<>();
        typeCodeMap.put("Transplant", Arrays.asList("TP"));
        typeCodeMap.put("HD", Arrays.asList("HD"));
        typeCodeMap.put("PD", Arrays.asList("PD"));
        typeCodeMap.put("GEN", Arrays.asList("GEN", "PRE"));
        typeCodeMap.put("Total on RRT", Arrays.asList("HD", "PD", "TP"));
        return typeCodeMap;
    }

    @Override
    public NhsIndicators getNhsIndicators(Long groupId) throws ResourceNotFoundException, FhirResourceException {
        Group group = groupRepository.findOne(groupId);
        if (group == null) {
            throw new ResourceNotFoundException("The group could not be found");
        }

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

    private NhsIndicators getNhsIndicators(Group group, Map<String, List<String>> typeCodeMap,
                   Map<String, Code> entityCodeMap, Date loginAfter)
            throws ResourceNotFoundException, FhirResourceException {
        List<Group> groups = new ArrayList<>();

        // if specialty get child groups
        if (group.getGroupType() != null && group.getGroupType().getValue().equals(GroupTypes.SPECIALTY.toString())) {
            // specialty, get children
            groups.addAll(convertIterable(groupRepository.findChildren(group)));
        } else {
            // single group, just add group
            groups.add(group);
        }

        if (groups.isEmpty()) {
            return new NhsIndicators(group.getId());
        }

        // create object to return results
        NhsIndicators nhsIndicators = new NhsIndicators(group.getId());
        nhsIndicators.setCodeMap(entityCodeMap);

        List<FhirLink> fhirLinks = fhirLinkRepository.findByGroups(groups);
        List<FhirLink> fhirLinksLoginAfter = fhirLinkRepository.findByGroupsAndRecentLogin(groups, loginAfter);

        // note: cannot directly get resourceId from FhirLink using JPA due to postgres driver
        List<UUID> uuids = (List<UUID>) CollectionUtils.collect(fhirLinks,
                TransformerUtils.invokerTransformer("getResourceId"));
        List<UUID> uuidsLoginAfter = (List<UUID>) CollectionUtils.collect(fhirLinksLoginAfter,
                TransformerUtils.invokerTransformer("getResourceId"));

        // used when doing NOT IN for encounters that are not in code list
        Set<String> codesSearched = new HashSet<>();

        // iterate through indicators
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

        // get "no clinical data", no fhir link for this group
        nhsIndicators.getData().getIndicatorCount().put("No Clinical Data",
                fhirLinkRepository.findPatientCountWithoutFhirLink(group.getId()));
        nhsIndicators.getData().getIndicatorCountLoginAfter().put("No Clinical Data",
                fhirLinkRepository.findPatientCountWithoutFhirLinkAndRecentLogin(group.getId(), loginAfter));

        // get "no treatment data" (includes patients without fhirlink and patients with fhirlink but no TREATMENT
        // encounters)
        Long countTreatment = (uuids.size() - fhirResource.getCountEncounterTreatmentBySubjectIds(uuids))
                + nhsIndicators.getData().getIndicatorCount().get("No Clinical Data");
        Long countTreatmentLoginAfter
                = (uuidsLoginAfter.size() - fhirResource.getCountEncounterTreatmentBySubjectIds(uuidsLoginAfter))
                + nhsIndicators.getData().getIndicatorCountLoginAfter().get("No Clinical Data");

        nhsIndicators.getData().getIndicatorCount().put("No Treatment Data", countTreatment);
        nhsIndicators.getData().getIndicatorCountLoginAfter().put("No Treatment Data", countTreatmentLoginAfter);

        return nhsIndicators;
    }

    @Override
    public NhsIndicators getNhsIndicatorsByGroupAndDate(Long groupId, Long date)
            throws ResourceNotFoundException, IOException {
        if (!groupRepository.exists(groupId)) {
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

    private void storeNhsIndicators(List<NhsIndicators> nhsIndicatorList) throws JsonProcessingException {
        Date now = new Date();
        List<org.patientview.persistence.model.NhsIndicators> toSave = new ArrayList<>();
        for (NhsIndicators nhsIndicators : nhsIndicatorList) {
            toSave.add(new org.patientview.persistence.model.NhsIndicators(nhsIndicators.getGroupId(),
                    OBJECT_MAPPER.writeValueAsString(nhsIndicators.getData()), now));

        }

        nhsIndicatorsRepository.save(toSave);
    }
}
