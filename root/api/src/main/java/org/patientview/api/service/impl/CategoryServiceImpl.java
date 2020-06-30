package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.service.CategoryService;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Category;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import java.util.List;

/**
 * Category service, used for creating, deleting, retrieving and modifying Category objects.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 20/06/2016
 */
@Service
public class CategoryServiceImpl extends AbstractServiceImpl<CategoryServiceImpl> implements CategoryService {

    @Inject
    private CategoryRepository categoryRepository;

    @Override
    public Category add(Category category) throws EntityExistsException, ResourceInvalidException {
        if (category.getNumber() == null) {
            throw new ResourceInvalidException("Number must not be empty");
        }
        if (!CollectionUtils.isEmpty(categoryRepository.findByNumber(category.getNumber()))) {
            throw new ResourceInvalidException("Category with this Number already exists");
        }
        if (StringUtils.isEmpty(category.getFriendlyDescription())) {
            throw new ResourceInvalidException("Friendly Description must not be empty");
        }

        return categoryRepository.save(new Category(
                category.getNumber(), category.getIcd10Description(),
                category.getFriendlyDescription(), category.isHidden()));
    }

    @Override
    public void delete(Long categoryId) throws ResourceNotFoundException {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found with ID " + categoryId));
        categoryRepository.delete(category);
    }

    @Override
    public List<Category> getCategories() {
        return categoryRepository.findVisible();
    }

    @Override
    public Page<Category> getCategories(GetParameters getParameters) {
        String size = getParameters.getSize();
        String page = getParameters.getPage();
        String sortField = getParameters.getSortField();
        String sortDirection = getParameters.getSortDirection();
        String filterText = getParameters.getFilterText();

        Integer pageConverted = (StringUtils.isNotEmpty(page)) ? Integer.parseInt(page) : 0;
        Integer sizeConverted = (StringUtils.isNotEmpty(size)) ? Integer.parseInt(size) : Integer.MAX_VALUE;

        PageRequest pageable = createPageRequest(pageConverted, sizeConverted, sortField, sortDirection);

        if (StringUtils.isEmpty(filterText)) {
            filterText = "%%";
        } else {
            filterText = "%" + filterText.trim().toUpperCase() + "%";
        }

        return categoryRepository.findAllFiltered(filterText, pageable);
    }

    @Override
    public Category getCategory(Long categoryId) throws ResourceNotFoundException {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found with ID " + categoryId));
    }

    @Override
    public Category save(Category category) throws ResourceNotFoundException, ResourceInvalidException {
        Category entityCategory = getCategory(category.getId());

        if (category.getNumber() == null) {
            throw new ResourceInvalidException("Number must not be empty");
        }

        List<Category> categories = categoryRepository.findByNumber(category.getNumber());
        if (!CollectionUtils.isEmpty(categories)) {
            if (categories.size() > 1) {
                throw new ResourceInvalidException("Category error, multiple Categories with the same number");
            }
            if (!categories.get(0).getId().equals(category.getId())) {
                throw new ResourceInvalidException("Category with this Number already exists");
            }
        }

        if (StringUtils.isEmpty(category.getFriendlyDescription())) {
            throw new ResourceInvalidException("Friendly Description must not be empty");
        }

        entityCategory.setNumber(category.getNumber());
        entityCategory.setFriendlyDescription(category.getFriendlyDescription());
        entityCategory.setIcd10Description(category.getIcd10Description());
        entityCategory.setHidden(category.isHidden());
        return categoryRepository.save(entityCategory);
    }
}
