package org.patientview.api.job;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.Timer;
import org.patientview.persistence.repository.UserTokenRepository;

import static org.mockito.Mockito.verify;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 31/07/2017
 */
public class DeleteExpiredUserTokenTaskTest {

    @Mock
    UserTokenRepository userTokenRepository;

    @Mock
    Timer timer;

    @InjectMocks
    DeleteExpiredUserTokenTask deleteExpiredUserTokenTask;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDeleteExpiredUserTokenTask() throws Exception {
        deleteExpiredUserTokenTask.deleteExpiredUserTokens();
        verify(userTokenRepository, Mockito.times(1)).deleteExpired();
    }
}
