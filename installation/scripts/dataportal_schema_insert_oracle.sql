-- 
-- Initialisation values for DP core schema
--
 
INSERT INTO SEQUENCE(SEQ_NAME, SEQ_COUNT) values ('SEQ_GEN', 0) ;
INSERT INTO SEQUENCE(SEQ_NAME, SEQ_COUNT) values ('SESSION', 0) ;
INSERT INTO SEQUENCE(SEQ_NAME, SEQ_COUNT) values ('USER', 0) ;
INSERT INTO SEQUENCE(SEQ_NAME, SEQ_COUNT) values ('EVENT_LOG', 0) ;
INSERT INTO SEQUENCE(SEQ_NAME, SEQ_COUNT) values ('EVENT_LOG_DETAILS', 0) ;
INSERT INTO SEQUENCE(SEQ_NAME, SEQ_COUNT) values ('CONSTANTS', 0) ;


insert into DP_ROLE values (1, 'USER',systimestamp);
insert into DP_ROLE values (2, 'ADMIN',systimestamp);

insert into DP_PROXY_SERVERS values (1,'myproxy-sso.grid-support.ac.uk', 7512, '/C=UK/O=eScience/OU=CLRC/L=RAL/CN=myproxy-sso.grid-support.ac.uk/E=support@grid-support.ac.uk', 'Y', systimestamp);
insert into DP_PROXY_SERVERS values (2,'myproxy.grid-support.ac.uk', 7512, '/C=UK/O=eScience/OU=CLRC/L=DL/CN=host/myproxy.grid-support.ac.uk/E=a.j.richards@dl.ac.uk','N',systimestamp);

insert into DP_FACILITY values (1,'DLS', 'Diamond Project','http://www.diamond.ac.uk/', systimestamp);
insert into DP_FACILITY values (2,'ISIS', 'ISIS Pulsed Neutron \\& Muon Source','http://www.isis.rl.ac.uk/', systimestamp); 

-- Set both as inactive
insert into DP_MODULE_LOOKUP values (1, 'https://localhost:8181/ICATService/ICAT?wsdl', 'user', 'password', 'oracle', 'dpal', 'DLS', 'N','N','N','N','Y', systimestamp) ;
insert into DP_MODULE_LOOKUP values (2, 'https://localhost:8181/ICATService/ICAT?wsdl', 'user', 'password', 'oracle', 'dpal', 'ISIS', 'N','N','N','N','Y', systimestamp) ;

insert into DP_EVENT values (1,'LOG_OFF','',systimestamp);
insert into DP_EVENT values (2,'LOG_ON','',systimestamp);
insert into DP_EVENT values (7,'LOG_ON_KERBEROS','',systimestamp);
insert into DP_EVENT values (3,'BASIC_SEARCH','',systimestamp);
insert into DP_EVENT values (4,'ADVANCED_SEARCH','',systimestamp);
insert into DP_EVENT values (5,'DOWNLOAD_DATAFILES','',systimestamp);
insert into DP_EVENT values (6,'DOWNLOAD_DATASET','',systimestamp);
insert into DP_EVENT values (8,'MYDATA_SEARCH','',systimestamp);
insert into DP_EVENT values (9,'INVESTIGATION_INCLUDE_SEARCH','',systimestamp);


insert into DP_CREDENTIAL_TYPE values (1,'PROXY','Normal proxy',systimestamp);
insert into DP_CREDENTIAL_TYPE values (2,'CERTIFICATE','Certificate only, no private key',systimestamp);
commit;





