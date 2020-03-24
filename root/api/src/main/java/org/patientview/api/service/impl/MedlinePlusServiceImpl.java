package org.patientview.api.service.impl;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.patientview.api.client.MedlineplusApiClient;
import org.patientview.api.client.MedlineplusResponseJson;
import org.patientview.api.service.LinkService;
import org.patientview.api.service.MedlinePlusService;
import org.patientview.config.exception.ImportResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.CodeExternalStandard;
import org.patientview.persistence.model.ExternalStandard;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ExternalStandardType;
import org.patientview.persistence.model.enums.LinkTypes;
import org.patientview.persistence.repository.CodeExternalStandardRepository;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.ExternalStandardRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.persistence.NonUniqueResultException;
import javax.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.patientview.api.util.ApiUtil.getCurrentUser;

/**
 * MedlinePlusService implementation
 */
@Service
public class MedlinePlusServiceImpl extends AbstractServiceImpl<MedlinePlusServiceImpl> implements MedlinePlusService {

    @Inject
    private CodeExternalStandardRepository codeExternalStandardRepository;

    @Inject
    private CodeRepository codeRepository;

    @Inject
    private ExternalStandardRepository externalStandardRepository;

    @Inject
    private LinkService linkService;

    @Inject
    private LookupRepository lookupRepository;

    @Override
    @Transactional
    public void setLink(Code entityCode) {
        try {

            if (entityCode == null) {
                LOG.error("Missing Code, cannot add Medline Plus link");
                return;
            }

            Set<CodeExternalStandard> codeExternalStandards = new HashSet<>();
            if (!CollectionUtils.isEmpty(entityCode.getExternalStandards())) {
                codeExternalStandards = new HashSet<>(entityCode.getExternalStandards());
            }
            // for each code external standard add or update link
            for (CodeExternalStandard codeExternalStandard : codeExternalStandards) {
                codeExternalStandard.setCode(entityCode);

                setCodeExternalStandardLink(entityCode, codeExternalStandard);
            }
        } catch (Exception e) {
            LOG.error("Failed to add MediaPlus link to Code", e);
        }
    }

    @Override
    @Transactional
    public void setCodeExternalStandardLink(Code entityCode, CodeExternalStandard codeExternalEntity) {
        try {

            if (codeExternalEntity == null || entityCode == null) {
                LOG.error("Missing CodeExternalStandard or Code, cannot add Medline Plus link");
                return;
            }

            Date now = new Date();
            org.patientview.persistence.model.Link existingLink = null;

            // check Link exists already with Medline Plus type
            for (org.patientview.persistence.model.Link link : entityCode.getLinks()) {
                if (link.getLinkType() != null && LinkTypes.MEDLINE_PLUS.id() == link.getLinkType().getId()) {
                    existingLink = link;
                }
            }

            /**
             * Need to check what system to use to query the link ICD-10 or SNOMED-CT.
             * Will bring the same link url though, but still nice to have support
             *
             * Defaults to ICD-10
             */
            MedlineplusApiClient.CodeSystem codeSystem = MedlineplusApiClient.CodeSystem.ICD_10_CM;
            if (MedlineplusApiClient.CodeSystem.SNOMED_CT.nameCode().equals(
                    codeExternalEntity.getExternalStandard().getName())) {
                codeSystem = MedlineplusApiClient.CodeSystem.SNOMED_CT;
            }

            MedlineplusApiClient apiClient = MedlineplusApiClient
                    .newBuilder()
                    .setCodeSystem(codeSystem)
                    .build();
            MedlineplusResponseJson json = apiClient.getLink(codeExternalEntity.getCodeString());

            String linkUrl = null;

            // Deep down in json, need to check all the bits before getting url
            if (json.getFeed() != null
                    && json.getFeed().getEntry() != null
                    && json.getFeed().getEntry().length > 0
                    && json.getFeed().getEntry()[0].getLink().length > 0) {

                linkUrl = json.getFeed().getEntry()[0].getLink()[0].getHref();
                User currentUser = getCurrentUser();

                if (existingLink == null) {
                    // should have them already configured
                    Lookup linkType = lookupRepository.findById(LinkTypes.MEDLINE_PLUS.id())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException("Could not find MEDLINE_PLUS link type Lookup"));

                    // no medline plus link exist create one Link
                    Link medlinePlusLink = new Link();

                    medlinePlusLink.setLinkType(linkType);
                    medlinePlusLink.setLink(linkUrl);
                    medlinePlusLink.setName(linkType.getDescription());
                    medlinePlusLink.setCode(entityCode);
                    medlinePlusLink.setCreator(currentUser);
                    medlinePlusLink.setCreated(now);
                    medlinePlusLink.setLastUpdater(currentUser);
                    medlinePlusLink.setLastUpdate(medlinePlusLink.getCreated());

                    if (entityCode.getLinks().isEmpty()) {
                        medlinePlusLink.setDisplayOrder(1);
                    } else {
                        medlinePlusLink.setDisplayOrder(2);
                    }

                    entityCode.getLinks().add(medlinePlusLink);
                    entityCode.setLastUpdater(currentUser);
                } else {
                    // update existing MedlineLink link
                    existingLink.setLink(linkUrl);
                    existingLink.setLastUpdater(currentUser);
                    existingLink.setLastUpdate(now);
                }
                LOG.info("Done medline plus link for code {}", codeExternalEntity.getCodeString());
            } else {
                LOG.error("Could not find medline plus url for {}", codeExternalEntity.getCodeString());
            }

            entityCode.setLastUpdate(now);
            codeRepository.save(entityCode);

            linkService.reorderLinks(entityCode.getCode());

        } catch (Exception e) {
            LOG.error("Failed to add MediaPlus link to Code", e);
        }
    }

    @Override
    @Transactional
    public void syncICD10Codes() throws ResourceNotFoundException, ImportResourceException {

        LOG.info("Synchronising Nhschoices codes with ICD-10 codes");
        long start = System.currentTimeMillis();
        int syncCount = 0;
        try {
            URL filePath = Thread.currentThread().getContextClassLoader().getResource(
                    "nhschoices/pv_nhschoices_ICD10_coding.xlsx");

            File file = new File(filePath.toURI());
            FileInputStream inputStream = new FileInputStream(new File(file.getAbsolutePath()));

            Workbook workbook = new XSSFWorkbook(inputStream);
            Iterator<Row> categoryIterator = workbook.getSheetAt(0).iterator();
            int count = 0;


            while (categoryIterator.hasNext()) {
                Row nextRow = categoryIterator.next();

                // first row for data starts at 4
                if (count > 1) {
                    String nhsChoiceCode = getCellContent(nextRow.getCell(0));
                    String icd10Code = getCellContent(nextRow.getCell(24));

                    if ((nhsChoiceCode != null && !nhsChoiceCode.isEmpty())
                            && (icd10Code != null && !icd10Code.isEmpty())) {

                        // find NHSChoices Code
                        Code entityCode = codeRepository.findOneByCode(nhsChoiceCode);
                        if (entityCode == null) {
                            LOG.error("Could not find Code with code {}", nhsChoiceCode);
                            continue;
                        }

                        ExternalStandard externalStandard
                                = externalStandardRepository.findById(ExternalStandardType.ICD_10.id())
                                .orElseThrow(() -> new ResourceNotFoundException("External standard not found"));

                        // check for ICD-10 CodeExternalStandard if in the Code already
                        Set<CodeExternalStandard> codeExternalStandards = new HashSet<>();
                        if (!CollectionUtils.isEmpty(entityCode.getExternalStandards())) {
                            codeExternalStandards = new HashSet<>(entityCode.getExternalStandards());
                        }

                        // check if we already have ICD-10 code for this Code
                        CodeExternalStandard standardToAdd = null;
                        for (CodeExternalStandard codeExternalStandard : codeExternalStandards) {
                            if (ExternalStandardType.ICD_10.id()
                                    == codeExternalStandard.getExternalStandard().getId()) {
                                standardToAdd = codeExternalStandard;
                            }
                        }

                        // not in the list create new one, otherwise just update the code string
                        if (standardToAdd == null) {
                            standardToAdd = new CodeExternalStandard(entityCode, externalStandard, icd10Code);
                        } else {
                            standardToAdd.setCodeString(icd10Code);
                        }
                        CodeExternalStandard savedExternal = codeExternalStandardRepository.save(standardToAdd);
                        entityCode.setLastUpdate(new Date());
                        entityCode.setLastUpdater(getCurrentUser());
                        codeRepository.save(entityCode);

                        LOG.info("Synced Nhschoices codes {} with code {}", nhsChoiceCode, icd10Code);

                        // sleep for 1 seconds MedlinePlus Connect allows no more than 100
                        // requests per minute per IP address
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            throw new ImportResourceException("Thread interrupted");
                        }

                        // add Medline Plus link
                        setCodeExternalStandardLink(entityCode, savedExternal);

                        syncCount++;
                    }
                }
                count++;
            }


        } catch (URISyntaxException use) {
            LOG.error("URISyntaxException: " + use.getMessage());
        } catch (IOException ioe) {
            LOG.error("IOException: " + ioe.getMessage());
        } catch (NonUniqueResultException nure) {
            LOG.error("NonUniqueResultException: " + nure.getMessage());
        }
        long end = System.currentTimeMillis();
        LOG.info("Done Synchronising Nhschoices codes with ICD-10 codes, total {}, timing {}",
                syncCount, (end - start));
    }

    private String getCellContent(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case Cell.CELL_TYPE_NUMERIC:
                return String.valueOf(Double.valueOf(cell.getNumericCellValue()).intValue());
            default:
                return null;
        }
    }
}
