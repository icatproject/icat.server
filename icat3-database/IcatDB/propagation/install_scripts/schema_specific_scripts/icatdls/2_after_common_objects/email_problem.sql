CREATE OR REPLACE PROCEDURE email_problem (
   ikit        IN   VARCHAR2,
   sql_error   IN   VARCHAR2,
   is_clear    IN   VARCHAR2 := 'NO',
   icat        in   varchar2 := 'NO'
)
AS
   -- mailhost VARCHAR2(100) := 'outbox.rl.ac.uk'; changed on 12/11/2007 due to poor connection to email sever
   mailhost    VARCHAR2 (100)      := 'localhost';
   mail_conn   UTL_SMTP.connection;
   sender      VARCHAR2 (50);
   recipient   VARCHAR2 (50);
   subject     VARCHAR2 (200);
   MESSAGE     CLOB;
   email_no    NUMBER;
   count1      NUMBER;
BEGIN
   sender := 'databaseservices@stfc.ac.uk';
--   sender := 'c.cioffi@rl.ac.uk';
   recipient := 'databaseservices@stfc.ac.uk';
   --recipient := 'carmine.cioffi@stfc.ac.uk';
if icat <> 'NO' then
subject := 'ICATDLS33 Problem with Icat Propagation in facilities';
      MESSAGE :=
            '
To the Database Services Team,

There has been a problem with propagating data between the User Offcice and Maia.icatdls33

The problem is:' || sql_error ||

'Please investigation and inform Alun Ashton of any updates immediately.

Thankyou
Database Services Team.
';
else

   IF is_clear = 'NO'
   THEN
      subject := 'ICATDLS33 Problem with Icat Propagation in facilities';
      MESSAGE :=
            '
To the Database Services Team,

There has been a problem with propagating data between Maia.icatdls33 and the iKittens.

It is likely to have been a problem on ikitten: '
         || ikit
         || '.  It is failing with the error "'
         || sql_error
         || '".

Please investigate and make sure that Oracle Express on this machine is running correctly.

Please inform Alun Ashton of any updates immediately.

Thankyou
Database Services Team.
';
   ELSE
      subject := 'Clear: Problem with Icat Propagation in facilities';
      MESSAGE :=
            '
To the Database Services Team,

The iKitten '
         || ikit
         || ' is now reachable therefore has been added to the the list of valid iKittens.


Thankyou
Database Services Team.
';
   END IF;
   end if;

--email_no := email_queue.email_no;
   mail_conn := UTL_SMTP.open_connection (mailhost);
   UTL_SMTP.helo (mail_conn, mailhost);
   UTL_SMTP.mail (mail_conn, sender);                                -- sender
   UTL_SMTP.rcpt (mail_conn, recipient);                          -- recipient
   UTL_SMTP.open_data (mail_conn);
   UTL_SMTP.write_data (mail_conn,
                           'From: "Facilities Error" <'
                        || sender
                        || '>'
                        || UTL_TCP.crlf
                       );
   UTL_SMTP.write_data (mail_conn,
                           'To:       "Recipient" <'
                        || recipient
                        || '>'
                        || UTL_TCP.crlf
                       );
   UTL_SMTP.write_data (mail_conn, 'Subject: ' || subject || UTL_TCP.crlf);
   UTL_SMTP.write_data (mail_conn, UTL_TCP.crlf);

   FOR i IN 1 .. DBMS_LOB.getlength (MESSAGE)
   LOOP
      UTL_SMTP.write_data (mail_conn, SUBSTR (MESSAGE, i, 1));
   END LOOP;

   UTL_SMTP.close_data (mail_conn);
   UTL_SMTP.quit (mail_conn);
END;
/