CREATE OR REPLACE PROCEDURE TEST_LINKS 
AS
   l_last_update   beamline_instrument.last_update%TYPE;
   l_valid         beamline_instrument.valid%TYPE;
   err_message     VARCHAR2 (500);

   CURSOR c_instruments
   IS
      SELECT dblink
        FROM beamline_instrument;
BEGIN
   FOR ins_link IN c_instruments
   LOOP
      BEGIN
         SELECT last_update, valid
           INTO l_last_update, l_valid
           FROM beamline_instrument
          WHERE dblink = ins_link.dblink;

         EXECUTE IMMEDIATE    'select ''connected!!!'' from dual@'
                           || ins_link.dblink;

         UPDATE beamline_instrument
            SET valid = 'Y',
                last_update = SYSDATE
          WHERE dblink = ins_link.dblink;

         IF l_valid = 'N'
         THEN
            email_problem (ins_link.dblink, NULL, 'Yes');
         END IF;
         
      COMMIT;
      execute immediate ('alter session close database link ' || ins_link.dblink);         
      EXCEPTION
         WHEN OTHERS
         THEN
            err_message := SQLERRM;

            IF (l_valid = 'Y')
            THEN
               email_problem (ins_link.dblink, err_message);

               UPDATE beamline_instrument
                  SET valid = 'N',
                      last_update = SYSDATE
                WHERE dblink = ins_link.dblink;
            ELSIF (l_last_update + 1 < SYSDATE)
            THEN
               email_problem (ins_link.dblink, err_message);

               UPDATE beamline_instrument
                  SET last_update = SYSDATE
                WHERE dblink = ins_link.dblink;
            END IF;
      --raise;
      END;

            
   END LOOP;
END;
/
