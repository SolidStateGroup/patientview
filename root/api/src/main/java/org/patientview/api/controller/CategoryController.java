package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.CategoryService;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Category;
import org.patientview.persistence.model.GetParameters;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import java.util.List;

/**
 * RESTful interface for the basic Crud operation for Category objects, associated with Codes.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 20/06/2016
 */
@RestController
@ExcludeFromApiDoc
public class CategoryController extends BaseController<CategoryController> {

    @Inject
    private CategoryService categoryService;

    /**
     * Add new Category
     * @param category Category to add
     * @return Newly added Category
     * @throws EntityExistsException
     * @throws ResourceInvalidException
     */
    @RequestMapping(value = "/categories", method = RequestMethod.POST
            , consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Category> add(@RequestBody Category category)
            throws EntityExistsException, ResourceInvalidException {
        return new ResponseEntity<>(categoryService.add(category), HttpStatus.CREATED);
    }

    /**
     * Delete a Category given ID
     * @param categoryId ID of Category to delete
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/categories/{categoryId}", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void deleteCategory(@PathVariable("categoryId") Long categoryId) throws ResourceNotFoundException {
        categoryService.delete(categoryId);
    }

    /**
     * Get all visible Category objects, used when patients adding own conditions
     * @return List of Category
     */
    @RequestMapping(value = "/categories/all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<Category>> getAllCategories() {
        return new ResponseEntity<>(categoryService.getCategories(), HttpStatus.OK);
    }

    /**
     * Get a Page of Category, with pagination parameters (page, size of page etc) passed in as GetParameters object.
     * @param getParameters GetParameters object for pagination properties defined in UI, including page number, size
     * of page etc
     * @return Page of Category objects
     */
    @RequestMapping(value = "/categories", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Page<Category>> getCategories(GetParameters getParameters) {
        return new ResponseEntity<>(categoryService.getCategories(getParameters), HttpStatus.OK);
    }

    /**
     * Get a single Category given ID
     * @param categoryId ID of Category to retrieve
     * @return Category found
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/categories/{categoryId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Category> getCategory(@PathVariable("categoryId") Long categoryId)
        throws ResourceNotFoundException {
        return new ResponseEntity<>(categoryService.getCategory(categoryId), HttpStatus.OK);
    }

    /**
     * Save a Category
     * @param category Category to save
     * @return Saved Category
     * @throws ResourceNotFoundException
     * @throws ResourceInvalidException
     */
    @RequestMapping(value = "/categories", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Category> save(@RequestBody Category category)
            throws ResourceNotFoundException, ResourceInvalidException {
        return new ResponseEntity<>(categoryService.save(category), HttpStatus.OK);
    }
}
