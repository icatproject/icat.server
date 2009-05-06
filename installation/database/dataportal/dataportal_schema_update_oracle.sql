UPDATE  DP_MODULE_LOOKUP SET LOCATION = 'https://localhost:8181/ICATService/ICAT?wsdl' ;
COMMIT;
--Not Needed below
--DELETE DP_PROXY_SERVERS;
--INSERT INTO DP_PROXY_SERVERS values (1, 'myproxy-sso.grid-support.ac.uk',7512, '/C=UK/O=eScience/OU=CLRC/L=RAL/CN=myproxy-sso.grid-support.ac.uk/E=support@grid-support.ac.uk',  'Y', systimestamp);
--COMMIT;
UPDATE DP_MODULE_LOOKUP SET ACTIVE = 'N';
UPDATE DP_MODULE_LOOKUP SET ACTIVE = 'Y' WHERE FACILITY = 'FACILITY_TO_INSERT';
COMMIT;