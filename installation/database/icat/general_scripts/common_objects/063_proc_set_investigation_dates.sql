CREATE OR REPLACE PROCEDURE set_investigation_dates

AS

	v_datafile_create_time_erliest		datafile.datafile_create_time%TYPE;
	v_datafile_create_time_latest		datafile.datafile_create_time%TYPE;


BEGIN

	-- This process set the INVESTIGATION.INV_START_DATE and INVESTIGATION.INV_END_DATE
	-- based on metadata held in the files associated with the investigation.

	-- Firstly, INVESTIGATION.INV_START_DATE should be set to the value of DATAFILE.DATAFILE_CREATE_TIME
	-- in the earliest DATAFILE entry associated with this investigation where the DATASET.DATASET_TYPE
	-- of the DATAFILEs parent DATASET is .experiment_raw.. If no such files exist then the value is not set.

	-- Secondly, INVESTIGATION.INV_END_DATE to the value of DATAFILE.DATAFILE_CREATE_TIME
	-- (or DATAFILE.DATAFILE_MODIFY_TIME which ever is later) in the latest DATAFILE entry associated with
	-- this investigation where the DATASET.DATASET_TYPE of the DATAFILEs parent DATASET is .experiment_raw.
	-- If no such files exist then the value is not set.

	DECLARE
		CURSOR c1 IS
			SELECT id,
			       inv_start_date,
				   inv_end_date
			FROM   investigation
			FOR UPDATE;

	BEGIN
		FOR c1rec IN c1 LOOP

			BEGIN
				-- Get the newest start date
				SELECT MIN(datafile.datafile_create_time)
				INTO   v_datafile_create_time_erliest
				FROM   datafile,
					   dataset
				WHERE  datafile.dataset_id = dataset.id
				AND	   dataset.investigation_id = c1rec.id
				AND    datafile.datafile_create_time IS NOT NULL
				AND    dataset.dataset_type = 'experiment_raw';

				-- Great, found one, now update the investigation record if it's newer
				IF (v_datafile_create_time_erliest < c1rec.inv_start_date) OR c1rec.inv_start_date IS NULL THEN
					UPDATE investigation
					SET inv_start_date = v_datafile_create_time_erliest
					WHERE CURRENT OF c1;
				END IF;

			EXCEPTION
				WHEN NO_DATA_FOUND THEN
					-- We don't care if a newer date wasn't found
					NULL;
			END;

			BEGIN
				-- Get the latest end date
				SELECT MAX(datafile.datafile_create_time)
				INTO   v_datafile_create_time_latest
				FROM   datafile,
					   dataset
				WHERE  datafile.dataset_id = dataset.id
				AND	   dataset.investigation_id = c1rec.id
				AND    datafile.datafile_create_time IS NOT NULL
				AND    dataset.dataset_type = 'experiment_raw';

				-- Great, found one, now update the investigation record if it's newer
				IF (v_datafile_create_time_latest > c1rec.inv_end_date) OR c1rec.inv_end_date IS NULL THEN
					UPDATE investigation
					SET inv_end_date = v_datafile_create_time_latest
					WHERE CURRENT OF c1;
				END IF;

			EXCEPTION
				WHEN NO_DATA_FOUND THEN
					-- We don't care if a newer date wasn't found
					NULL;
			END;
		END LOOP;
	END;
	COMMIT;
END;
/