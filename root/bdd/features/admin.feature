Feature: Admin Section Global Administrator

	As a PatientView Administrator
	I want to administrate PatientView
	So I can manage the application user and settings

	Scenario: Access the application as Global Administrator
		Given the patient view homepage
		When the "global admin" login credentials ("globaladmin","pppppp") are submitted
		Then the staff, patient, codes, links sections should be available

    Scenario: Add a Specialty to Patient View
        Given the "Groups" section
        When a Specialty group "uat-renal5" is created with the code "uat-renal5"
        And  the "Groups" section is selected
        Then the group "uat-renal5" should be accessible