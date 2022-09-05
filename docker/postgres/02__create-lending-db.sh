#!/usr/bin/env bash

set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER lending_user WITH PASSWORD 'changeme';
    CREATE DATABASE lending_db;
    GRANT ALL PRIVILEGES ON DATABASE lending_db TO lending_user;
    \connect lending_db lending_user
    DO \$\$
    DECLARE
      schema_names TEXT[] := ARRAY['public', 'test_lending'];
      schema_name TEXT;
    BEGIN
      FOREACH schema_name IN ARRAY schema_names
      LOOP
        EXECUTE 'CREATE SCHEMA IF NOT EXISTS ' || quote_ident(schema_name);
        EXECUTE 'CREATE TABLE IF NOT EXISTS ' || quote_ident(schema_name) || '.books (
          book_id UUID PRIMARY KEY,
          book_type VARCHAR(32) NOT NULL,
          book_state VARCHAR(32) NOT NULL,
          available_at_branch UUID,
          on_hold_at_branch UUID,
          on_hold_by_patron UUID,
          checked_out_at_branch UUID,
          checked_out_by_patron UUID,
          on_hold_till TIMESTAMPTZ
        )';
        EXECUTE 'CREATE TABLE IF NOT EXISTS ' || quote_ident(schema_name) || '.patrons (
          patron_id UUID PRIMARY KEY,
          patron_type VARCHAR(32) NOT NULL
        )';
        EXECUTE 'CREATE TABLE IF NOT EXISTS ' || quote_ident(schema_name) || '.holds (
          book_id UUID NOT NULL REFERENCES ' || quote_ident(schema_name) || '.books ON DELETE RESTRICT,
          patron_id UUID NOT NULL REFERENCES ' || quote_ident(schema_name) || '.patrons ON DELETE RESTRICT,
          library_branch_id UUID NOT NULL,
          till TIMESTAMPTZ NOT NULL,
          PRIMARY KEY (book_id, patron_id, library_branch_id)
        )';
        EXECUTE 'CREATE TABLE IF NOT EXISTS ' || quote_ident(schema_name) || '.overdue_checkouts (
          book_id UUID NOT NULL REFERENCES ' || quote_ident(schema_name) || '.books ON DELETE RESTRICT,
          patron_id UUID NOT NULL REFERENCES ' || quote_ident(schema_name) || '.patrons ON DELETE RESTRICT,
          library_branch_id UUID NOT NULL,
          PRIMARY KEY (book_id, patron_id, library_branch_id)
        )';
      END LOOP;
    END\$\$;
EOSQL