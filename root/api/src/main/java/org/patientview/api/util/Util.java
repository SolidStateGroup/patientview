package org.patientview.api.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public final class Util {

    private Util() {}

    public static <T> List<T> iterableToList(Iterable<T> iterable) {

        List<T> list = new ArrayList<T>();
        Iterator<T> lookupIterator = iterable.iterator();

        while (lookupIterator.hasNext()) {
            list.add(lookupIterator.next());
        }
        return list;

    }

}
