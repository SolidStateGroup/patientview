#!/bin/sh

#-------------------------------------------------------
#Author: james@solidstategroup.com
#Date:   09/06/2014

#Script build on the assumption that :-
# 1) There is a Postgres database installed on Dev with
#    the postgres-contrib package
# 2) There is a Postgres user on the OS


#command [environment] [fhir_password] [pv_password]

#---------------------------------------------------------



psql postgres -c "DROP USER IF EXISTS patientview;"
psql postgres -c "DROP SCHEMA IF EXISTS patientview CASCADE;"
psql postgres -c "DROP SCHEMA IF EXISTS fhir CASCADE;"
psql postgres -c "DROP SCHEMA IF EXISTS meta CASCADE;"
psql postgres -c "DROP DATABASE IF EXISTS local;"
psql postgres -c "DROP USER IF EXISTS fhir;"
psql postgres -c "CREATE USER fhir WITH PASSWORD 'fhir' SUPERUSER;"
psql postgres -c "CREATE USER patientview WITH PASSWORD 'fhir' SUPERUSER;"
psql postgres -c "CREATE schema patientview AUTHORIZATION patientview;"

curl "https://raw.githubusercontent.com/fhirbase/fhirbase/master/fhirbase--1.0.sql" -o "fhirbase.sql"

if test $? -ne 0
then
	echo "Unable to contact github for file"
fi

echo "Running FhirBase script"

psql postgres -c "\i fhirbase.sql"

echo "List of created objects"

psql postgres -c "\dt fhir.*";



