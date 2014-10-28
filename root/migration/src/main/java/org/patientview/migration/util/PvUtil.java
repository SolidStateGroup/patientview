package org.patientview.migration.util;

import org.apache.maven.surefire.shade.org.codehaus.plexus.util.StringUtils;
import org.patientview.persistence.model.Link;
import org.patientview.patientview.model.EdtaCode;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public final class PvUtil {

    private PvUtil() {

    }

    public static Set<Link> getLinks(EdtaCode edtaCode) {

        Set<Link> links = new HashSet<Link>();

        if (!StringUtils.isEmpty(edtaCode.getPatientLink01())) {
            Link link = new Link();
            link.setDisplayOrder(links.size() + 1);
            link.setName(edtaCode.getPatientLinkText01());
            link.setLink(edtaCode.getPatientLink01());
            links.add(link);
        }

        if (!StringUtils.isEmpty(edtaCode.getPatientLink02())) {
            Link link = new Link();
            link.setDisplayOrder(links.size() + 1);
            link.setName(edtaCode.getPatientLinkText02());
            link.setLink(edtaCode.getPatientLink02());
            links.add(link);
        }

        if (!StringUtils.isEmpty(edtaCode.getPatientLink03())) {
            Link link = new Link();
            link.setDisplayOrder(links.size() + 1);
            link.setName(edtaCode.getPatientLinkText03());
            link.setLink(edtaCode.getPatientLink03());
            links.add(link);
        }

        if (!StringUtils.isEmpty(edtaCode.getPatientLink04())) {
            Link link = new Link();
            link.setDisplayOrder(links.size() + 1);
            link.setName(edtaCode.getPatientLinkText04());
            link.setLink(edtaCode.getPatientLink04());
            links.add(link);
        }

        if (!StringUtils.isEmpty(edtaCode.getPatientLink05())) {
            Link link = new Link();
            link.setDisplayOrder(links.size() + 1);
            link.setName(edtaCode.getPatientLinkText05());
            link.setLink(edtaCode.getPatientLink05());
            links.add(link);
        }

        if (!StringUtils.isEmpty(edtaCode.getPatientLink06())) {
            Link link = new Link();
            link.setDisplayOrder(links.size() + 1);
            link.setName(edtaCode.getPatientLinkText06());
            link.setLink(edtaCode.getPatientLink06());
            links.add(link);
        }

        return links;
    }
}
