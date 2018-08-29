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
import org.patientview.persistence.model.FhirDiagnosticReport;
import org.patientview.persistence.model.FhirDocumentReference;
import uk.org.rixg.Document;

/**
 * This maps between parameters from old PatientView and the new PatientView fhir record
 *
 * Created by jamesr@solidstategroup.com
 * Created on 01/05/2014
 */
public class MediaBuilder {

    // PV XML, from importer
    private Diagnostic diagnostic;

    // UKRDC XML, from importer
    private Document document;

    // API importer
    private FhirDocumentReference fhirDocumentReference;

    // API Importer
    private FhirDiagnosticReport fhirDiagnosticReport;

    // PV XML, from importer
    private Letter letter;

    private Media media;

    public MediaBuilder(Diagnostic diagnostic) {
        this.diagnostic = diagnostic;
    }

    public MediaBuilder(Document document) {
        this.document = document;
    }

    public MediaBuilder(FhirDocumentReference fhirDocumentReference) {
        this.fhirDocumentReference = fhirDocumentReference;
    }

    public MediaBuilder(FhirDiagnosticReport fhirDiagnosticReport) {
        this.fhirDiagnosticReport = fhirDiagnosticReport;
    }

    public MediaBuilder(Letter letter) {
        this.letter = letter;
    }

    public void build() throws FhirResourceException {
        this.media = new Media();
        Attachment attachment = new Attachment();

        // build based on object type
        if (this.letter != null) {
            // date
            if (this.letter.getLetterdate() != null) {
                try {
                    DateAndTime dateAndTime
                            = new DateAndTime(CommonUtils.getDateFromString(this.letter.getLetterdate()));
                    DateTime date = new DateTime();
                    date.setValue(dateAndTime);
                    this.media.setDateTime(date);
                } catch (NullPointerException npe) {
                    throw new FhirResourceException("Letter timestamp is incorrectly formatted");
                }
            }

            // filename
            if (StringUtils.isNotEmpty(this.letter.getLetterfilename())) {
                attachment.setTitleSimple(this.letter.getLetterfilename());
            }

            // file type
            if (StringUtils.isNotEmpty(this.letter.getLetterfiletype())) {
                attachment.setContentTypeSimple(this.letter.getLetterfiletype());
            }
        } else if (this.document != null) {
            // date
            if (this.document.getDocumentTime() != null) {
                DateAndTime dateAndTime
                        = new DateAndTime(this.document.getDocumentTime().toGregorianCalendar().getTime());
                DateTime date = new DateTime();
                date.setValue(dateAndTime);
                this.media.setDateTime(date);
            }

            // file type
            if (this.document.getFileType() != null && StringUtils.isNotEmpty(this.document.getFileType())) {
                attachment.setContentTypeSimple(this.document.getFileType().toLowerCase());
            } else {
                attachment.setContentTypeSimple("application/unknown");
            }

            // filename
            if (StringUtils.isNotEmpty(this.document.getDocumentName())) {
                attachment.setTitleSimple(this.document.getDocumentName());
            } else if (this.document.getDocumentType() != null
                    && StringUtils.isNotEmpty(this.document.getDocumentType().getCode())) {
                attachment.setTitleSimple(this.document.getDocumentType().getCode());
            } else if (this.media.getDateTimeSimple() != null){
                attachment.setTitleSimple(this.media.getDateTimeSimple().toString());
            }

            // handle pdf file type by appending to file name
            if (attachment.getContentTypeSimple().equals("application/pdf")) {
                attachment.setTitleSimple(attachment.getTitleSimple() + ".pdf");
            } else if (attachment.getContentTypeSimple().equals("application/msword")) {
                attachment.setTitleSimple(attachment.getTitleSimple() + ".doc");
            } else if (attachment.getContentTypeSimple().equals("application/jpeg")) {
                attachment.setTitleSimple(attachment.getTitleSimple() + ".jpg");
            }
        } else if (this.diagnostic != null) {
            // date
            if (this.diagnostic.getDiagnosticdate() != null) {
                try {
                    DateAndTime dateAndTime
                            = new DateAndTime(this.diagnostic.getDiagnosticdate().toGregorianCalendar().getTime());
                    DateTime date = new DateTime();
                    date.setValue(dateAndTime);
                    this.media.setDateTime(date);
                } catch (NullPointerException npe) {
                    throw new FhirResourceException("Diagnostic timestamp is incorrectly formatted");
                }
            }

            // filename
            if (StringUtils.isNotEmpty(this.diagnostic.getDiagnosticfilename())) {
                attachment.setTitleSimple(this.diagnostic.getDiagnosticfilename());
            }

            // file type
            if (StringUtils.isNotEmpty(this.diagnostic.getDiagnosticfiletype())) {
                attachment.setContentTypeSimple(this.diagnostic.getDiagnosticfiletype());
            }
        } else if (this.fhirDocumentReference != null) {
            // date
            if (this.fhirDocumentReference.getDate() != null) {
                this.media.setDateTimeSimple(new DateAndTime(this.fhirDocumentReference.getDate()));
            }

            // filename
            if (StringUtils.isNotEmpty(this.fhirDocumentReference.getFilename())) {
                attachment.setTitleSimple(this.fhirDocumentReference.getFilename());
            }

            // file type
            if (StringUtils.isNotEmpty(this.fhirDocumentReference.getFiletype())) {
                attachment.setContentTypeSimple(this.fhirDocumentReference.getFiletype());
            }
        } else if (this.fhirDiagnosticReport != null) {
            // date
            if (this.fhirDiagnosticReport.getDate() != null) {
                this.media.setDateTimeSimple(new DateAndTime(this.fhirDiagnosticReport.getDate()));
            }

            // filename
            if (StringUtils.isNotEmpty(this.fhirDiagnosticReport.getFilename())) {
                attachment.setTitleSimple(this.fhirDiagnosticReport.getFilename());
            }

            // file type
            if (StringUtils.isNotEmpty(this.fhirDiagnosticReport.getFiletype())) {
                attachment.setContentTypeSimple(this.fhirDiagnosticReport.getFiletype());
            }
        }

        this.media.setContent(attachment);
    }

    public Media getMedia() throws FhirResourceException {
        if (this.media == null) {
            throw new FhirResourceException("Must have built Media");
        }

        return this.media;
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
