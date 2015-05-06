package org.patientview.importer.builder;

import generated.Patientview.Patient.Letterdetails.Letter;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Attachment;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.Media;
import org.patientview.config.exception.FhirResourceException;

/**
 * This maps between parameters from old PatientView and the new PatientView fhir record
 *
 * Created by jamesr@solidstategroup.com
 * Created on 01/05/2014
 */
public class MediaBuilder {

    private Letter letter;
    private Media media;

    public MediaBuilder(Letter letter) {
        this.letter = letter;
    }

    public void build() throws FhirResourceException {
        media = new Media();
        Attachment attachment = new Attachment();

        // date
        if (letter.getLetterdate() != null) {
            try {
                DateAndTime dateAndTime = new DateAndTime(letter.getLetterdate().toGregorianCalendar().getTime());
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
        media.getContent().setUrlSimple(fileDataId.toString());

        return media;
    }

    public Media setFileSize(Media media, Integer fileSize) throws FhirResourceException {
        if (media.getContent() == null) {
            throw new FhirResourceException("Must have built Media");
        }

        media.getContent().setSizeSimple(fileSize);
        return media;
    }
}
