package org.patientview.persistence.model.enums;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
//@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum LookupTypes {
  /*  GROUP("Group"), MENU("Menu"), ROLE("Role"),
    CODE_STANDARD("Code Standard"), CODE_TYPE("Code Type"),
    FEATURE_TYPE("Feature Type"), RELATIONSHIP_TYPE("Relationship Type"),
    IDENTIFIER("Identifier");

    private String name;
    LookupTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }*/

    GROUP, MENU, ROLE,
    CODE_STANDARD, CODE_TYPE,
    FEATURE_TYPE, RELATIONSHIP_TYPE,
    IDENTIFIER, CONTACT_POINT_TYPE;

}
