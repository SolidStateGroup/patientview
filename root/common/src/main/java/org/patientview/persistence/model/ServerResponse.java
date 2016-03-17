package org.patientview.persistence.model;

/**
 * For responding to API calls, also extended by others, such as ExternalConversation
 * Created by jamesr@solidstategroup.com
 * Created on 01/09/2014
 */
public class ServerResponse {

    // for returning success and error status
    private String errorMessage;
    private boolean success;
    private String successMessage;

    public ServerResponse() { }

    public ServerResponse(String errorMessage, String successMessage, boolean success) {
        this.errorMessage = errorMessage;
        this.successMessage = successMessage;
        this.success = success;
    }

    public ServerResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }
}
