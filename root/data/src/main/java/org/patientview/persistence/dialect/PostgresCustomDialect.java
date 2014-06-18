package org.patientview.persistence.dialect;

import org.hibernate.dialect.PostgreSQL9Dialect;

import java.sql.Types;

/**
 * Created by james@solidstategroup.com
 * Created on 18/06/2014
 */
public class PostgresCustomDialect extends PostgreSQL9Dialect {

    public PostgresCustomDialect() {
        super();
        registerHibernateType(Types.OTHER, "pg-uuid");
    }




}
