SET SERVEROUTPUT ON SIZE 100000;
CREATE OR REPLACE PROCEDURE set_inv_run_number_range

AS

	v_bol_first_run					BOOLEAN;
	v_bol_in_a_increment			BOOLEAN;
	v_run_range						investigation.inv_param_value%TYPE;
	v_old_run_number				datafile_parameter.numeric_value%TYPE;

BEGIN


	-- This process sets the run number range in the investigation table; this is
	-- of specific use to ISIS and is required such that it can be displayed in the
	-- front end as this is one of the key ways that scientists are able to identify
	-- their data. This should be run as a Job; suggested minimum is once per day.
	-- The process should support both the usual case and the special case.

	-- The usual case:

	-- The INVESTIGATION.INV_PARAM_NAME should be set to .run_number_range..
	-- The INVESTIGATION.INV_PARAM_VALUE should be   A concatenated with .-. concatenated
	-- with B. A and B are defined below:

	-- A - The smallest value of DATAFILE_PARAMETER.NUMERIC_VALUE associated with the
	-- INVESTIGATION where DATAFILE_PARAMETER.NAME is equal to .run_number. and the
	-- associated DATASET has a DATASET.DATASET_TYPE equal to .experiment_raw.
	-- If no such records exist then INVESTIGATION.INV_PARAM_VALUE should not be set.

	-- B - The largest value of DATAFILE_PARAMETER.NUMERIC_VALUE associated with the
	-- INVESTIGATION where DATAFILE_PARAMETER.NAME is equal to .run_number. and the
	-- associated DATASET has a DATASET.DATASET_TYPE equal to .experiment_raw.
	-- If no such records exist then INVESTIGATION.INV_PARAM_VALUE should not be set.

	-- The special case:

	-- Please see this note from Damian Flannery of ISIS:

	-- In ingesting our historical data into the ICAT database we had to look at various
	-- pieces of metadata (that we could absolutely rely on) within each raw file
	-- e.g. instrument, investigator name, dates, run numbers and run title.  So we
	-- had to write some logic that grouped these files (with the limited metadata that we had)
	-- into investigations.  

	-- It therefore means that although we will have many cases as simple as described
	-- by Shoaib ( in the .The Usual Case. - Shoaib) 

	-- e.g. start_run = 1000, end run 1009.
	-- we will also have many cases where the logical sequence is not complete 
	-- e.g. 1000 . 1005, 1007, 1009 etc.
	-- I have attached a screenshot of a previous ICAT gui to illustrate this (please see
	-- Appendix B - Shoaib). 
	-- I just want to make it clear that it is not just a case of picking the start run
	-- and the end run.

	-- In some case you make have an INVESTIGATION.INV_PARAM_VALUE which is not like.
	-- The Usual Case. and has to be constructed in parts.

	-- 1) It is suggested that all DATAFILE_PARAMETER.NUMERIC_VALUE associated with the
	-- INVESTIGATION where DATAFILE_PARAMETER.NAME is equal to .run_number. and the associated
	-- DATASET has a DATASET.DATASET_TYPE equal to .experiment_raw. for all DATAFILEs
	-- associated with the Investigation be selected in an ascending numerical order.
	-- 2) Then starting with the smallest numbers a contiguous ranges of numbers should
	-- be represented as start_range-end_range or if there is no range then just a single-value 
	-- 3) followed by (if more numbers exist) concatenating a comma .,. and then concatenating
	-- the next highest value start-range-end_range or if no such range exists a single-value
	-- 4) 3) should be continued until all the numbers in 1) have been processed to produce the final  INVESTIGATION.INV_PARAM_VALUE


	-- For example, if the number chosen in 1) were 12,14,15,16,21,22,33,36,37,38,41 then
	-- the value of INVESTIGATION.INV_PARAM_VALUE should be set to 12,14-16,21-22,33,36-38,41.


	-- OK, enough spec, let's get going. This isn't going to be pretty.

	-- Get all of them for now
	DECLARE
		CURSOR c1 IS
			SELECT id,
			       inv_param_name,
				   inv_param_value
			FROM   investigation
			FOR UPDATE;

	BEGIN
		FOR c1rec IN c1 LOOP

			v_bol_first_run := TRUE;

			DECLARE
				-- Let's get a list of all the run numbers, in order
				CURSOR c2 IS
					SELECT datafile_parameter.numeric_value
					FROM   datafile_parameter,
				       	   datafile,
					   	   dataset
					WHERE  dataset.investigation_id = c1rec.id
					AND    dataset.id = datafile.dataset_id
					AND    datafile.id = datafile_parameter.datafile_id
					AND    dataset.dataset_type = 'experiment_raw'
					AND    datafile_parameter.name = 'run_number'
					ORDER BY datafile_parameter.numeric_value;

			BEGIN
				FOR c2rec IN c2 LOOP

					-- OK, slightly crude, but if it's the first one then set a flag as we'll always have it in the final string
					IF v_bol_first_run THEN
						v_run_range := c2rec.numeric_value;
						v_bol_first_run := FALSE;
						v_bol_in_a_increment := FALSE;
					ELSE
						IF c2rec.numeric_value = v_old_run_number + 1 THEN
							-- OK, they're incremental
							IF v_bol_in_a_increment THEN
								-- ALready in an increment so just keep going
								NULL;
							ELSE
								-- First increment, so add a dash
								v_run_range := v_run_range || '-';
								v_bol_in_a_increment := TRUE;
							END IF;
						ELSE
							-- Not in an increment, add the last value if we were
							IF v_bol_in_a_increment THEN
								v_bol_in_a_increment := FALSE;
								v_run_range := v_run_range || v_old_run_number;

								-- Now add this next number again, we know we have to add a comma too
								v_run_range := v_run_range || ', ' || c2rec.numeric_value;

							ELSE
								-- A new run number, but not incremental from the last. Add it but check if we need a comma.
								IF v_run_range IS NOT NULL THEN
									v_run_range := v_run_range || ', ';
								END IF;

								v_run_range := v_run_range || c2rec.numeric_value;

							END IF;
						END IF;
					END IF;

					v_old_run_number := c2rec.numeric_value;

				END LOOP;

				-- Careful here, we might have exited in the middle of an incremental range. If so, tack on the last number
				IF v_bol_in_a_increment THEN
					v_run_range := v_run_range || v_old_run_number;
					v_bol_in_a_increment := FALSE;
				END IF;

				-- Update the investigation record if we did find some run numbers
				IF v_run_range IS NOT NULL THEN
					UPDATE investigation
					SET    inv_param_name = 'run_number_range',
					       inv_param_value = v_run_range
					WHERE CURRENT OF c1;

					-- Importantly, reset the range to null for the next one
					v_run_range := NULL;
				END IF;

			END;
		END LOOP;
	END;
	COMMIT;
END;
/
show errors;
