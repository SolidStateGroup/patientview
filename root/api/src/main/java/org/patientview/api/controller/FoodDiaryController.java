package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.FoodDiaryService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FoodDiary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

/**
 * RESTful interface for managing patient food diaries (FoodDiary)
 *
 * Created by jamesr@solidstategroup.com
 * Created on 21/12/2015
 */
@RestController
@ExcludeFromApiDoc
public class FoodDiaryController extends BaseController<FoodDiaryController> {

    @Inject
    private FoodDiaryService foodDiaryService;

    /**
     * Add a Food Diary entry for a User
     * @param userId Long User ID of patient to add Food Diary for
     * @param foodDiary FoodDiary object containing food diary information
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/fooddiary", method = RequestMethod.POST)
    public void add(@PathVariable("userId") Long userId, @RequestBody FoodDiary foodDiary)
            throws ResourceNotFoundException {
        foodDiaryService.add(userId, foodDiary);
    }

    /**
     * Delete a FoodDiary associated with a User
     * @param userId Long User ID of patient to delete FoodDiary for
     * @param foodDiaryId Long FoodDiary ID to delete
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/fooddiary/{foodDiaryId}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("userId") Long userId, @PathVariable("foodDiaryId") Long foodDiaryId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        foodDiaryService.delete(userId, foodDiaryId);
    }

    /**
     * Get a List of all a User's FoodDiary objects
     * @param userId Long User ID of patient to get Food Diary objects for
     * @return List of FoodDiary
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/fooddiary", method = RequestMethod.GET)
    public ResponseEntity<List<FoodDiary>> get(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(foodDiaryService.get(userId), HttpStatus.OK);
    }

    @RequestMapping(value = "/fooddiary/migrate", method = RequestMethod.POST)
    public void migrate(@RequestParam MultipartFile csvFile) throws IOException {
        foodDiaryService.migrate(csvFile);
    }

    /**
     * Update a FoodDiary
     * @param userId Long User ID of User associated with FoodDiary
     * @param foodDiary FoodDiary object to update
     * @return FoodDiary object that has been updated
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/fooddiary", method = RequestMethod.PUT)
    public ResponseEntity<FoodDiary> update(@PathVariable("userId") Long userId, @RequestBody FoodDiary foodDiary)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(foodDiaryService.update(userId, foodDiary), HttpStatus.OK);
    }
}
