# This script removes duplicate Flyway migration files with version 1 from the target/classes directory.
# Only one V1__*.sql file should exist in target/classes/db/migration/ for Flyway to work.

rm -v /Users/agustingarridosnoopy/Desktop/huertohogar-api/target/classes/db/migration/V1__Create_Usuarios_Table.sql
# If you want to keep V1__Create_Usuarios_Table.sql instead, remove V1__Initial_schema.sql instead.
