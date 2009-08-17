-- Author keir Hawker
-- Date: 20/06/2008
-- Purpose: Migration script to migrate all the data
-- from ICATDLS 3.1 to 3.3 

ACCEPT db_password CHAR prompt  'Mars passowrd:'

--Create database Link
 CREATE DATABASE LINK "ICATDLS.RL.AC.UK"
 CONNECT TO ICATDLS
 IDENTIFIED BY kjadh31
 USING 'MARS';

--
insert into  dataset_status
(name, description, seq_number, mod_time, mod_id, create_time, create_id,facility_acquired,deleted)
select name, description, '999', sysdate, 'Import from Spreadsheet' ,sysdate, 'Import from Spreadsheet' , 'Y','N' from extern_dataset_status where dls = 'y';

insert into dataset_type
(name, description, seq_number, mod_time, mod_id, create_time, create_id,facility_acquired,deleted)
select name, description, '999', sysdate, 'Import from Spreadsheet' ,sysdate, 'Import from Spreadsheet' , 'Y','N' from extern_dataset_type where dls = 'y';

insert into datafile_format
(name, version, format_type, description, seq_number, mod_time, mod_id, create_time, create_id, facility_acquired,deleted)
select name, version, format_type, description, '999', sysdate, 'Import from Spreadsheet' ,sysdate, 'Import from Spreadsheet' , 'Y', 'N' from extern_datafile_format where dls = 'y';

insert into investigation_type
(name, description, seq_number, mod_time, mod_id, create_time, create_id,facility_acquired,deleted)
select name, description, '999', sysdate, 'Import from Spreadsheet' ,sysdate, 'Import from Spreadsheet' , 'Y','N' from extern_investigation_type where dls = 'y';

insert into instrument
(name, short_name, type, description, seq_number, mod_time, mod_id, create_time, create_id, facility_acquired, deleted)
select name, short_name, type, description, '999',sysdate, 'Import from Spreadsheet' ,sysdate, 'Import from Spreadsheet','Y','N'   from extern_instrument where dls='y';

insert into facility_cycle
(name, start_date, finish_date, description, seq_number, mod_time, mod_id, create_time, create_id,facility_acquired,deleted)
select name, start_date, finish_date, description, '999', sysdate, 'Import from Spreadsheet' ,sysdate, 'Import from Spreadsheet' , 'Y','N' from extern_facility_cycle where dls = 'y';

insert into parameter
(name, units, units_long_version, searchable, numeric_value, non_numeric_value_format, is_sample_parameter, is_dataset_parameter, is_datafile_parameter, description,
verified, seq_number, mod_time, mod_id, create_time, create_id, facility_acquired, deleted)
select  name, nvl(units,0), units_long_version, nvl(searchable,'N'), numeric_value, non_numeric_value_format, is_sample_parameter, is_dataset_parameter, is_datafile_parameter, description,
'Y','999',sysdate, 'Import from Spreadsheet' ,sysdate, 'Import from Spreadsheet','Y','N' from extern_parameter_list where dls='y';
 
insert into this_icat
(facility_short_name, facility_long_name, facility_url, facility_description, days_until_public_release, seq_number, mod_time, mod_id, create_time, create_id,facility_acquired,deleted)
select facility_short_name, facility_long_name, facility_url, facility_description, '10','999',sysdate, 'Import from Spreadsheet' ,sysdate, 'Import from Spreadsheet','Y','N' from extern_this_icat where dls='y';

insert into icat_role
(role, role_weight, action_insert, action_insert_weight, action_select, action_select_weight, action_download, action_download_weight, action_update,
action_update_weight, action_delete, action_delete_weight, action_remove, action_remove_weight, action_root_insert, action_root_insert_weight, action_root_remove,
action_root_remove_weight, action_set_fa, action_set_fa_weight, action_manage_users, action_manage_users_weight, action_super, action_super_weight,  seq_number, mod_time, mod_id, create_time, create_id,facility_acquired,deleted)
select role, '10',action_insert, action_insert_weight, action_select, action_select_weight, action_download, action_download_weight, action_update,
action_update_weight, action_delete, action_delete_weight, action_remove, action_remove_weight, action_root_insert, action_root_insert_weight, action_root_remove,
action_root_remove_weight, action_set_fa, action_set_fa_weight, action_manage_users, action_manage_users_weight, action_super, action_super_weight,  '999',sysdate, 'Import from Spreadsheet' ,sysdate, 'Import from Spreadsheet','Y','N'
from extern_icat_role where dls='y'

--set the icat_role weight 
update icat_role
set role_weight=
decode(action_insert,'Y',action_insert_weight,0)+
decode(action_select,'Y',action_select_weight,0)+
decode(action_download,'Y',action_download_weight,0)+
decode(action_update,'Y',action_update_weight,0)+
decode(action_delete,'Y',action_delete_weight,0)+
decode(action_remove,'Y',action_remove_weight,0)+
decode(action_root_insert,'Y',action_root_insert_weight,0)+
decode(action_root_remove,'Y',action_root_remove_weight,0)+
decode(action_set_fa,'Y',action_set_fa_weight,0)+
decode(action_manage_users,'Y',action_manage_users_weight,0)+
decode(action_super,'Y',action_super_weight,0);


--Redo Instrument from Live.
--insert into instrument
--(name, short_name, type, description, seq_number, mod_time, mod_id, create_time, create_id, facility_acquired, deleted)
--select name, name, type, description, '999',mod_time, mod_id ,mod_time, mod_id,'Y','N'  from instrument@icatdls.rl.ac.uk;

insert into  facility_instrument_scientist
(instrument_name, federal_id,seq_number, mod_time, mod_id, create_time, create_id,facility_acquired,deleted)
select instrument_name, federal_id,'9999',sysdate, 'Import from Spreadsheet' ,sysdate, 'Import from Spreadsheet','Y','N' from extern_station_scienist where dls='y';


--Empty
--insert into software_version
--(id,name,sw_version,features,description,authors,seq_number, mod_time, mod_id, create_time, create_id, facility_acquired, deleted)


--Rdo Parameter from Live
insert into parameter
(name, units, units_long_version, searchable, numeric_value, non_numeric_value_format, is_sample_parameter, is_dataset_parameter, is_datafile_parameter, description,
verified, seq_number, mod_time, mod_id, create_time, create_id, facility_acquired, deleted)
select name, units, units_long_version, searchable, numeric_value, non_numeric_value_format, is_sample_parameter, is_dataset_parameter, is_datafile_parameter, description,
'N','999', mod_time, mod_id, mod_time, mod_id, 'Y','N' from parameter@icatdls.rl.ac.uk
where (name, units) not in 
(select name, units from parameter);

insert into investigation
(id, inv_number, visit_id, facility, facility_cycle, instrument, title, inv_type, inv_abstract, prev_inv_number,bcat_inv_str,grant_id,release_date, mod_time, mod_id, create_time, create_id, facility_acquired, deleted)
select id, inv_number, visit_id, 'DLS',  facility_cycle, decode(instrument,'i04 1','i04-1',instrument), title, inv_type, inv_abstract, prev_inv_number,bcat_inv_str,grant_id,release_date, mod_time, mod_id, mod_time, mod_id, 'Y', 'N' from investigation@icatdls.rl.ac.uk a
where rowid in (select max(rowid) from investigation@icatdls.rl.ac.uk b 
where a.visit_id=b.visit_id
and a.inv_number=b.inv_number
and decode(a.instrument,'i04 1','i04-1',a.instrument)=decode(b.instrument,'i04 1','i04-1',b.instrument)
and nvl(a.facility_cycle,'0')=nvl(b.facility_cycle,'0'));


insert into dataset
(id, sample_id, investigation_id, name, dataset_type, dataset_status, location, description, mod_time, mod_id, create_time, create_id, facility_acquired, deleted)
select id, sample_id, investigation_id, name, dataset_type, dataset_status, '', description,mod_time, mod_id ,mod_time, mod_id,'Y','N' from dataset@icatdls.rl.ac.uk;

insert into datafile 
(id, dataset_id, name, description, datafile_version, datafile_version_comment, location, datafile_format, datafile_format_version
,datafile_create_time, datafile_modify_time, file_size, command, checksum, signature, mod_time, mod_id, create_time, create_id, facility_acquired, deleted)
select id, dataset_id, name, description, datafile_version, datafile_version_comment, location, datafile_format, datafile_format_version
,datafile_create_time, datafile_modify_time, file_size, command, checksum, signature, mod_time, mod_id ,mod_time, mod_id,'Y','N'  
from datafile@icatdls.rl.ac.uk a
where rowid in (select min(rowid) from datafile@icatdls.rl.ac.uk b 
where a.name=b.name
and a.dataset_id = b.dataset_id
and a.location = b.location) 
order by dataset_id, name, location;

insert into datafile_parameter
(datafile_id,name,units,string_value, numeric_value, range_top, range_bottom, error, description,mod_time, mod_id, create_time, create_id, facility_acquired, deleted)
 select datafile_id,name,units,string_value, numeric_value, range_top, range_bottom, error, description,mod_time, mod_id, mod_time, mod_id, 'Y','N'
  from datafile_parameter@icatdls.rl.ac.uk
where datafile_id in (select id from datafile);


insert into shift
(investigation_id, start_date, end_date, shift_comment,  mod_time, mod_id, create_time, create_id, facility_acquired, deleted)
select investigation_id, start_date, end_date, shift_comment,mod_time, mod_id ,mod_time, mod_id,'Y','N'  from shift@icatdls.rl.ac.uk
where investigation_id in (select id from investigation);

insert into keyword
(investigation_id, name, mod_time, mod_id, create_time, create_id, facility_acquired, deleted)
select investigation_id, name,mod_time, mod_id ,mod_time, mod_id,'Y','N'  from keyword@icatdls.rl.ac.uk
where investigation_id in (select id from investigation);

insert into publication
(id, investigation_id, full_reference, url, repository_id, repository, mod_time, mod_id, create_time, create_id, facility_acquired, deleted)
select id, investigation_id, full_reference, url, repository_id, repository, mod_time, mod_id ,mod_time, mod_id,'Y','N'  from publication@icatdls.rl.ac.uk;

insert into facility_user
(facility_user_id, federal_id, title, initials, first_name, middle_name, last_name, mod_time, mod_id, create_time, create_id, facility_acquired, deleted)
select facility_user_id, federal_id, title, initials, first_name, middle_name, last_name,mod_time, mod_id ,mod_time, mod_id,'Y','N'  from facility_user@icatdls.rl.ac.uk;

insert into investigator
(investigation_id, facility_user_id, role, mod_time, mod_id, create_time, create_id, facility_acquired, deleted)
select investigation_id, facility_user_id, role,mod_time, mod_id ,mod_time, mod_id,'Y','N'  from investigator@icatdls.rl.ac.uk
where investigation_id in (select id from investigation);


insert into sample
(id, investigation_id, name, instance, chemical_formula, safety_information, proposal_sample_id, mod_time, mod_id, create_time, create_id, facility_acquired, deleted)
select id, investigation_id, name, instance, chemical_formula, safety_information, proposal_sample_id,mod_time, mod_id ,mod_time, mod_id,'Y','N'  from sample@icatdls.rl.ac.uk
where investigation_id in (select id from investigation);

insert into sample_parameter
(sample_id, name, units, string_value, numeric_value, error, range_top, range_bottom, description, mod_time, mod_id, create_time, create_id, facility_acquired, deleted)
select sample_id, name, units, string_value, numeric_value, error, range_top, range_bottom, '',mod_time, mod_id ,mod_time, mod_id,'Y','N'   from sample_parameter@icatdls.rl.ac.uk
where sample_id in (select id from sample);

commit;


declare
cursor c1 is 
select * from dataset where dataset_type = 'experiment_raw';
len1 number;
count1 number;
count2 number;
loc varchar2(4000);
begin

for i in c1 loop

select max(length(location)) into len1 from datafile where dataset_id = i.id;
select count(*) into count1 from datafile  where dataset_id = i.id;
select max(location) into loc from datafile where dataset_id = i.id and length(location) = len1;
loop

loc := substr(loc,0,length(loc)-1);

select count(*) into count2 from datafile 
where dataset_id = i.id
and location like loc || '%';

if count2 = count1 then
exit;
end if; 
end loop;

update dataset
set location= loc
where id=i.id;

end loop;
end;
/

