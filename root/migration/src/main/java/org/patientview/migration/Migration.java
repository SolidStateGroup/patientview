package org.patientview.migration;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public class Migration {

    public Migration () {

        ApplicationContext ctx = new FileSystemXmlApplicationContext("classpath*:migration-repository.xml");

    }


    public void start() {


    }
}
