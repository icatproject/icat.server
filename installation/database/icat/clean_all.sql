undefine database_name
undefine sys_password

ACCEPT database_name CHAR prompt          'Enter Database Name             : '
ACCEPT sys_password CHAR hide prompt      'Enter SYS password              : '

prompt
prompt ====================================================================
prompt removing users icat, icatuser, dataportal and testicat
prompt

connect sys/&sys_password@&database_name as sysdba

DROP USER icat CASCADE;
DROP USER icatuser CASCADE;
DROP USER dataportal CASCADE;
DROP USER testicat CASCADE;

prompt
prompt ====================================================================
prompt database users now removed!
prompt
