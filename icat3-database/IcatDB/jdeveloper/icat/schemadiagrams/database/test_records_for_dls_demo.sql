delete from datafile where dataset_id = (select id from dataset where investigation_id = (select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02') and dataset_type = 'experiment_raw') ;

delete from dataset where investigation_id = (select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02') ;

delete from shift where investigation_id = (select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02') ;

delete from investigator where investigation_id = (select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02') ;

-- delete investigators also

delete from facility_user where federal_id in
('mtg63', 'gjd37', 'ksa67', 'awa25', 'shk78', 'sas27', 'sic78', 'rd73', 'rpt23', 'vsc89113');

delete from sample where investigation_id = (select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02') ;

delete from keyword where investigation_id = (select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02') ;

delete from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02' ;

---------


-- investigation

insert into investigation 
(id, inv_number, visit_id, instrument, title, inv_abstract,  inv_type, mod_time, mod_id)
values
(INVESTIGATION_ID_SEQ.nextval, 'sp123', '1', 'i02', 'Structure of Copper under Extreme Conditions',
 'Copper under extreme temperatures and pressures', 'experiment', systimestamp, 'icat-test-records') ;

-- keywords

insert into keyword (investigation_id, name, mod_time, mod_id)
values
((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02'), 'Copper',
 systimestamp, 'icat-test-records') ;


insert into keyword (investigation_id, name, mod_time, mod_id)
values
((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02'), 'High',
 systimestamp, 'icat-test-records') ;
   

insert into keyword (investigation_id, name, mod_time, mod_id)
values
((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02'), 'Temperatures',
 systimestamp, 'icat-test-records') ;

-- sample

insert into sample (id, investigation_id, name,safety_information, mod_time, mod_id) 
values
(sample_id_seq.nextval, 
(select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02'),
'Copper',
'As Safe as Houses',
systimestamp,
'icat-test-records') ;

-- facility_users
insert into facility_user (facility_user_id, federal_id, first_name, last_name, mod_time, mod_id)
values
(123063, 'mtg63', 'Michael', 'Gleaves', systimestamp, 'icat-test-records') ;

insert into facility_user (facility_user_id, federal_id, first_name, last_name, mod_time, mod_id)
values
(123037, 'gjd37', 'Glen', 'Drinkwater', systimestamp, 'icat-test-records') ;

insert into facility_user (facility_user_id, federal_id, first_name, last_name, mod_time, mod_id)
values
(123067, 'ksa67', 'Karen', 'Ackroyd', systimestamp, 'icat-test-records') ;

insert into facility_user (facility_user_id, federal_id, first_name, last_name, mod_time, mod_id)
values
(1230078, 'shk78', 'Steve', 'Kinder', systimestamp, 'icat-test-records') ;

insert into facility_user (facility_user_id, federal_id, first_name, last_name, mod_time, mod_id)
values
(123025, 'awa25', 'Alun', 'Ashton', systimestamp, 'icat-test-records') ;

--Bill already exists as user 13
--insert into facility_user (facility_user_id, federal_id, first_name, last_name, mod_time, mod_id)
--values
--(123093, 'wcap93', 'Bill', 'Pulford', systimestamp, 'icat-test-records') ;

insert into facility_user (facility_user_id, federal_id, first_name, last_name, mod_time, mod_id)
values
(123027, 'sas27', 'Shoaib', 'Sufi', systimestamp, 'icat-test-records') ;

insert into facility_user (facility_user_id, federal_id, first_name, last_name, mod_time, mod_id)
values
(123078, 'sic78', 'Stuart', 'Cambell', systimestamp, 'icat-test-records') ;

insert into facility_user (facility_user_id, federal_id, first_name, last_name, mod_time, mod_id)
values
(123073, 'rd73', 'Roger', 'Downing', systimestamp, 'icat-test-records') ;

insert into facility_user (facility_user_id, federal_id, first_name, last_name, mod_time, mod_id)
values
(123023, 'rpt23', 'Rik', 'Tyer', systimestamp, 'icat-test-records') ;

insert into facility_user (facility_user_id, federal_id, first_name, last_name, mod_time, mod_id)
values
(123089113, 'vsc89113', 'James', 'Healy', systimestamp, 'icat-test-records') ;


-- investigator 

insert into investigator(investigation_id, facility_user_id, mod_time, mod_id)
values ((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02'),
123063, systimestamp, 'icat-test-records') ;

insert into investigator(investigation_id, facility_user_id, mod_time, mod_id)
values ((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02'),
123037, systimestamp, 'icat-test-records') ;

insert into investigator(investigation_id, facility_user_id, mod_time, mod_id)
values ((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02'),
123067, systimestamp, 'icat-test-records') ;

insert into investigator(investigation_id, facility_user_id, mod_time, mod_id)
values ((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02'),
123078, systimestamp, 'icat-test-records') ;

insert into investigator(investigation_id, facility_user_id, mod_time, mod_id)
values ((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02'),
1230078, systimestamp, 'icat-test-records') ;

insert into investigator(investigation_id, facility_user_id, mod_time, mod_id)
values ((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02'),
123025, systimestamp, 'icat-test-records') ;

insert into investigator(investigation_id, facility_user_id, mod_time, mod_id)
values ((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02'),
13, systimestamp, 'icat-test-records') ;

insert into investigator(investigation_id, facility_user_id, mod_time, mod_id)
values ((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02'),
123027, systimestamp, 'icat-test-records') ;

insert into investigator(investigation_id, facility_user_id, mod_time, mod_id)
values ((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02'),
123073, systimestamp, 'icat-test-records') ;

insert into investigator(investigation_id, facility_user_id, mod_time, mod_id)
values ((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02'),
123023, systimestamp, 'icat-test-records') ;

insert into investigator(investigation_id, facility_user_id, mod_time, mod_id)
values ((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02'),
123089113, systimestamp, 'icat-test-records') ;

-- shift

--insert into shift (investigation_id, start_date, end_date, mod_time, mod_id) values 
--((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02'),
--systimestamp-0.1, systimestamp+0.1, systimestamp, 'icat-test-records') ;

--creating a monster shift for testing
insert into shift (investigation_id, start_date, end_date, mod_time, mod_id) values 
((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02'),
systimestamp-0.1, systimestamp+8.1, systimestamp, 'icat-test-records') ;

-- data set (just one for experiment raw)

insert into dataset (id, investigation_id, name, dataset_type, mod_time, mod_id) 
values
(dataset_id_seq.nextval,
 (select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02'),
 'my_copper_run',
 'experiment_raw',
 systimestamp,
 'icat-test-records') ;
 
 
-- data file (from roger e-mail)

insert into datafile (id, dataset_id, name, location, mod_time, mod_id)
values (datafile_id_seq.nextval,
(select id from dataset where investigation_id = (select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02') and dataset_type = 'experiment_raw'),
'file-1.nxs', 'srb://mombasa.esc.rl.ac.uk:5640/i02/data/data/2007/sp123-1/SRB-3.4.2-r2.tar.gz',
systimestamp, 'icat-test-records') ;

--insert into datafile (id, dataset_id, name, location, mod_time, mod_id)
--values (datafile_id_seq.nextval,
--(select id from dataset where investigation_id = (select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'i02') and dataset_type = 'experiment_raw'),
--'file-2.nxs', 'srb://kisumu.esc.rl.ac.uk:7640/tdlszone/i02/data/data/2007/sp123-1/data/file-2.nxs',
--systimestamp, 'icat-test-records') ;



commit ;

