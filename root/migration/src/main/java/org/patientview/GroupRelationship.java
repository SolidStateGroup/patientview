package org.patientview;

import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * Contains the one level mapping between groups and their parents/children
 *
 * Created by james@solidstategroup.com
 * Created on 08/07/2014
 */
public class GroupRelationship extends RangeModel {

    @OneToOne
    @JoinColumn(name = "source_group_id")
    private Group sourceGroup;

    @OneToOne
    @JoinColumn(name = "object_group_id")
    private Group objectGroup;

    @OneToOne
    @JoinColumn(name = "type_id")
    Lookup lookup;

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

    public Lookup getLookup() {
        return lookup;
    }

    public void setLookup(final Lookup lookup) {
        this.lookup = lookup;
    }
}
