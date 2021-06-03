#!/bin/bash

docker cp docker-db/sqls/users.sql aiml-service-db:/var/users.sql
docker exec -it --user=oracle aiml-service-db bash -c "exit | sqlplus sys/mysecurepassword@XE as sysdba @/var/users.sql"
docker cp docker-db/sqls/drop.sql aiml-service-db:/var/drop.sql
docker exec -it --user=oracle aiml-service-db bash -c "exit | sqlplus CMSTR/CMSTR@XE @/var/drop.sql"
docker cp docker-db/sqls/schema.sql aiml-service-db:/var/schema.sql
docker exec -it --user=oracle aiml-service-db bash -c "exit | sqlplus CMSTR/CMSTR@XE @/var/schema.sql"
docker cp docker-db/sqls/data.sql aiml-service-db:/var/data.sql
docker exec -it --user=oracle aiml-service-db bash -c "exit | sqlplus CMSTR/CMSTR@XE @/var/data.sql"
docker cp docker-db/sqls/data_symbol.sql aiml-service-db:/var/data_symbol.sql
docker exec -it --user=oracle aiml-service-db bash -c "exit | sqlplus CMSTR/CMSTR@XE @/var/data_symbol.sql"
