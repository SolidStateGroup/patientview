package org.patientview.api.service.impl;

import com.opencsv.CSVReader;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.patientview.api.service.FoodDiaryService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FoodDiary;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.FoodDiaryRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static org.patientview.api.util.ApiUtil.getCurrentUser;

/**
 * Food Diary service, used when patient's enter foods that disagree with them
 *
 * Created by jamesr@solidstategroup.com
 * Created on 21/12/2015
 */
@Service
@Transactional
public class FoodDiaryServiceImpl extends AbstractServiceImpl<FoodDiaryServiceImpl> implements FoodDiaryService {

    @Inject
    private FoodDiaryRepository foodDiaryRepository;

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private UserRepository userRepository;

    @Override
    public void add(Long userId, FoodDiary foodDiary) throws ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        foodDiary.setUser(user);
        foodDiary.setCreator(user);

        foodDiaryRepository.save(foodDiary);
    }

    @Override
    public void delete(Long userId, Long foodDiaryId) throws ResourceNotFoundException, ResourceForbiddenException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        FoodDiary foodDiary = foodDiaryRepository.findById(foodDiaryId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find diary"));

        if (!foodDiary.getUser().equals(user)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        foodDiaryRepository.delete(foodDiary);
    }

    @Override
    public List<FoodDiary> get(Long userId) throws ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        return foodDiaryRepository.findByUser(user);
    }

    @Override
    public FoodDiary update(Long userId, FoodDiary foodDiary)
            throws ResourceNotFoundException, ResourceForbiddenException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        FoodDiary entityFoodDiary = foodDiaryRepository.findById(foodDiary.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Could not find diary"));

        if (!entityFoodDiary.getUser().equals(user)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        entityFoodDiary.setFood(foodDiary.getFood());
        entityFoodDiary.setComment(foodDiary.getComment());
        entityFoodDiary.setDateNutrition(foodDiary.getDateNutrition());
        return foodDiaryRepository.save(entityFoodDiary);
    }

    @Override
    public void migrate(MultipartFile csvFile) throws IOException {
        CSVReader reader = new CSVReader(new InputStreamReader(csvFile.getInputStream()));
        String [] nextLine;

        int success = 0;
        int total = 0;

        while ((nextLine = reader.readNext()) != null) {
            String comment = nextLine[1];
            String food = nextLine[2];
            String nhsNo = nextLine[3];
            String nutritionDate = nextLine[4].replace(" 00:00:00", "");
            DateTime dateTime = new DateTime(nutritionDate);

            List<Identifier> identifiers = identifierRepository.findByValue(nhsNo);
            if (!identifiers.isEmpty()) {
                User patient = identifiers.get(0).getUser();

                FoodDiary foodDiary = new FoodDiary(patient, food, dateTime.toDate());
                if (StringUtils.isNotEmpty(comment)) {
                    foodDiary.setComment(comment);
                }

                foodDiary.setCreator(getCurrentUser());
                foodDiaryRepository.save(foodDiary);
                success++;
            }

            total++;
        }

        System.out.println("Migrated " + success + " out of " + total + " food diary records.");
    }
}
