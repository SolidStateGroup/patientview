package org.patientview.service.impl;

import org.apache.commons.dbutils.DbUtils;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.service.FileDataService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FileData;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.FileDataRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 14/11/2014
 */
@Service
public class FileDataServiceImpl extends AbstractServiceImpl<FileDataServiceImpl> implements FileDataService {

    @Inject
    @Named("fhir")
    private DataSource dataSource;

    @Inject
    private FileDataRepository fileDataRepository;

    @Override
    public byte[] base64ToByteArray(String base64) {
        return CommonUtils.base64ToByteArray(base64);
    }

    @Override
    public String byteArrayToBase64(byte[] byteArray) {
        return CommonUtils.byteArrayToBase64(byteArray);
    }

    @Override
    public void delete(Long id) {
        fileDataRepository.deleteById(id);
    }

    @Override
    public FileData get(Long id) {
        return fileDataRepository.getOne(id);
    }

    @Override
    public boolean userHasFileData(User user, Long fileDataId, ResourceType resourceType)
        throws FhirResourceException {
        // get all FhirLink and check FileData exists for this DocumentReference for this user
        StringBuilder inString = new StringBuilder("'");
        FhirLink[] fhirLinks = user.getFhirLinks().toArray(new FhirLink[user.getFhirLinks().size()]);

        for (FhirLink fhirLink : fhirLinks) {
            if (fhirLink.getActive()) {
                inString.append("");
                inString.append(fhirLink.getResourceId().toString());
                inString.append("','");
            }
        }

        if (inString.length() > 0) {
            inString.delete(inString.length() - 2, inString.length());
            StringBuilder query = new StringBuilder();

            if (resourceType.equals(ResourceType.DocumentReference)) {
                // retrieve Media url if they exist for these subjects for document reference
                query.append("SELECT CONTENT -> 'content' ->> 'url' FROM media WHERE logical_id::TEXT IN ");
                query.append("(SELECT content ->> 'location' ");
                query.append("FROM documentreference ");
                query.append("WHERE content -> 'subject' ->> 'display' IN (");
                query.append(inString.toString());
                query.append("))");
            } else if (resourceType.equals(ResourceType.DiagnosticReport)) {
                // retrieve Media url if they exist for these subjects for diagnostic report
                query = new StringBuilder();
                query.append("SELECT CONTENT -> 'content' ->> 'url' FROM media WHERE logical_id::TEXT IN ");
                query.append("(SELECT CONTENT #> '{image,0}' -> 'link' ->> 'display' ");
                query.append("FROM diagnosticreport ");
                query.append("WHERE content -> 'subject' ->> 'display' IN (");
                query.append(inString.toString());
                query.append("))");
            }

            if (query.length() > 0) {
                Connection connection = null;
                Statement statement = null;
                ResultSet results = null;
                try {
                    connection = dataSource.getConnection();
                    statement = connection.createStatement();
                    results = statement.executeQuery(query.toString());

                    while ((results.next())) {
                        Long foundFileDataId = results.getLong(1);
                        if (fileDataId.equals(foundFileDataId)) {
                            connection.close();
                            return true;
                        }
                    }

                } catch (SQLException e) {
                    try {
                        if (connection != null) {
                            connection.close();
                        }
                    } catch (SQLException e2) {
                        throw new FhirResourceException(e2);
                    }

                    throw new FhirResourceException(e);
                } finally {
                    DbUtils.closeQuietly(results);
                    DbUtils.closeQuietly(statement);
                    DbUtils.closeQuietly(connection);
                }
            }
        }
        return false;
    }
}
