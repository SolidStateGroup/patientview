create schema meta;
-- FIXME: foreign keys and indexes

CREATE TABLE meta.datatypes (
  version varchar,
  type varchar,
  kind varchar,
  extension varchar,
  restriction_base varchar,
  documentation text[],
  PRIMARY KEY(type)
);

CREATE TABLE meta.datatype_elements (
  version varchar,
  datatype varchar references meta.datatypes(type),
  name varchar,
  type varchar,
  min_occurs varchar,
  max_occurs varchar,
  documentation text,
  PRIMARY KEY(datatype, name)
);

CREATE TABLE meta.datatype_enums (
  version varchar,
  datatype varchar references meta.datatypes(type),
  value varchar,
  documentation text,
  PRIMARY KEY(datatype, value)
);


CREATE TABLE meta.resource_elements (
  version varchar,
  path varchar[],
  min varchar,
  max varchar,
  type varchar[],
  PRIMARY KEY(path)
);

CREATE TABLE meta.type_to_pg_type (
  type varchar,
  pg_type varchar
);

INSERT INTO meta.type_to_pg_type (type, pg_type)
VALUES
('code', 'varchar'),
('date_time', 'timestamp'),
('string', 'varchar'),
('text', 'text'),
('uri', 'varchar'),
('datetime', 'timestamp'),
('instant', 'timestamp'),
('boolean', 'boolean'),
('base64_binary', 'bytea'),
('integer', 'integer'),
('decimal', 'decimal'),
('sampled_data_data_type', 'text'),
('date', 'date'),
('id', 'varchar'),
('oid', 'varchar');
