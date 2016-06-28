package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Category;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import java.util.List;

/**
 * Category service, used for creating, cloning, deleting, retrieving and modifying Category objects.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 20/06/2016
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface CategoryService {

    /**
     * Add new Category
     * @param category Category to add
     * @return Newly added Category
     * @throws EntityExistsException
     * @throws ResourceInvalidException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    Category add(Category category) throws EntityExistsException, ResourceInvalidException;

    /**
     * Delete a Category given ID
     * @param categoryId ID of Category to delete
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void delete(Long categoryId) throws ResourceNotFoundException;

    /**
     * Get all visible Category objects
     * @return List of Category
     */
    List<Category> getCategories();

    /**
     * Get a Page of Category, with pagination parameters (page, size of page etc) passed in as GetParameters object.
     * @param getParameters GetParameters object for pagination properties defined in UI, including page number, size
     * of page etc
     * @return Page of Category objects
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    Page<Category> getCategories(GetParameters getParameters);

    /**
     * Get a single Category given ID
     * @param categoryId ID of Category to retrieve
     * @return Category found
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    Category getCategory(Long categoryId) throws ResourceNotFoundException;

    /**
     * Save a Category
     * @param category Category to save
     * @return Saved Category
     * @throws ResourceNotFoundException
     * @throws ResourceInvalidException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    Category save(Category category) throws ResourceNotFoundException, ResourceInvalidException;
}
