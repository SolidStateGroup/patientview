package org.patientview.persistence.model;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 13/08/2014
 */
public interface Editable {

    public boolean isEdit();
    public void setEdit(final boolean edit);

    public boolean isDelete();
    public void setDelete(final boolean delete);
}
