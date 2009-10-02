CREATE OR REPLACE TRIGGER investigation_date_trg
  before INSERT  ON INVESTIGATION   FOR EACH ROW
DECLARE

days number;
BEGIN

--update by K Hawker on 15/07/2009 to ensure all visit id's are upper case
:new.visit_id := upper(:new.visit_id);

IF :NEW.inv_type = 'experiment' THEN

select days_until_public_release into days from this_icat;

:new.release_date := :new.inv_end_date + days;

elsif :new.inv_type='calibration' then

:new.release_date := sysdate;

end if;

END;
/

