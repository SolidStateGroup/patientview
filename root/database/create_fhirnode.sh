#!/bin/sh

#---------------------------------------
# Author: james@solidstategroup.com
# Date:   11/06/2014
#
# Script to build nodejs, assumes:
# 1) There is a patient view user
# 2) Node installed
#
# ---------------------------------------

fhirnode_dir=""

if test $# -ne 1
then
	echo "Please supply all paramaters"
	echo "command [environment]"
	exit 1
fi

environment=$1

echo $environment

if test "$environment" == "local"
then
    fhirnode_dir="~/work/"
    cp -R src/main/resources/fhirnode fhirnode_dir
else
    fhirnode_dir="/home/patientview/"
    scp -R src/main/resources/fhirnode dev.solidstategroup.com:$fhirnode_dir
fi

#echo $fhirnode_dir

#ls $fhirnode_dir

#if test $? -ne 0
#then
#    mkdir $fhirnode_dir
#    echo "Created fhir node directory"
#fi




if test $? -ne 0
then
    echo "Successfully copied files"
fi

