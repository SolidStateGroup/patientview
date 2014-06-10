#!/bin/sh

#
#Author: james@solidstategroup.com
#Date: 09/06/2014

#command [environment] [os_username] [db_username]

#

environment=$1
username=$2
db_username=$3

if test "$environment" == "local" 
then 
	echo "Running in local"
else
	echo "Switching to dev.solidstategroup.com" 
	ssh dev.solidstategroup.com

	if test $? -ne 0
	then
		echo "Cannot access dev.solidstategroup.com"
		exit 2
	else
		echo "Switched to dev.solidstategroup.com"
	fi

	su - postgres

	if test $? -ne 0
	then
		echo "Cannot switch to user Postgres"
		exit 2
	else
		echo "Switched to user Postgres"
	fi
fi

if test $# -ne 3
then
	echo "Please supply all paramaters"
	echo "command [environment] [os_username] [db_username]"
	exit 2
fi

psql $username << EOF
	DROP DATABASE IF EXISTS $environment;
	DROP USER IF EXISTS fhir;
	CREATE USER $db_username WITH PASSWORD '$db_username' SUPERUSER;
	CREATE DATABASE $environment OWNER $db_username;
EOF

if test $? -ne 0
then
	echo "Database creation failed"
	exit 2
else
	echo "Created the database"
fi

curl "https://raw.githubusercontent.com/fhirbase/fhirbase/master/fhirbase.sql" -o "fhirbase.sql"

if test $? -ne 0
then
	echo "Unable to contact github for file"
	exit 2
fi


psql $environment $db_username << EOF
	\i fhirbase.sql
	\dt fhir.*;
EOF

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

