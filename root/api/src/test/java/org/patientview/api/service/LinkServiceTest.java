package org.patientview.api.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.LinkServiceImpl;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.LinkRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.test.util.TestUtils;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 15/07/2014
 */
public class LinkServiceTest {

    User creator;

    @Mock
    LinkRepository linkRepository;

    @Mock
    GroupRepository groupRepository;

    @Mock
    CodeRepository codeRepository;

    @Mock
    LookupRepository lookupRepository;

    @InjectMocks
    LinkService linkService = new LinkServiceImpl();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
    }


    /**
     * Test: Create a link that has a code attached and save
     * Fail: The link fails to be created and the associated code is not reattached
     */
    @Test
    public void testCreateLink() {

        LookupType lookupType = TestUtils.createLookupType(LookupTypes.CODE_TYPE);
        Lookup linkType = TestUtils.createLookup(lookupType, "LinkType");
        Code code = TestUtils.createCode("testGroup");
        Link link = TestUtils.createLink(code, "TestLink", linkType);

        when(linkRepository.save(eq(link))).thenReturn(link);
        when(codeRepository.findOne(eq(code.getId()))).thenReturn(code);

        linkService.add(link);


        Assert.assertNotNull("The returned link should not be null", link);
        verify(codeRepository, Mockito.times(1)).findOne(eq(code.getId()));
        verify(linkRepository, Mockito.times(1)).save(eq(link));

    }

}
