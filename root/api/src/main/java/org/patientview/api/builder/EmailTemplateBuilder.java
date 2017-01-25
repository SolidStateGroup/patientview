package org.patientview.api.builder;

import org.patientview.persistence.model.Email;
import org.patientview.persistence.model.User;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Common email template builder.
 */
@Component
public class EmailTemplateBuilder {

    private EmailTemplateBuilder() {
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private Properties properties;
        private Email result = new Email();
        private User user;

        private Builder() {
        }

        public Builder setProperties(Properties properties) {
            if (properties != null) {
                this.properties = properties;
            }
            return this;
        }

        public Builder setUser(User user) {
            if (user != null) {
                this.user = user;
            }
            return this;
        }

        public Builder setBody(String body) {
            result.setBody(body);
            return this;
        }

        public Builder buildDonorViewEmail() {
            this.result.setRecipients(new String[]{user.getEmail()});
            this.result.setSubject("DonorView - you have an update");

            // build body
            StringBuilder sb = new StringBuilder();
            sb.append("Dear ");
            sb.append(user.getForename());
            sb.append(" ");
            sb.append(user.getSurname());
            sb.append(", <br/><br/>An update has been made to your donor pathway on DonorView. ");
            sb.append("<br/><br/>Please log in to view the update.");
            result.setBody(sb.toString());
            result.setBody(sb.toString());
            return this;
        }

        public Email build() {
            if (properties != null) {
                result.setSenderEmail(properties.getProperty("smtp.sender.email"));
                result.setSenderName(properties.getProperty("smtp.sender.name"));
            }

            return result;
        }
    }
}
