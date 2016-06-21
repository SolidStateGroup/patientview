package org.patientview.builder;

import generated.Patientview.Patient.Letterdetails.Letter;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.DocumentReference;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.FhirDocumentReference;
import uk.org.rixg.Document;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/09/2014
 */
public class DocumentReferenceBuilder {
    private FhirDocumentReference fhirDocumentReference;
    private Document document;
    private Letter letter;
    private ResourceReference subjectReference;

    // PV XML, from importer
    public DocumentReferenceBuilder(Letter letter, ResourceReference subjectReference) {
        this.letter = letter;
        this.subjectReference = subjectReference;
    }

    // UKRDC XML, from importer
    public DocumentReferenceBuilder(Document document, ResourceReference subjectReference) {
        this.document = document;
        this.subjectReference = subjectReference;
    }

    // from import/letter endpoint
    public DocumentReferenceBuilder(FhirDocumentReference fhirDocumentReference, ResourceReference subjectReference) {
        this.fhirDocumentReference = fhirDocumentReference;
        this.subjectReference = subjectReference;
    }

    public DocumentReference build() throws FhirResourceException {
        DocumentReference documentReference = new DocumentReference();
        documentReference.setStatusSimple(DocumentReference.DocumentReferenceStatus.current);
        documentReference.setSubject(this.subjectReference);

        if (this.letter != null) {
            if (StringUtils.isNotEmpty(this.letter.getLettertype())) {
                CodeableConcept type = new CodeableConcept();
                type.setTextSimple(CommonUtils.cleanSql(this.letter.getLettertype()));
                documentReference.setType(type);
            }

            if (StringUtils.isNotEmpty(this.letter.getLettercontent())) {
                documentReference.setDescriptionSimple(CommonUtils.cleanSql(this.letter.getLettercontent()));
            }

            if (this.letter.getLetterdate() != null) {
                try {
                    DateAndTime dateAndTime
                            = new DateAndTime(CommonUtils.getDateFromString(this.letter.getLetterdate()));
                    DateTime date = new DateTime();
                    date.setValue(dateAndTime);
                    documentReference.setCreated(date);
                } catch (NullPointerException npe) {
                    throw new FhirResourceException("Letter timestamp is incorrectly formatted");
                }
            }
        } else if (this.fhirDocumentReference != null) {
            if (StringUtils.isNotEmpty(this.fhirDocumentReference.getType())) {
                CodeableConcept type = new CodeableConcept();
                type.setTextSimple(CommonUtils.cleanSql(this.fhirDocumentReference.getType()));
                documentReference.setType(type);
            }

            if (StringUtils.isNotEmpty(this.fhirDocumentReference.getContent())) {
                documentReference.setDescriptionSimple(CommonUtils.cleanSql(this.fhirDocumentReference.getContent()));
            }

            if (this.fhirDocumentReference.getDate() != null) {
                documentReference.setCreatedSimple(new DateAndTime(this.fhirDocumentReference.getDate()));
            }
        } else if (this.document != null) {
            if (this.document.getDocumentType() != null
                    && StringUtils.isNotEmpty(this.document.getDocumentType().getCode())) {
                CodeableConcept type = new CodeableConcept();
                type.setTextSimple(CommonUtils.cleanSql(this.document.getDocumentType().getCode()));
                documentReference.setType(type);

                // class is new for UKRDC, used to differentiate between letters, images, YOUR_HEALTH_SURVEY, etc
                documentReference.setClass_(type);
            }

            if (this.document.getDocumentTime() != null) {
                DateAndTime dateAndTime
                    = new DateAndTime(this.document.getDocumentTime().toGregorianCalendar().getTime());
                DateTime date = new DateTime();
                date.setValue(dateAndTime);
                documentReference.setCreated(date);
            }
        } else {
            throw new FhirResourceException("cannot build, missing Letter, FhirDocumentReference or Document");
        }

        return documentReference;
    }
}
