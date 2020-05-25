-- Add size columns to Dataset and Investigation tables

ALTER TABLE DATASET ADD DATASETSIZE NUMBER(19);
ALTER TABLE INVESTIGATION ADD INVESTIGATIONSIZE NUMBER(19);

-- Create procedures to update the size of a Dataset or Invesitgation

CREATE PROCEDURE UPDATE_DATASET_SIZE (DATASET_ID NUMBER, DELTA NUMBER) AS
BEGIN
    UPDATE DATASET SET DATASETSIZE = DATASETSIZE + DELTA WHERE ID = DATASET_ID;
END;
/

CREATE PROCEDURE UPDATE_INVESTIGATION_SIZE (INVESTIGATION_ID NUMBER, DELTA NUMBER) AS
BEGIN
    UPDATE INVESTIGATION SET INVESTIGATIONSIZE = INVESTIGATIONSIZE + DELTA WHERE ID = INVESTIGATION_ID;
END;
/

-- Create triggers to recalculate the size after a Datafile insert/update/delete operation

CREATE TRIGGER RECALCULATE_SIZES_ON_DATAFILE_INSERT AFTER INSERT ON DATAFILE
FOR EACH ROW
DECLARE
    INVESTIGATION_ID NUMBER(19);
BEGIN
    UPDATE_DATASET_SIZE(:NEW.DATASET_ID, :NEW.FILESIZE);
    SELECT i.ID INTO INVESTIGATION_ID FROM INVESTIGATION i JOIN DATASET ds ON ds.INVESTIGATION_ID = i.ID WHERE ds.ID = :NEW.DATASET_ID;
    UPDATE_INVESTIGATION_SIZE(INVESTIGATION_ID, :NEW.FILESIZE);
END;
/

CREATE TRIGGER RECALCULATE_SIZES_ON_DATAFILE_UPDATE AFTER UPDATE ON DATAFILE
FOR EACH ROW
DECLARE
    DELTA NUMBER(19);
    INVESTIGATION_ID NUMBER(19);
BEGIN
    IF :NEW.DATASET_ID != :OLD.DATASET_ID THEN
        DELTA := - :OLD.FILESIZE;
        UPDATE_DATASET_SIZE(:OLD.DATASET_ID, DELTA);
        SELECT i.ID INTO INVESTIGATION_ID FROM INVESTIGATION i JOIN DATASET ds ON ds.INVESTIGATION_ID = i.ID WHERE ds.ID = :OLD.DATASET_ID;
        UPDATE_INVESTIGATION_SIZE(INVESTIGATION_ID, DELTA);

        DELTA := :NEW.FILESIZE;
        UPDATE_DATASET_SIZE(:NEW.DATASET_ID, DELTA);
        SELECT i.ID INTO INVESTIGATION_ID FROM INVESTIGATION i JOIN DATASET ds ON ds.INVESTIGATION_ID = i.ID WHERE ds.ID = :NEW.DATASET_ID;
        UPDATE_INVESTIGATION_SIZE(INVESTIGATION_ID, DELTA);
    END IF;
    IF :NEW.FILESIZE != :OLD.FILESIZE THEN
        DELTA := :NEW.FILESIZE - :OLD.FILESIZE;
        UPDATE_DATASET_SIZE(:NEW.DATASET_ID, DELTA);
        SELECT i.ID INTO INVESTIGATION_ID FROM INVESTIGATION i JOIN DATASET ds ON ds.INVESTIGATION_ID = i.ID WHERE ds.ID = :NEW.DATASET_ID;
        UPDATE_INVESTIGATION_SIZE(INVESTIGATION_ID, DELTA);
    END IF;
END;
/

CREATE TRIGGER RECALCULATE_SIZES_ON_DATAFILE_DELETE AFTER DELETE ON DATAFILE
FOR EACH ROW
DECLARE
    DELTA NUMBER(19);
    INVESTIGATION_ID NUMBER(19);
BEGIN
    DELTA := - :OLD.FILESIZE;
    UPDATE_DATASET_SIZE(:OLD.DATASET_ID, DELTA);
    SELECT i.ID INTO INVESTIGATION_ID FROM INVESTIGATION i JOIN DATASET ds ON ds.INVESTIGATION_ID = i.ID WHERE ds.ID = :OLD.DATASET_ID;
    UPDATE_INVESTIGATION_SIZE(INVESTIGATION_ID, DELTA);
END;
/

-- Initialize the sizes of all existing Datasets and Investigations

CREATE PROCEDURE INITIALIZE_DATASET_SIZES AS
DATASET_SIZE NUMBER(19);
CURSOR CUR IS SELECT ID FROM DATASET;
BEGIN
    FOR CUR_DATASET in CUR LOOP
        SELECT SUM(df.FILESIZE) INTO DATASET_SIZE FROM DATASET ds JOIN DATAFILE df ON df.DATASET_ID = ds.ID WHERE ds.ID = CUR_DATASET.ID;
        UPDATE DATASET SET DATASETSIZE = DATASET_SIZE WHERE ID = CUR_DATASET.ID;
    END LOOP;
END;
/

CREATE PROCEDURE INITIALIZE_INVESTIGATION_SIZES AS
INVESTIGATION_SIZE NUMBER(19);
CURSOR CUR IS SELECT ID FROM INVESTIGATION;
BEGIN
    FOR CUR_INVESTIGATION in CUR LOOP
        SELECT SUM(ds.DATASETSIZE) INTO INVESTIGATION_SIZE FROM INVESTIGATION i JOIN DATASET ds ON ds.INVESTIGATION_ID = i.ID WHERE i.ID = CUR_INVESTIGATION.ID;
        UPDATE INVESTIGATION SET INVESTIGATIONSIZE = INVESTIGATION_SIZE WHERE ID = CUR_INVESTIGATION.ID;
    END LOOP;
END;
/

BEGIN
    INITIALIZE_DATASET_SIZES;
    INITIALIZE_INVESTIGATION_SIZES;
END;
/

DROP PROCEDURE INITIALIZE_DATASET_SIZES;
DROP PROCEDURE INITIALIZE_INVESTIGATION_SIZES;

exit