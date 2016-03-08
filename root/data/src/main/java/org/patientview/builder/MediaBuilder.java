package org.patientview.builder;

import generated.Patientview.Patient.Diagnostics.Diagnostic;
import generated.Patientview.Patient.Letterdetails.Letter;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Attachment;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.Media;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.FhirDocumentReference;

/**
 * This maps between parameters from old PatientView and the new PatientView fhir record
 *
 * Created by jamesr@solidstategroup.com
 * Created on 01/05/2014
 */
public class MediaBuilder {

    private Diagnostic diagnostic;
    private FhirDocumentReference fhirDocumentReference;
    private Letter letter;
    private Media media;

    public MediaBuilder(Diagnostic diagnostic) {
        this.diagnostic = diagnostic;
    }

    public MediaBuilder(FhirDocumentReference fhirDocumentReference) {
        this.fhirDocumentReference = fhirDocumentReference;
    }

    public MediaBuilder(Letter letter) {
        this.letter = letter;
    }

    public void build() throws FhirResourceException {
        media = new Media();
        Attachment attachment = new Attachment();

        // build based on either Letter or Diagnostic
        if (letter != null) {
            // date
            if (letter.getLetterdate() != null) {
                try {
                    DateAndTime dateAndTime = new DateAndTime(CommonUtils.getDateFromString(letter.getLetterdate()));
                    DateTime date = new DateTime();
                    date.setValue(dateAndTime);
                    media.setDateTime(date);
                } catch (NullPointerException npe) {
                    throw new FhirResourceException("Letter timestamp is incorrectly formatted");
                }
            }

            // filename
            if (StringUtils.isNotEmpty(letter.getLetterfilename())) {
                attachment.setTitleSimple(letter.getLetterfilename());
            }

            // file type
            if (StringUtils.isNotEmpty(letter.getLetterfiletype())) {
                attachment.setContentTypeSimple(letter.getLetterfiletype());
            }
        } else if (diagnostic != null) {
            // date
            if (diagnostic.getDiagnosticdate() != null) {
                try {
                    DateAndTime dateAndTime
                            = new DateAndTime(diagnostic.getDiagnosticdate().toGregorianCalendar().getTime());
                    DateTime date = new DateTime();
                    date.setValue(dateAndTime);
                    media.setDateTime(date);
                } catch (NullPointerException npe) {
                    throw new FhirResourceException("Diagnostic timestamp is incorrectly formatted");
                }
            }

            // filename
            if (StringUtils.isNotEmpty(diagnostic.getDiagnosticfilename())) {
                attachment.setTitleSimple(diagnostic.getDiagnosticfilename());
            }

            // file type
            if (StringUtils.isNotEmpty(diagnostic.getDiagnosticfiletype())) {
                attachment.setContentTypeSimple(diagnostic.getDiagnosticfiletype());
            }
        } else if (fhirDocumentReference != null) {
            // date
            if (fhirDocumentReference.getDate() != null) {
                media.setDateTimeSimple(new DateAndTime(fhirDocumentReference.getDate()));
            }

            // filename
            if (StringUtils.isNotEmpty(fhirDocumentReference.getFilename())) {
                attachment.setTitleSimple(fhirDocumentReference.getFilename());
            }

            // file type
            if (StringUtils.isNotEmpty(fhirDocumentReference.getFiletype())) {
                attachment.setContentTypeSimple(fhirDocumentReference.getFiletype());
            }
        }

        media.setContent(attachment);
    }

    public Media getMedia() throws FhirResourceException {
        if (media == null) {
            throw new FhirResourceException("Must have built Media");
        }

        return media;
    }

    public Media setFileDataId(Media media, Long fileDataId) throws FhirResourceException {
        if (media.getContent() == null) {
            throw new FhirResourceException("Must have built Media");
        }

        // reference to patientview FileData object containing binary data
        if (fileDataId != null) {
            media.getContent().setUrlSimple(fileDataId.toString());
        }

        return media;
    }

    public Media setFileSize(Media media, Integer fileSize) throws FhirResourceException {
        if (media.getContent() == null) {
            throw new FhirResourceException("Must have built Media");
        }

        if (fileSize != null) {
            media.getContent().setSizeSimple(fileSize);
        }
        return media;
    }
}
