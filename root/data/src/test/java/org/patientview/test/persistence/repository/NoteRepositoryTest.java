package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Note;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.NoteTypes;
import org.patientview.persistence.repository.NoteRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 30/12/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class NoteRepositoryTest {

    @Inject
    DataTestUtils dataTestUtils;
    User creator;
    User user;
    @Inject
    private NoteRepository noteRepository;
    private Long noteId;

    @Before
    public void setup() {
        creator = dataTestUtils.createUser("testCreator");
        user = dataTestUtils.createUser("donorUser");

        Note note = new Note();
        note.setNoteType(NoteTypes.DONORVIEW);
        note.setBody("tTest note");
        note.setUser(user);
        note.setCreator(creator);
        Note created = noteRepository.save(note);
        noteId = created.getId();
    }

    @Test
    public void givenNoteRepo_whenSave_thenIdAssigned() {

        Note found = noteRepository.findOne(noteId);
        Assert.assertNotNull("Should have found note", found);
        Assert.assertNotNull("Should have note user", found.getUser());
        Assert.assertNotNull("Should have note type", found.getNoteType());
        Assert.assertNotNull("Should have note body", found.getBody());
        Assert.assertNotNull("Should have created date set", found.getCreated());
        Assert.assertNotNull("Should have creator date set", found.getCreator());
        Assert.assertNotNull("Should have updated date set", found.getLastUpdate());
    }

    @Test
    public void givenNoteRepo_whenFindByUserAndType_correctFound() {

        List<Note> list = noteRepository.findByUserAndNoteType(user, NoteTypes.DONORVIEW);
        Assert.assertTrue("Should have found at least 1 note", list.size() > 0);
    }
}
