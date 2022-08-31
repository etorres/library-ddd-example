#!/usr/bin/env bash

set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER lending_user WITH PASSWORD 'changeme';
    CREATE DATABASE lending_db;
    GRANT ALL PRIVILEGES ON DATABASE lending_db TO lending_user;
    \connect lending_db lending_user
    DO \$\$
    DECLARE
      schema_names TEXT[] := ARRAY['public', 'test_available_books'];
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
          on_hold_till TIMESTAMP
        )';
      END LOOP;
    END\$\$;
EOSQL