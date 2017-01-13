package org.patientview.api.model;

import org.patientview.persistence.model.enums.PathwayTypes;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Pathway represents pathway for the User eg Donorview
 */
public class Pathway implements Serializable {

    private Long id;
    private PathwayTypes pathwayType;
    private BaseUser user;
    private BaseUser creator;
    private Map<String, Stage> stages;
    private Date lastUpdate;
    private Date created;

    public Pathway() {
    }

    public Pathway(org.patientview.persistence.model.Pathway pathway) {
        this.id = pathway.getId();
        this.pathwayType = pathway.getPathwayType();
        if (pathway.getUser() != null) {
            setUser(new BaseUser(pathway.getUser()));
        }
        this.created = pathway.getCreated();
        if (pathway.getCreator() != null) {
            setCreator(new BaseUser(pathway.getCreator()));
        }
        this.lastUpdate = pathway.getLastUpdate();

        stages = new HashMap<>();
        for (org.patientview.persistence.model.Stage stage : pathway.getStages()) {
            stages.put(stage.getStageType().getName(), new Stage(stage));
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PathwayTypes getPathwayType() {
        return pathwayType;
    }

    public void setPathwayType(PathwayTypes pathwayType) {
        this.pathwayType = pathwayType;
    }

    public BaseUser getUser() {
        return user;
    }

    public void setUser(BaseUser user) {
        this.user = user;
    }

    public BaseUser getCreator() {
        return creator;
    }

    public void setCreator(BaseUser creator) {
        this.creator = creator;
    }

    public Map<String, Stage> getStages() {
        return stages;
    }

    public void setStages(Map<String, Stage> stages) {
        this.stages = stages;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
