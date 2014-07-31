package org.patientview.persistence.model;

import org.patientview.persistence.model.enums.RelationshipTypes;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Contains the one level mapping between groups and their parents/children
 *
 * Created by james@solidstategroup.com
 * Created on 08/07/2014
 */
@Entity
@Table(name = "pv_group_relationship")
public class GroupRelationship extends RangeModel {

    @OneToOne
    @JoinColumn(name = "source_group_id")
    private Group sourceGroup;

    @OneToOne
    @JoinColumn(name = "object_group_id")
    private Group objectGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type")
    private RelationshipTypes relationshipType;

    public Group getSourceGroup() {
        return sourceGroup;
    }

    public void setSourceGroup(final Group sourceGroup) {
        this.sourceGroup = sourceGroup;
    }

    public Group getObjectGroup() {
        return objectGroup;
    }

    public void setObjectGroup(final Group objectGroup) {
        this.objectGroup = objectGroup;
    }

    public RelationshipTypes getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(final RelationshipTypes relationshipType) {
        this.relationshipType = relationshipType;
    }
}
