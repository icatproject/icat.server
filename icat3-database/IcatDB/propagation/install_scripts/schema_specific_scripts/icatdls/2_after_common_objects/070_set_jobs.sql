begin
  dbms_scheduler.create_job(
      job_name => 'SET_RANGE_AND_DATES'
     ,job_type => 'PLSQL_BLOCK'
     ,job_action => 'begin  SET_INVESTIGATION_DATES(); set_icat_auth_data_entities();end; '
     ,start_date => (trunc(sysdate +1 )-2/24)
     ,repeat_interval => 'FREQ=DAILY'
     ,enabled => TRUE
     ,comments => 'Demo for job schedule.');
end;
/