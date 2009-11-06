-- this will need (very likely) modifying to bring in line with icat 3.2
CREATE OR REPLACE FORCE VIEW v_federal_investigation (ID,
                                                              inv_number,
                                                              visit_id,
                                                              facility_cycle,
                                                              instrument,
                                                              title,
                                                              inv_type,
                                                              inv_abstract,
                                                              prev_inv_number,
                                                              bcat_inv_str,
                                                              grant_id,
                                                              release_date,
                                                              mod_time,
                                                              mod_id,
                                                              federal_id,
                                                              full_name,
                                                              public_investigation
                                                             )
AS
   WITH inv_users AS
        (
         -- all the investigators for each investigation (there may be no
         -- investigators on some investigations - outer join)
         SELECT g.investigation_id, f.federal_id,
                f.first_name || ' ' || f.last_name AS full_name,
                g.ROLE AS user_role
           FROM investigator g, facility_user f
          WHERE f.facility_user_id = g.facility_user_id),
        first_shift AS
        (
         -- start time of the first shift for each visit
         SELECT   MIN (start_date) start_date, investigation_id
             FROM shift
         GROUP BY investigation_id)
   SELECT i.ID ID, i.inv_number inv_number, LOWER (i.visit_id) visit_id,
          i.facility_cycle facility_cycle, i.instrument instrument,
          i.title title, i.inv_type inv_type, i.inv_abstract inv_abstract,
          i.prev_inv_number prev_inv_number, i.bcat_inv_str bcat_inv_str,
          i.grant_id grant_id, i.release_date release_date,
          i.mod_time mod_time, i.mod_id mod_id, u.federal_id federal_id,
          u.full_name full_name,
          NVL2 (u.investigation_id, 'N', 'Y') public_investigation
     FROM investigation i, inv_users u, first_shift s
    WHERE u.investigation_id(+) = i.ID AND s.investigation_id(+) = i.ID
/

