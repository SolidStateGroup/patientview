package org.patientview.persistence.dialect;

import org.hibernate.dialect.PostgreSQL9Dialect;

import java.sql.Types;

/**
 * Created to register the UUID for Postgres as it's not done by default.
 *
 * Created by james@solidstategroup.com
 * Created on 18/06/2014
 */
public class PostgresCustomDialect extends PostgreSQL9Dialect {

    public PostgresCustomDialect() {
        super();
        registerColumnType(Types.OTHER, "pg-uuid" );
    }

}
