#!/bin/sh

#-------------------------------------------------------
#Author: jamesr@solidstategroup.com
#Date:   10/06/2014
#---------------------------------------------------------
#npm install


port=$1


if test $# -ne 1
then
	echo "Please supply all paramaters"
	echo "command [portNumber]"
else
    grunt minimal --port=$1

    if [ $? -eq 0 ]; then
        curl -T "dist/webapp.war" "http://tomcat:tomcat@localhost:$port/manager/text/deploy?path=/&update=true"
        if [ $? -eq 0 ]; then
            echo 'SUCCESS'
        else
            echo 'FAILED TOMCAT'
        fi
    else
        echo 'FAILED GRUNT'
    fi
fi




