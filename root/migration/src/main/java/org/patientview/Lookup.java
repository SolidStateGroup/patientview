package org.patientview;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * TODO Add generics for enum http://www.gabiaxel.com/2011/01/better-enum-mapping-with-hibernate.html
 *
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */
public class Lookup extends AuditModel {

    @Column(name = "value")
    private String value;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lookup_type_id")
    private LookupType lookupType;

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public LookupType getLookupType() {
        return lookupType;
    }

    public void setLookupType(final LookupType lookupType) {
        this.lookupType = lookupType;
    }
}
