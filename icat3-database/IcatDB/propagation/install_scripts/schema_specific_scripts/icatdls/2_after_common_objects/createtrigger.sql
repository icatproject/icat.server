
CREATE OR REPLACE TRIGGER TRG_CHECK_LOG_TABLE
BEFORE INSERT
ON LOG_TABLE REFERENCING NEW AS New OLD AS Old
FOR EACH ROW
DECLARE
tmpVar NUMBER;
/******************************************************************************
   NAME:       TRG_CHECK_LOG_TABLE
   PURPOSE:    the purpost of this trigger is to make sure that when a problem 
                is entered into the log table, an email is sent out to 
                the DBS team to alert them to the fact.

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        19/02/2010   Keir hakwer     1. Created this trigger.

   NOTES:

   Automatically available Auto Replace Keywords:
******************************************************************************/
BEGIN
  
if instr(:new.text,'Propagation failed') > 0 then
&icatdls_username..email_problem('Unknown','ERROR ENTRY INTO ICATDLS33.LOG_TABLE');
end if;

   EXCEPTION
     WHEN OTHERS THEN
       -- Consider logging the error and then re-raise
       RAISE;
END TRG_CHECK_LOG_TABLE;
/
SHOW ERRORS;