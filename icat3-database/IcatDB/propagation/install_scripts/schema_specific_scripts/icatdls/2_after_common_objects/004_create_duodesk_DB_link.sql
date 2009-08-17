set define ON

Prompt
Prompt
Prompt  Now creating the link to DuoDesk
Accept duodesk_password CHAR hide prompt 'Enter Duodesk Password   :'

CREATE DATABASE LINK "DUODESK.DL.AC.UK"
 CONNECT TO DUODESK1
 IDENTIFIED BY &duodesk_password
 USING 'duocore';

