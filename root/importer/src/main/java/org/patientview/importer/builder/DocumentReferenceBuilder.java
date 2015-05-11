package org.patientview.importer.builder;

import generated.Patientview.Patient.Letterdetails.Letter;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.DocumentReference;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.utils.CommonUtils;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/09/2014
 */
public class DocumentReferenceBuilder {
    private Letter letter;
    private ResourceReference subjectReference;

    public DocumentReferenceBuilder(Letter letter, ResourceReference subjectReference) {
        this.letter = letter;
        this.subjectReference = subjectReference;
    }

    public DocumentReference build() throws FhirResourceException {
        DocumentReference documentReference = new DocumentReference();
        documentReference.setStatusSimple(DocumentReference.DocumentReferenceStatus.current);
        documentReference.setSubject(subjectReference);

        if (StringUtils.isNotEmpty(letter.getLettertype())) {
            CodeableConcept type = new CodeableConcept();
            type.setTextSimple(CommonUtils.cleanSql(letter.getLettertype()));
            documentReference.setType(type);
        }

        if (StringUtils.isNotEmpty(letter.getLettercontent())) {
            documentReference.setDescriptionSimple(CommonUtils.cleanSql(letter.getLettercontent()));
        }

        if (letter.getLetterdate() != null) {
            try {
                DateAndTime dateAndTime = new DateAndTime(letter.getLetterdate().toGregorianCalendar().getTime());
                DateTime date = new DateTime();
                date.setValue(dateAndTime);
                documentReference.setCreated(date);
            } catch (NullPointerException npe) {
                throw new FhirResourceException("Letter timestamp is incorrectly formatted");
            }
        }

        return documentReference;
    }
}
