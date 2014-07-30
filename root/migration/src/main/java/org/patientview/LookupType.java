package org.patientview;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.patientview.enums.LookupTypes;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

/**
 * TODO Add generics for enum
 *
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */
@Entity
@Table(name = "pv_lookup_type")
public class LookupType extends AuditModel {

    @Column(name = "lookup_type")
    @Enumerated(EnumType.STRING)
    private LookupTypes type;

    @Column(name = "description")
    private String description;

    @JsonIgnore
    @OneToMany(mappedBy = "lookupType")
    private Set<Lookup> lookups;

    public LookupTypes getType() {
        return type;
    }

    public void setType(final LookupTypes type) {
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
