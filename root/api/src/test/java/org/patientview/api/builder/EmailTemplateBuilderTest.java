package org.patientview.api.builder;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.patientview.persistence.model.Email;
import org.patientview.test.util.TestUtils;

import java.util.Properties;

/**
 * Unit test for PathwayBuilder
 */
public class EmailTemplateBuilderTest {

    @Mock
    private Properties properties;

    @Test
    public void testEmailBuilder() throws Exception {


        //when(properties.getProperty((eq("smtp.sender.email")))).thenReturn("test@solidstategroup.com");
        Email email = EmailTemplateBuilder.newBuilder()
                .setUser(TestUtils.createUser("newUser"))
                .setProperties(properties)
                .buildDonorViewEmail()
                .build();

        //when(properties.getProperty((eq("smtp.sender.email")))).thenReturn("test@solidstategroup.com");


        Assert.assertNotNull("Should have created email template", email);
        Assert.assertNotNull("Should have user", email.getSubject());
        Assert.assertNotNull("Should have creator", email.getBody());

        // TODO: fix property set
        //Assert.assertNotNull("Should have creator", email.getSenderName());
        //Assert.assertNotNull("Should have creator", email.getSenderEmail());
    }
}
