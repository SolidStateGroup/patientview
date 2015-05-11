package org.patientview.api.service;

import org.patientview.api.model.Audit;
import org.patientview.persistence.model.Group;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.springframework.data.domain.Page;
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
    void createAudit(AuditActions auditActions, String username, org.patientview.persistence.model.User actor,
                            Long sourceObjectId, AuditObjectTypes sourceObjectType, Group group);

    /**
     * Remove all Audit entries associated with a User.
     * @param user User to delete Audit entries for
     */
    void deleteUserFromAudit(User user);

    /**
     * Gets a Page of Audit information, with pagination parameters passed in as GetParameters.
     * @param getParameters GetParameters object for pagination properties defined in UI, including page number, size
     * of page etc
     * @return Page containing a number of Audit objects, each of which has a Date, Action etc
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    Page<Audit> findAll(GetParameters getParameters) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Set xml column to NULL for older audit entries, configured by properties file
     */
    void removeOldAuditXml();

    /**
     * Update an existing Audit object.
     * @param audit Audit object to update
     * @return Updated Audit object
     */
    org.patientview.persistence.model.Audit save(org.patientview.persistence.model.Audit audit);
}
