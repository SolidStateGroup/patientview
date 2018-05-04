package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.config.exception.ImportResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.Map;

/**
 * NHS Choices service, for retrieving data from NHS Choices
 * <p>
 * Created by jamesr@solidstategroup.com
 * Created on 18/01/2016
 */
public interface NhsChoicesService {
    /**
     * Step 3 of update PV Codes, called manually, not part of task, reads xlsx file and generates Category and links
     * between Code and CodeCategory.
     */
    @RoleOnly
    void categoriseConditions();

    /**
     * Get GP details from NHS Choices API, used to update GpMaster url if url is not set.
     *
     * @param practiceCode String code of practice
     * @return Map of details, just url -> "http://www.nhs.uk/somepractice.com"
     */
    Map<String, String> getDetailsByPracticeCode(String practiceCode);

    /**
     * Updates data for Code from NhschoicesCondition.
     * Also updates Link on Code
     *
     * @param code a code to update data for
     * @throws ResourceNotFoundException
     * @throws ImportResourceException
     */
    void updateCodeData(Code code) throws ResourceNotFoundException, ImportResourceException;

    /**
     * Set the NhschoicesCondition description and Code fullDescription using NHS Choices API.
     * Done on get Code with 3s delay to avoid NHS Choices API limits.
     *
     * @param code String code, used for finding NhschoicesCondition and Code
     * @return Code object
     * @throws ResourceNotFoundException
     * @throws ImportResourceException
     * @deprecated NHS Choices api v1 are deprecated use updateCodeData(Code) instead
     */
    Code setDescription(String code) throws ResourceNotFoundException, ImportResourceException;

    /**
     * Set the introduction URL (actual link to NHS Choices website) for NhschoicesCondition by calling NHS Choices API.
     * Also updates Link on Code. Done on get Code with 3s delay to avoid NHS Choices API limits.
     *
     * @param code String code, used for finding NhschoicesCondition and Code
     * @throws ResourceNotFoundException
     * @throws ImportResourceException
     * @deprecated NHS Choices api v1 are deprecated use updateCodeData(Code) instead
     */
    void setIntroductionUrl(String code) throws ResourceNotFoundException, ImportResourceException;

    /**
     * Step 2 of update PV Codes, synchronises NhschoicesConditions with Codes. Secured for call from endpoint.
     * If an NhschoicesCondition has been deleted, marks Code as externallyRemoved = true.
     *
     * @throws ResourceNotFoundException
     */
    @RoleOnly
    void synchroniseConditions() throws ResourceNotFoundException;

    /**
     * Step 2 of update PV Codes, synchronises NhschoicesConditions with Codes.
     * If an NhschoicesCondition has been deleted, marks Code as externallyRemoved = true.
     *
     * @throws ResourceNotFoundException
     */
    void synchroniseConditionsFromJob() throws ResourceNotFoundException;

    /**
     * Step 1 of update PV Codes from NHS Choices, reads from API and stores each condition as NhschoicesCondition.
     * Secured for call from endpoint.
     * Will create new NhschoicesConditions and delete from PV if no longer found in API.
     *
     * @throws ImportResourceException
     */
    @RoleOnly
    void updateConditions() throws ImportResourceException;

    /**
     * Step 1 of update PV Codes from NHS Choices, reads from API and stores each condition as NhschoicesCondition.
     * Will create new NhschoicesConditions and delete from PV if no longer found in API.
     *
     * @throws ImportResourceException
     */
    void updateConditionsFromJob() throws ImportResourceException;

    // testing only
    @RoleOnly
    void updateOrganisations() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException;
}
