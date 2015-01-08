package org.patientview.persistence.dialect;

import org.hibernate.dialect.PostgreSQL9Dialect;
import org.hibernate.metamodel.spi.TypeContributions;
import org.hibernate.type.PostgresUUIDType;

/**
 * Created to register the UUID for Postgres as it's not done by default.
 *
 * Created by james@solidstategroup.com
 * Created on 18/06/2014
 *
 * https://forum.hibernate.org/viewtopic.php?f=1&t=1014157
 */
public class PostgresCustomDialect extends PostgreSQL9Dialect {

    @Override
    public void contributeTypes(final TypeContributions typeContributions, final org.hibernate.service.ServiceRegistry serviceRegistry) {
        super.contributeTypes(typeContributions, serviceRegistry);
        typeContributions.contributeType(new InternalPostgresUUIDType());
    }

    protected static class InternalPostgresUUIDType extends PostgresUUIDType {

        @Override
        protected boolean registerUnderJavaType() {
            return true;
        }
    }
}
