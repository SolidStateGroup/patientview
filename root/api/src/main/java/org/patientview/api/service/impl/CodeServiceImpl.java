package org.patientview.api.service.impl;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.patientview.api.model.BaseCode;
import org.patientview.api.service.CodeService;
import org.patientview.api.service.NhsChoicesService;
import org.patientview.config.exception.ImportResourceException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.CodeExternalStandard;
import org.patientview.persistence.model.ExternalStandard;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.CodeStandardTypes;
import org.patientview.persistence.model.enums.CodeTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.CodeExternalStandardRepository;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.ExternalStandardRepository;
import org.patientview.persistence.repository.LinkRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Class to control the crud operations of Codes.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 25/06/2014
 */
@Service
public class CodeServiceImpl extends AbstractServiceImpl<CodeServiceImpl> implements CodeService {

    @Inject
    private CodeRepository codeRepository;

    @Inject
    private CodeExternalStandardRepository codeExternalStandardRepository;

    @Inject
    private ExternalStandardRepository externalStandardRepository;

    @Inject
    private LinkRepository linkRepository;

    @Inject
    private LookupRepository lookupRepository;

    @Inject
    private NhsChoicesService nhsChoicesService;

    @Inject
    private Properties properties;

    @Inject
    private UserRepository userRepository;

    @Override
    public Code add(final Code code) throws EntityExistsException, ResourceInvalidException {
        if (code.getCodeType() == null || code.getCodeType().getId() == null) {
            throw new ResourceInvalidException("Code Type must be set");
        }
        if (code.getStandardType() == null || code.getStandardType().getId() == null) {
            throw new ResourceInvalidException("Standard Type must be set");
        }
        if (StringUtils.isEmpty(code.getCode())) {
            throw new ResourceInvalidException("Code must be set");
        }
        if (StringUtils.isEmpty(code.getDescription())) {
            throw new ResourceInvalidException("Name must be set");
        }
        if (codeExists(code)) {
            throw new EntityExistsException("Code already exists with these details");
        }

        Date now = new Date();
        User currentUser = getCurrentUser();
        Code newCode = new Code();

        Set<Link> links = new HashSet<>();
        if (!CollectionUtils.isEmpty(code.getLinks())) {
            links = new HashSet<>(code.getLinks());
        }

        Set<CodeExternalStandard> codeExternalStandards = new HashSet<>();
        if (!CollectionUtils.isEmpty(code.getExternalStandards())) {
            codeExternalStandards = new HashSet<>(code.getExternalStandards());
        }

        // save basic details
        newCode.setCode(code.getCode());
        newCode.setCodeType(code.getCodeType());
        newCode.setDescription(code.getDescription());
        newCode.setFullDescription(code.getFullDescription());
        newCode.setHideFromPatients(code.isHideFromPatients());
        newCode.setPatientFriendlyName(code.getPatientFriendlyName());
        newCode.setStandardType(code.getStandardType());
        newCode.setCreated(now);
        newCode.setCreator(getCurrentUser());
        newCode.setLastUpdate(now);
        newCode.setLastUpdater(getCurrentUser());
        Code entityCode = codeRepository.save(newCode);

        // save links
        for (Link link : links) {
            link.setCode(entityCode);
            link.setCreated(now);
            link.setCreator(currentUser);
            link.setLastUpdate(now);
            link.setLastUpdater(getCurrentUser());
            entityCode.getLinks().add(linkRepository.save(link));
        }

        // save code external standards
        for (CodeExternalStandard codeExternalStandard : codeExternalStandards) {
            codeExternalStandard.setCode(entityCode);
            entityCode.getExternalStandards().add(codeExternalStandardRepository.save(codeExternalStandard));
        }

        return entityCode;
    }

    @Override
    public CodeExternalStandard addCodeExternalStandard(Long codeId, CodeExternalStandard codeExternalStandard)
            throws ResourceNotFoundException {
        Code code = codeRepository.findOne(codeId);

        if (code == null) {
            throw new ResourceNotFoundException("Code not found");
        }

        if (codeExternalStandard.getExternalStandard() == null) {
            throw new ResourceNotFoundException("External standard must be set");
        }

        ExternalStandard externalStandard
                = externalStandardRepository.findOne(codeExternalStandard.getExternalStandard().getId());

        if (externalStandard == null) {
            throw new ResourceNotFoundException("External standard not found");
        }

        String codeString = codeExternalStandard.getCodeString();

        if (StringUtils.isEmpty(codeString)) {
            throw new ResourceNotFoundException("External Standard Code must be set");
        }

        CodeExternalStandard newCodeExternalStandard = new CodeExternalStandard(code, externalStandard, codeString);

        //code.getExternalStandards().add(newCodeExternalStandard);
        code.setLastUpdate(new Date());
        code.setLastUpdater(getCurrentUser());
        codeRepository.save(code);

        return codeExternalStandardRepository.save(newCodeExternalStandard);
    }

    @Override
    public Code cloneCode(final Long codeId) {
        // clone original
        Code entityCode = codeRepository.findOne(codeId);
        Code newCode = (Code) SerializationUtils.clone(entityCode);
        newCode.setCode(newCode.getCode() + "_new");

        // set up links
        newCode.setLinks(new HashSet<Link>());
        for (Link link : entityCode.getLinks()) {
            Link newLink = new Link();
            newLink.setLink(link.getLink());
            newLink.setName(link.getName());
            newLink.setDisplayOrder(link.getDisplayOrder());
            newLink.setCode(newCode);
            newLink.setLinkType(link.getLinkType());
            newLink.setCreator(userRepository.findOne(1L));
            newCode.getLinks().add(newLink);
        }
        newCode.setId(null);
        newCode.setCreated(new Date());
        newCode.setCreator(getCurrentUser());
        newCode.setLastUpdate(newCode.getCreated());
        newCode.setLastUpdater(getCurrentUser());
        return codeRepository.save(newCode);
    }

    private boolean codeExists(Code code) {
        return codeRepository.findOneByCode(code.getCode()) != null;
    }

    @Override
    public void delete(final Long codeId) {
        codeRepository.delete(codeId);
    }

    @Override
    public void deleteCodeExternalStandard(Long codeExternalStandardId) throws ResourceNotFoundException {
        CodeExternalStandard codeExternalStandard = codeExternalStandardRepository.findOne(codeExternalStandardId);
        if (codeExternalStandard == null) {
            throw new ResourceNotFoundException("Code External Standard not found");
        }

        Code code = codeExternalStandard.getCode();
        if (code == null) {
            throw new ResourceNotFoundException("Code not found");
        }

        code.getExternalStandards().remove(codeExternalStandard);
        code.setLastUpdate(new Date());
        code.setLastUpdater(getCurrentUser());
        codeRepository.save(code);

        codeExternalStandardRepository.delete(codeExternalStandardId);
    }

    @Override
    public List<Code> findAllByCodeAndType(String code, Lookup codeType) {
        return codeRepository.findAllByCodeAndType(code, codeType);
    }

    @Override
    public Code get(final Long codeId) throws ResourceNotFoundException {
        Code code = codeRepository.findOne(codeId);
        if (code == null) {
            throw new ResourceNotFoundException("Code does not exist");
        }

        // handle check against NHS Choices, avoid hitting NHS api too much during sync
        if (code.getStandardType().getValue().equals(CodeStandardTypes.PATIENTVIEW.toString())) {
            try {
                // sets introduction url on NhschoicesCondition and adds/updates link on Code if not set in last month
                nhsChoicesService.setIntroductionUrl(code.getCode());
            } catch (ResourceNotFoundException | ImportResourceException e) {
                LOG.info("Error updating Introduction URL Link, continuing: " + e.getMessage());
            }
            try {
                // sets description on NhsChoiceCondition and updates Code fullDescription if available and not
                // already set and not set in last month
                Code updatedCode = nhsChoicesService.setDescription(code.getCode());
                if (updatedCode != null) {
                    // has had description updated
                    return updatedCode;
                }
            } catch (ResourceNotFoundException | ImportResourceException e) {
                LOG.info("Error updating Description, continuing: " + e.getMessage());
            }
        }

        return code;
    }

    @Override
    public Page<Code> getAllCodes(GetParameters getParameters) {

        String size = getParameters.getSize();
        String page = getParameters.getPage();
        String sortField = getParameters.getSortField();
        String sortDirection = getParameters.getSortDirection();
        String[] codeTypes = getParameters.getCodeTypes();
        String[] standardTypes = getParameters.getStandardTypes();
        String filterText = getParameters.getFilterText();

        PageRequest pageable;
        Integer pageConverted = (StringUtils.isNotEmpty(page)) ? Integer.parseInt(page) : 0;
        Integer sizeConverted = (StringUtils.isNotEmpty(size)) ? Integer.parseInt(size) : Integer.MAX_VALUE;

        if (StringUtils.isNotEmpty(sortField) && StringUtils.isNotEmpty(sortDirection)) {
            Sort.Direction direction = Sort.Direction.ASC;
            if (sortDirection.equals("DESC")) {
                direction = Sort.Direction.DESC;
            }

            pageable = new PageRequest(pageConverted, sizeConverted, new Sort(new Sort.Order(direction, sortField)));
        } else {
            pageable = new PageRequest(pageConverted, sizeConverted);
        }

        List<Long> codeTypesList = convertStringArrayToLongs(codeTypes);
        List<Long> standardTypesList = convertStringArrayToLongs(standardTypes);

        if (StringUtils.isEmpty(filterText)) {
            filterText = "%%";
        } else {
            filterText = "%" + filterText.trim().toUpperCase() + "%";
        }

        if (ArrayUtils.isNotEmpty(codeTypes) && ArrayUtils.isNotEmpty(standardTypes)) {
            return codeRepository.findAllByCodeAndStandardTypesFiltered(filterText, codeTypesList,
                    standardTypesList, pageable);
        }
        if (ArrayUtils.isNotEmpty(codeTypes)) {
            return codeRepository.findAllByCodeTypesFiltered(filterText, codeTypesList, pageable);
        }
        if (ArrayUtils.isNotEmpty(standardTypes)) {
            return codeRepository.findAllByStandardTypesFiltered(filterText, standardTypesList, pageable);
        }

        return codeRepository.findAllFiltered(filterText, pageable);
    }

    @Override
    public List<Code> getPatientManagementDiagnoses() {
        List<Code> codes = new ArrayList<>();

        // todo: make generic, currently hardcoded
        String diagnosesCodeString = properties.getProperty("patient.management.diagnoses.codes");

        if (StringUtils.isNotEmpty(diagnosesCodeString)) {
            List<String> diagnosesCodeArr = Arrays.asList(diagnosesCodeString.split(","));
            if (!CollectionUtils.isEmpty(diagnosesCodeArr)) {
                for (String code : diagnosesCodeArr) {
                    Code codeEntity = codeRepository.findOneByCode(code);
                    if (codeEntity != null) {
                        codes.add(codeEntity);
                    }
                }
            }
        }

        return codes;
    }

    @Override
    public List<BaseCode> getPatientViewStandardCodes(String searchTerm) throws ResourceNotFoundException {
        Lookup codeType = lookupRepository.findByTypeAndValue(LookupTypes.CODE_TYPE, CodeTypes.DIAGNOSIS.toString());
        if (codeType == null) {
            throw new ResourceNotFoundException("DIAGNOSIS Code type not found");
        }

        List<Long> codeTypesList = new ArrayList<>();
        codeTypesList.add(codeType.getId());

        Lookup standardType
            = lookupRepository.findByTypeAndValue(LookupTypes.CODE_STANDARD, CodeStandardTypes.PATIENTVIEW.toString());
        if (standardType == null) {
            throw new ResourceNotFoundException("PATIENTVIEW Code standard not found");
        }

        List<Long> standardTypesList = new ArrayList<>();
        standardTypesList.add(standardType.getId());

        if (searchTerm == null) {
            searchTerm = "%%";
        } else {
            searchTerm = "%" + searchTerm.toUpperCase() + "%";
        }

        Page<Code> found = codeRepository.findAllByCodeAndStandardTypesFiltered(searchTerm, codeTypesList,
                standardTypesList, new PageRequest(0, Integer.MAX_VALUE));

        List<BaseCode> reduced = new ArrayList<>();

        if (!CollectionUtils.isEmpty(found.getContent())) {
            for (Code code : found.getContent()) {
                reduced.add(new BaseCode(code));
            }
        }

        return reduced;
    }

    public Code save(final Code code) throws ResourceNotFoundException, EntityExistsException {
        Code entityCode = codeRepository.findOne(code.getId());
        if (entityCode == null) {
            throw new ResourceNotFoundException("Code does not exist");
        }

        // check if another code with this code exists
        Code existingCode = codeRepository.findOneByCode(code.getCode());
        if (codeExists(code) && !(existingCode.getId().equals(code.getId()))) {
            throw new EntityExistsException("Code already exists with this code");
        }

        entityCode.setCode(code.getCode());
        entityCode.setCodeType(code.getCodeType());
        entityCode.setDescription(code.getDescription());
        entityCode.setFullDescription(code.getFullDescription());
        entityCode.setHideFromPatients(code.isHideFromPatients());
        entityCode.setPatientFriendlyName(code.getPatientFriendlyName());
        entityCode.setStandardType(code.getStandardType());
        entityCode.setLastUpdate(new Date());
        entityCode.setLastUpdater(getCurrentUser());
        return codeRepository.save(entityCode);
    }

    @Override
    public void saveCodeExternalStandard(CodeExternalStandard codeExternalStandard) throws ResourceNotFoundException {
        CodeExternalStandard entityCodeExternalStandard
                = codeExternalStandardRepository.findOne(codeExternalStandard.getId());
        if (entityCodeExternalStandard == null) {
            throw new ResourceNotFoundException("Code External Standard not found");
        }

        if (codeExternalStandard.getExternalStandard() == null) {
            throw new ResourceNotFoundException("External Standard must be set");
        }

        ExternalStandard externalStandard
                = externalStandardRepository.findOne(codeExternalStandard.getExternalStandard().getId());

        if (externalStandard == null) {
            throw new ResourceNotFoundException("External Standard not found");
        }

        entityCodeExternalStandard.setCodeString(codeExternalStandard.getCodeString());
        entityCodeExternalStandard.setExternalStandard(externalStandard);
        codeExternalStandardRepository.save(entityCodeExternalStandard);
    }
}
