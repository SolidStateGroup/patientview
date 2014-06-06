package org.patientview;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.OneToMany;
import java.util.Set;

/**
 * TODO Add generics for enum
 *
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */
public class LookupType extends AuditModel {

    @Column(name = "lookup_type")
    private String type;

    @Column(name = "description")
    private String description;

    @JsonIgnore
    @OneToMany(mappedBy = "lookupType")
    private Set<Lookup> lookups;

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Set<Lookup> getLookups() {
        return lookups;
    }

    public void setLookups(final Set<Lookup> lookups) {
        this.lookups = lookups;
    }
}
