CREATE OR REPLACE VIEW v_investigation AS
/*
in phase 1 this used the start dates in the shift table to calculate visit ids
but those are now set during the propagation process
*/
SELECT *
FROM investigation i
/
