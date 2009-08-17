/*

This is specific to DLS
we'll need a separate script for each installation type which has the phase 1
schema installed and uses the src_hash column.

Sets the src_hash column using the same method as in the
phase 1 propagation process.

Also sets the visit_id.

*/

PROMPT Populate the src_hash and visit_id columns

UPDATE investigation inv SET (src_hash, mod_time, mod_id) = (
  SELECT util_pkg.get_md5(p.propos_no, i.instr_no, pl.pl_no),
         systimestamp, 'Phase 2 upgrade'
    FROM proposal@duodesk p,
         duo_proposal@duodesk dp,
         mesure@duodesk m,
         instrument@duodesk i,
         planning@duodesk pl
    WHERE dp.desk_propos_no = p.propos_no
    AND m.mes_propos_no = p.propos_no
    AND m.mes_instr_no = i.instr_no
    AND mes_uni_all > 0 -- indicates an approved proposal
    AND pl.pl_mes_no = m.mes_no
    AND pl.pl_date_deb IS NOT NULL
    AND pl.pl_date_fin IS NOT NULL
    AND Nvl(p.propos_efface,'N') != 'Y'
    AND Nvl(m.mes_efface,'N') != 'Y'
    AND Nvl(i.instr_efface,'N') != 'Y'
    AND Nvl(pl.pl_efface,'N') != 'Y'
    AND inv.inv_number = To_Char(p.propos_no)
    AND inv.visit_id = To_Char(pl.pl_no)
    AND inv.facility_cycle IS NULL
    AND inv.instrument = Lower(i.instr_nom)
  );




-- process takes ages when using the md5 function on the remote tables, so
-- grab it to a temp table first.
PROMPT Dropping temporary table.  Errors here can be ignored.
DROP TABLE install_tmp1 purge;
CREATE TABLE install_tmp1 unrecoverable AS
  SELECT  Row_Number() over(PARTITION BY p.propos_no, i.instr_nom
                            ORDER BY pl.pl_date_deb, pl.pl_no) visit_id,
          p.propos_no, i.instr_no, pl.pl_no
    FROM proposal@duodesk p,
         duo_proposal@duodesk dp,
         mesure@duodesk m,
         instrument@duodesk i,
         planning@duodesk pl
    WHERE dp.desk_propos_no = p.propos_no
    AND m.mes_propos_no = p.propos_no
    AND m.mes_instr_no = i.instr_no
    AND mes_uni_all > 0 -- indicates an approved proposal
    AND pl.pl_mes_no = m.mes_no
    AND pl.pl_date_deb IS NOT NULL
    AND pl.pl_date_fin IS NOT NULL
    AND Nvl(p.propos_efface,'N') != 'Y'
    AND Nvl(m.mes_efface,'N') != 'Y'
    AND Nvl(i.instr_efface,'N') != 'Y'
    AND Nvl(pl.pl_efface,'N') != 'Y'
;



UPDATE investigation inv SET (visit_id) = (
  SELECT visit_id
    FROM install_tmp1
    WHERE util_pkg.get_md5(propos_no, instr_no, pl_no) = inv.src_hash
  )
WHERE inv.src_hash IS NOT NULL;

DROP TABLE install_tmp1 purge;
