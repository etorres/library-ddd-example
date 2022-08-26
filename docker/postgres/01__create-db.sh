#!/usr/bin/env bash

set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER library_user WITH PASSWORD 'changeme';
    CREATE DATABASE library_db;
    GRANT ALL PRIVILEGES ON DATABASE library_db TO library_user;
    \connect library_db library_user
    DO \$\$
    DECLARE
      schema_names TEXT[] := ARRAY['public', 'test_book_catalogue', 'test_available_books'];
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
        EXECUTE 'CREATE TABLE IF NOT EXISTS ' || quote_ident(schema_name) || '.available_books_for_lending (
          book_id UUID PRIMARY KEY,
          book_type VARCHAR(32) NOT NULL,
          book_state VARCHAR(32) NOT NULL,
          available_at_branch UUID,
          on_hold_at_branch UUID,
          on_hold_by_patron UUID,
          checked_out_at_branch UUID,
          checked_out_by_patron UUID,
          on_hold_till TIMESTAMP
        )';
      END LOOP;
    END\$\$;
EOSQL