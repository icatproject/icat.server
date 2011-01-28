CREATE OR REPLACE procedure check_missing_instrument as
begin
FOR beamline IN (select instrument from ((select distinct instrument from investigation)
 minus 
(select instrument from beamline_instrument)) where instrument !='p45') LOOP
email_problem(ikit =>beamline.instrument,SQL_ERROR => 'This beamline has not been set up yet');
end loop;
end check_missing_instrument;
/