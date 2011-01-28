CREATE OR REPLACE FUNCTION shift_time (a DATE, b NUMBER)
      RETURN DATE is
      shift   CHAR (5);
   BEGIN
      CASE b
         WHEN 1
         THEN
            shift := '01:00';
         WHEN 2
         THEN
            shift := '09:00';
         WHEN 3
         THEN
            shift := '17:00';
         ELSE
            shift := '09:00';
      END CASE;
      RETURN TO_DATE (TO_CHAR (a, 'DD/MM/YYYY') || ' ' || shift,
                      'DD/MM/YYYY HH24:MI'
                     );
   END;
/