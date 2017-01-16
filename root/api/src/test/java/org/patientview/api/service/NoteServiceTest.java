package org.patientview.api.service;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.NoteServiceImpl;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Note;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.NoteTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.AuditRepository;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.NoteRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for Note service
 */
public class NoteServiceTest {

    User creator;

    @Mock
    AuditRepository auditRepository;

    @Mock
    NoteRepository noteRepository;

    @InjectMocks
    NoteService noteService = new NoteServiceImpl();

    @Mock
    FeatureRepository featureRepository;

    @Mock
    EmailService emailService;

    @Mock
    GroupRepository groupRepository;

    @Mock
    GroupService groupService;

    @Mock
    Properties properties;

    @Mock
    RoleRepository roleRepository;

    @Mock
    UserRepository userRepository;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testCreateNote() throws ResourceNotFoundException, ResourceForbiddenException {

        // user to add note for
        Group groupPatient = TestUtils.createGroup("GROUP1");
        Role rolePatient = TestUtils.createRole(RoleName.PATIENT);
        User patient = TestUtils.createUser("testUser");
        GroupRole groupRolePatient = TestUtils.createGroupRole(rolePatient, groupPatient, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN_API, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        Note note = new Note();
        note.setId(1L);
        note.setBody("Test body");
        note.setUser(patient);
        note.setCreator(creator);
        note.setLastUpdater(creator);
        note.setNoteType(NoteTypes.DONORVIEW);

        org.patientview.api.model.Note apiNote = new org.patientview.api.model.Note(note);

        when(userRepository.findOne(eq(patient.getId()))).thenReturn(patient);
        when(noteRepository.save(any(Note.class))).thenReturn(note);

        noteService.createNote(patient.getId(), apiNote);
        verify(noteRepository, Mockito.times(1)).save(any(Note.class));
    }

    @Test
    public void testGetNote() throws ResourceNotFoundException, ResourceForbiddenException {

        // user to get note for
        Group groupPatient = TestUtils.createGroup("GROUP1");
        Role rolePatient = TestUtils.createRole(RoleName.PATIENT);
        User patient = TestUtils.createUser("testUser");
        GroupRole groupRolePatient = TestUtils.createGroupRole(rolePatient, groupPatient, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);


        Note note = new Note();
        note.setId(1L);
        note.setBody("Test body");
        note.setUser(patient);
        note.setCreator(creator);
        note.setLastUpdater(creator);
        note.setNoteType(NoteTypes.DONORVIEW);

        when(userRepository.findOne(eq(patient.getId()))).thenReturn(patient);
        when(noteRepository.findOne(eq(note.getId()))).thenReturn(note);

        org.patientview.api.model.Note apiNote = noteService.getNote(patient.getId(), note.getId());
        Assert.assertNotNull("Should have found note", apiNote);
        //Assert.assertNotNull("Should have note user", apiNote.getUser());
        Assert.assertNotNull("Should have note type", apiNote.getNoteType());
        Assert.assertNotNull("Should have note body", apiNote.getBody());
        Assert.assertNotNull("Should have creator date set", apiNote.getCreator());
        Assert.assertNotNull("Should have updated by set", apiNote.getLastUpdater());
    }

    @Test
    public void testUpdateNote() throws ResourceNotFoundException, ResourceForbiddenException {

        // user to add note for
        Group groupPatient = TestUtils.createGroup("GROUP1");
        Role rolePatient = TestUtils.createRole(RoleName.PATIENT);
        User patient = TestUtils.createUser("testUser");
        GroupRole groupRolePatient = TestUtils.createGroupRole(rolePatient, groupPatient, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN_API, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        Note note = new Note();
        note.setId(1L);
        note.setBody("Test body");
        note.setUser(patient);
        note.setCreator(creator);
        note.setLastUpdater(creator);
        note.setNoteType(NoteTypes.DONORVIEW);

        org.patientview.api.model.Note apiNote = new org.patientview.api.model.Note(note);

        when(userRepository.findOne(eq(patient.getId()))).thenReturn(patient);
        when(noteRepository.findOne(eq(note.getId()))).thenReturn(note);
        when(noteRepository.save(any(Note.class))).thenReturn(note);

        noteService.updateNote(patient.getId(), apiNote);
        verify(noteRepository, Mockito.times(1)).save(any(Note.class));
    }

    @Test
    public void testRemoveNote() throws ResourceNotFoundException, ResourceForbiddenException {

        // user to add note for
        Group groupPatient = TestUtils.createGroup("GROUP1");
        Role rolePatient = TestUtils.createRole(RoleName.PATIENT);
        User patient = TestUtils.createUser("testUser");
        GroupRole groupRolePatient = TestUtils.createGroupRole(rolePatient, groupPatient, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);

        Note note = new Note();
        note.setId(1L);
        note.setBody("Test body");
        note.setUser(patient);
        note.setCreator(creator);
        note.setLastUpdater(creator);
        note.setNoteType(NoteTypes.DONORVIEW);

        when(userRepository.findOne(eq(patient.getId()))).thenReturn(patient);
        when(noteRepository.findOne(eq(note.getId()))).thenReturn(note);

        noteService.removeNote(patient.getId(), note.getId());
        verify(noteRepository, Mockito.times(1)).delete(any(Note.class));
    }

    @Test
    public void testGetNotes() throws ResourceNotFoundException, ResourceForbiddenException {

        // user to add note for
        Group groupPatient = TestUtils.createGroup("GROUP1");
        Role rolePatient = TestUtils.createRole(RoleName.PATIENT);
        User patient = TestUtils.createUser("testUser");
        GroupRole groupRolePatient = TestUtils.createGroupRole(rolePatient, groupPatient, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);

        List<Note> notes = new ArrayList<>();
        {
            Note note = new Note();
            note.setId(1L);
            note.setBody("Test body");
            note.setUser(patient);
            note.setCreator(creator);
            note.setLastUpdater(creator);
            note.setNoteType(NoteTypes.DONORVIEW);
            notes.add(note);
        }

        {
            Note note = new Note();
            note.setId(2L);
            note.setBody("Test body 2");
            note.setUser(patient);
            note.setCreator(creator);
            note.setLastUpdater(creator);
            note.setNoteType(NoteTypes.DONORVIEW);
            notes.add(note);
        }
        NoteTypes noteType = NoteTypes.DONORVIEW;

        when(userRepository.findOne(eq(patient.getId()))).thenReturn(patient);
        when(noteRepository.findByUserAndNoteType(eq(patient), eq(noteType))).thenReturn(notes);

        List<org.patientview.api.model.Note> noteList = noteService.getNotes(patient.getId(), noteType);
        Assert.assertEquals("Should return 2 notes", 2, noteList.size());
    }
}

