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

environment=$1
fhir_password=$2
pv_password=$3


if test $# -ne 3
then
	echo "Please supply all paramaters"
	echo "command [environment] [fhir_password] [pv_username]"
	exit 2
fi

if test "$environment" == "local"
then
	echo "Running in local"
else
	echo "Switching to dev.solidstategroup.com"

	ssh -T -q dev.solidstategroup.com >/dev/null

	if test $? -ne 0
	then
		echo "Cannot access dev.solidstategroup.com"
		exit 2
	else
		echo "Switched to dev.solidstategroup.com"
	fi
fi


id
hostname

sudo -u postgres -s psql postgres -c "DROP SCHEMA IF EXISTS fhir CASCADE;"
sudo -u postgres -s psql postgres -c "DROP SCHEMA IF EXISTS meta CASCADE;"
sudo -u postgres -s psql postgres -c "DROP DATABASE IF EXISTS $environment;"
sudo -u postgres -s psql postgres -c "DROP USER IF EXISTS fhir;"
sudo -u postgres -s psql postgres -c "CREATE USER fhir WITH PASSWORD '$fhir_password' SUPERUSER;"
sudo -u postgres -s psql postgres -c "CREATE USER patientview WITH PASSWORD '$pv_password' SUPERUSER;"
sudo -u postgres -s psql postgres -c "CREATE schema patientview AUTHORIZATION patientview;"



if test $? -ne 0
then
	echo "Database creation failed"
	exit 2
else
	echo "Created the database"
fi

curl "https://raw.githubusercontent.com/fhirbase/fhirbase/master/fhirbase.sql" -o "fhirbase.sql"
mv fhirbase.sql /home/postgres/
chown -f postgres:postgres  /home/postgres/fhirbase.sql

if test $? -ne 0
then
	echo "Unable to contact github for file"
	exit 2
fi


sudo -u postgres -s psql postgres -c "\i /home/postgres/fhirbase.sql"
sudo -u postgres -s psql postgres -c "\dt fhir.*";


echo "List of created objects"

if test "$environment" == "local"
then
	echo "Running in local"
else
	echo "Exiting to build server"
	exit;
	exit;
fi

exit 0

