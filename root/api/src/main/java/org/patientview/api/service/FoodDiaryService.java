package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FoodDiary;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Food Diary service, used when patient's enter foods that disagree with them
 *
 * Created by jamesr@solidstategroup.com
 * Created on 21/12/2015
 */
public interface FoodDiaryService {

    /**
     * Add a Food Diary entry for a User
     * @param userId Long User ID of patient to add Food Diary for
     * @param foodDiary FoodDiary object containing food diary information
     * @throws ResourceNotFoundException
     */
    @UserOnly
    void add(Long userId, FoodDiary foodDiary) throws ResourceNotFoundException;

    /**
     * Delete a FoodDiary associated with a User
     * @param userId Long User ID of patient to delete FoodDiary for
     * @param foodDiaryId Long FoodDiary ID to delete
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @UserOnly
    void delete(Long userId, Long foodDiaryId) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get a List of all a User's FoodDiary objects
     * @param userId Long User ID of patient to get Food Diary objects for
     * @return List of FoodDiary
     * @throws ResourceNotFoundException
     */
    @UserOnly
    List<FoodDiary> get(Long userId) throws ResourceNotFoundException;

    /**
     * Update a FoodDiary
     * @param userId Long User ID of User associated with FoodDiary
     * @param foodDiary FoodDiary object to update
     * @return FoodDiary object that has been updated
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @UserOnly
    FoodDiary update(Long userId, FoodDiary foodDiary) throws ResourceNotFoundException, ResourceForbiddenException;

    @RoleOnly
    void migrate(MultipartFile csvFile) throws IOException;
}
