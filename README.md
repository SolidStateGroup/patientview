PatientView 2
=============

PatientView shows patients' latest test results plus information about their diagnosis and treatment. They can share 
this information with anyone they want, and view it from anywhere in the world. PatientView has developed from a project 
launched for patients of Renal Units, but has expanded to be able to show information for others too. It requires your 
local unit to have joined. (e.g. renal unit, diabetes unit, IBD unit)

Technology
==========

PatientView (PV) uses [PostgreSQL](http://www.postgresql.org/) for data storage in two distinct databases. PV data 
related to users, configuration and non patient data is stored in the "patientview" database. Patient specific data is 
stored in a specific version of the [FHIR](http://hl7.org/implement/standards/fhir/) database schema.

The SQL creation scripts for creation of a standard database are located in 
/root/database/src/main/resources/db/migration