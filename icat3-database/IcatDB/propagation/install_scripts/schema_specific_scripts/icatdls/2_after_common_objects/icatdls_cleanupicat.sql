CREATE OR REPLACE PACKAGE cleanupicat_pkg AS

PROCEDURE cleanup_instruments(p_mod_id IN investigation.mod_id%TYPE);
PROCEDURE cleanup_parameters(p_mod_id IN investigation.mod_id%TYPE);
--PROCEDURE cleanup_investigations(p_mod_id IN investigation.mod_id%TYPE);
PROCEDURE cleanup_shifts(p_mod_id IN investigation.mod_id%TYPE);
PROCEDURE cleanup_facility_users(p_mod_id IN investigation.mod_id%TYPE);
PROCEDURE cleanup_investigators(p_mod_id IN investigation.mod_id%TYPE);
PROCEDURE cleanup_keywords(p_mod_id IN investigation.mod_id%TYPE);
PROCEDURE cleanup_publications(p_mod_id IN investigation.mod_id%TYPE);
PROCEDURE cleanup_samples(p_mod_id IN investigation.mod_id%TYPE);
PROCEDURE cleanup_sample_parameters(p_mod_id IN investigation.mod_id%TYPE);
PROCEDURE cleanup_icat_authorisation(p_mod_id IN investigation.mod_id%TYPE);
PROCEDURE cleanup(p_mod_id IN investigation.mod_id%TYPE);
END cleanupicat_pkg;
/

CREATE OR REPLACE PACKAGE BODY cleanupicat_pkg AS

PROCEDURE cleanup_instruments(p_mod_id IN investigation.mod_id%TYPE) as
begin
update instrument ti set deleted='Y',mod_time = systimestamp, mod_id = p_mod_id where not exists(select '' from instrument@duodesk si where lower(ti.name) = lower(si.instr_nom));
commit; 
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception(SQLERRM,1);
end;

PROCEDURE cleanup_parameters(p_mod_id IN investigation.mod_id%TYPE) as 
begin
for i in (select * from user_tab_cols@duodesk   where table_name = 'SAMPLESHEET' and column_name not in ('SMPS_EFFACE','UCR','DCR','DDM')
) loop
    execute immediate 'insert into temp_samplesheet (select ''' || substr(i.column_name,instr(i.column_name,'_')+1) || ''',sum(is_number(' || i.column_name || ')) from samplesheet@duodesk)';
    commit;
end loop;

update parameter tp set deleted='Y', mod_time = systimestamp, mod_id = p_mod_id where not exists ((select '' from sample_parameter@duodesk sp where sp.name IS NOT NULL and 
tp.name = lower(sp.name) AND tp.units = Nvl(sp.units,'N/A')) UNION (select '' from temp_samplesheet ts where lower(tp.name) = lower(ts.name)));

delete from temp_samplesheet;
commit; 
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception(SQLERRM,1);
end;

PROCEDURE cleanup_investigations(p_mod_id IN investigation.mod_id%TYPE) as
begin
update investigation ti set deleted='Y', mod_time = systimestamp, mod_id = p_mod_id where not exists (
        SELECT ''
        FROM proposal@duodesk p,
            duo_proposal@duodesk dp,
            mesure@duodesk m,
            instrument@duodesk i,
            planning@duodesk pl
        WHERE dp.desk_propos_no = p.propos_no
        AND m.mes_propos_no = p.propos_no
        AND m.mes_instr_no = i.instr_no
        AND mes_uni_all > 0
        AND pl.pl_mes_no = m.mes_no
        AND pl.pl_date_deb IS NOT NULL
        AND pl.pl_date_fin IS NOT NULL
        AND Nvl(p.propos_efface,'N') != 'Y'
        AND Nvl(m.mes_efface,'N') != 'Y'
        AND Nvl(i.instr_efface,'N') != 'Y'
        AND Nvl(pl.pl_efface,'N')  != 'Y' 
        AND util_pkg.get_md5(propos_no,instr_no,pl_no)= ti.src_hash); 
commit;
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception(SQLERRM,1);
end;

PROCEDURE cleanup_shifts(p_mod_id IN investigation.mod_id%TYPE) as
begin
update shift ts set deleted='Y',  mod_time = systimestamp, mod_id = p_mod_id where not exists (SELECT ''
     FROM proposal@duodesk p,
         duo_proposal@duodesk dp,
         mesure@duodesk m,
         instrument@duodesk i,
         planning@duodesk pl,
         investigation inv
    WHERE dp.desk_propos_no = p.propos_no
    AND m.mes_propos_no = p.propos_no
    AND m.mes_instr_no = i.instr_no
    AND mes_uni_all > 0 -- indicates an approved proposal
    AND pl.pl_mes_no = m.mes_no
    AND pl.pl_date_deb IS NOT NULL
    AND pl.pl_date_fin IS NOT NULL
    AND inv.src_hash = util_pkg.get_md5(propos_no,instr_no,pl_no)
    -- need to include this bit if we have facility cycles in diamond...
    -- AND inv.facility_cyle = ?????
    AND Nvl(p.propos_efface,'N') != 'Y'
    AND Nvl(m.mes_efface,'N') != 'Y'
    AND Nvl(i.instr_efface,'N') != 'Y'
    AND Nvl(pl.pl_efface,'N') != 'Y'
    AND inv.id  =ts.investigation_id
    AND shift_time(pl.pl_date_deb,pl.pl_shifts_deb)=ts.start_date
    AND shift_time(pl.pl_date_fin,pl.pl_shifts_fin)=ts.end_date);
commit; 
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception(SQLERRM,1);
end;

PROCEDURE cleanup_facility_users(p_mod_id IN investigation.mod_id%TYPE) as
BEGIN
update facility_user fu set deleted='Y',  mod_time = systimestamp, mod_id = p_mod_id where not exists(
SELECT '' FROM tblpeople@duodesk where thrudate is null AND TO_CHAR (usernumber)= fu.facility_user_id);
commit;
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception(SQLERRM,1);
END;

PROCEDURE cleanup_investigators(p_mod_id IN investigation.mod_id%TYPE) AS
BEGIN
update investigator ti set deleted='Y';
for i in (
SELECT to_char(bp.usernumber) fu_id ,i.ID FROM vwselectpeopleforproposal_ts@duodesk bp,
                                 investigation i
                           WHERE bp.visit || '-' || bp.visit_number =
                                                            LOWER (i.visit_id)
                             --AND i.inv_number = to_char(bp.propos_no)
                             AND LOWER(i.inv_number)=bp.visit 
                             AND bp.fedid IS NOT NULL
UNION
SELECT to_char(bp.usernumber) fu_id,i.ID FROM vwselectpeopleforplanning@duodesk bp,
                                 investigation i
                           WHERE bp.visit || '-' || bp.visit_number =
                                                            LOWER (i.visit_id)
                             --AND i.inv_number = to_char(bp.propos_no)
                             AND LOWER(i.inv_number) = bp.visit
                             AND bp.fedid IS NOT NULL
UNION
SELECT fu.facility_user_id fu_id,i.id  FROM
        investigation i,
        (SELECT DISTINCT
            proposc.proposc_sc_mat,
            --proposal_ts.proposal_no
            p.propos_categ_code,
            p.propos_categ_cpt
          FROM proposc@duodesk proposc,
              proposal_ts@duodesk proposal_ts,
              proposal@duodesk p
          WHERE proposc.proposc_propos_no = proposal_ts.proposal_no
          AND p.propos_no=proposal_ts.proposal_no
        ) prop,
        tblpeople@duodesk p,
        facility_user fu
      WHERE --i.inv_number = To_Char(prop.proposal_no)
      LOWER(i.inv_number) = prop.propos_categ_code || prop.propos_categ_cpt
      AND p.entid = prop.proposc_sc_mat
      AND fu.facility_user_id = To_Char(p.usernumber)
) LOOP

update investigator ti set deleted='N' where 
i.fu_id=ti.facility_user_id AND i.ID=ti.investigation_id;
END LOOP;
update investigator set  mod_time = systimestamp, mod_id = p_mod_id WHERE deleted='Y'; 
commit;
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception(SQLERRM,1);
END;


PROCEDURE cleanup_keywords(p_mod_id IN investigation.mod_id%TYPE) AS
BEGIN
update keyword tk set deleted='Y',  mod_time = systimestamp, mod_id = p_mod_id where not exists(
    SELECT
      inv_number,
      instrument,
      visit_id,
      facility_cycle,
      investigation_id,
      name
    FROM(
      SELECT
        i.inv_number                        AS inv_number,
        i.instrument                        AS instrument,
        i.visit_id                          AS visit_id,
        i.facility_cycle                    AS facility_cycle,
        i.id                                AS investigation_id,
        SubStr(t.column_value,1,255) AS name,
        -- for each investigation, keywords must be unique when in the same case
        Row_Number() over(
          PARTITION BY i.id, SubStr(Lower(t.column_value),1,255) ORDER BY 1) rn
      FROM
        investigation i,
        proposal@duodesk p,
        TABLE(CAST(util_pkg.split_at_whitespace(p.propos_title) AS vc_array)) t
      WHERE --i.inv_number = To_Char(p.propos_no)
      LOWER(i.inv_number) = p.propos_categ_code || p.propos_categ_cpt
      AND Nvl(p.propos_efface,'N') != 'Y'
      AND t.column_value IS NOT NULL
      AND Lower(t.column_value) NOT IN(
  -- list of unuseful keywords selected from actual data
    'I',
    'a',
    'about',
    'an',
    'are',
    'as',
    'at',
    'be',
    'by',
    'etc',
    'for',
    'from',
    'how',
    'in',
    'is',
    'it',
    'of',
    'on',
    'or',
    'that',
    'the',
    'this',
    'to',
    'was',
    'what',
    'when',
    'where',
    'who',
    'will',
    'with',
    'the')
    ) s
    WHERE rn = 1 AND tk.investigation_id = s.investigation_id
      AND Lower(tk.name) = Lower(s.name));
commit;
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception(SQLERRM,1);
END;


PROCEDURE cleanup_publications(p_mod_id IN investigation.mod_id%TYPE) AS
BEGIN
update publication tp set deleted='Y',  mod_time = systimestamp, mod_id = p_mod_id where not exists(
    SELECT '' FROM duo_proposal@duodesk dp, investigation i,
                   proposal@duodesk p,
        TABLE(CAST(util_pkg.string_to_table(
                RTrim(dp.exp_publications),Chr(10)) AS vc_array)) t
    WHERE t.column_value IS NOT NULL
    --AND To_Char(dp.desk_propos_no) = i.inv_number
    AND LOWER(i.inv_number) = p.propos_categ_code || p.propos_categ_cpt
    AND p.propos_no=dp.desk_propos_no
    AND Upper(tp.full_reference) = Upper(SubStr(t.column_value,1,4000))
      AND tp.investigation_id = i.id);

Commit;
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception(SQLERRM,1);
END;

PROCEDURE cleanup_samples(p_mod_id IN investigation.mod_id%TYPE) AS
BEGIN
update sample ts set deleted='Y',  mod_time = systimestamp, mod_id = p_mod_id where not exists(
    SELECT 
      i.inv_number               AS inv_number,
      i.instrument               AS instrument,
      i.visit_id                 AS visit_id,
      i.facility_cycle           AS facility_cycle,
      i.id                       AS investigation_id,
      s.name                     AS name,
      NULL                       AS instance,
      s.chemical_formula         AS chemical_formula,
      'See sample parameters'    AS safety_information,
      s.id                       AS proposal_sample_id
    FROM sample@duodesk s,
         requested_instrument@duodesk ri,
         duo_proposal@duodesk p,
         investigation i,
         proposal@duodesk pp
    WHERE ri.proposal_id = p.duo_propos_no
    AND s.requested_instrument_id = ri.id
    AND s.name IS NOT NULL
    --AND i.inv_number = To_Char(p.desk_propos_no)
    AND LOWER(i.inv_number) = pp.propos_categ_code || pp.propos_categ_cpt
    AND p.desk_propos_no=pp.propos_no
    AND ts.proposal_sample_id = s.id
     AND ts.investigation_id = i.id
    );
    
commit;
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception(SQLERRM,1);
END;

PROCEDURE cleanup_sample_parameters(
  p_mod_id IN investigation.mod_id%TYPE) IS

  LV_CREATE_ID CONSTANT investigation.create_id%TYPE := 'FROM PROPAGATION';
  bulk_errors EXCEPTION;
  PRAGMA EXCEPTION_INIT(bulk_errors, -24381);

  -- data to be loaded into the sample_parameter table
  CURSOR c_sample_parameter_data IS
    SELECT
      i.inv_number               AS inv_number,
      i.instrument               AS instrument,
      i.visit_id                 AS visit_id,
      i.facility_cycle           AS facility_cycle,
      s.id                       AS sample_id,
      Lower(dsp.name)            AS name, -- lowercase lookup name!
      Nvl(dsp.units,'N/A')       AS units,
      CASE param.numeric_value
        WHEN 'Y' THEN NULL
          ELSE dsp.Value
          END                    AS string_value,
      CASE param.numeric_value
        WHEN 'Y' THEN To_Number(dsp.Value)
        ELSE To_Number(NULL)
        END                      AS numeric_value,
      dsp.error                  AS error,
      dsp.range_top              AS range_top,
      dsp.range_bottom           AS range_bottom
    FROM sample_parameter@duodesk dsp,
         parameter param,
         investigation i,
         sample s
    WHERE s.investigation_id = i.id
    AND dsp.sample_id = s.proposal_sample_id
    AND dsp.name IS NOT NULL
    AND dsp.value IS NOT NULL
    AND param.name = Lower(dsp.name)
    AND param.units = Nvl(dsp.units,'N/A')    ;

  lva_safety_info vc_array := vc_array(
    'storage',
    'hazard details',
    'sensitive to air',
    'sensitive to vapour',
    'other hazards',
    'other sample prep hazards',
    'other equipment hazards',
    'prep lab required',
    'other special equipment requirements',
    'disposal method');


  -- more sample parameters, for safety information.
  -- there's some hard-coding here but all these parameters should be included
  -- in the spreadsheet from where lookup data are originally loaded (which are
  -- also hard-coded!)
  CURSOR c_safety_information_data IS
    SELECT
      i.inv_number       AS inv_number,
      i.instrument       AS instrument,
      i.visit_id         AS visit_id,
      i.facility_cycle   AS facility_cycle,
      s.id               AS sample_id,
      x.column_value     AS name,
      'N/A'              AS units,
      CASE x.column_value
        WHEN 'storage' THEN ds.storage_reqs
        WHEN 'hazard details' THEN ds.sfty_hazard_details
        WHEN 'sensitive to air' THEN ds.sfty_sensitive_to_air
        WHEN 'sensitive to vapour' THEN ds.sfty_sensitive_to_vapour
        WHEN 'other hazards' THEN ds.sfty_oth_exp_hazards
        WHEN 'other sample prep hazards' THEN ds.sfty_oth_sample_prep_hazards
        WHEN 'other equipment hazards' THEN ds.sfty_oth_equip_hazards
        WHEN 'prep lab required' THEN ds.sfty_prep_lab_required
        WHEN 'other special equipment requirements' THEN ds.sfty_oth_special_equip_reqs
        WHEN 'disposal method' THEN ds.sfty_disposal_method
        END              AS string_value,
      NULL               AS numeric_value,
      NULL               AS error,
      NULL               AS range_top,
      NULL               AS range_bottom
    FROM sample s, sample@duodesk ds, investigation i,
         TABLE(lva_safety_info) x
    WHERE ds.id = s.proposal_sample_id
    AND i.id = s.investigation_id;

  TYPE t_inv_number IS TABLE OF investigation.inv_number%TYPE;
  TYPE t_instrument IS TABLE OF investigation.instrument%TYPE;
  TYPE t_visit_id IS TABLE OF investigation.visit_id%TYPE;
  TYPE t_facility_cycle IS TABLE OF investigation.facility_cycle%TYPE;
  TYPE t_sample_id IS TABLE OF sample_parameter.sample_id%TYPE;
  TYPE t_name IS TABLE OF sample_parameter.name%TYPE;
  TYPE t_units IS TABLE OF sample_parameter.units%TYPE;
  TYPE t_string_value IS TABLE OF sample_parameter.string_value%TYPE;
  TYPE t_numeric_value IS TABLE OF sample_parameter.numeric_value%TYPE;
  TYPE t_error IS TABLE OF sample_parameter.error%TYPE;
  TYPE t_range_top IS TABLE OF sample_parameter.range_top%TYPE;
  TYPE t_range_bottom IS TABLE OF sample_parameter.range_bottom%TYPE;

  l_inv_number t_inv_number;
  l_instrument t_instrument;
  l_visit_id t_visit_id;
  l_facility_cycle t_facility_cycle;
  l_sample_id t_sample_id;
  l_name t_name;
  l_units t_units;
  l_string_value t_string_value;
  l_numeric_value t_numeric_value;
  l_error t_error;
  l_range_top t_range_top;
  l_range_bottom t_range_bottom;

  l_success BOOLEAN := TRUE;


  --This is the cursor that will update sample_parametr with a entries from the columns of samplesheet
  --in duodesk.
   CURSOR c1 IS
                  SELECT DISTINCT thissam.id "SAM_ID" ,sam.id "DUO_SAM_ID",
                   samsh.smps_no "SMPS"
            FROM   samplesheet@duodesk samsh,
                   proposal@duodesk prop,
                     duo_proposal@duodesk dprop,
                     requested_instrument@duodesk req,
                     sample@duodesk sam,
                   sample thissam
              WHERE  samsh.smps_propos_no = prop.propos_no
              AND    dprop.desk_propos_no = prop.propos_no
              AND    req.proposal_id = dprop.duo_propos_no
              AND    req.id = sam.requested_instrument_id
            And    nvl(samsh.smps_efface,'N') <> 'Y'
            and    thissam.proposal_sample_id = sam.id;

  PROCEDURE merge_data IS
  BEGIN
  /* testing error trapping.  cause a constraint violation
  Dbms_Output.put_line('testing bulk error collection: sample parameters');
  FOR i IN 1..20 LOOP
    IF g_inv_unique_fields.Count = 0 AND Mod(i,3) = 0 THEN
      Dbms_Output.put_line(
        'breaking investigation '||
        'Proposal: '||l_inv_number(i)||' | '||
        'Instrument: '||Nvl(l_instrument(i),'Unspecified')||' | '||
        'Visit: '||Nvl(l_visit_id(i),'Unspecified')||' | '||
        'Facility Cycle: '||Nvl(l_facility_cycle(i),'Unspecified'));

      l_sample_id(i) := l_sample_id(i) * -1;
    END IF;
  END LOOP;
  --*/

    FORALL indx IN l_inv_number.FIRST..l_inv_number.LAST
    SAVE EXCEPTIONS
    MERGE INTO cleanup_sample_parameter target
    USING(
      SELECT
        l_sample_id(indx)      AS sample_id,
        l_name(indx)           AS name,
        l_units(indx)          AS units,
        l_string_value(indx)   AS string_value,
        l_numeric_value(indx)  AS numeric_value,
        l_error(indx)          AS error,
        l_range_top(indx)      AS range_top,
        l_range_bottom(indx)   AS range_bottom
      FROM dual
      ) source
    ON(target.sample_id = source.sample_id
       AND target.name = source.name
       AND target.units = source.units
     )
    WHEN matched THEN
      UPDATE SET
        string_value = source.string_value,
        numeric_value = source.numeric_value,
        error = source.error,
        range_top = source.range_top,
        range_bottom = source.range_bottom,
        mod_time = systimestamp,
        mod_id = p_mod_id,
        create_id = LV_CREATE_ID,
        create_time = nvl(mod_time,systimestamp),
        facility_acquired = 'Y',
        deleted = 'N'
      WHERE (target.target.string_value != source.string_value
            OR (target.string_value IS NULL AND source.string_value IS NOT NULL)
            OR (target.string_value IS NOT NULL AND source.string_value IS NULL)
            )
      OR (target.numeric_value != source.numeric_value
          OR (target.numeric_value IS NULL AND source.numeric_value IS NOT NULL)
          OR (target.numeric_value IS NOT NULL AND source.numeric_value IS NULL)
          )
      OR (target.error != source.error
          OR (target.error IS NULL AND source.error IS NOT NULL)
          OR (target.error IS NOT NULL AND source.error IS NULL)
          )
      OR (target.range_top != source.range_top
          OR (target.range_top IS NULL AND source.range_top IS NOT NULL)
          OR (target.range_top IS NOT NULL AND source.range_top IS NULL)
          )
      OR (target.range_bottom != source.range_bottom
          OR (target.range_bottom IS NULL AND source.range_bottom IS NOT NULL)
          OR (target.range_bottom IS NOT NULL AND source.range_bottom IS NULL)
          )
      OR target.create_id != LV_CREATE_ID
      OR target.create_time is null
      OR target.facility_acquired is null
      OR target.deleted is null
    WHEN NOT matched THEN
      INSERT(
        sample_id,
        name,
        units,
        string_value,
        numeric_value,
        error,
        range_top,
        range_bottom,
        mod_time,
        mod_id,
        create_id,
        create_time,
        facility_acquired,
        deleted)
    VALUES(
      source.sample_id,
      source.name,
      source.units,
      source.string_value,
      source.numeric_value,
      source.error,
      source.range_top,
      source.range_bottom,
      systimestamp,
      p_mod_id,
      LV_CREATE_ID,
      systimestamp,
      'Y',
      'N');
  EXCEPTION
    WHEN bulk_errors THEN
      DECLARE
        ln_errors PLS_INTEGER;
        ln_error_index PLS_INTEGER;
      BEGIN
        ln_errors := SQL%bulk_exceptions.COUNT;
        log_pkg.write_exception('sample parameter migration failed');
        log_pkg.write_exception('number of records that failed: ' || ln_errors,1);

        -- log details of the record and the error
        FOR i IN 1..ln_errors
        LOOP
          ln_error_index := SQL%bulk_exceptions(i).error_index;
          log_pkg.write_exception(
            'Sample Id: '||Nvl(To_Char(l_sample_id(ln_error_index),'Unspecified'),2));

          log_pkg.write_exception('Parameter Name: '||l_name(ln_error_index),2);

          log_pkg.write_exception(SQLERRM(-SQL%bulk_exceptions(i).error_code),3);
        END LOOP;
      END;
  END;
BEGIN

  OPEN c_sample_parameter_data;
  FETCH c_sample_parameter_data BULK COLLECT INTO
    l_inv_number,
    l_instrument,
    l_visit_id,
    l_facility_cycle,
    l_sample_id,
    l_name,
    l_units,
    l_string_value,
    l_numeric_value,
    l_error,
    l_range_top,
    l_range_bottom;
  CLOSE c_sample_parameter_data;

  merge_data;




  OPEN c_safety_information_data;
  FETCH c_safety_information_data BULK COLLECT INTO
    l_inv_number,
    l_instrument,
    l_visit_id,
    l_facility_cycle,
    l_sample_id,
    l_name,
    l_units,
    l_string_value,
    l_numeric_value,
    l_error,
    l_range_top,
    l_range_bottom;
  CLOSE c_safety_information_data;

  merge_data;


FOR c1rec IN c1 LOOP

            begin

            merge into sample_parameter target
                   using(
                   select smps_no "SMPS_NO", thissam.id "SAM_ID",pam.units "UNITS",pam.numeric_value "NUMERIC_VAL", pam.name "NAME",
                   case pam.name
                   when 'user_phone' then smps_user_phone
                   when 'anom_scat_1' then smps_anom_scat_1
                   when 'cell_b' then to_char(smps_cell_b)
                   when 'cell_gamma' then to_char(smps_cell_gamma)
                    when 'is_virfactor' then smps_is_virfactor
                    when 'cell_c' then to_char(smps_cell_c)
                    when 'native_ds' then to_char(smps_native_ds)
                    when 'other_holder' then smps_other_holder
                    when 'laser_wavelength' then to_char(smps_laser_wavelength)
                    when 'cooler' then smps_cooler
                    when 'opmode_no' then to_char(smps_opmode_no)
                    when 'opmode_date' then to_char(smps_opmode_date)
                    when 'anom_scat_2' then smps_anom_scat_2
                    when 'sci_justif' then smps_sci_justif
                    when 'no' then to_char(smps_no)
                    when 'propsbm_no' then to_char(smps_propsbm_no)
                    when 'propos_no' then to_char(smps_propos_no)
                    when 'source' then smps_source
                    when 'is_recombinant' then smps_is_recombinant
                    when 'danger_txt' then smps_danger_txt
                    when 'frozen' then smps_frozen
                    when 'capillary' then smps_capillary
                    when 'user_name' then smps_user_name
                    when 'user_email' then smps_user_email
                    when 'anom_scat_3' then smps_anom_scat_3
                    when 'anom_scat_4' then smps_anom_scat_4
                    when 'cell_a' then to_char(smps_cell_a)
                    when 'exprhost_class' then to_char(smps_exprhost_class)
                    when 'laser_class' then smps_laser_class
                    when 'danger_reception_txt' then smps_danger_reception_txt
                    when 'cell_beta' then to_char(smps_cell_beta)
                    when 'order_in_prop' then to_char(smps_order_in_prop)
                    when 'is_powder' then smps_is_powder
                    when 'is_solution' then smps_is_solution
                    when 'source_class' then to_char(smps_source_class)
                    when 'is_virus' then smps_is_virus
                    when 'cryogenic_gas' then smps_cryogenic_gas
                    when 'space_group' then smps_space_group
                    when 'sad_ds' then to_char(smps_sad_ds)
                    when 'comment' then smps_comment
                    when 'is_crystal' then smps_is_crystal
                    when 'concentration' then smps_concentration
                    when 'is_prion' then smps_is_prion
                    when 'ligands' then smps_ligands
                    when 'danger_reception' then smps_danger_reception
                    when 'cell_alpha' then to_char(smps_cell_alpha)
                    when 'mad_ds' then to_char(smps_mad_ds)
                    when 'description' then smps_description
                    when 'is_toxin' then smps_is_toxin
                    when 'is_danger' then smps_is_danger
                    when 'crystal_tray' then smps_crystal_tray
                    when 'ligands_txt' then smps_ligands_txt
                    when 'laser' then smps_laser
                    when 'pressurized_cell' then smps_pressurized_cell
                    when 'propane' then smps_propane
                    when 'removed' then smps_removed
                    when 'stored_esrf' then smps_stored_esrf
                    when 'bag_shift_type' then smps_bag_shift_type
                    when 'acronym' then smps_acronym
                    when 'expr_host' then smps_expr_host
                   end "VALUE"
                   FROM
                   samplesheet@duodesk samsh,
                   sample thissam,
                   parameter pam
                   WHERE
                   smps_no=c1rec.smps
                   and thissam.proposal_sample_id = c1rec.duo_sam_id
                   and pam.name in (SELECT lower(substr(column_name,6))
                               FROM user_tab_cols@duodesk
                               WHERE table_name = 'SAMPLESHEET'
                               AND column_name NOT IN ('SMPS_EFFACE', 'UCR', 'DCR', 'DDM'))
                                                  ) source
                   on(target.sample_id = source.sam_id
                   and target.name=source.name)
                   when matched then
                   update set
                   mod_time = sysdate,
                   mod_id   = LV_CREATE_ID,
                   units    = source.units,
                   numeric_value = decode(source.numeric_val,'Y',source.value,''),
                   string_value  = decode(source.numeric_val,'N',source.value,'')
                   where
                   units <> source.units
                   or (numeric_value= decode(source.numeric_val,'Y',source.value,''))
                   or (string_value  = decode(source.numeric_val,'N',source.value,''))
                   when not matched then
                   insert (
                   sample_id,
                   name,
                   units,
                   create_time,
                   create_id,
                   mod_time,
                   mod_id,
                   facility_acquired,
                   numeric_value,
                   string_value,
                   deleted)
                   values
                   (source.sam_id,
                   source.name,
                   source.units,
                   sysdate,
                   LV_CREATE_ID,
                   sysdate,
                   LV_CREATE_ID,
                   'Y',
                   decode(source.numeric_val,'Y',source.value,''),
                   decode(source.numeric_val,'N',source.value,''),
                   'N');


                 exception when others then
                 log_pkg.write_log('PROBLEM in cleanup sample parameters from samplesheet');
                   end;
                   end loop;

  update sample_parameter ts set deleted='Y',  mod_time = systimestamp, mod_id = p_mod_id where not exists(
  select '' from cleanup_sample_parameter csp where ts.sample_id=csp.sample_id and ts.name=csp.name and ts.units=csp.units);

  IF l_success THEN
    log_pkg.write_log('Cleanup sample parameter data succeeded');
  END IF;
  delete from cleanup_sample_parameter;
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception('Cleanup sample parameter data  failed');
     delete from cleanup_sample_parameter;
    RAISE;
END cleanup_sample_parameters;

PROCEDURE cleanup_icat_authorisation(p_mod_id IN investigation.mod_id%TYPE) AS
BEGIN
update icat_authorisation ta set deleted='Y',  mod_time = systimestamp, mod_id = p_mod_id where not exists(
 SELECT    '' FROM      facility_user a, investigator b
  WHERE     a.facility_user_id = b.facility_user_id
  AND       a.federal_id is not null
  AND ta.user_id          = a.federal_id)
  AND ta.user_id != 'GUARDIAN';
Commit;  
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception(SQLERRM,1);
END;



PROCEDURE cleanup(p_mod_id IN investigation.mod_id%TYPE) AS
BEGIN
log_pkg.init;
log_pkg.write_log('Begin ICAT cleanup');
log_pkg.write_log('Starting cleanup of instrument');
cleanup_instruments(p_mod_id);
log_pkg.write_log('Cleanup of instrument finished');
log_pkg.write_log('Starting cleanup of parameter');
cleanup_parameters(p_mod_id);
log_pkg.write_log('Cleanup of parameter finished');
-- no need to cleaup investigation because it is cleanup at propagation time
--log_pkg.write_log('Starting cleanup of investigation');
--cleanup_investigations(p_mod_id);
--log_pkg.write_log('Cleanup of investigation finished');
log_pkg.write_log('Starting cleanup of shift');
cleanup_shifts(p_mod_id);
log_pkg.write_log('Cleanup of shift finished');
log_pkg.write_log('Starting cleanup of facility_user');
cleanup_facility_users(p_mod_id);
log_pkg.write_log('Cleanup of facility_user finished');
log_pkg.write_log('Starting cleanup of investigator');
cleanup_investigators(p_mod_id);
log_pkg.write_log('Cleanup of investigator finished');
log_pkg.write_log('Starting cleanup of keyword');
cleanup_keywords(p_mod_id);
log_pkg.write_log('Cleanup of keyword finished');
log_pkg.write_log('Starting cleanup of publication');
cleanup_publications(p_mod_id);
log_pkg.write_log('Cleanup of publication finished');
-- no need to cleaup sample because it is cleanup at propagation time
--log_pkg.write_log('Starting cleanup of sample');
--cleanup_samples(p_mod_id);
--log_pkg.write_log('Cleanup of sample finished');
log_pkg.write_log('Starting cleanup of sample_parameter');
cleanup_sample_parameters(p_mod_id);
log_pkg.write_log('Cleanup of sample_parameter finished');
log_pkg.write_log('Starting cleanup of icat_authorisation');
cleanup_icat_authorisation(p_mod_id);
log_pkg.write_log('Cleanup of icat_authorisation finished');
log_pkg.write_log('ICAT Cleanup finshed SUCCESSFUL');

END;

END cleanupicat_pkg;

--'PROPAGATION'
/