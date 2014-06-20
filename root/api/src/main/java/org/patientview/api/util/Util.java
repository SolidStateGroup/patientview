package org.patientview.api.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public final class Util {

    private Util() {}

    /**
     * This is convert the Iterable<T> type passed for Spring DAO interface into
     * a more useful typed List.
     *
     * @param iterable
     * @param <T>
     * @return
     */
    public static <T> List<T> iterableToList(Iterable<T> iterable) {

        if (iterable == null) {
            return Collections.emptyList();
        }

        List<T> list = new ArrayList<T>();
        Iterator<T> lookupIterator = iterable.iterator();

        while (lookupIterator.hasNext()) {
            list.add(lookupIterator.next());
        }
        return list;

    }

}
