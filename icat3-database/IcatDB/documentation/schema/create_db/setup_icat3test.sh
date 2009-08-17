#!/bin/bash

#. oraenv
sqlplus / as sysdba <<EOF

@create_user_icat3test.sql
connect icat3test/icat34all
@create_schema_icat3.sql
@create_records_icat3test.sql
commit
exit
EOF
