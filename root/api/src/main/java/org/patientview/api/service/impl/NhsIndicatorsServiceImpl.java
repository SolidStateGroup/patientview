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
        LOG.info("Starting get all NHS indicators");

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

        // group codes by type of treatment
        Map<String, List<String>> typeCodeMap = new HashMap<>();
        typeCodeMap.put("Transplant", Arrays.asList("TP"));
        typeCodeMap.put("HD", Arrays.asList("HD"));
        typeCodeMap.put("PD", Arrays.asList("PD"));
        typeCodeMap.put("GEN", Arrays.asList("GEN"));

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

        LOG.info("Done get all NHS indicators");

        if (store) {
            storeNhsIndicators(nhsIndicatorList);
        }

        return nhsIndicatorList;
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


        return nhsIndicators;
    }

    @Override
    public NhsIndicators getNhsIndicators(Long groupId)
            throws ResourceNotFoundException, FhirResourceException {
        Group group = groupRepository.findOne(groupId);
        if (group == null) {
            throw new ResourceNotFoundException("The group could not be found");
        }

        // group codes by type of treatment
        Map<String, List<String>> typeCodeMap = new HashMap<>();
        typeCodeMap.put("Transplant", Arrays.asList("TP"));
        typeCodeMap.put("HD", Arrays.asList("HD"));
        typeCodeMap.put("PD", Arrays.asList("PD"));
        typeCodeMap.put("GEN", Arrays.asList("GEN"));

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
}
