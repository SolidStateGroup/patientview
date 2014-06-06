package org.patientview.migration.util;

import org.patientview.Group;
import org.patientview.model.Unit;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public final class PvUtil {

    private PvUtil() {


    }


    public static Group createGroup(Unit unit) {
        Group group = new Group();
        group.setName(unit.getName());
        group.setCode(unit.getUnitcode());
        return group;

    }

}
