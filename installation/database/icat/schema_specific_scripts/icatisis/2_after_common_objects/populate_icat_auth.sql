CREATE OR REPLACE PROCEDURE populate_icat_auth (p_mod_id IN icat_authorisation.mod_id%type) iS




BEGIN

  log_pkg.write_log('begin auth population error');

INSERT INTO ICAT_AUTHORISATION (
INVESTIGATION_ID,       
USER_ID,                
ROLE,                   
ELEMENT_TYPE,           
ELEMENT_ID,             
MOD_TIME,               
MOD_ID,                 
CREATE_TIME,            
CREATE_ID,              
FACILITY_AQUIRED,       
DELETED)
SELECT ID,
'ANY',
'DOWNLOADER',
NULL,
NULL,
systimestamp,
p_mod_id,
systimestamp,
p_mod_id,
'Y',
'N'
FROM investigation;

commit;
                
log_pkg.write_log('end auth population succesful');

EXCEPTION
WHEN OTHERS THEN
  log_pkg.write_log('icat auth population error '||sqlerrm);
  RAISE;



END;
/

