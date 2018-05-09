package org.patientview.persistence.dialect;

import org.hibernate.NullPrecedence;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.dialect.PostgreSQL9Dialect;
import org.hibernate.type.PostgresUUIDType;
import org.patientview.persistence.model.types.StringJsonUserType;

import java.sql.Types;

/**
 * Created to register the UUID for Postgres as it's not done by default.
 *
 * Created by james@solidstategroup.com
 * Created on 18/06/2014
 *
 * https://forum.hibernate.org/viewtopic.php?f=1&t=1014157
 */
public class PostgresCustomDialect extends PostgreSQL9Dialect {


    public PostgresCustomDialect() {
        registerColumnType(Types.JAVA_OBJECT, "jsonb");
    }

    @Override
    public void contributeTypes(final TypeContributions typeContributions, final org.hibernate.service.ServiceRegistry serviceRegistry) {
        super.contributeTypes(typeContributions, serviceRegistry);
        typeContributions.contributeType(new InternalPostgresUUIDType());
    }

    /*
    // Todo: fix Sort.NullHandling in Hibernate with PostgreSQL, this is copied from the MySQL dialect
    @Override
    public String renderOrderByElement(String expression, String collation, String order, NullPrecedence nulls) {
        final StringBuilder orderByElement = new StringBuilder();
        if ( nulls != NullPrecedence.NONE ) {
            // Workaround for NULLS FIRST / LAST support.
            orderByElement.append( "case when " ).append( expression ).append( " is null then " );
            if ( nulls == NullPrecedence.FIRST ) {
                orderByElement.append( "0 else 1" );
            }
            else {
                orderByElement.append( "1 else 0" );
            }
            orderByElement.append( " end, " );
        }
        // Nulls precedence has already been handled so passing NONE value.
        orderByElement.append( super.renderOrderByElement( expression, collation, order, NullPrecedence.NONE ) );
        return orderByElement.toString();
    }
    */

    protected static class InternalPostgresUUIDType extends PostgresUUIDType {

        @Override
        protected boolean registerUnderJavaType() {
            return true;
        }
    }
}
