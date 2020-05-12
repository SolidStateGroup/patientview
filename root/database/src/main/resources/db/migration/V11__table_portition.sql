-- create function to handle create child partition table with indexes and new record inserts
CREATE OR REPLACE FUNCTION func_audit_log_partition_trigger()
RETURNS TRIGGER AS $$
DECLARE
	tableName character varying(100);
	logdateYear int;
	isTableAlreadyExist boolean;
	queryString text;
	dateString character varying(100);
	entryTime TIMESTAMP;
    exitTime TIMESTAMP;
BEGIN
        SELECT EXTRACT(YEAR FROM NEW.creation_date) INTO logdateYear;
        SELECT 'pv_audit_'||logdateYear into tableName;
        SELECT logdateYear||'-01-01' INTO dateString;
        SELECT to_date(dateString,'YYYY-MM-DD')::date INTO entryTime;
        exitTime:=entryTime + interval '1 year';

		IF NOT EXISTS (SELECT relname FROM pg_class WHERE relname=''||tableName||'')
			then
				queryString:='create table '||tableName;
				queryString:=queryString||'( ';
				queryString:=queryString||'		CHECK ( EXTRACT(YEAR FROM creation_date) = '||logdateYear||' ) ';
				queryString:=queryString||'	) ';
				queryString:=queryString||'	INHERITS (pv_audit);';
				EXECUTE queryString;

				queryString:=' ALTER TABLE '||tableName||' ADD CONSTRAINT '||tableName||'_pkey PRIMARY KEY (id);';
				EXECUTE queryString;

				queryString:=' CREATE INDEX  '||tableName||'_creation_date_idx ON '||tableName||' USING  btree( creation_date Asc NULLS Last ); ';
				EXECUTE queryString;

				queryString:=' CREATE INDEX  '||tableName||'_source_object_idx ON '||tableName||' USING  btree( source_object_id Asc NULLS Last ); ';
				EXECUTE queryString;

				queryString:=' CREATE INDEX  '||tableName||'_actor_id_idx ON '||tableName||' USING  btree( actor_id Asc NULLS Last ); ';
				EXECUTE queryString;

				queryString:=' CREATE INDEX  '||tableName||'_action_idx ON '||tableName||' USING  btree( action Asc NULLS Last ); ';
				EXECUTE queryString;

				queryString:=' ALTER TABLE '||tableName||' ADD CONSTRAINT '||tableName||'_group_id_fkey FOREIGN KEY ( "group_id" ) REFERENCES "pv_group" ( "id" ) MATCH SIMPLE ON DELETE No Action ON UPDATE No Action;';
				EXECUTE queryString;

				queryString:=' ALTER TABLE '||tableName||' ADD CONSTRAINT '||tableName||'_actor_id_fkey FOREIGN KEY ( "actor_id" ) REFERENCES "pv_user" ( "id" ) MATCH SIMPLE ON DELETE No Action ON UPDATE No Action;';
				EXECUTE queryString;

				queryString:=' ALTER TABLE '||tableName||' ADD CONSTRAINT '||tableName||'_range_constrain CHECK (creation_date >= '''||entryTime||''' AND creation_date < '''||exitTime||''' ) ';
				EXECUTE queryString;

		END IF;
		queryString:=' INSERT INTO '||tableName||' select ($1).* ';
		EXECUTE queryString using NEW;
    RETURN NULL;
END;
$$
LANGUAGE plpgsql;


-- apply function to pv_audit table
CREATE TRIGGER trigger_pv_audit BEFORE INSERT ON pv_audit FOR EACH ROW EXECUTE PROCEDURE func_audit_log_partition_trigger();