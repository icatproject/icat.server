#!/bin/bash
# should be run as sys or system
# needs a corresponding cleanup script
# pre-requisites need to have DIRECTORY setup in Oracle 
# export external_dir=/home/oracle/external_tables_3_3
# mkdir $external_dir
# chmod 775 $external_dir
# chown -R oracle:dba $external_dir

export db_user=ICATISIS_TEST

sqlplus / as sysdba <<EOF

drop user $db_user cascade ;

exit
EOF


