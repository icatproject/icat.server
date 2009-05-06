CREATE OR REPLACE PROCEDURE set_icat_auth_data_entities

AS

	v_days_until_public_release		this_icat.days_until_public_release%TYPE;

BEGIN

	-- Setting Data Entities for Public Release in ICAT_AUTHORISATION
	 
	-- This purpose of this process is to mark data as available for viewing by other
	-- people who are registered with the authentication system but were not explicitly
	-- given permission to view the investigation and dataset details.

	-- On a Timed basis (suggested minimum is once a day) the time since the investigation
	-- last collected data should be used to determine whether or not that investigation
	-- should be marked such that any user can view the metadata and download the associated
	-- raw experimental data. Note this should only be possible for experiments which are
	-- not commercial. Commercial experiments should be excluded from this process.

	-- 1) The value in THIS_ICAT.DAYS_UNTIL_PUBLIC_RELEASE (A) should be used to determine
	-- when to add appropriate records to the ICAT_AUTHORISATION table
	-- a. if the value of A is 0 then the entries should be added without any further checking
	-- b. otherwise 2) onwards should be used to make the appropriate changes.
	-- 2) If the current date is later then INVESTIGATION.INV_END_DATE + A and
	-- INVESTIGATION.INV_TYPE is .experiment. then entries allowing access to the collected
	-- experimental data should be allowed for any user who can access the system.
	-- 3) Assuming the checks in 2) are passed then any INVESTIGATION and all of its
	-- corresponding DATASETs which have the DATASET.DATASET_TYPE set to .experiment_raw.
	-- should have rows added to the ICAT_AUTHORISATION table for the .ANY. user. 


	-- Get the days until public release
	SELECT days_until_public_release
	INTO   v_days_until_public_release
	FROM   this_icat;

	-- Do this for all investigations - some rules though:
	-- Investigations marked as .calibration. become publicly available immediately
	-- Investigations marked as .experiment. become publicly available after 3 years
	-- Investigations marked as .commercial_experiment. are ignored by trigger (i.e. exempt from being made public)

	-- We can ignore investigations that have an icat_auth record for "ANY" already too
	DECLARE
		CURSOR c1 IS
			SELECT id
			FROM   investigation
			WHERE  (SYSDATE > (inv_end_date + v_days_until_public_release)
			       AND inv_type = 'experiment')
			OR     (v_days_until_public_release = 0
			        AND inv_type != 'experiment_commercial')
			OR	   inv_type = 'calibration'
			MINUS
			SELECT element_id
			FROM   icat_authorisation
			WHERE  user_id = 'ANY'
			AND    role = 'DOWNLOADER'
			AND    element_type = 'INVESTIGATION';

	BEGIN

		FOR c1rec IN c1 LOOP

			-- OK, we need to check if each investigation has an entry in icat_auth
			-- plus all its corresponding datasets

			-- Add the record
			INSERT INTO icat_authorisation (
				id,
				user_id,
				role,
				element_type,
				element_id,
				parent_element_type,
				parent_element_id,
				user_child_record,
				mod_time,
				mod_id,
				create_time,
				create_id,
				facility_acquired,
				deleted)
			VALUES (
				ICAT_Authorisation_ID_Seq.NEXTVAL,
				'ANY',
				'DOWNLOADER',
				'INVESTIGATION',
				c1rec.id,
				NULL,
				NULL,
				NULL,
				SYSDATE,
				'set_icat_auth_data_entities',
				SYSDATE,
				'set_icat_auth_data_entities',
				'Y',
				'N');

			-- Now get all the datasets for this investigation
			DECLARE
				CURSOR c2 IS
					SELECT id
					FROM   dataset
					WHERE  investigation_id = c1rec.id
					MINUS
					SELECT element_id
					FROM   icat_authorisation
					WHERE  user_id = 'ANY'
					AND    role = 'DOWNLOADER'
					AND    element_type = 'DATASET'
					AND    parent_element_id = c1rec.id;

			BEGIN
				FOR c2rec IN c2 LOOP

					-- Add the record
					INSERT INTO icat_authorisation (
						id,
						user_id,
						role,
						element_type,
						element_id,
						parent_element_type,
						parent_element_id,
						user_child_record,
						mod_time,
						mod_id,
						create_time,
						create_id,
						facility_acquired,
						deleted)
					VALUES (
						ICAT_Authorisation_ID_Seq.NEXTVAL,
						'ANY',
						'DOWNLOADER',
						'DATASET',
						c2rec.id,
						'INVESTIGATION',
						c1rec.id,
						NULL,
						SYSDATE,
						'set_icat_auth_data_entities',
						SYSDATE,
						'set_icat_auth_data_entities',
						'Y',
						'N');

				END LOOP;
			END;
		END LOOP;
	END;

	COMMIT;

EXCEPTION
	WHEN OTHERS THEN

		log_pkg.init;
		log_pkg.write_exception('ICAT Auth Error. Procedure: set_icat_auth_data_entities. Message: ' || SQLERRM);

		COMMIT;

END;
/
show errors;
