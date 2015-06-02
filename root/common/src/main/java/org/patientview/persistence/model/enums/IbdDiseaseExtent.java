package org.patientview.persistence.model.enums;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 * Similar to myibdportal DiseaseExtent but name covers both display name and value in XML imported.
 */
public enum IbdDiseaseExtent {

    PROCTITIS("Proctitis", "x.jpg"),
    LEFT_SIDED_COLITIS("Left-sided Colitis", "x.jpg"),
    EXTENSIVE_COLITIS("Extensive Colitis", "x.jpg"),
    ILEAL_CROHNS("Ileal Crohn's", "x.jpg"),
    ILEO_COLONIC_DISEASE("Ileal-colonic Crohn's", "x.jpg"),
    CROHNS_COLITIS("Crohn's Colitis", "x.jpg"),
    ISOLATED_UPPER_GI_DISEASE("Isolated upper GI disease", "x.jpg");

    private String name;
    private String diagram;

    IbdDiseaseExtent(String name, String diagram) {
        this.name = name;
        this.diagram = diagram;
    }

    public static String getDiagramByName(String name) {
        for (IbdDiseaseExtent diseaseExtent : IbdDiseaseExtent.values()) {
            if (diseaseExtent.getName().equalsIgnoreCase(name)) {
                return diseaseExtent.getDiagram();
            }
        }

        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDiagram() {
        return diagram;
    }

    public void setDiagram(String diagram) {
        this.diagram = diagram;
    }
}
