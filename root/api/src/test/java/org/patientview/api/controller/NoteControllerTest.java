package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.model.Note;
import org.patientview.api.service.NoteService;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.NoteTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.test.util.TestUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.verify;

/**
 * Unit test for Note endpoints
 */
public class NoteControllerTest {

    @Mock
    NoteService noteService;
    private ObjectMapper mapper = new ObjectMapper();
    @InjectMocks
    private NoteController noteController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(noteController).build();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testCreateNote() throws Exception {
        Group groupPatient = TestUtils.createGroup("GROUP1");
        Role rolePatient = TestUtils.createRole(RoleName.PATIENT);
        User patient = TestUtils.createUser("testUser");
        GroupRole groupRolePatient = TestUtils.createGroupRole(rolePatient, groupPatient, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        Note note = new Note();
        note.setBody("Test Note");
        note.setNoteType(NoteTypes.DONORVIEW);

        mockMvc.perform(MockMvcRequestBuilders.post("/user/" + patient.getId() + "/notes")
                .content(mapper.writeValueAsString(note))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testGetNote() throws Exception {
        Group groupPatient = TestUtils.createGroup("GROUP1");
        Role rolePatient = TestUtils.createRole(RoleName.PATIENT);
        User patient = TestUtils.createUser("testUser");
        GroupRole groupRolePatient = TestUtils.createGroupRole(rolePatient, groupPatient, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        long noteId = 123;
        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + patient.getId() + "/notes/" + noteId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(noteService, Mockito.times(1)).getNote(patient.getId(), noteId);
    }

    @Test
    public void testUpdateNote() throws Exception {
        Group groupPatient = TestUtils.createGroup("GROUP1");
        Role rolePatient = TestUtils.createRole(RoleName.PATIENT);
        User patient = TestUtils.createUser("testUser");
        GroupRole groupRolePatient = TestUtils.createGroupRole(rolePatient, groupPatient, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        org.patientview.api.model.Note note = new org.patientview.api.model.Note();
        note.setBody("Test Note");
        note.setNoteType(NoteTypes.DONORVIEW);

        mockMvc.perform(MockMvcRequestBuilders.put("/user/" + patient.getId() + "/notes")
                .content(mapper.writeValueAsString(note))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(noteService, Mockito.times(1)).updateNote(anyLong(), any(Note.class));
    }

    @Test
    public void testDeleteNote() throws Exception {
        Group groupPatient = TestUtils.createGroup("GROUP1");
        Role rolePatient = TestUtils.createRole(RoleName.PATIENT);
        User patient = TestUtils.createUser("testUser");
        GroupRole groupRolePatient = TestUtils.createGroupRole(rolePatient, groupPatient, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.delete("/user/" + patient.getId() + "/notes/" + 123))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(noteService, Mockito.times(1)).removeNote(anyLong(), anyLong());
    }

    @Test
    public void testGetNotes() throws Exception {
        Group groupPatient = TestUtils.createGroup("GROUP1");
        Role rolePatient = TestUtils.createRole(RoleName.PATIENT);
        User patient = TestUtils.createUser("testUser");
        GroupRole groupRolePatient = TestUtils.createGroupRole(rolePatient, groupPatient, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        NoteTypes type = NoteTypes.DONORVIEW;
        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + patient.getId() + "/notes/"
                + NoteTypes.DONORVIEW.toString()+ "/type")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(noteService, Mockito.times(1)).getNotes(patient.getId(), type);
    }
}
