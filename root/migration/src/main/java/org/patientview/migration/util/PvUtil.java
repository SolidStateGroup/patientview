package org.patientview.migration.util;

import org.apache.maven.surefire.shade.org.codehaus.plexus.util.StringUtils;
import org.patientview.Link;
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

        if (!StringUtils.isEmpty(edtaCode.getMedicalLink01())) {
            Link link = new Link();
            link.setDisplayOrder(links.size() + 1);
            link.setName(edtaCode.getMedicalLinkText01());
            link.setLink(edtaCode.getMedicalLink01());
            links.add(link);
        }

        if (!StringUtils.isEmpty(edtaCode.getMedicalLink02())) {
            Link link = new Link();
            link.setDisplayOrder(links.size() + 1);
            link.setName(edtaCode.getMedicalLinkText02());
            link.setLink(edtaCode.getMedicalLink02());
            links.add(link);
        }

        if (!StringUtils.isEmpty(edtaCode.getMedicalLink03())) {
            Link link = new Link();
            link.setDisplayOrder(links.size() + 1);
            link.setName(edtaCode.getMedicalLinkText03());
            link.setLink(edtaCode.getMedicalLink03());
            links.add(link);
        }

        if (!StringUtils.isEmpty(edtaCode.getMedicalLink04())) {
            Link link = new Link();
            link.setDisplayOrder(links.size() + 1);
            link.setName(edtaCode.getMedicalLinkText04());
            link.setLink(edtaCode.getMedicalLink04());
            links.add(link);
        }

        if (!StringUtils.isEmpty(edtaCode.getMedicalLink05())) {
            Link link = new Link();
            link.setDisplayOrder(links.size() + 1);
            link.setName(edtaCode.getMedicalLinkText05());
            link.setLink(edtaCode.getMedicalLink05());
            links.add(link);
        }

        if (!StringUtils.isEmpty(edtaCode.getMedicalLink06())) {
            Link link = new Link();
            link.setDisplayOrder(links.size() + 1);
            link.setName(edtaCode.getMedicalLinkText06());
            link.setLink(edtaCode.getMedicalLink06());
            links.add(link);
        }

        return links;

    }


}
