PatientView 2
=============

PatientView shows patients' latest test results plus information about their diagnosis and treatment. They can share 
this information with anyone they want, and view it from anywhere in the world. PatientView has developed from a project 
launched for patients of Renal Units, but has expanded to be able to show information for others too. It requires your 
local unit to have joined. (e.g. renal unit, diabetes unit, IBD unit)

PatientView 2 (PV2) is a complete rewrite of the original PatientView (PV1) with the aim of creating a more modern tool 
for patients and staff while allowing future improvements to be easily integrated.

Technology
==========

PatientView 2 (PV2) uses [PostgreSQL](http://www.postgresql.org/) for data storage in two distinct databases. PV data 
related to users, configuration and non patient data is stored in the "patientview" database. Patient specific data is 
stored in a specific version of the [FHIR](http://hl7.org/implement/standards/fhir/) database schema.

The SQL creation scripts for creation of a standard database are located in 
/root/database/src/main/resources/db/migration

Active Development
==================

Active development is either on develop or branches off develop to be merged in via pull request. The develop branch is
considered the latest version of the code but may include incomplete features or code.

New release branches are named after their version number, e.g. 2.0.1-RELEASE. When a release is finalised a new branch
is created and then merged into master, so master is considered the latest stable release.

Database
========

[PostgreSQL](http://www.postgresql.org/) 9.4 must be used as FHIR requires JSONB support. Two separate databases are 
used, each with their own user. For an example .properties file containing database options (and others) see
/root/api/src/main/resources/conf/local-api.properties.

The main SQL files used to build the 'patientview' and 'fhir' databases prior to running PV2 are:

- V1__Create_Schema.sql (table structure for 'patientview' database)
- V2__Static_Data.sql (static data for 'patientview' database, used throughout PV, including example accounts)
- V3__fhirbase--1.0.sql (specific version of 'fhir' database for patientview)
- V4__Fhir_Fixes.sql (updates to 'fhir' database to avoid issues and improve performance)
- V5__Post_Migration.sql (only used once migration has taken place from PV1)

Note that migration is a one time task from PV1 and is not required to set up PV2.



RabbitMQ
========
RabbitMQ is used as a queuing system to process messages. You can mount the rabbit MQ volume to your local disk using the -v parameter within docker.


To run in docker use:
```
docker run -d --hostname rabbit --name patientview-mgtmt -e RABBITMQ_DEFAULT_USER=patientview -e RABBITMQ_DEFAULT_PASS=patientview -p 1212:15672 -p 4369:4369 -p 5671:5671 -p 5672:5672 -v /Users/username/RabbitMQ/data:/var/lib/rabbitmq rabbitmq:3-management
```

The rabbit MQ management console will then be available on port 1212, by visiting [http://localhost:1212](http://localhost:1212)

You will need to create the following queues:
`patient_import`
`survey_import`
`survey_response_import`
`ukrdc_import`