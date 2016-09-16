package org.patientview.api.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.TransformerUtils;
import org.joda.time.DateTime;
import org.patientview.api.model.GroupStatisticTO;
import org.patientview.api.model.NhsIndicators;
import org.patientview.api.service.GroupStatisticService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupStatistic;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.StatisticPeriod;
import org.patientview.persistence.model.enums.StatisticType;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupStatisticRepository;
import org.patientview.persistence.repository.LookupTypeRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.util.Util;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 07/08/2014
 */
@Service
public class GroupStatisticsServiceImpl extends AbstractServiceImpl<GroupStatisticsServiceImpl>
        implements GroupStatisticService {

    @Inject
    private CodeRepository codeRepository;

    @Inject
    private EntityManager entityManager;

    @Inject
    private FhirLinkRepository fhirLinkRepository;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private GroupStatisticRepository groupStatisticRepository;

    @Inject
    private LookupTypeRepository lookupTypeRepository;

    /**
     * Summary view of the statistics month by month
     *
     * @param groupId ID of group to get statistics
     * @return Map by date of group statistics
     * @throws ResourceNotFoundException
     */
    @Override
    public List<GroupStatisticTO> getMonthlyGroupStatistics(final Long groupId)
            throws ResourceNotFoundException {

        Group group = groupRepository.findOne(groupId);
        if (group == null) {
            throw new ResourceNotFoundException("The group could not be found");
        }

        List<GroupStatistic> groupStatistics =
                Util.convertIterable(groupStatisticRepository.findByGroupAndStatisticPeriod(group,
                        StatisticPeriod.MONTH));

        return convertToTransportObject(groupStatistics);
    }

    @Override
    public NhsIndicators getAllNhsIndicators() throws ResourceNotFoundException, FhirResourceException {
        LOG.info("Starting get all NHS indicators");
        List<Group> groups = groupRepository.findAll();

        List<NhsIndicators> nhsIndicators = new ArrayList<>();

        // group codes by type of treatment
        Map<String, List<String>> typeCodeMap = new HashMap<>();
        typeCodeMap.put("Transplant", Arrays.asList("TP", "T"));
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

        for (Group group : groups) {
            nhsIndicators.add(getNhsIndicators(group, typeCodeMap, codeMap));
        }

        LOG.info("Done get all NHS indicators");

        return nhsIndicators.get(1);
    }

    private NhsIndicators getNhsIndicators(Group group, Map<String, List<String>> typeCodeMap,
                       Map<String, Code> codeMap) throws ResourceNotFoundException, FhirResourceException {
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

        // get fhirlink resource id of patients where last_login or current_login in last 3 months
        Date threeMonthsAgo = new DateTime(new Date()).minusMonths(3).toDate();
        List<FhirLink> fhirLinks = fhirLinkRepository.findByGroupsAndRecentLogin(groups, threeMonthsAgo);

        // note: cannot directly get resourceId from FhirLink using JPA due to postgres driver
        List<UUID> uuids = (List<UUID>) CollectionUtils.collect(fhirLinks,
                TransformerUtils.invokerTransformer("getResourceId"));

        // create object to return results
        NhsIndicators nhsIndicators = new NhsIndicators(group.getId());

        // iterate through types
        for (String key : typeCodeMap.keySet()) {
            nhsIndicators.getCodeCount().put(key, 0L);
            List<Code> codesToReturn = new ArrayList<>();
            // for each code in type (e.g. Transplant, other) get Code and count of patients with that treatment
            for (String codeString : typeCodeMap.get(key)) {
                if (codeMap.get(codeString) != null) {
                    nhsIndicators.getCodeCount().put(key, nhsIndicators.getCodeCount().get(key)
                            + fhirResource.getCountEncounterBySubjectIdsAndCode(uuids, codeString));
                    codesToReturn.add(codeMap.get(codeString));
                }
                nhsIndicators.getCodeMap().put(key, codesToReturn);
            }
        }

        return nhsIndicators;
    }

    @Override
    public NhsIndicators getNhsIndicators(Long groupId) throws ResourceNotFoundException, FhirResourceException {
        Group group = groupRepository.findOne(groupId);
        if (group == null) {
            throw new ResourceNotFoundException("The group could not be found");
        }

        List<Group> groups = new ArrayList<>();

        // if specialty get child groups
        if (group.getGroupType() != null && group.getGroupType().getValue().equals(GroupTypes.SPECIALTY.toString())) {
            // specialty, get children
            groups.addAll(convertIterable(groupRepository.findChildren(group)));
        } else {
            // single group, just add group
            groups.add(group);
        }

        // get fhirlink resource id of patients where last_login or current_login in last 3 months
        Date threeMonthsAgo = new DateTime(new Date()).minusMonths(3).toDate();
        List<FhirLink> fhirLinks = fhirLinkRepository.findByGroupsAndRecentLogin(groups, threeMonthsAgo);

        // note: cannot directly get resourceId from FhirLink using JPA due to postgres driver
        List<UUID> uuids = (List<UUID>) CollectionUtils.collect(fhirLinks,
                TransformerUtils.invokerTransformer("getResourceId"));

        // create object to return results
        NhsIndicators nhsIndicators = new NhsIndicators();
        nhsIndicators.setGroupId(group.getId());

        // group codes by type of treatment
        Map<String, List<String>> typeCodeMap = new HashMap<>();
        typeCodeMap.put("Transplant", Arrays.asList("TP", "T"));
        typeCodeMap.put("Other", Arrays.asList("HD", "PD", "GEN"));

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

        // iterate through types
        for (String key : typeCodeMap.keySet()) {
            nhsIndicators.getCodeCount().put(key, 0L);
            List<Code> codesToReturn = new ArrayList<>();
            // for each code in type (e.g. Transplant, other) get Code and count of patients with that treatment
            for (String codeString : typeCodeMap.get(key)) {
                if (codeMap.get(codeString) != null) {
                    nhsIndicators.getCodeCount().put(key, nhsIndicators.getCodeCount().get(key)
                            + fhirResource.getCountEncounterBySubjectIdsAndCode(uuids, codeString));
                    codesToReturn.add(codeMap.get(codeString));
                }
                nhsIndicators.getCodeMap().put(key, codesToReturn);
            }
        }

        return nhsIndicators;
    }

    private List<GroupStatisticTO> convertToTransportObject(List<GroupStatistic> groupStatistics) {

        Map<Long, GroupStatisticTO> statisticTOMap = new HashMap<>();

        for (GroupStatistic groupStatistic : groupStatistics) {

            if (statisticTOMap.get(groupStatistic.getStartDate().getTime()) == null) {
                GroupStatisticTO groupStatisticTO = new GroupStatisticTO();
                groupStatisticTO.setStatistics(new HashMap<StatisticType, BigInteger>());
                statisticTOMap.put(groupStatistic.getStartDate().getTime(), groupStatisticTO);
            }

            StatisticType statisticType = StatisticType.valueOf(groupStatistic.getStatisticType().getValue());

            GroupStatisticTO groupStatisticTO = statisticTOMap.get(groupStatistic.getStartDate().getTime());
            groupStatisticTO.getStatistics().put(statisticType, groupStatistic.getValue());
            groupStatisticTO.setStartDate(groupStatistic.getStartDate());
            groupStatisticTO.setEndDate(groupStatistic.getEndDate());
        }

        // convert to list of group statistics ordered by start date
        List<GroupStatisticTO> statisticTOList = new ArrayList<>();
        for (GroupStatisticTO groupStatisticTO : statisticTOMap.values()) {
            statisticTOList.add(groupStatisticTO);
        }
        Collections.sort(statisticTOList);

        return statisticTOList;
    }

    /**
     * Creates statistics for all the groups. Loop through the statistics and then the groups.
     *
     * @param startDate Date start date of statistics
     * @param endDate Date end date of statistics
     * @param statisticPeriod StatisticsPeriod, DAY, MONTH or CUMULATIVE_MONTH
     */
    public void generateGroupStatistic(Date startDate, Date endDate, StatisticPeriod statisticPeriod) {

        // Create the groupStatistic object which we are going to persist repeatably
        GroupStatistic groupStatistic = new GroupStatistic();
        groupStatistic.setStartDate(startDate);
        groupStatistic.setEndDate(endDate);
        groupStatistic.setStatisticPeriod(statisticPeriod);

        for (Group group : groupRepository.findAll()) {
            groupStatisticRepository.deleteByGroupStartDateAndPeriod(group, startDate, statisticPeriod);
            groupStatistic.setGroup(group);

            for (Lookup lookup : lookupTypeRepository.findByType(LookupTypes.STATISTIC_TYPE).getLookups()) {
                groupStatistic.setStatisticType(lookup);

                Query query = entityManager.createNativeQuery(lookup.getDescription());
                query.setParameter("groupId", group.getId());

                // do start/end date for those with startDate and endDate present
                if (lookup.getDescription().contains("startDate")) {
                    query.setParameter("startDate", startDate);
                }
                if (lookup.getDescription().contains("endDate")) {
                    query.setParameter("endDate", endDate);
                }

                LOG.debug("Process statistic {}", groupStatistic.getStatisticType().getValue());
                // Direct cast as hibernate returns BigInteger
                try {
                    groupStatistic.setValue((BigInteger) query.getSingleResult());
                } catch (Exception sge) {
                    LOG.error("The SQL is invalid ", sge);
                    LOG.error("The SQL is: " + lookup.getDescription());
                }
                groupStatisticRepository.save(groupStatistic);
                groupStatistic = createGroupStatistic(groupStatistic, group);
            }
        }
    }

    @Override
    public void migrateStatistics(Long groupId, List<GroupStatistic> statistics) throws ResourceNotFoundException {

        Group group = groupRepository.findOne(groupId);
        if (group == null) {
            throw new ResourceNotFoundException("Group not found");
        }

        Date latestStartDate = new Date(0);

        for (GroupStatistic groupStatistic : statistics) {
            if (groupStatistic.getStartDate().after(latestStartDate)) {
                latestStartDate = groupStatistic.getStartDate();
            }
        }

        // delete older than latest start date
        groupStatisticRepository.deleteByGroupBeforeStartDateAndPeriod(group, latestStartDate, StatisticPeriod.MONTH);
        groupStatisticRepository.save(statistics);
    }

    // Refresh the group statistic object for the next statistics
    private GroupStatistic createGroupStatistic(GroupStatistic groupStatistic, Group group) {
        GroupStatistic newGroupStatistic = new GroupStatistic();
        newGroupStatistic.setStartDate(groupStatistic.getStartDate());
        newGroupStatistic.setEndDate(groupStatistic.getEndDate());
        newGroupStatistic.setStartDate(groupStatistic.getStartDate());
        newGroupStatistic.setStatisticPeriod(groupStatistic.getStatisticPeriod());
        newGroupStatistic.setGroup(group);
        return newGroupStatistic;
    }
}
