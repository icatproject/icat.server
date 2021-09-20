-- Create procedures to update the size of a Dataset or Invesitgation

DELIMITER //

CREATE PROCEDURE UPDATE_DATASET_SIZE (DATASET_ID INTEGER, DELTA BIGINT)
BEGIN
    UPDATE DATASET SET DATASETSIZE = IFNULL(DATASETSIZE, 0) + DELTA WHERE ID = DATASET_ID;
END; //

CREATE PROCEDURE UPDATE_INVESTIGATION_SIZE (INVESTIGATION_ID INTEGER, DELTA BIGINT)
BEGIN
    UPDATE INVESTIGATION SET INVESTIGATIONSIZE = IFNULL(INVESTIGATIONSIZE, 0) + DELTA WHERE ID = INVESTIGATION_ID;
END; //

-- Create procedures to update the fileCount of a Dataset or Invesitgation

CREATE PROCEDURE UPDATE_DATASET_FILECOUNT (DATASET_ID INTEGER, DELTA BIGINT)
BEGIN
    UPDATE DATASET SET FILECOUNT = IFNULL(FILECOUNT, 0) + DELTA WHERE ID = DATASET_ID;
END; //

CREATE PROCEDURE UPDATE_INVESTIGATION_FILECOUNT (INVESTIGATION_ID INTEGER, DELTA BIGINT)
BEGIN
    UPDATE INVESTIGATION SET FILECOUNT = IFNULL(FILECOUNT, 0) + DELTA WHERE ID = INVESTIGATION_ID;
END; //

-- Create triggers to recalculate the size and fileCount after a Datafile insert/update/delete operation

CREATE TRIGGER RECALCULATE_SIZES_FILECOUNT_ON_DATAFILE_INSERT AFTER INSERT ON DATAFILE
FOR EACH ROW
BEGIN
    SET @DELTA = IFNULL(NEW.FILESIZE, 0);
    CALL UPDATE_DATASET_SIZE(NEW.DATASET_ID, @DELTA);
    CALL UPDATE_DATASET_FILECOUNT(NEW.DATASET_ID, 1);
    SELECT i.ID INTO @INVESTIGATION_ID FROM INVESTIGATION i JOIN DATASET AS ds ON ds.INVESTIGATION_ID = i.ID WHERE ds.ID = NEW.DATASET_ID;
    CALL UPDATE_INVESTIGATION_SIZE(@INVESTIGATION_ID, @DELTA);
    CALL UPDATE_INVESTIGATION_FILECOUNT(@INVESTIGATION_ID, 1);
END; //

CREATE TRIGGER RECALCULATE_SIZES_FILECOUNT_ON_DATAFILE_UPDATE AFTER UPDATE ON DATAFILE
FOR EACH ROW
BEGIN
    IF NEW.DATASET_ID != OLD.DATASET_ID THEN
        SET @DELTA = - IFNULL(OLD.FILESIZE, 0);
        CALL UPDATE_DATASET_SIZE(OLD.DATASET_ID, @DELTA);
        CALL UPDATE_DATASET_FILECOUNT(OLD.DATASET_ID, -1);
        SELECT i.ID INTO @INVESTIGATION_ID FROM INVESTIGATION i JOIN DATASET AS ds ON ds.INVESTIGATION_ID = i.ID WHERE ds.ID = OLD.DATASET_ID;
        CALL UPDATE_INVESTIGATION_SIZE(@INVESTIGATION_ID, @DELTA);
        CALL UPDATE_INVESTIGATION_FILECOUNT(@INVESTIGATION_ID, -1);

        SET @DELTA = IFNULL(NEW.FILESIZE, 0);
        CALL UPDATE_DATASET_SIZE(NEW.DATASET_ID, @DELTA);
        CALL UPDATE_DATASET_FILECOUNT(NEW.DATASET_ID, 1);
        SELECT i.ID INTO @INVESTIGATION_ID FROM INVESTIGATION i JOIN DATASET AS ds ON ds.INVESTIGATION_ID = i.ID WHERE ds.ID = NEW.DATASET_ID;
        CALL UPDATE_INVESTIGATION_SIZE(@INVESTIGATION_ID, @DELTA);
        CALL UPDATE_INVESTIGATION_FILECOUNT(@INVESTIGATION_ID, 1);

    ELSEIF IFNULL(NEW.FILESIZE, 0) != IFNULL(OLD.FILESIZE, 0) THEN
        SET @DELTA = IFNULL(NEW.FILESIZE, 0) - IFNULL(OLD.FILESIZE, 0);
        CALL UPDATE_DATASET_SIZE(NEW.DATASET_ID, @DELTA);
        SELECT i.ID INTO @INVESTIGATION_ID FROM INVESTIGATION i JOIN DATASET AS ds ON ds.INVESTIGATION_ID = i.ID WHERE ds.ID = NEW.DATASET_ID;
        CALL UPDATE_INVESTIGATION_SIZE(@INVESTIGATION_ID, @DELTA);
    END IF;
END; //

CREATE TRIGGER RECALCULATE_SIZES_FILECOUNT_ON_DATAFILE_DELETE AFTER DELETE ON DATAFILE
FOR EACH ROW
BEGIN
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET @INVESTIGATION_ID = NULL;

    SET @DELTA = - IFNULL(OLD.FILESIZE, 0);
    CALL UPDATE_DATASET_SIZE(OLD.DATASET_ID, @DELTA);
    CALL UPDATE_DATASET_FILECOUNT(OLD.DATASET_ID, -1);
    SELECT i.ID INTO @INVESTIGATION_ID FROM INVESTIGATION i JOIN DATASET AS ds ON ds.INVESTIGATION_ID = i.ID WHERE ds.ID = OLD.DATASET_ID;
    CALL UPDATE_INVESTIGATION_SIZE(@INVESTIGATION_ID, @DELTA);
    CALL UPDATE_INVESTIGATION_FILECOUNT(@INVESTIGATION_ID, -1);
END; //

-- Create triggers to recalculate the Investigation size and fileCount after a Dataset update/delete operation

CREATE TRIGGER RECALCULATE_SIZES_FILECOUNT_ON_DATASET_UPDATE AFTER UPDATE ON DATASET
FOR EACH ROW
BEGIN
    IF NEW.INVESTIGATION_ID != OLD.INVESTIGATION_ID THEN
        SET @SIZE_DELTA = - IFNULL(OLD.DATASETSIZE, 0);
        SET @COUNT_DELTA = - IFNULL(OLD.FILECOUNT, 0);
        CALL UPDATE_INVESTIGATION_SIZE(OLD.INVESTIGATION_ID, @SIZE_DELTA);
        CALL UPDATE_INVESTIGATION_FILECOUNT(OLD.INVESTIGATION_ID, @COUNT_DELTA);

        SET @SIZE_DELTA = IFNULL(NEW.DATASETSIZE, 0);
        SET @COUNT_DELTA = IFNULL(NEW.FILECOUNT, 0);
        CALL UPDATE_INVESTIGATION_SIZE(NEW.INVESTIGATION_ID, @SIZE_DELTA);
        CALL UPDATE_INVESTIGATION_FILECOUNT(NEW.INVESTIGATION_ID, @COUNT_DELTA);
    END IF;
END; //

CREATE TRIGGER RECALCULATE_SIZES_FILECOUNT_ON_DATASET_DELETE AFTER DELETE ON DATASET
FOR EACH ROW
BEGIN
    SET @SIZE_DELTA = - IFNULL(OLD.DATASETSIZE, 0);
    SET @COUNT_DELTA = - IFNULL(OLD.FILECOUNT, 0);
    CALL UPDATE_INVESTIGATION_SIZE(OLD.INVESTIGATION_ID, @SIZE_DELTA);
    CALL UPDATE_INVESTIGATION_FILECOUNT(OLD.INVESTIGATION_ID, @COUNT_DELTA);
END; //

-- Initialize the sizes and fileCounts of all existing Datasets and Investigations

CREATE PROCEDURE INITIALIZE_DATASET_SIZES_FILECOUNTS()
BEGIN
    DECLARE done BOOLEAN DEFAULT FALSE;
    DECLARE _id BIGINT UNSIGNED;
    DECLARE cur CURSOR FOR SELECT ID FROM DATASET;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

    OPEN cur;
    datasetLoop: LOOP
        FETCH cur INTO _id;
        IF done THEN
        LEAVE datasetLoop;
        END IF;

        SELECT SUM(df.FILESIZE) INTO @DATASET_SIZE FROM DATASET ds JOIN DATAFILE AS df ON df.DATASET_ID = ds.ID WHERE ds.ID = _id;
        SELECT COUNT(df.ID) INTO @FILE_COUNT FROM DATASET ds JOIN DATAFILE AS df ON df.DATASET_ID = ds.ID WHERE ds.ID = _id;
        UPDATE DATASET SET DATASETSIZE = @DATASET_SIZE WHERE ID = _id;
        UPDATE DATASET SET FILECOUNT = @FILE_COUNT WHERE ID = _id;
    END LOOP datasetLoop;
    CLOSE cur;
END; //

CREATE PROCEDURE INITIALIZE_INVESTIGATION_SIZES_FILECOUNTS()
BEGIN
    DECLARE done BOOLEAN DEFAULT FALSE;
    DECLARE _id BIGINT UNSIGNED;
    DECLARE cur CURSOR FOR SELECT ID FROM INVESTIGATION;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

    OPEN cur;
    investigationLoop: LOOP
        FETCH cur INTO _id;
        IF done THEN
        LEAVE investigationLoop;
        END IF;

        SELECT SUM(ds.DATASETSIZE) INTO @INVESTIGATION_SIZE FROM INVESTIGATION i JOIN DATASET AS ds ON ds.INVESTIGATION_ID = i.ID WHERE i.ID = _id;
        SELECT SUM(ds.FILECOUNT) INTO @FILE_COUNT FROM INVESTIGATION i JOIN DATASET AS ds ON ds.INVESTIGATION_ID = i.ID WHERE i.ID = _id;
        UPDATE INVESTIGATION SET INVESTIGATIONSIZE = @INVESTIGATION_SIZE WHERE ID = _id;
        UPDATE INVESTIGATION SET FILECOUNT = @FILE_COUNT WHERE ID = _id;
    END LOOP investigationLoop;
    CLOSE cur;
END; //

DELIMITER ;

CALL INITIALIZE_DATASET_SIZES_FILECOUNTS();
CALL INITIALIZE_INVESTIGATION_SIZES_FILECOUNTS();

DROP PROCEDURE INITIALIZE_DATASET_SIZES_FILECOUNTS;
DROP PROCEDURE INITIALIZE_INVESTIGATION_SIZES_FILECOUNTS;