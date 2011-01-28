set define ON

Prompt
Prompt
Prompt  Now creating the link to DuoDesk
--Accept duodesk_password CHAR hide prompt 'Enter Duodesk Password   :'
Accept duodesk_password CHAR prompt 'Enter Duodesk Password   :'

--CREATE DATABASE LINK "DUODESK.ESC.RL.AC.UK"
CREATE DATABASE LINK "DUODESK"
 CONNECT TO DUODESK1
 IDENTIFIED BY &duodesk_password
 USING 'duocore';

