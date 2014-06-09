\set ECHO
\set QUIET 1
-- Turn off echo and keep things quiet.

-- Format the output for nice TAP.
\pset format unaligned
--\pset tuples_only true
\pset pager

-- Revert all changes on failure.
\set ON_ERROR_ROLLBACK 1
\set ON_ERROR_STOP true
\set QUIET 1

\c postgres
\set test_db_name `echo $TEST_DB_NAME`
drop database if exists :test_db_name;
create database :test_db_name;

\c :test_db_name
CREATE EXTENSION IF NOT EXISTS pgtap ;
SET log_statement TO 'none';
