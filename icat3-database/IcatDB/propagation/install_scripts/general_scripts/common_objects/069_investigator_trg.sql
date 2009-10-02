CREATE OR REPLACE TRIGGER trg_investigator
    AFTER
    INSERT
    ON ICATDLS33.INVESTIGATOR     FOR EACH ROW
DECLARE

    v_error_message                VARCHAR2(200);

    v_bol_fed_id                BOOLEAN := FALSE;

    v_count_guardian            NUMBER;
    v_count1                    number;
    v_federal_id                facility_user.federal_id%TYPE;
    v_role                        VARCHAR2(20);
    v_icat_authorisation_id        icat_authorisation.id%TYPE;

    cursor code1(invest_id varchar2) is select * from dataset where investigation_id = invest_id;

BEGIN

    -- Population of ICAT_AUTHORISATION once Facility Users are Linked to Investigations

    -- Once entries exist in the INVESTIGATOR table linking record in INVESTIGATION and
    -- FACILITY_USER this should cause addition of records to the ICAT_AUTHORISATION table
    -- based on certain rules. Note apart from the GUARDIAN user this should only happen
    -- if the user has their FACILITY_USER.FEDERAL_ID set to a valid .federal id. . i.e. valid
    -- in the MS Active Directory operated by CICT, STFC; please check Appendix A for an example
    -- of interacting with the MS Active Directory server using PL/SQL. How the checking of
    -- federal id.s against MS Active Directory is done is left as an implementation task but it
    -- should be noted that performance of the database should not be adversely affected and this
    -- processing should be possible in a timely manner.

    -- An entry should be added into the ICAT_AUTHORISATION table for each user (as defined by
    -- their FACILITY_USER.FEDERAL_ID) with the ICAT_AUTHORISATION.ROLE set to ADMIN if the
    -- INVESTIGATOR.ROLE is .principal_experimenter. or CREATOR if the INVESTIGATOR.ROLE is .experimenter..
    -- Also an ICAT_AUTHORISATION.ROLE of ICAT_ADMIN should be recorded for the <facility>_GUARDIAN
    -- user (e.g. in the case of ISIS this will be the GUARDIAN user). These users should have
    -- entries added for not only the INVESTIGATION, but any existing DATASETs and to be allowed to
    -- create new DATASETs


    -- Look up the facility_user record
    BEGIN
        SELECT federal_id
        INTO   v_federal_id
        FROM   facility_user
        WHERE  facility_user_id = :new.facility_user_id;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN

            v_error_message := 'No entry in facility_user table for user ' || :new.facility_user_id;
            RAISE NO_DATA_FOUND;
    END;

    -- Check the federal ID exists
    --v_bol_fed_id := ldap_authorisation(v_federal_id, NULL);

    IF :new.facility_user_id is null then
    --if not v_bol_fed_if then
        -- Oh dear, no federal ID
        v_error_message := 'No federal ID found for user ' || :new.facility_user_id;
        RAISE NO_DATA_FOUND;
    END IF;

    -- Set their role in this investigation
    IF :new.role in ('DLS_STAFF','DELI') THEN
        v_role := 'ADMIN';

    ELSIF :new.role = 'NORMAL_USER' THEN
        v_role := 'CREATOR';
        else
        v_role := 'READER';

    END IF;

select count(*) into v_count1 from icat_authorisation
where user_id = v_federal_id
and element_type = 'DATASET'
and element_id is null
and parent_element_type ='INVESTIGATION'
and parent_element_id=:new.investigation_id;

    -- Before we insert the main icat_authorisation record, let's check if there's any
    -- dataset records so we can create this child record first
    IF v_role IN ('ADMIN', 'CREATOR') and v_count1 = 0 THEN
        -- Insert the dataset record
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
            v_federal_id,
            v_role,
            'DATASET',
            NULL,
            'INVESTIGATION',
            :new.investigation_id,
            NULL,
            SYSDATE,
            'trg_investigator',
            SYSDATE,
            'trg_investigator',
            'Y',
            'N')
        RETURNING id
        INTO      v_icat_authorisation_id;


    select count(*) into v_count1 from icat_authorisation
    where user_id = v_federal_id
    and element_type = 'INVESTIGATION'
    and element_id = :new.investigation_id
    and parent_element_type is null
    and parent_element_id is null;
    if v_count1 = 0 then

    -- Insert the main investigator record, using the dataset ID from the previous record as the child record
    INSERT INTO icat_authorisation (
        id,
        user_id,
        role,
        element_type,
        element_id,
        parent_element_type,
        parent_element_iD,
        user_child_record,
        mod_time,
        mod_id,
        create_time,
        create_id,
        facility_acquired,
        deleted)
    VALUES (
        ICAT_Authorisation_ID_Seq.NEXTVAL,
        v_federal_id,
        v_role,
        'INVESTIGATION',
        :new.investigation_id,
        NULL,
        NULL,
        v_icat_authorisation_id,
        SYSDATE,
        'trg_investigator',
        SYSDATE,
        'trg_investigator',
        'Y',
        'N');
end if;
END IF;
    -- Need to check if there's an "GUARDIAN" entry for this investigation already
    -- If there isn't then do the same whole process again
    SELECT COUNT(*)
    INTO   v_count_guardian
    FROM   icat_authorisation
    WHERE  user_id = 'GUARDIAN'
    AND    element_id = :new.investigation_id;

    IF v_count_guardian = 0 THEN
        -- No record exists so let's create it

        -- Before we insert the main icat_authorisation record, let's check if there's any
        -- dataset records so we can create this child record first
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
            'GUARDIAN',
            'ICAT_ADMIN',
            'DATASET',
            NULL,
            'INVESTIGATION',
            :new.investigation_id,
            NULL,
            SYSDATE,
            'trg_investigator',
            SYSDATE,
            'trg_investigator',
            'Y',
            'N')
        RETURNING id
        INTO      v_icat_authorisation_id;

        -- Insert the main investigator record, using the dataset ID from the previous record as the child record
        INSERT INTO icat_authorisation (
            id,
            user_id,
            role,
            element_type,
            element_id,
            parent_element_type,
            parent_element_iD,
            user_child_record,
            mod_time,
            mod_id,
            create_time,
            create_id,
            facility_acquired,
            deleted)
        VALUES (
            ICAT_Authorisation_ID_Seq.NEXTVAL,
            'GUARDIAN',
            'ICAT_ADMIN',
            'INVESTIGATION',
            :new.investigation_id,
            NULL,
            NULL,
            v_icat_authorisation_id,
            SYSDATE,
            'trg_investigator',
            SYSDATE,
            'trg_investigator',
            'Y',
            'N');
        ----------------------------------------------------------------------------------
        --                                                                              --
        --                   DEALING WITH THE SUPER USER                                --
        --                                                                              --
        -- -------------------------------------------------------------------------------
        -- Before we insert the main icat_authorisation record, let's check if there's any
        -- dataset records so we can create this child record first
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
            'SUPER_USER',
            'SUPER',
            'DATASET',
            NULL,
            'INVESTIGATION',
            :new.investigation_id,
            NULL,
            SYSDATE,
            'trg_investigator',
            SYSDATE,
            'trg_investigator',
            'Y',
            'N')
        RETURNING id
        INTO      v_icat_authorisation_id;
        -- Insert the main investigator record, using the dataset ID from the previous record as the child record
        INSERT INTO icat_authorisation (
            id,
            user_id,
            role,
            element_type,
            element_id,
            parent_element_type,
            parent_element_iD,
            user_child_record,
            mod_time,
            mod_id,
            create_time,
            create_id,
            facility_acquired,
            deleted)
        VALUES (
            ICAT_Authorisation_ID_Seq.NEXTVAL,
            'SUPER_USER',
            'SUPER',
            'INVESTIGATION',
            :new.investigation_id,
            NULL,
            NULL,
            v_icat_authorisation_id,
            SYSDATE,
            'trg_investigator',
            SYSDATE,
            'trg_investigator',
            'Y',
            'N');

    END IF;

/*
--Now go through datasets for any passed entries with the same investigation
--and add an entry in icat_authorisation so that the investigaion has access
--to all the datasets

for i in code1(:new.investigation_id) loop
begin
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
            v_federal_id,
            v_role,
            'DATASET',
            i.id,
            'INVESTIGATION',
            :new.investigation_id,
            NULL,
            SYSDATE,
            'trg_investigator',
            SYSDATE,
            'trg_investigator',
            'Y',
            'N');

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
            'SUPER_USER',
            'SUPER',
            'DATASET',
            i.id,
            'INVESTIGATION',
            :new.investigation_id,
            NULL,
            SYSDATE,
            'trg_investigator',
            SYSDATE,
            'trg_investigator',
            'Y',
            'N');

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
            'GUARDIAN',
            'ICAT_ADMIN',
            'DATASET',
            i.id,
            'INVESTIGATION',
            :new.investigation_id,
            NULL,
            SYSDATE,
            'trg_investigator',
            SYSDATE,
            'trg_investigator',
            'Y',
            'N');


EXCEPTION
    WHEN OTHERS THEN
        log_pkg.init;
        log_pkg.write_exception('Error adding dataset entry for previous datasets of same investigation');
        end;
end loop;
*/

EXCEPTION
    WHEN NO_DATA_FOUND THEN

        log_pkg.init;
        log_pkg.write_exception('ICAT Auth Error. Trigger: trg_investigator. Message: ' || v_error_message);

END;
/

