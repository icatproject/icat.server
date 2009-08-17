REM the directory for the source datafiles of the external tables.
REM it must be mounted with the same path on all machines in the cluster.
REM as different versions of icat could be on the same cluser we have a version
REM number in the directory

CREATE OR REPLACE DIRECTORY external_tables AS '/opt/oracle/backup/facilitiesext/3.3.0';
