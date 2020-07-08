package org.patientview.test.persistence.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.NhsIndicators;
import org.patientview.persistence.model.NhsIndicatorsData;
import org.patientview.persistence.repository.NhsIndicatorsRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests for NHS Indicators repository.
 * Created by jamesr@solidstategroup.com
 * Created on 16/09/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class NhsIndicatorsRepositoryTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Inject
    private NhsIndicatorsRepository nhsIndicatorsRepository;

    @Test
    public void testFindAll() throws Exception {

        String indicatorString = "Transplant";

        {
            Map<String, Long> indicatorCount = new HashMap<>();
            indicatorCount.put(indicatorString, 22L);
            Map<String, Long> indicatorCountLoginAfter = new HashMap<>();
            indicatorCountLoginAfter.put(indicatorString, 22L);
            Map<String, List<String>> indicatorCodeMap = new HashMap<>();
            indicatorCodeMap.put(indicatorString, new ArrayList<>(Arrays.asList("TP", "T")));
            NhsIndicatorsData data = new NhsIndicatorsData(indicatorCount, indicatorCountLoginAfter, indicatorCodeMap);
            nhsIndicatorsRepository.save(new NhsIndicators(1L, OBJECT_MAPPER.writeValueAsString(data)));
        }
        {
            Map<String, Long> indicatorCount = new HashMap<>();
            indicatorCount.put(indicatorString, 44L);
            Map<String, Long> indicatorCountLoginAfter = new HashMap<>();
            indicatorCountLoginAfter.put(indicatorString, 22L);
            Map<String, List<String>> indicatorCodeMap = new HashMap<>();
            indicatorCodeMap.put(indicatorString, new ArrayList<>(Arrays.asList("HD", "GEN")));
            NhsIndicatorsData data = new NhsIndicatorsData(indicatorCount, indicatorCountLoginAfter, indicatorCodeMap);
            nhsIndicatorsRepository.save(new NhsIndicators(2L, OBJECT_MAPPER.writeValueAsString(data)));
        }

        List<NhsIndicators> returned = nhsIndicatorsRepository.findAll();

        Assert.assertEquals("Should return 2 NhsIndicators", 2, returned.size());
    }

    @Test
    public void testFindByGroupId() throws Exception {

        String indicatorString = "Transplant";
        NhsIndicators nhsIndicators1;

        {
            Map<String, Long> indicatorCount = new HashMap<>();
            indicatorCount.put(indicatorString, 22L);
            Map<String, Long> indicatorCountLoginAfter = new HashMap<>();
            indicatorCountLoginAfter.put(indicatorString, 22L);
            Map<String, List<String>> indicatorCodeMap = new HashMap<>();
            indicatorCodeMap.put(indicatorString, new ArrayList<>(Arrays.asList("TP", "T")));
            NhsIndicatorsData data = new NhsIndicatorsData(indicatorCount, indicatorCountLoginAfter, indicatorCodeMap);
            nhsIndicators1 = nhsIndicatorsRepository.save(new NhsIndicators(1L, OBJECT_MAPPER.writeValueAsString(data)));
        }
        {
            Map<String, Long> indicatorCount = new HashMap<>();
            indicatorCount.put(indicatorString, 44L);
            Map<String, Long> indicatorCountLoginAfter = new HashMap<>();
            indicatorCountLoginAfter.put(indicatorString, 44L);
            Map<String, List<String>> indicatorCodeMap = new HashMap<>();
            indicatorCodeMap.put(indicatorString, new ArrayList<>(Arrays.asList("HD", "GEN")));
            NhsIndicatorsData data = new NhsIndicatorsData(indicatorCount, indicatorCountLoginAfter, indicatorCodeMap);
            nhsIndicatorsRepository.save(new NhsIndicators(2L, OBJECT_MAPPER.writeValueAsString(data)));
        }

        List<NhsIndicators> returned = nhsIndicatorsRepository.findByGroupId(1L);

        Assert.assertEquals("Should return 1 NhsIndicators", 1, returned.size());
        Assert.assertEquals("Should return correct NhsIndicators (ID)",
                returned.get(0).getId(), nhsIndicators1.getId());
        Assert.assertEquals("Should return correct NhsIndicators (group ID)", returned.get(0).getGroupId(), (Long) 1L);
    }

    @Test
    @Ignore("Date comparison in test not working")
    public void testFindByGroupIdAndDate() throws Exception {

        String indicatorString = "Transplant";
        NhsIndicators nhsIndicators1;
        Date now = new Date();
        Date weekAgo = new DateTime(new Date()).minusWeeks(1).toDate();

        {
            Map<String, Long> indicatorCount = new HashMap<>();
            indicatorCount.put(indicatorString, 22L);
            Map<String, Long> indicatorCountLoginAfter = new HashMap<>();
            indicatorCountLoginAfter.put(indicatorString, 22L);
            Map<String, List<String>> indicatorCodeMap = new HashMap<>();
            indicatorCodeMap.put(indicatorString, new ArrayList<>(Arrays.asList("TP", "T")));
            NhsIndicatorsData data = new NhsIndicatorsData(indicatorCount, indicatorCountLoginAfter, indicatorCodeMap);
            NhsIndicators nhsIndicators = new NhsIndicators(1L, OBJECT_MAPPER.writeValueAsString(data));
            nhsIndicators.setCreated(now);
            nhsIndicators1 = nhsIndicatorsRepository.save(new NhsIndicators(1L, OBJECT_MAPPER.writeValueAsString(data)));
        }
        {
            Map<String, Long> indicatorCount = new HashMap<>();
            indicatorCount.put(indicatorString, 44L);
            Map<String, Long> indicatorCountLoginAfter = new HashMap<>();
            indicatorCountLoginAfter.put(indicatorString, 44L);
            Map<String, List<String>> indicatorCodeMap = new HashMap<>();
            indicatorCodeMap.put(indicatorString, new ArrayList<>(Arrays.asList("HD", "GEN")));
            NhsIndicatorsData data = new NhsIndicatorsData(indicatorCount, indicatorCountLoginAfter, indicatorCodeMap);
            NhsIndicators nhsIndicators = new NhsIndicators(1L, OBJECT_MAPPER.writeValueAsString(data));
            nhsIndicators.setCreated(weekAgo);
            nhsIndicatorsRepository.save(new NhsIndicators(2L, OBJECT_MAPPER.writeValueAsString(data)));
        }

        NhsIndicators returned = nhsIndicatorsRepository.findByGroupIdAndDate(1L, now);

        Assert.assertEquals("Should return correct NhsIndicators (ID)", returned.getId(), nhsIndicators1.getId());
        Assert.assertEquals("Should return correct NhsIndicators (group ID)", returned.getGroupId(), (Long) 1L);
    }

    @Test
    public void testFindOneWithData() throws Exception {

        String indicatorString = "Transplant";
        Long count = 22L;
        NhsIndicators nhsIndicators1;

        Map<String, Long> indicatorCount = new HashMap<>();
        indicatorCount.put(indicatorString, count);
        Map<String, Long> indicatorCountLoginAfter = new HashMap<>();
        indicatorCountLoginAfter.put(indicatorString, count);
        Map<String, List<String>> indicatorCodeMap = new HashMap<>();
        indicatorCodeMap.put(indicatorString, new ArrayList<>(Arrays.asList("TP", "T")));
        NhsIndicatorsData data = new NhsIndicatorsData(indicatorCount, indicatorCountLoginAfter, indicatorCodeMap);
        nhsIndicators1 = nhsIndicatorsRepository.save(new NhsIndicators(1L, OBJECT_MAPPER.writeValueAsString(data)));

        NhsIndicators returned = nhsIndicatorsRepository.findById(nhsIndicators1.getId()).get();
        Assert.assertNotNull("Should return 1 NhsIndicators", returned);
        Assert.assertEquals("Should return correct NhsIndicators (ID)", returned.getId(), nhsIndicators1.getId());

        NhsIndicatorsData returnedData = OBJECT_MAPPER.convertValue(returned.getData(), NhsIndicatorsData.class);
        Assert.assertNotNull("Should return and convert data", returnedData);
        Assert.assertEquals("Should return correct correct indicatorCount key",
                indicatorString, returnedData.getIndicatorCount().keySet().iterator().next());
        Assert.assertEquals("Should return correct correct indicatorCount value",
                count, returnedData.getIndicatorCount().get(indicatorString));
    }
}
