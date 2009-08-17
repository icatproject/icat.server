CREATE OR REPLACE procedure update_icat_auth_with_guardian_and_super as

    v_error_message                VARCHAR2(200);

    v_bol_fed_id                BOOLEAN := FALSE;

    v_count_guardian            NUMBER;

    v_federal_id                facility_user.federal_id%TYPE;
    v_role                        VARCHAR2(20);
    v_icat_authorisation_id        icat_authorisation.id%TYPE;
    v_count number;
    v_dataset_id                number;    
   
count1 number :=0;
    cursor code1 is       
    select * from investigation;
    
      


BEGIN
   dbms_output.put_line('start');
for i in code1 loop
   count1:=count1+1;
    -- Need to check if there's an "GUARDIAN" entry for this investigation already
    -- If there isn't then do the same whole process again
    SELECT COUNT(*)
    INTO   v_count_guardian
    FROM   icat_authorisation
    WHERE  user_id = 'GUARDIAN'
    and element_type = 'DATASET'
    AND    element_id is null
    and parent_element_id =i.id ;

begin
    IF v_count_guardian = 0 THEN
    
    
        -- No record exists so let's create it
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
            null,
            'INVESTIGATION',
            i.id,
            NULL,
            SYSDATE,
            'proc_investigator_update',
            SYSDATE,
            'proc_investigator_update',
            'Y',
            'N')
        RETURNING id
        INTO      v_icat_authorisation_id;
       end if;

EXCEPTION
    WHEN NO_DATA_FOUND THEN null;
end; 

   SELECT COUNT(*)
    INTO   v_count_guardian
    FROM   icat_authorisation
    WHERE  user_id = 'GUARDIAN'
    and element_type = 'INVESTIGATION'
    AND    element_id = i.id;

begin     
    IF v_count_guardian = 0 THEN
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
            i.id,
            NULL,
            NULL,
            v_icat_authorisation_id,
            SYSDATE,
            'proc_investigator_update',
            SYSDATE,
            'proc_investigator_update',
            'Y',
            'N');
            
            
       end if;

EXCEPTION
    WHEN NO_DATA_FOUND THEN null;
end;     

          SELECT COUNT(*)
    INTO   v_count_guardian
    FROM   icat_authorisation
    WHERE  user_id = 'SUPER_USER'
    and element_type ='DATASET'
    AND    element_id is null
    and parent_element_id =i.id;

begin
    IF v_count_guardian = 0 THEN
    
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
            null,
            'INVESTIGATION',
            i.id,
            NULL,
            SYSDATE,
            'proc_investigator_update',
            SYSDATE,
            'proc_investigator_update',
            'Y',
            'N')
        RETURNING id
        INTO      v_icat_authorisation_id;

end if;

 EXCEPTION
    WHEN NO_DATA_FOUND THEN null;
end;     

          SELECT COUNT(*)
    INTO   v_count_guardian
    FROM   icat_authorisation
    WHERE  user_id = 'SUPER_USER'
    and element_type ='INVESTIGATION'
    AND    element_id = i.id;

begin
    IF v_count_guardian = 0 THEN       -- Insert the main investigator record, using the dataset ID from the previous record as the child record
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
            i.id,
            NULL,
            NULL,
            v_icat_authorisation_id,
            SYSDATE,
            'proc_investigator_update',
            SYSDATE,
            'proc_investigator_update',
            'Y',
            'N');

end if;

EXCEPTION
    WHEN NO_DATA_FOUND THEN null;
end;



    --now check if there are any existing datasets
    select count(*) into v_count from dataset a, icat_authorisation b
    where investigation_id = i.id
    and b.element_type='DATASET'
    and b.element_id=a.id
    and b.user_id = 'GUARDIAN';
 
    begin
    if v_count =0  then
    select id into v_dataset_id from dataset
    where investigation_id = i.id; 
    
    
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
            v_dataset_id ,
            'INVESTIGATION',
            i.id,
            NULL,
            SYSDATE,
            'proc_investigator_update',
            SYSDATE,
            'proc_investigator_update',
            'Y',
            'N');

end if;

EXCEPTION
    WHEN NO_DATA_FOUND THEN null;
end;
    
    select count(*) into v_count from dataset a, icat_authorisation b
    where investigation_id = i.id
    and b.element_type='DATASET'
    and b.element_id=a.id
    and b.user_id = 'SUPER_USER';
 
    begin
    if v_count =0  then
    select id into v_dataset_id from dataset
    where investigation_id = i.id; 

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
            v_dataset_id ,
            'INVESTIGATION',
            i.id,
            NULL,
            SYSDATE,
            'proc_investigator_update',
            SYSDATE,
            'proc_investigator_update',
            'Y',
            'N');
        
      
    
    end if;
    
EXCEPTION
    WHEN NO_DATA_FOUND THEN null;
end;
    
    
 commit;   
    
end loop;
dbms_output.put_line('end');
dbms_output.put_line(count1);
EXCEPTION
    WHEN NO_DATA_FOUND THEN

        log_pkg.init;
        log_pkg.write_exception('Proc_investigator_update. Message: ' || v_error_message);

END;
/