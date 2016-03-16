package org.patientview.api.model;

import org.patientview.persistence.model.enums.LookupTypes;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 16/03/2016
 */
public class LookupType {

    private Long id;
    private LookupTypes type;
    private String description;
    private Set<Lookup> lookups = new HashSet<>();

    public LookupType() {
    }

    public LookupType(org.patientview.persistence.model.LookupType lookupType) {
        this.id = lookupType.getId();
        this.type = lookupType.getType();
        this.description = lookupType.getDescription();

        if (!CollectionUtils.isEmpty(lookupType.getLookups())) {
            for (org.patientview.persistence.model.Lookup lookup : lookupType.getLookups()) {
                this.lookups.add(new Lookup(lookup));
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LookupTypes getType() {
        return type;
    }

    public void setType(LookupTypes type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Lookup> getLookups() {
        return lookups;
    }

    public void setLookups(Set<Lookup> lookups) {
        this.lookups = lookups;
    }
}
