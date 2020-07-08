package org.patientview.api.service.impl;

import org.patientview.api.model.UserIdentifier;
import org.patientview.api.service.IdentifierService;
import org.patientview.api.service.UserService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.NonUniqueResultException;
import java.util.List;
import java.util.Optional;

import static java.lang.Integer.parseInt;
import static org.patientview.api.util.ApiUtil.getCurrentUser;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 04/08/2014
 */
@Service
public class IdentifierServiceImpl extends AbstractServiceImpl<IdentifierServiceImpl> implements IdentifierService {

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private LookupRepository lookupRepository;

    @Inject
    private UserService userService;

    private static final String GENERIC_GROUP_CODE = "GENERIC";
    private static final Long CHI_NUMBER_START = 10000010L;
    private static final Long CHI_NUMBER_END = 3199999999L;
    private static final Long HSC_NUMBER_START = 3200000010L;
    private static final Long HSC_NUMBER_END = 3999999999L;
    private static final Long NHS_NUMBER_START = 4000000000L;
    private static final Long NHS_NUMBER_END = 9000000000L;
    private static final int IDENTIFIER_NUMBER_LENGTH = 10;
    private static final int IDENTIFIER_NUMBER_MODULUS = 11;
    private static final int MODULUS_OFFSET = 11;

    public Identifier get(final Long identifierId) throws ResourceNotFoundException, ResourceForbiddenException {

        Identifier identifier = identifierRepository.findById(identifierId)
                .orElseThrow(() -> new ResourceNotFoundException("Identifier does not exist"));

        if (!isMemberOfCurrentUsersGroups(identifier.getUser())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        return identifier;
    }

    public void delete(final Long identifierId) throws ResourceNotFoundException, ResourceForbiddenException {

        Identifier identifier = identifierRepository.findById(identifierId)
                .orElseThrow(() -> new ResourceNotFoundException("Identifier does not exist"));

        if (!isMemberOfCurrentUsersGroups(identifier.getUser())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        // check is not being used by fhirlinks (cannot be deleted)
        if (!CollectionUtils.isEmpty(identifier.getFhirLink())) {
            throw new ResourceForbiddenException("Cannot be deleted, in use by FHIR data");
        }

        // for NHS Number, CHI Number, H&SC Number or Radar se
        if (identifier.getIdentifierType().getValue().equals(IdentifierTypes.NHS_NUMBER.toString())
                || identifier.getIdentifierType().getValue().equals(IdentifierTypes.CHI_NUMBER.toString())
                || identifier.getIdentifierType().getValue().equals(IdentifierTypes.HSC_NUMBER.toString())
                || identifier.getIdentifierType().getValue().equals(IdentifierTypes.RADAR_NUMBER.toString())) {

            userService.sendUserUpdatedGroupNotification(identifier.getUser(), true);
        }

        identifierRepository.deleteById(identifierId);
    }

    public void save(Identifier identifier)
            throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException {

        Identifier entityIdentifier = identifierRepository.findById(identifier.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Identifier does not exist"));

        // should only ever get 1
        List<Identifier> existingIdentifiers = identifierRepository.findByValue(identifier.getIdentifier());

        if (!CollectionUtils.isEmpty(existingIdentifiers) && !existingIdentifiers.get(0).equals(entityIdentifier)) {
            throw new EntityExistsException("Cannot save Identifier, another Identifier with the same "
                    + "value already exists");
        }

        if (!isMemberOfCurrentUsersGroups(entityIdentifier.getUser())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        entityIdentifier.setIdentifier(identifier.getIdentifier());
        Optional<Lookup> lookupOptional = lookupRepository.findById(identifier.getIdentifierType().getId());
        if (lookupOptional.isPresent()) {
            entityIdentifier.setIdentifierType(lookupOptional.get());
        }
        Identifier saved = identifierRepository.save(entityIdentifier);

        // for NHS Number, CHI Number, H&SC Number or Radar se
        if (saved.getIdentifierType().getValue().equals(IdentifierTypes.NHS_NUMBER.toString())
                || saved.getIdentifierType().getValue().equals(IdentifierTypes.CHI_NUMBER.toString())
                || saved.getIdentifierType().getValue().equals(IdentifierTypes.HSC_NUMBER.toString())
                || saved.getIdentifierType().getValue().equals(IdentifierTypes.RADAR_NUMBER.toString())) {

            userService.sendUserUpdatedGroupNotification(entityIdentifier.getUser(), true);
        }
    }

    public Identifier add(Long userId, Identifier identifier)
            throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        // check Identifier doesn't already exist for another user, should only ever return one
        List<Identifier> entityIdentifiers = identifierRepository.findByValue(identifier.getIdentifier());

        if (!CollectionUtils.isEmpty(entityIdentifiers)) {
            if (!(user.getId().equals(entityIdentifiers.get(0).getUser().getId()))) {
                throw new EntityExistsException("A patient with that identifier is already on the system");
            }
        }

        if (!isMemberOfCurrentUsersGroups(user)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        identifier.setCreator(getCurrentUser());
        user.getIdentifiers().add(identifier);
        identifier.setUser(user);
        Identifier saved = identifierRepository.save(identifier);

        // for NHS Number, CHI Number, H&SC Number or Radar se
        if (saved.getIdentifierType().getValue().equals(IdentifierTypes.NHS_NUMBER.toString())
                || saved.getIdentifierType().getValue().equals(IdentifierTypes.CHI_NUMBER.toString())
                || saved.getIdentifierType().getValue().equals(IdentifierTypes.HSC_NUMBER.toString())
                || saved.getIdentifierType().getValue().equals(IdentifierTypes.RADAR_NUMBER.toString())) {

            userService.sendUserUpdatedGroupNotification(user, true);
        }

        return saved;
    }

    public List<Identifier> getIdentifierByValue(String identifierValue) throws ResourceNotFoundException {
        List<Identifier> identifiers = identifierRepository.findByValue(identifierValue);
        if (identifiers.isEmpty()) {
            throw new ResourceNotFoundException(String.format("Could not find identifier with value %s",
                    identifierValue));
        }
        return identifiers;
    }

    public void validate(UserIdentifier userIdentifier)
            throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException, ResourceInvalidException {

        Long userId = userIdentifier.getUserId();
        Identifier identifier = userIdentifier.getIdentifier();
        boolean dummy = userIdentifier.isDummy();

        if (userId != null) {
            User user = userRepository.findById(userIdentifier.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

            if (!isMemberOfCurrentUsersGroups(user)) {
                throw new ResourceForbiddenException("Forbidden");
            }
        }

        // check Identifier doesn't already exist for other user (should only ever be one result returned)
        try {
            List<Identifier> entityIdentifiers = identifierRepository.findByValue(identifier.getIdentifier());

            if (!CollectionUtils.isEmpty(entityIdentifiers)) {
                for (Identifier entityIdentifier : entityIdentifiers) {
                    if (!entityIdentifier.getUser().getId().equals(userId)) {
                        throw new EntityExistsException("A patient with that identifier is already on the system");
                    }
                }
            }
        } catch (NonUniqueResultException nure) {
            throw new EntityExistsException("A patient with that identifier is already on the system");
        }

        if (!dummy) {
            try {
                // handle updating identifier type
                Lookup foundLookup = lookupRepository.findById(identifier.getIdentifierType().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Could not find Identifier type"));
                identifier.setIdentifierType(foundLookup);
                isValidIdentifier(identifier);
            } catch (ResourceInvalidException rie) {
                throw new ResourceInvalidException("Invalid " + identifier.getIdentifierType().getDescription()
                        + " Identifier. " + rie.getMessage() + ".");
            }
        }
    }

    @Override
    public List<String> findByGroupCode(String code) {
        return identifierRepository.findByGroupCode(code);
    }

    private void isValidIdentifier(Identifier identifier) throws ResourceInvalidException {

        String value = identifier.getIdentifier();
        Long numericValue = 0L;

        if (identifier.getIdentifierType() == null) {
            throw new ResourceInvalidException("Invalid type");
        }

        if (identifier.getIdentifierType().getValue() == null) {
            throw new ResourceInvalidException("Invalid type");
        }

        // for NHS Number, CHI Number, H&SC Number
        if (identifier.getIdentifierType().getValue().equals(IdentifierTypes.NHS_NUMBER.toString())
                || identifier.getIdentifierType().getValue().equals(IdentifierTypes.CHI_NUMBER.toString())
                || identifier.getIdentifierType().getValue().equals(IdentifierTypes.HSC_NUMBER.toString())) {

            // should be numeric
            try {
                numericValue = Long.parseLong(value);
            } catch (NumberFormatException e) {
                throw new ResourceInvalidException("Not a number");
            }

            // should be 10 characters
            if (value.length() != IDENTIFIER_NUMBER_LENGTH) {
                throw new ResourceInvalidException("Incorrect length");
            }
        }

        // NHS Number
        if (identifier.getIdentifierType().getValue().equals(IdentifierTypes.NHS_NUMBER.toString())) {
            // should be in correct range
            if (numericValue < NHS_NUMBER_START || numericValue > NHS_NUMBER_END) {
                throw new ResourceInvalidException(
                        "Should be between " + NHS_NUMBER_START + " and " + NHS_NUMBER_END);
            }

            // should be numeric and pass checksum
            if (!isChecksumValid(value)) {
                throw new ResourceInvalidException("Invalid number");
            }
        }

        // CHI Number
        if (identifier.getIdentifierType().getValue().equals(IdentifierTypes.CHI_NUMBER.toString())) {
            // should be in correct range
            if (numericValue < CHI_NUMBER_START || numericValue > CHI_NUMBER_END) {
                throw new ResourceInvalidException(
                        "Should be between 00" + CHI_NUMBER_START + " and " + CHI_NUMBER_END);
            }

            // should be numeric and pass checksum
            if (!isChecksumValid(value)) {
                throw new ResourceInvalidException("Invalid number");
            }
        }

        // H&SC Number
        if (identifier.getIdentifierType().getValue().equals(IdentifierTypes.HSC_NUMBER.toString())) {
            // should be in correct range
            if (numericValue < HSC_NUMBER_START || numericValue > HSC_NUMBER_END) {
                throw new ResourceInvalidException(
                        "Should be between " + HSC_NUMBER_START + " and " + HSC_NUMBER_END);
            }

            // should be numeric and pass checksum
            if (!isChecksumValid(value)) {
                throw new ResourceInvalidException("Invalid number");
            }
        }
    }

    private boolean isChecksumValid(String identifierNumber) {
        /**
         * Generate the checksum using modulus 11 algorithm
         */
        int checksum = 0;

        try {
            // Multiply each of the first 9 digits by 10-character position (where the left character is in position 0)
            for (int i = 0; i < IDENTIFIER_NUMBER_LENGTH - 1; i++) {
                int value = parseInt(identifierNumber.charAt(i) + "") * (IDENTIFIER_NUMBER_LENGTH - i);
                checksum += value;
            }

            //(modulus 11)
            checksum = MODULUS_OFFSET - checksum % IDENTIFIER_NUMBER_MODULUS;

            if (checksum == MODULUS_OFFSET) {
                checksum = 0;
            }

            // Does checksum match the 10th digit?
            return checksum == parseInt(String.valueOf(identifierNumber.charAt(IDENTIFIER_NUMBER_LENGTH - 1)));

        } catch (NumberFormatException e) {
            return false; // nhsNumber contains letters
        }
    }


    private boolean isMemberOfCurrentUsersGroups(User user) {
        for (GroupRole groupRole : user.getGroupRoles()) {
            if (!groupRole.getGroup().getCode().toUpperCase().equals(GENERIC_GROUP_CODE)) {
                if (isUserMemberOfGroup(getCurrentUser(), groupRole.getGroup())) {
                    return true;
                }
            }
        }

        return false;
    }
}
