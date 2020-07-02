#!/bin/sh

#-------------------------------------------------------
#Author: jamesr@solidstategroup.com
#Date:   10/06/2014
#
#Build script to CI the front end
#---------------------------------------------------------


gruntTask=$1

if test $# -ne 1
then
	echo "Please supply all paramaters"
	echo "command [gruntTask]"
	exit 2
else
    echo "Starting build process";
fi

#cd root/webapp;

#/usr/bin/npm install
npm install

if test $? -ne 0
then
	echo "Could not install packages"
	exit 2
else
	echo "Installed npm dependencies"
fi

#/usr/bin/bower install --allow-root
bower install --allow-root

if test $? -ne 0
then
	echo "Could not execute bower install"
	exit 2
else
	echo "Installed bower dependencies"
fi

grunt $gruntTask --force

if [ $? -eq 0 ]; then
    echo 'SUCCESS'
else
    echo 'FAILED GRUNT'
  #  exit 1
fi


