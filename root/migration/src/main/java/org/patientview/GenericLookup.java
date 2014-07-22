package org.patientview;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * Created by james@solidstategroup.com
 * Created on 17/07/2014
 */
public class GenericLookup extends BaseModel {

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lookup_type_id")
    private LookupType lookupType;

    public LookupType getLookupType() {
        return lookupType;
    }

    public void setLookupType(final LookupType lookupType) {
        this.lookupType = lookupType;
    }
}


