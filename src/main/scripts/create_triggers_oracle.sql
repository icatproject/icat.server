-- Create procedures to update the fileSize of a Dataset or Invesitgation

CREATE PROCEDURE UPDATE_DS_FILESIZE (DATASET_ID NUMBER, DELTA NUMBER) AS
BEGIN
    UPDATE DATASET SET FILESIZE = NVL(FILESIZE, 0) + DELTA WHERE ID = DATASET_ID;
END;
/

CREATE PROCEDURE UPDATE_INV_FILESIZE (INVESTIGATION_ID NUMBER, DELTA NUMBER) AS
BEGIN
    UPDATE INVESTIGATION SET FILESIZE = NVL(FILESIZE, 0) + DELTA WHERE ID = INVESTIGATION_ID;
END;
/

-- Create procedures to update the fileCount of a Dataset or Invesitgation

CREATE PROCEDURE UPDATE_DS_FILECOUNT (DATASET_ID NUMBER, DELTA NUMBER) AS
BEGIN
    UPDATE DATASET SET FILECOUNT = NVL(FILECOUNT, 0) + DELTA WHERE ID = DATASET_ID;
END;
/

CREATE PROCEDURE UPDATE_INV_FILECOUNT (INVESTIGATION_ID NUMBER, DELTA NUMBER) AS
BEGIN
    UPDATE INVESTIGATION SET FILECOUNT = NVL(FILECOUNT, 0) + DELTA WHERE ID = INVESTIGATION_ID;
END;
/

-- Create triggers to recalculate the fileSize and fileCount after a Datafile insert/update/delete operation

CREATE TRIGGER RECALCULATE_ON_DF_INSERT AFTER INSERT ON DATAFILE
FOR EACH ROW
DECLARE
    DELTA NUMBER(19);
    INVESTIGATION_ID NUMBER(19);
BEGIN
    DELTA := NVL(:NEW.FILESIZE, 0);
    UPDATE_DS_FILESIZE(:NEW.DATASET_ID, DELTA);
    UPDATE_DS_FILECOUNT(:NEW.DATASET_ID, 1);
    SELECT i.ID INTO INVESTIGATION_ID FROM INVESTIGATION i JOIN DATASET ds ON ds.INVESTIGATION_ID = i.ID WHERE ds.ID = :NEW.DATASET_ID;
    UPDATE_INV_FILESIZE(INVESTIGATION_ID, DELTA);
    UPDATE_INV_FILECOUNT(INVESTIGATION_ID, 1);
END;
/

CREATE TRIGGER RECALCULATE_ON_DF_UPDATE AFTER UPDATE ON DATAFILE
FOR EACH ROW
DECLARE
    DELTA NUMBER(19);
    INVESTIGATION_ID NUMBER(19);
BEGIN
    IF :NEW.DATASET_ID != :OLD.DATASET_ID THEN
        DELTA := - NVL(:OLD.FILESIZE, 0);
        UPDATE_DS_FILESIZE(:OLD.DATASET_ID, DELTA);
        UPDATE_DS_FILECOUNT(:OLD.DATASET_ID, -1);
        SELECT i.ID INTO INVESTIGATION_ID FROM INVESTIGATION i JOIN DATASET ds ON ds.INVESTIGATION_ID = i.ID WHERE ds.ID = :OLD.DATASET_ID;
        UPDATE_INV_FILESIZE(INVESTIGATION_ID, DELTA);
        UPDATE_INV_FILECOUNT(INVESTIGATION_ID, -1);

        DELTA := NVL(:NEW.FILESIZE, 0);
        UPDATE_DS_FILESIZE(:NEW.DATASET_ID, DELTA);
        UPDATE_DS_FILECOUNT(:NEW.DATASET_ID, 1);
        SELECT i.ID INTO INVESTIGATION_ID FROM INVESTIGATION i JOIN DATASET ds ON ds.INVESTIGATION_ID = i.ID WHERE ds.ID = :NEW.DATASET_ID;
        UPDATE_INV_FILESIZE(INVESTIGATION_ID, DELTA);
        UPDATE_INV_FILECOUNT(INVESTIGATION_ID, 1);

    ELSIF NVL(:NEW.FILESIZE, 0) != NVL(:OLD.FILESIZE, 0) THEN
        DELTA := NVL(:NEW.FILESIZE, 0) - NVL(:OLD.FILESIZE, 0);
        UPDATE_DS_FILESIZE(:NEW.DATASET_ID, DELTA);
        SELECT i.ID INTO INVESTIGATION_ID FROM INVESTIGATION i JOIN DATASET ds ON ds.INVESTIGATION_ID = i.ID WHERE ds.ID = :NEW.DATASET_ID;
        UPDATE_INV_FILESIZE(INVESTIGATION_ID, DELTA);
    END IF;
END;
/

CREATE TRIGGER RECALCULATE_ON_DF_DELETE AFTER DELETE ON DATAFILE
FOR EACH ROW
DECLARE
    DELTA NUMBER(19);
    INVESTIGATION_ID NUMBER(19);
BEGIN
    DELTA := - NVL(:OLD.FILESIZE, 0);
    UPDATE_DS_FILESIZE(:OLD.DATASET_ID, DELTA);
    UPDATE_DS_FILECOUNT(:OLD.DATASET_ID, -1);
    BEGIN
        SELECT i.ID INTO INVESTIGATION_ID FROM INVESTIGATION i JOIN DATASET ds ON ds.INVESTIGATION_ID = i.ID WHERE ds.ID = :OLD.DATASET_ID;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN INVESTIGATION_ID := NULL;
    END;
    UPDATE_INV_FILESIZE(INVESTIGATION_ID, DELTA);
    UPDATE_INV_FILECOUNT(INVESTIGATION_ID, -1);
END;
/

-- Create triggers to recalculate the Investigation fileSize and fileCount after a Dataset update/delete operation

CREATE TRIGGER RECALCULATE_ON_DS_UPDATE AFTER UPDATE ON DATASET
FOR EACH ROW
DECLARE
    SIZE_DELTA NUMBER(19);
    COUNT_DELTA NUMBER(19);
BEGIN
    IF :NEW.INVESTIGATION_ID != :OLD.INVESTIGATION_ID THEN
        SIZE_DELTA := - NVL(:OLD.FILESIZE, 0);
        COUNT_DELTA := - NVL(:OLD.FILECOUNT, 0);
        UPDATE_INV_FILESIZE(:OLD.INVESTIGATION_ID, SIZE_DELTA);
        UPDATE_INV_FILECOUNT(:OLD.INVESTIGATION_ID, COUNT_DELTA);

        SIZE_DELTA := NVL(:NEW.FILESIZE, 0);
        COUNT_DELTA := NVL(:NEW.FILECOUNT, 0);
        UPDATE_INV_FILESIZE(:NEW.INVESTIGATION_ID, SIZE_DELTA);
        UPDATE_INV_FILECOUNT(:NEW.INVESTIGATION_ID, COUNT_DELTA);
    END IF;
END;
/

CREATE TRIGGER RECALCULATE_ON_DS_DELETE AFTER DELETE ON DATASET
FOR EACH ROW
DECLARE
    SIZE_DELTA NUMBER(19);
    COUNT_DELTA NUMBER(19);
BEGIN
    SIZE_DELTA := - NVL(:OLD.FILESIZE, 0);
    COUNT_DELTA := - NVL(:OLD.FILECOUNT, 0);
    UPDATE_INV_FILESIZE(:OLD.INVESTIGATION_ID, SIZE_DELTA);
    UPDATE_INV_FILECOUNT(:OLD.INVESTIGATION_ID, COUNT_DELTA);
END;
/

-- Initialize the fileSizes and fileCounts of all existing Datasets and Investigations

CREATE PROCEDURE INITIALIZE_DS_SIZE_COUNT AS
FILE_SIZE NUMBER(19);
FILE_COUNT NUMBER(19);
CURSOR CUR IS SELECT ID FROM DATASET;
BEGIN
    FOR CUR_DATASET in CUR LOOP
        SELECT SUM(df.FILESIZE) INTO FILE_SIZE FROM DATASET ds JOIN DATAFILE df ON df.DATASET_ID = ds.ID WHERE ds.ID = CUR_DATASET.ID;
        SELECT COUNT(df.ID) INTO FILE_COUNT FROM DATASET ds JOIN DATAFILE df ON df.DATASET_ID = ds.ID WHERE ds.ID = CUR_DATASET.ID;
        UPDATE DATASET SET FILESIZE = FILE_SIZE WHERE ID = CUR_DATASET.ID;
        UPDATE DATASET SET FILECOUNT = FILE_COUNT WHERE ID = CUR_DATASET.ID;
    END LOOP;
END;
/

CREATE PROCEDURE INITIALIZE_INV_SIZE_COUNT AS
FILE_SIZE NUMBER(19);
FILE_COUNT NUMBER(19);
CURSOR CUR IS SELECT ID FROM INVESTIGATION;
BEGIN
    FOR CUR_INVESTIGATION in CUR LOOP
        SELECT SUM(ds.FILESIZE) INTO FILE_SIZE FROM INVESTIGATION i JOIN DATASET ds ON ds.INVESTIGATION_ID = i.ID WHERE i.ID = CUR_INVESTIGATION.ID;
        SELECT SUM(ds.FILECOUNT) INTO FILE_COUNT FROM INVESTIGATION i JOIN DATASET ds ON ds.INVESTIGATION_ID = i.ID WHERE i.ID = CUR_INVESTIGATION.ID;
        UPDATE INVESTIGATION SET FILESIZE = FILE_SIZE WHERE ID = CUR_INVESTIGATION.ID;
        UPDATE INVESTIGATION SET FILECOUNT = FILE_COUNT WHERE ID = CUR_INVESTIGATION.ID;
    END LOOP;
END;
/

BEGIN
    INITIALIZE_DS_SIZE_COUNT;
    INITIALIZE_INV_SIZE_COUNT;
END;
/

DROP PROCEDURE INITIALIZE_DS_SIZE_COUNT;
DROP PROCEDURE INITIALIZE_INV_SIZE_COUNT;

exit