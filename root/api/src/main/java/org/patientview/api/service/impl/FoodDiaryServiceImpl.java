package org.patientview.api.service.impl;

import org.patientview.api.service.FoodDiaryService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FoodDiary;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.FoodDiaryRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * Food Diary service, used when patient's enter foods that disagree with them
 *
 * Created by jamesr@solidstategroup.com
 * Created on 21/12/2015
 */
@Service
public class FoodDiaryServiceImpl implements FoodDiaryService {

    @Inject
    FoodDiaryRepository foodDiaryRepository;

    @Inject
    UserRepository userRepository;

    @Override
    public void add(Long userId, FoodDiary foodDiary) throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        foodDiary.setUser(user);
        foodDiary.setCreator(user);

        foodDiaryRepository.save(foodDiary);
    }

    @Override
    public void delete(Long userId, Long foodDiaryId) throws ResourceNotFoundException, ResourceForbiddenException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        FoodDiary foodDiary = foodDiaryRepository.findOne(foodDiaryId);
        if (foodDiary == null) {
            throw new ResourceNotFoundException("Could not find food diary");
        }

        if (!foodDiary.getUser().equals(user)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        foodDiaryRepository.delete(foodDiary);
    }

    @Override
    public List<FoodDiary> get(Long userId) throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        return foodDiaryRepository.findByUser(user);
    }

    @Override
    public FoodDiary update(Long userId, FoodDiary foodDiary) throws ResourceNotFoundException, ResourceForbiddenException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        FoodDiary entityFoodDiary = foodDiaryRepository.findOne(foodDiary.getId());
        if (entityFoodDiary == null) {
            throw new ResourceNotFoundException("Could not find food diary");
        }

        if (!entityFoodDiary.getUser().equals(user)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        entityFoodDiary.setFood(foodDiary.getFood());
        entityFoodDiary.setComment(foodDiary.getComment());
        entityFoodDiary.setDateNutrition(foodDiary.getDateNutrition());
        return foodDiaryRepository.save(entityFoodDiary);
    }
}
