CREATE OR REPLACE PROCEDURE update_loq_data is

CURSOR c_get_loq_data IS
SELECT rb_no, runs_from, runs_to
FROM   extern_loq_data
order by runs_from;


CURSOR c_get_investigation_details (p_run_from  number, p_run_to  number) IS
select distinct id from investigation 
where instrument = 'loq'
and
id in (
select investigation_id 
from dataset where id in (
	 		   select dataset_id 
			   from datafile where id in 
			                            (SELECT datafile_id FROM datafile_parameter
				   		     WHERE NAME = 'run_number'
				   		     AND numeric_value between p_run_from and p_run_to)));



TYPE t_investigation_id IS TABLE OF investigator.investigation_id%TYPE;

l_investigation_id t_investigation_id;


BEGIN

  FOR r_get_loq_data  IN  c_get_loq_data  LOOP

 	  for  c_r in c_get_investigation_details(r_get_loq_data.runs_from, r_get_loq_data.runs_to)  loop

    	UPDATE investigation SET
      	inv_type  = 'commercial_experiment'
		WHERE      id = c_r.id
		and inv_type <> 'commercial_experiment';

		end loop;


 END LOOP;


EXCEPTION

  WHEN OTHERS THEN
 log_pkg.write_exception('error in update_loq_data raised');
    RAISE;


END;
/						 
