-- Add Technique table

CREATE TABLE TECHNIQUE (
	ID BIGINT NOT NULL AUTO_INCREMENT,
	CREATE_ID VARCHAR(255) NOT NULL,
	CREATE_TIME DATETIME NOT NULL,
	MOD_ID VARCHAR(255) NOT NULL,
	MOD_TIME DATETIME NOT NULL,
	NAME VARCHAR(255) NOT NULL,
	PID VARCHAR(255),
	DESCRIPTION VARCHAR(255),
	PRIMARY KEY (ID)
);

ALTER TABLE TECHNIQUE ADD CONSTRAINT UNQ_TECHNIQUE_0 UNIQUE (NAME);

-- Add DatasetTechnique table

CREATE TABLE DATASETTECHNIQUE (
	ID BIGINT NOT NULL AUTO_INCREMENT,
	CREATE_ID VARCHAR(255) NOT NULL,
	CREATE_TIME DATETIME NOT NULL,
	MOD_ID VARCHAR(255) NOT NULL,
	MOD_TIME DATETIME NOT NULL,
	DATASET_ID BIGINT NOT NULL,
	TECHNIQUE_ID BIGINT NOT NULL,
	PRIMARY KEY (ID)
);

ALTER TABLE DATASETTECHNIQUE ADD CONSTRAINT FK_DATASETTECHNIQUE_DATASET_ID FOREIGN KEY (DATASET_ID) REFERENCES DATASET (ID);
ALTER TABLE DATASETTECHNIQUE ADD CONSTRAINT FK_DATASETTECHNIQUE_TECHNIQUE_ID FOREIGN KEY (TECHNIQUE_ID) REFERENCES TECHNIQUE (ID);
ALTER TABLE DATASETTECHNIQUE ADD CONSTRAINT UNQ_DATASETTECHNIQUE_0 UNIQUE (DATASET_ID, TECHNIQUE_ID);

-- Add DatasetInstrument table

CREATE TABLE DATASETINSTRUMENT (
	ID BIGINT NOT NULL AUTO_INCREMENT,
	CREATE_ID VARCHAR(255) NOT NULL,
	CREATE_TIME DATETIME NOT NULL,
	MOD_ID VARCHAR(255) NOT NULL,
	MOD_TIME DATETIME NOT NULL,
	DATASET_ID BIGINT NOT NULL,
	INSTRUMENT_ID BIGINT NOT NULL,
	PRIMARY KEY (ID)
);

ALTER TABLE DATASETINSTRUMENT ADD CONSTRAINT FK_DATASETINSTRUMENT_DATASET_ID FOREIGN KEY (DATASET_ID) REFERENCES DATASET (ID);
ALTER TABLE DATASETINSTRUMENT ADD CONSTRAINT FK_DATASETINSTRUMENT_INSTRUMENT_ID FOREIGN KEY (INSTRUMENT_ID) REFERENCES INSTRUMENT (ID);
ALTER TABLE DATASETINSTRUMENT ADD CONSTRAINT UNQ_DATASETINSTRUMENT_0 UNIQUE (DATASET_ID, INSTRUMENT_ID);

-- Add fileSize and fileCount columns to Dataset and Investigation tables

ALTER TABLE DATASET ADD FILESIZE BIGINT;
ALTER TABLE DATASET ADD FILECOUNT BIGINT;
ALTER TABLE INVESTIGATION ADD FILESIZE BIGINT;
ALTER TABLE INVESTIGATION ADD FILECOUNT BIGINT;

-- Add DataPublication table

CREATE TABLE DATAPUBLICATION (
	ID BIGINT NOT NULL AUTO_INCREMENT,
	CREATE_ID VARCHAR(255) NOT NULL,
	CREATE_TIME DATETIME NOT NULL,
	MOD_ID VARCHAR(255) NOT NULL,
	MOD_TIME DATETIME NOT NULL,
	FACILITY_ID BIGINT NOT NULL,
	DATACOLLECTION_ID BIGINT NOT NULL,
	PID VARCHAR(255) NOT NULL,
	TITLE VARCHAR(255) NOT NULL,
	PUBLICATIONDATE DATETIME,
	SUBJECT VARCHAR(1023),
	DESCRIPTION VARCHAR(4000),
	PRIMARY KEY (ID)
);

ALTER TABLE DATAPUBLICATION ADD CONSTRAINT FK_DATAPUBLICATION_FACILITY_ID FOREIGN KEY (FACILITY_ID) REFERENCES FACILITY (ID);
ALTER TABLE DATAPUBLICATION ADD CONSTRAINT FK_DATAPUBLICATION_DATACOLLECTION_ID FOREIGN KEY (DATACOLLECTION_ID) REFERENCES DATACOLLECTION (ID);
ALTER TABLE DATAPUBLICATION ADD CONSTRAINT UNQ_DATAPUBLICATION_0 UNIQUE (FACILITY_ID, PID);

-- Add DataPublicationUser table

CREATE TABLE DATAPUBLICATIONUSER (
	ID BIGINT NOT NULL AUTO_INCREMENT,
	CREATE_ID VARCHAR(255) NOT NULL,
	CREATE_TIME DATETIME NOT NULL,
	MOD_ID VARCHAR(255) NOT NULL,
	MOD_TIME DATETIME NOT NULL,
	DATAPUBLICATION_ID BIGINT NOT NULL,
	USER_ID BIGINT NOT NULL,
	CONTRIBUTORTYPE VARCHAR(255) NOT NULL,
	ORDERKEY VARCHAR(255),
	FULLNAME VARCHAR(255),
	FAMILYNAME VARCHAR(255),
	GIVENNAME VARCHAR(255),
	PRIMARY KEY (ID)
);

ALTER TABLE DATAPUBLICATIONUSER ADD CONSTRAINT FK_DATAPUBLICATIONUSER_DATAPUBLICATION_ID FOREIGN KEY (DATAPUBLICATION_ID) REFERENCES DATAPUBLICATION (ID);
ALTER TABLE DATAPUBLICATIONUSER ADD CONSTRAINT FK_DATAPUBLICATIONUSER_USER_ID FOREIGN KEY (USER_ID) REFERENCES USER_ (ID);
ALTER TABLE DATAPUBLICATIONUSER ADD CONSTRAINT UNQ_DATAPUBLICATIONUSER_0 UNIQUE (DATAPUBLICATION_ID, USER_ID, CONTRIBUTORTYPE);

-- Add Affiliation table

CREATE TABLE AFFILIATION (
	ID BIGINT NOT NULL AUTO_INCREMENT,
	CREATE_ID VARCHAR(255) NOT NULL,
	CREATE_TIME DATETIME NOT NULL,
	MOD_ID VARCHAR(255) NOT NULL,
	MOD_TIME DATETIME NOT NULL,
	DATAPUBLICATIONUSER_ID BIGINT NOT NULL,
	NAME VARCHAR(1023) NOT NULL,
	PID VARCHAR(255),
	PRIMARY KEY (ID)
);

ALTER TABLE AFFILIATION ADD CONSTRAINT FK_AFFILIATION_DATAPUBLICATIONUSER_ID FOREIGN KEY (DATAPUBLICATIONUSER_ID) REFERENCES DATAPUBLICATIONUSER (ID);
ALTER TABLE AFFILIATION ADD CONSTRAINT UNQ_AFFILIATION_0 UNIQUE (DATAPUBLICATIONUSER_ID, NAME);

-- Add DataPublicationDate table

CREATE TABLE DATAPUBLICATIONDATE (
	ID BIGINT NOT NULL AUTO_INCREMENT,
	CREATE_ID VARCHAR(255) NOT NULL,
	CREATE_TIME DATETIME NOT NULL,
	MOD_ID VARCHAR(255) NOT NULL,
	MOD_TIME DATETIME NOT NULL,
	DATAPUBLICATION_ID BIGINT NOT NULL,
	DATETYPE VARCHAR(255) NOT NULL,
	DATE VARCHAR(255) NOT NULL,
	PRIMARY KEY (ID)
);

ALTER TABLE DATAPUBLICATIONDATE ADD CONSTRAINT FK_DATAPUBLICATIONDATE_DATAPUBLICATION_ID FOREIGN KEY (DATAPUBLICATION_ID) REFERENCES DATAPUBLICATION (ID);
ALTER TABLE DATAPUBLICATIONDATE ADD CONSTRAINT UNQ_DATAPUBLICATIONDATE_0 UNIQUE (DATAPUBLICATION_ID, DATETYPE);

-- Add RelatedIdentifier table

CREATE TABLE RELATEDIDENTIFIER (
	ID BIGINT NOT NULL AUTO_INCREMENT,
	CREATE_ID VARCHAR(255) NOT NULL,
	CREATE_TIME DATETIME NOT NULL,
	MOD_ID VARCHAR(255) NOT NULL,
	MOD_TIME DATETIME NOT NULL,
	DATAPUBLICATION_ID BIGINT NOT NULL,
	IDENTIFIER VARCHAR(255) NOT NULL,
	RELATIONTYPE VARCHAR(255) NOT NULL,
	FULLREFERENCE VARCHAR(1023),
	PRIMARY KEY (ID)
);

ALTER TABLE RELATEDIDENTIFIER ADD CONSTRAINT FK_RELATEDIDENTIFIER_DATAPUBLICATION_ID FOREIGN KEY (DATAPUBLICATION_ID) REFERENCES DATAPUBLICATION (ID);
ALTER TABLE RELATEDIDENTIFIER ADD CONSTRAINT UNQ_RELATEDIDENTIFIER_0 UNIQUE (DATAPUBLICATION_ID, IDENTIFIER);

-- Add FundingReference table

CREATE TABLE FUNDINGREFERENCE (
	ID BIGINT NOT NULL AUTO_INCREMENT,
	CREATE_ID VARCHAR(255) NOT NULL,
	CREATE_TIME DATETIME NOT NULL,
	MOD_ID VARCHAR(255) NOT NULL,
	MOD_TIME DATETIME NOT NULL,
	FUNDERNAME VARCHAR(255) NOT NULL,
	FUNDERIDENTIFIER VARCHAR(255),
	AWARDNUMBER VARCHAR(255) NOT NULL,
	AWARDTITLE VARCHAR(255),
	PRIMARY KEY (ID)
);

ALTER TABLE FUNDINGREFERENCE ADD CONSTRAINT UNQ_FUNDINGREFERENCE_0 UNIQUE (FUNDERNAME, AWARDNUMBER);

-- Add InvestigationFunding table

CREATE TABLE INVESTIGATIONFUNDING (
	ID BIGINT NOT NULL AUTO_INCREMENT,
	CREATE_ID VARCHAR(255) NOT NULL,
	CREATE_TIME DATETIME NOT NULL,
	MOD_ID VARCHAR(255) NOT NULL,
	MOD_TIME DATETIME NOT NULL,
	INVESTIGATION_ID BIGINT NOT NULL,
	FUNDING_ID BIGINT NOT NULL,
	PRIMARY KEY (ID)
);

ALTER TABLE INVESTIGATIONFUNDING ADD CONSTRAINT FK_INVESTIGATIONFUNDING_INVESTIGATION_ID FOREIGN KEY (INVESTIGATION_ID) REFERENCES INVESTIGATION (ID);
ALTER TABLE INVESTIGATIONFUNDING ADD CONSTRAINT FK_INVESTIGATIONFUNDING_FUNDING_ID FOREIGN KEY (FUNDING_ID) REFERENCES FUNDINGREFERENCE (ID);
ALTER TABLE INVESTIGATIONFUNDING ADD CONSTRAINT UNQ_INVESTIGATIONFUNDING_0 UNIQUE (INVESTIGATION_ID, FUNDING_ID);

-- Add DataPublicationFunding table

CREATE TABLE DATAPUBLICATIONFUNDING (
	ID BIGINT NOT NULL AUTO_INCREMENT,
	CREATE_ID VARCHAR(255) NOT NULL,
	CREATE_TIME DATETIME NOT NULL,
	MOD_ID VARCHAR(255) NOT NULL,
	MOD_TIME DATETIME NOT NULL,
	DATAPUBLICATION_ID BIGINT NOT NULL,
	FUNDING_ID BIGINT NOT NULL,
	PRIMARY KEY (ID)
);

ALTER TABLE DATAPUBLICATIONFUNDING ADD CONSTRAINT FK_DATAPUBLICATIONFUNDING_INVESTIGATION_ID FOREIGN KEY (DATAPUBLICATION_ID) REFERENCES DATAPUBLICATION (ID);
ALTER TABLE DATAPUBLICATIONFUNDING ADD CONSTRAINT FK_DATAPUBLICATIONFUNDING_FUNDING_ID FOREIGN KEY (FUNDING_ID) REFERENCES FUNDINGREFERENCE (ID);
ALTER TABLE DATAPUBLICATIONFUNDING ADD CONSTRAINT UNQ_DATAPUBLICATIONFUNDING_0 UNIQUE (DATAPUBLICATION_ID, FUNDING_ID);