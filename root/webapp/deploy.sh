#!/bin/sh

#-------------------------------------------------------
#Author: jamesr@solidstategroup.com
#Date:   10/06/2014
#
#Build script to CI the front end
#---------------------------------------------------------


tomcatUrl=$1
username=$2
password=$3

if test $# -ne 3
then
	echo "Please supply all paramaters"
	echo "command [tomcatUrl] [username] [password]"
	exit 2
else
    echo "Deploying to $tomcatUrl";
fi


cd root/webapp;

/usr/bin/npm install

if test $? -ne 0
then
	echo "Could not install packages"
	exit 2
else
	echo "Installed npm dependencies"
fi

/usr/bin/bower install --allow-root

if test $? -ne 0
then
	echo "Could not execute bower install"
	exit 2
else
	echo "Installed bower dependencies"
fi

/usr/local/bin/grunt minimallive

if [ $? -eq 0 ]; then
    curl -T "dist/webapp.war" "http://$username:$password@$tomcatUrl/manager/text/deploy?path=/&update=true"
    if [ $? -eq 0 ]; then
        echo 'SUCCESS'
    #    exit 0
    else
        echo 'FAILED TOMCAT'
   #     exit 1
    fi
else
    echo 'FAILED GRUNT'
  #  exit 1
fi


