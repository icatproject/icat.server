alter table INSTRUMENT add column PID varchar(255) default NULL;
alter table PARAMETERTYPE add column PID varchar(255) default NULL;
alter table SAMPLE add column PID varchar(255) default NULL;
alter table SHIFT add column INSTRUMENT_ID bigint(20) default NULL,
	add constraint FK_SHIFT_INSTRUMENT_ID
	foreign key (INSTRUMENT_ID) references INSTRUMENT (ID);
alter table USER_ add column GIVENNAME varchar(255) default NULL,
	add column FAMILYNAME varchar(255) default NULL,
	add column AFFILIATION varchar(255) default NULL;
