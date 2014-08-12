package org.patientview.api.service;

import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.persistence.model.NewsItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface NewsService extends CrudService<NewsItem> {

    public Page<NewsItem> findByUserId(Long userId, Pageable pageable) throws ResourceNotFoundException;

    NewsItem save(NewsItem newsItem) throws ResourceNotFoundException;

    public void addGroup(Long newsItemId, Long groupId) throws ResourceNotFoundException;
    
    public void removeGroup(Long newsItemId, Long groupId) throws ResourceNotFoundException;

    public void addRole(Long newsItemId, Long roleId) throws ResourceNotFoundException;
    
    public void removeRole(Long newsItemId, Long roleId) throws ResourceNotFoundException;

    public void addGroupAndRole(Long newsItemId, Long groupId, Long roleId) throws ResourceNotFoundException;

}