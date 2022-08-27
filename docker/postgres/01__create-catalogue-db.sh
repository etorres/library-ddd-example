#!/usr/bin/env bash

set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER catalogue_user WITH PASSWORD 'changeme';
    CREATE DATABASE catalogue_db;
    GRANT ALL PRIVILEGES ON DATABASE catalogue_db TO catalogue_user;
    \connect catalogue_db catalogue_user
    DO \$\$
    DECLARE
      schema_names TEXT[] := ARRAY['public', 'test_catalogue'];
      schema_name TEXT;
    BEGIN
      FOREACH schema_name IN ARRAY schema_names
      LOOP
        EXECUTE 'CREATE SCHEMA IF NOT EXISTS ' || quote_ident(schema_name);
        EXECUTE 'CREATE TABLE IF NOT EXISTS ' || quote_ident(schema_name) || '.book_catalogue (
          isbn VARCHAR(10) PRIMARY KEY,
          title VARCHAR(255) NOT NULL,
          author VARCHAR(128) NOT NULL
        )';
        EXECUTE 'CREATE TABLE IF NOT EXISTS ' || quote_ident(schema_name) || '.book_instance_catalogue (
          book_id UUID PRIMARY KEY,
          isbn VARCHAR(10) REFERENCES ' || quote_ident(schema_name) || '.book_catalogue ON DELETE RESTRICT
        )';
      END LOOP;
    END\$\$;
EOSQL