CREATE OR REPLACE TRIGGER investigation_date_trg
  before INSERT  ON investigation
  FOR EACH ROW
DECLARE

days number; 
BEGIN


IF :NEW.inv_type = 'experiment' THEN

select days_until_public_release into days from this_icat;

:new.release_date := :new.inv_end_date + days;

elsif :new.inv_type='calibration' then

:new.release_date := sysdate;

end if;

END;
/

