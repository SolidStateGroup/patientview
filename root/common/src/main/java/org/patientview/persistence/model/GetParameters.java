package org.patientview.persistence.model;

import org.patientview.persistence.model.enums.ConversationLabel;

/**
 * GetParameters, used for handling typical parameters in GET requests.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 26/08/2014
 */
public class GetParameters {
    
    // search and pagination
    private String filterText;
    private String page;
    private String size;
    private String sortField;
    private String sortDirection;
    
    // codes
    private String[] standardTypes;
    private String[] codeTypes;
    
    private String[] statuses;
    private String[] groupTypes;
    private String[] groupIds;
    private String[] roleIds;
    private String[] auditActions;
    private String[] featureIds;
    
    // date ranges
    private Long start;
    private Long end;

    // for multi search
    private String searchUsername;
    private String searchForename;
    private String searchSurname;
    private String searchIdentifier;
    private String searchEmail;
    
    // for filtering users by status (e.g. locked, active, inactive)
    private String statusFilter;
    
    // for messaging, filter by label (used for inbox, archive)
    private String[] conversationLabels;
    
    public GetParameters() {
    }

    public String[] getCodeTypes() {
        return codeTypes;
    }

    public void setCodeTypes(String[] codeTypes) {
        this.codeTypes = codeTypes;
    }

    public String getFilterText() {
        return filterText;
    }

    public void setFilterText(String filterText) {
        this.filterText = filterText;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public String[] getStandardTypes() {
        return standardTypes;
    }

    public void setStandardTypes(String[] standardTypes) {
        this.standardTypes = standardTypes;
    }

    public String[] getStatuses() {
        return statuses;
    }

    public void setStatuses(String[] statuses) {
        this.statuses = statuses;
    }

    public String[] getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(String[] groupIds) {
        this.groupIds = groupIds;
    }

    public String[] getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(String[] roleIds) {
        this.roleIds = roleIds;
    }

    public String[] getGroupTypes() {
        return groupTypes;
    }

    public void setGroupTypes(String[] groupTypes) {
        this.groupTypes = groupTypes;
    }

    public String[] getAuditActions() {
        return auditActions;
    }

    public void setAuditActions(String[] auditActions) {
        this.auditActions = auditActions;
    }

    public String[] getFeatureIds() {
        return featureIds;
    }

    public void setFeatureIds(String[] featureIds) {
        this.featureIds = featureIds;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public String getSearchUsername() {
        return searchUsername;
    }

    public void setSearchUsername(String searchUsername) {
        this.searchUsername = searchUsername;
    }

    public String getSearchForename() {
        return searchForename;
    }

    public void setSearchForename(String searchForename) {
        this.searchForename = searchForename;
    }

    public String getSearchSurname() {
        return searchSurname;
    }

    public void setSearchSurname(String searchSurname) {
        this.searchSurname = searchSurname;
    }

    public String getSearchIdentifier() {
        return searchIdentifier;
    }

    public void setSearchIdentifier(String searchIdentifier) {
        this.searchIdentifier = searchIdentifier;
    }

    public String getSearchEmail() {
        return searchEmail;
    }

    public void setSearchEmail(String searchEmail) {
        this.searchEmail = searchEmail;
    }

    public String getStatusFilter() {
        return statusFilter;
    }

    public void setStatusFilter(String statusFilter) {
        this.statusFilter = statusFilter;
    }

    public String[] getConversationLabels() {
        return conversationLabels;
    }

    public void setConversationLabels(String[] conversationLabels) {
        this.conversationLabels = conversationLabels;
    }
}
