#!/bin/sh

#-------------------------------------------------------
#Author: jamesr@solidstategroup.com
#Date:   10/06/2014

#command

#---------------------------------------------------------
#npm install
grunt buildsmall
if [ $? -eq 0 ]; then
#    curl -T "dist/webapp.war" "http://tomcat:tomcat@diabetes-pv.dev.solidstategroup.com/manager/text/deploy?path=/webapp&update=true"
    curl -T "dist/webapp.war" "http://username:password@localhost:8080/manager/text/deploy?path=/webapp&update=true"
    if [ $? -eq 0 ]; then
        echo 'SUCCESS'
    else
        echo 'FAILED TOMCAT'
    fi
else
    echo 'FAILED GRUNT'
fi


