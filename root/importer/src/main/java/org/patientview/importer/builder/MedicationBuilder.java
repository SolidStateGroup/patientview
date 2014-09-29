package org.patientview.importer.builder;

import generated.Patientview.Patient.Drugdetails.Drug;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Medication;
import org.hl7.fhir.instance.model.Narrative;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This maps between parameters from old PatientView and the new PatientView fhir record
 *
 * Created by jamesr@solidstategroup.com
 * Created on 29/09/2014
 */
public class MedicationBuilder {

    private final Logger LOG = LoggerFactory.getLogger(MedicationBuilder.class);

    private Drug data;

    public MedicationBuilder(Drug data) {
        this.data = data;
    }

    public Medication build() {
        Medication medication = new Medication();

        CodeableConcept code = new CodeableConcept();
        code.setTextSimple(data.getDrugname());
        medication.setCode(code);

        medication.setNameSimple(data.getDrugdose());

        /*Narrative narrative = new Narrative();
        narrative.setStatusSimple(Narrative.NarrativeStatus.generated);
        narrative.setDiv(new XhtmlNode(NodeType.Text, "div"));
        narrative.getDiv().setContent(null);

        //narrative.setDiv(new XhtmlNode(NodeType.Comment, "<div xmlns=\"http://www.w3.org/1999/xhtml\"></div>"));
        //narrative.getDiv().setContent(data.getDrugdose());
        //narrative.getDiv().setContent("<div>x</div>");
        medication.setText(narrative);*/

        return medication;
    }
}
