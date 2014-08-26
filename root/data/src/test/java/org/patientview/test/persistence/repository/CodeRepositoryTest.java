package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Tests concerned with retrieving the correct news for a user.
 *
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class CodeRepositoryTest {

    @Inject
    CodeRepository codeRepository;

    @Inject
    DataTestUtils dataTestUtils;

    User creator;

    @Before
    public void setup () {
        creator = dataTestUtils.createUser("testCreator");
    }

    @Test
    public void testFindAll() {

        Code code = new Code();
        code.setCode("TEST_CODE");
        code.setDescription("a test code");
        code.setCodeType(dataTestUtils.createLookup("READ", LookupTypes.CODE_TYPE));
        code.setStandardType(dataTestUtils.createLookup("STANDARD1", LookupTypes.CODE_STANDARD));
        code.setCreated(new Date());
        code.setCreator(creator);
        codeRepository.save(code);

        Code code2 = new Code();
        code2.setCode("TEST_CODE_2");
        code2.setDescription("a test code");
        code2.setCodeType(dataTestUtils.createLookup("READ", LookupTypes.CODE_TYPE));
        code2.setStandardType(dataTestUtils.createLookup("STANDARD1", LookupTypes.CODE_STANDARD));
        code2.setCreated(new Date());
        code2.setCreator(creator);
        codeRepository.save(code2);

        PageRequest pageable = new PageRequest(0, 1);
        Page<Code> codes = codeRepository.findAllFiltered("%%", pageable);

        // Should get 1 code back and it should be the one that was created
        Assert.assertEquals("There should be 1 code available", 1, codes.getContent().size());
        Assert.assertTrue("The code should be the one created", codes.getContent().get(0).equals(code));
    }

    @Test
    public void testFindAllFiltered() {

        Code code = new Code();
        code.setCode("TEST_CODE_1");
        code.setDescription("test code description 1");
        code.setCodeType(dataTestUtils.createLookup("READ", LookupTypes.CODE_TYPE));
        code.setStandardType(dataTestUtils.createLookup("STANDARD1", LookupTypes.CODE_STANDARD));
        code.setCreated(new Date());
        code.setCreator(creator);
        codeRepository.save(code);

        Code code2 = new Code();
        code2.setCode("ANOTHER_TEST_CODE");
        code2.setDescription("another kind of test code");
        code2.setCodeType(dataTestUtils.createLookup("EDTA", LookupTypes.CODE_TYPE));
        code2.setStandardType(dataTestUtils.createLookup("STANDARD2", LookupTypes.CODE_STANDARD));
        code2.setCreated(new Date());
        code2.setCreator(creator);
        codeRepository.save(code2);

        PageRequest pageable = new PageRequest(0, 999);

        Page<Code> codes = codeRepository.findAllFiltered("%%", pageable);
        Assert.assertEquals("There should be 2 codes available", 2, codes.getContent().size());
    }

    @Test
    public void testFindAllFilterText() {

        Code code = new Code();
        code.setCode("TEST_CODE_1");
        code.setDescription("test code description 1");
        code.setCodeType(dataTestUtils.createLookup("READ", LookupTypes.CODE_TYPE));
        code.setStandardType(dataTestUtils.createLookup("STANDARD1", LookupTypes.CODE_STANDARD));
        code.setCreated(new Date());
        code.setCreator(creator);
        codeRepository.save(code);

        Code code2 = new Code();
        code2.setCode("ANOTHER_TEST_CODE");
        code2.setDescription("another kind of test code");
        code2.setCodeType(dataTestUtils.createLookup("EDTA", LookupTypes.CODE_TYPE));
        code2.setStandardType(dataTestUtils.createLookup("STANDARD2", LookupTypes.CODE_STANDARD));
        code2.setCreated(new Date());
        code2.setCreator(creator);
        codeRepository.save(code2);

        PageRequest pageable = new PageRequest(0, 999);

        Page<Code> codes = codeRepository.findAllFiltered("%READ%".toUpperCase(), pageable);
        Assert.assertEquals("There should be 1 code available", 1, codes.getContent().size());
        Assert.assertTrue("The code should be the one created", codes.getContent().get(0).equals(code));

        codes = codeRepository.findAllFiltered("%STANDARD1%".toUpperCase(), pageable);
        Assert.assertEquals("There should be 1 code available", 1, codes.getContent().size());
        Assert.assertTrue("The code should be the one created", codes.getContent().get(0).equals(code));

        codes = codeRepository.findAllFiltered("%description 1%".toUpperCase(), pageable);
        Assert.assertEquals("There should be 1 code available", 1, codes.getContent().size());
        Assert.assertTrue("The code should be the one created", codes.getContent().get(0).equals(code));

        codes = codeRepository.findAllFiltered("%CODE_1%".toUpperCase(), pageable);
        Assert.assertEquals("There should be 1 code available", 1, codes.getContent().size());
        Assert.assertTrue("The code should be the one created", codes.getContent().get(0).equals(code));
    }

    @Test
    public void testFindAllCodeAndStandardType() {

        Lookup codeType1 = dataTestUtils.createLookup("READ", LookupTypes.CODE_TYPE);
        Lookup codeType2 = dataTestUtils.createLookup("EDTA", LookupTypes.CODE_TYPE);
        Lookup standardType1 = dataTestUtils.createLookup("STANDARD1", LookupTypes.CODE_STANDARD);
        Lookup standardType2 = dataTestUtils.createLookup("STANDARD2", LookupTypes.CODE_STANDARD);

        Code code = new Code();
        code.setCode("TEST_CODE_1");
        code.setDescription("test code description 1");
        code.setCodeType(codeType1);
        code.setStandardType(standardType1);
        code.setCreated(new Date());
        code.setCreator(creator);
        codeRepository.save(code);

        Code code2 = new Code();
        code2.setCode("ANOTHER_TEST_CODE");
        code2.setDescription("another kind of test code");
        code2.setCodeType(codeType2);
        code2.setStandardType(standardType2);
        code2.setCreated(new Date());
        code2.setCreator(creator);
        codeRepository.save(code2);

        PageRequest pageable = new PageRequest(0, 999);

        String[] codeTypes1 = new String[]{codeType1.getId().toString()};
        String[] standardTypes1 = new String[]{standardType1.getId().toString()};

        Page<Code> codes = codeRepository.findAllByCodeTypesFiltered("%%", convertStringArrayToLongs(codeTypes1), pageable);
        Assert.assertEquals("There should be 1 code available", 1, codes.getContent().size());
        Assert.assertTrue("The code should be the one created", codes.getContent().get(0).equals(code));

        codes = codeRepository.findAllByStandardTypesFiltered("%%", convertStringArrayToLongs(standardTypes1), pageable);
        Assert.assertEquals("There should be 1 code available", 1, codes.getContent().size());
        Assert.assertTrue("The code should be the one created", codes.getContent().get(0).equals(code));
    }

    private List<Long> convertStringArrayToLongs(String[] strings) {
        final List<Long> longs = new ArrayList<>();
        for (String string : strings) {
            longs.add(Long.parseLong(string));
        }
        return longs;
    }
}
