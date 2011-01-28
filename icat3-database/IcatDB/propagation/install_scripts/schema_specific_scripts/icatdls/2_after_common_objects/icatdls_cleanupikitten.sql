CREATE OR REPLACE PACKAGE cleanupikitten_pkg AS

PROCEDURE cleanup_instruments(dblink varchar2);
PROCEDURE cleanup_parameters(dblink varchar2);
PROCEDURE cleanup_investigations(dblink varchar2);
PROCEDURE cleanup_shifts(dblink varchar2);
PROCEDURE cleanup_facility_users(dblink varchar2);
PROCEDURE cleanup_investigators(dblink varchar2);
PROCEDURE cleanup_keywords(dblink varchar2);
PROCEDURE cleanup_publications(dblink varchar2);
PROCEDURE cleanup_samples(dblink varchar2);
PROCEDURE cleanup_sample_parameters(dblink varchar2);
PROCEDURE cleanup_icat_authorisation(dblink varchar2);
PROCEDURE cleanup;
END cleanupikitten_pkg;
/


CREATE OR REPLACE PACKAGE BODY cleanupikitten_pkg AS

PROCEDURE cleanup_instruments(dblink varchar2) as
begin
execute immediate 'delete from instrument@'||dblink||' ikit where exists( select '''' from instrument icat where icat.name=ikit.name and icat.deleted=''Y'')';
commit;
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception(SQLERRM,1);
    RAISE;
end;

PROCEDURE cleanup_parameters(dblink varchar2) as 
begin
execute immediate 'delete from parameter@'||dblink||' ikit where exists( select '''' from parameter icat where icat.name=ikit.name and icat.units=ikit.units and icat.deleted=''Y'')';
delete from temp_samplesheet;
commit; 
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception(SQLERRM,1);
    RAISE;
end;

PROCEDURE cleanup_investigations(dblink varchar2) as
begin
execute immediate 'delete from investigation@'||dblink||' ikit where exists( select '''' from investigation icat where icat.id=ikit.id AND icat.deleted=''Y'')';
commit;
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception(SQLERRM,1);
    RAISE;
end;

PROCEDURE cleanup_shifts(dblink varchar2) as
begin
execute immediate 'delete from shift@'||dblink||' ikit where exists( select '''' from shift icat where icat.investigation_id=ikit.investigation_id and icat.start_date=ikit.start_date and icat.end_date=ikit.end_date and icat.deleted=''Y'')';
commit; 
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception(SQLERRM,1);
    RAISE;
end;

PROCEDURE cleanup_facility_users(dblink varchar2) as
BEGIN
execute immediate 'delete from facility_user@'||dblink||' ikit where exists( select '''' from facility_user icat where icat.facility_user_id=ikit.facility_user_id AND icat.deleted=''Y'')';
commit;
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception(SQLERRM,1);
    RAISE;
END;

PROCEDURE cleanup_investigators(dblink varchar2) AS
BEGIN
execute immediate 'delete from investigator@'||dblink||' ikit where exists( select '''' from investigator icat where icat.facility_user_id=ikit.facility_user_id AND icat.investigation_id=ikit.investigation_id AND icat.deleted=''Y'')';
commit;
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception(SQLERRM,1);
END;


PROCEDURE cleanup_keywords(dblink varchar2) AS
BEGIN
execute immediate 'delete from keyword@'||dblink||' ikit where exists( select '''' from keyword icat where icat.name=ikit.name AND icat.investigation_id=ikit.investigation_id AND icat.deleted=''Y'')';
commit;
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception(SQLERRM,1);
END;


PROCEDURE cleanup_publications(dblink varchar2) AS
BEGIN
execute immediate 'delete from publication@'||dblink||' ikit where exists( select '''' from publication icat where icat.id=ikit.id  AND icat.deleted=''Y'')';
Commit;
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception(SQLERRM,1);
END;

PROCEDURE cleanup_samples(dblink varchar2) AS
BEGIN
execute immediate 'delete from sample@'||dblink||' ikit where exists( select '''' from sample icat where icat.id=ikit.id  AND icat.deleted=''Y'')';
commit;
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception(SQLERRM,1);
END;

PROCEDURE cleanup_sample_parameters(dblink varchar2) AS
BEGIN
execute immediate 'delete from sample_parameter@'||dblink||' ikit where exists( select '''' from sample_parameter icat where icat.sample_id=ikit.sample_id  AND icat.name=ikit.name  AND icat.units=ikit.units  AND icat.deleted=''Y'')';
commit; 
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception(SQLERRM,1);
END;

PROCEDURE cleanup_icat_authorisation(dblink varchar2) AS
BEGIN
execute immediate 'delete from icat_authorisation@'||dblink||' ikit where exists( select '''' from icat_authorisation icat where icat.id=ikit.id  AND icat.deleted=''Y'')';
Commit;  
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception(SQLERRM,1);
END;

PROCEDURE cleanup AS
begin
log_pkg.init;
log_pkg.write_log('Begin ikittens cleanup');
for row in (SELECT  dblink FROM beamline_instrument) LOOP
  log_pkg.write_log('Starting cleanup of ikitten '||row.dblink);
  log_pkg.write_log('Cleaning up instrument');
  cleanup_instruments(row.dblink);
  log_pkg.write_log('instrument cleaned up ');
  log_pkg.write_log('Cleaning up parameter');
  cleanup_parameters(row.dblink);
  log_pkg.write_log('parameter cleaned up ');
  log_pkg.write_log('Cleaning up investigation');
  cleanup_investigations(row.dblink);
  log_pkg.write_log('investigation cleaned up ');
  log_pkg.write_log('Cleaning up shift');
  cleanup_shifts(row.dblink);
  log_pkg.write_log('shift cleaned up ');
  log_pkg.write_log('Cleaning up facility_user');
  cleanup_facility_users(row.dblink);
  log_pkg.write_log('facility_user cleaned up ');
  log_pkg.write_log('Cleaning up investigator');
  cleanup_investigators(row.dblink);
  log_pkg.write_log('investigator cleaned up ');
  log_pkg.write_log('Cleaning up keyword');
  cleanup_keywords(row.dblink);
  log_pkg.write_log('keyword cleaned up ');
  log_pkg.write_log('Cleaning up publication');
  cleanup_publications(row.dblink);
  log_pkg.write_log('publication cleaned up ');
  log_pkg.write_log('Cleaning up sample');
  cleanup_samples(row.dblink);
  log_pkg.write_log('sample cleaned up ');
  log_pkg.write_log('Cleaning up sample_parameter');
  cleanup_sample_parameters(row.dblink);
  log_pkg.write_log('sample_parameter cleaned up ');
  log_pkg.write_log('Cleaning up icat_authorisation');
  cleanup_icat_authorisation(row.dblink);
  log_pkg.write_log('icat_authorisation cleaned up ');
  log_pkg.write_log('Cleanup of '||row.dblink||' finished');
END loop;
log_pkg.write_log('Ikittens Cleanup finshed SUCCESSFUL');
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception(SQLERRM,1);
    log_pkg.write_log('Ikitten Cleanup, UNSUCCESSFULLY');
end;

END cleanupikitten_pkg;

--'PROPAGATION'
/