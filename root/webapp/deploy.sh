#!/bin/sh

#-------------------------------------------------------
#Author: jamesr@solidstategroup.com
#Date:   10/06/2014
#
#Build script to CI the front end
#---------------------------------------------------------
/usr/bin/npm install

tomcatUrl=$1
username=$2
password=$3

export PATH="/usr/bin/node;$PATH"



if test $# -ne 3
then
	echo "Please supply all paramaters"
	echo "command [tomcatUrl] [username] [password]"
	exit 2
else
    echo "Deploying to $tomcatUrl";
fi


/usr/local/bin/grunt minimal
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


