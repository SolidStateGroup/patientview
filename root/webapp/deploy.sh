#!/bin/sh

#-------------------------------------------------------
#Author: jamesr@solidstategroup.com
#Date:   10/06/2014

#command

#---------------------------------------------------------
#npm install
grunt minimal
if [ $? -eq 0 ]; then
    curl -T "dist/webapp.war" "http://tomcat:tomcat@localhost:8080/manager/text/deploy?path=/&update=true"
#    curl -T "dist/webapp.war" "http://username:password@localhost:8080/manager/text/deploy?path=/webapp&update=true"
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


