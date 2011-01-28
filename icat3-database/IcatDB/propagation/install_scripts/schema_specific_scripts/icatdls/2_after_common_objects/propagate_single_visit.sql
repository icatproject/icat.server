CREATE OR REPLACE procedure propagate_single_visit(visit investigation.visit_id%TYPE, beamline investigation.instrument%TYPE)
as
begin
/* The script push the specified visit on the defined beamline from DuoDesk to Icat and then from Icat to the Ikittens
*/
batch_single_migration_pkg.duodesk_pr(visit,beamline);
commit;
populate_single_beamlines_pkg.propagate_data(beamline);
commit;
end;
/