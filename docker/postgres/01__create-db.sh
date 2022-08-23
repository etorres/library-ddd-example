#!/usr/bin/env bash

set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER library_user WITH PASSWORD 'changeme';
    CREATE DATABASE library_db;
    GRANT ALL PRIVILEGES ON DATABASE library_db TO library_user;
    \connect library_db library_user
    DO \$\$
    DECLARE
      schema_names TEXT[] := ARRAY['public', 'test_book_catalogue'];
      schema_name TEXT;
    BEGIN
      FOREACH schema_name IN ARRAY schema_names
      LOOP
        EXECUTE 'CREATE SCHEMA IF NOT EXISTS ' || quote_ident(schema_name);
        EXECUTE 'CREATE TABLE IF NOT EXISTS ' || quote_ident(schema_name) || '.book_catalogue (
          isbn VARCHAR(10) NOT NULL PRIMARY KEY,
          title VARCHAR(255) NOT NULL,
          author VARCHAR(127) NOT NULL
        )';
        EXECUTE 'CREATE TABLE IF NOT EXISTS ' || quote_ident(schema_name) || '.book_instance_catalogue (
          book_id UUID NOT NULL PRIMARY KEY,
          isbn VARCHAR(10) NOT NULL
        )';
      END LOOP;
    END\$\$;
EOSQL