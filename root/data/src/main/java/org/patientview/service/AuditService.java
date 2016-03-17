package org.patientview.service;

import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Audit;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Audit service, used for creating, modifying, retrieving Audits, used when the security context cannot be used (e.g.
 * Logon)
 *
 * Created by james@solidstategroup.com
 * Created on 06/08/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface AuditService {

    /**
     * Create an Audit item, given required properties.
     * @param auditActions An AuditActions enum representing the type of Audit
     * @param username String username
     * @param actor User who is performing the action, can be a regular User or the importer User etc
     * @param sourceObjectId ID of object being audited
     * @param sourceObjectType AuditObjectTypes type of the object being audited, e.g. Group or User
     * @param group Group, if relevant to Audit action, e.g. adding User to Group
     */
    void createAudit(AuditActions auditActions, String username, User actor,
                     Long sourceObjectId, AuditObjectTypes sourceObjectType, Group group);

    // used by queue processor
    void createAudit(AuditActions auditActions, String identifier, String unitCode,
                     String information, String xml, Long importerUserId);

    /**
     * Remove all Audit entries associated with a User.
     * @param user User to delete Audit entries for
     */
    void deleteUserFromAudit(User user);

    Long getImporterUserId() throws ResourceNotFoundException;

    /**
     * Set xml column to NULL for older audit entries, configured by properties file
     */
    void removeOldAuditXml();

    /**
     * Update an existing Audit object.
     * @param audit Audit object to update
     * @return Updated Audit object
     */
    Audit save(Audit audit);
}
