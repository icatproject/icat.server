CREATE OR REPLACE PACKAGE global_parameters_pkg AS


GV_CREATE_ID CONSTANT investigation.create_id%TYPE := 'FROM PROPAGATION';


GV_REMOVED_CREATE_ID CONSTANT investigation.create_id%TYPE
  := 'FROM PROPAGATION, SUBSEQUENTLY REMOVED';



END global_parameters_pkg;
/


/