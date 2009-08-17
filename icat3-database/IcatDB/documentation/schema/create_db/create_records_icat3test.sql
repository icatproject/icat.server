delete from datafile where dataset_id = (select id from dataset where investigation_id = (select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'nessie') and dataset_type = 'experiment_raw') ;

delete from dataset where investigation_id = (select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'nessie') ;

delete from shift where investigation_id = (select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'nessie') ;

delete from investigator where investigation_id = (select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'nessie') ;

-- delete investigators also

delete from facility_user where federal_id in
('mtg63', 'gjd37', 'ksa67', 'awa25', 'shk78', 'sas27', 'sic78', 'rd73', 'rpt23', 'vsc89113');

delete from sample where investigation_id = (select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'nessie') ;

delete from keyword where investigation_id = (select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'nessie') ;

delete from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'nessie' ;

delete from instrument where name = 'nessie' ;
delete from investigation_type where name = 'experiment';
delete from dataset_type where name = 'experiment_raw' ;

---------

-- lookup data

insert into instrument (name, type,  mod_time, mod_id, create_id, deleted) values
                       ('nessie', 'Microdiffraction Fluorescence Probe', systimestamp, 'icat-test-records','icat-test-records','N') ;

insert into investigation_type (name, DESCRIPTION,  mod_time, mod_id, create_id, deleted) values
                       ('experiment', 'an experiment', systimestamp, 'icat-test-records','icat-test-records','N') ;

insert into dataset_type (name, DESCRIPTION,  mod_time, mod_id, create_id, deleted) values
                       ('experiment_raw', 'actual data collected from the instrument', systimestamp, 'icat-test-records','icat-test-records','N') ;



-- investigation

insert into investigation 
(id, inv_number, visit_id, instrument, title, inv_abstract,  inv_type, mod_time, mod_id, create_id, deleted)
values
(INVESTIGATION_ID_SEQ.nextval, 'sp123', '1', 'nessie', 'Fluorescence probe measurement of sample-X ',
 'various uses of flurescence probes to measure useful things about sample-X under various conditions', 'experiment', systimestamp, 'icat-test-records', 'icat-test-records','N') ;

-- keywords

insert into keyword (investigation_id, name, mod_time, mod_id, create_id, deleted)
values
((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'nessie'), 'Fluorescence',
 systimestamp, 'icat-test-records', 'icat-test-records', 'N') ;


insert into keyword (investigation_id, name, mod_time, mod_id, create_id, deleted)
values
((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'nessie'), 'Probe',
 systimestamp, 'icat-test-records', 'icat-test-records', 'N') ;
   

insert into keyword (investigation_id, name, mod_time, mod_id, create_id, deleted)
values
((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'nessie'), 'sample-X',
 systimestamp, 'icat-test-records', 'icat-test-records', 'N') ;

-- sample

insert into sample (id, investigation_id, name,safety_information, mod_time, mod_id, create_id, deleted) 
values
(sample_id_seq.nextval, 
(select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'nessie'),
'sample-X',
'As Safe as Houses',
systimestamp,
'icat-test-records', 'icat-test-records', 'N') ;

-- facility_users
insert into facility_user (facility_user_id, federal_id, first_name, last_name, mod_time, mod_id, create_id, deleted)
values
(123063, 'mtg63', 'Michael', 'Gleaves', systimestamp, 'icat-test-records', 'icat-test-records', 'N') ;

insert into facility_user (facility_user_id, federal_id, first_name, last_name, mod_time, mod_id, create_id, deleted)
values
(123037, 'gjd37', 'Glen', 'Drinkwater', systimestamp, 'icat-test-records', 'icat-test-records', 'N') ;

insert into facility_user (facility_user_id, federal_id, first_name, last_name, mod_time, mod_id, create_id, deleted)
values
(123067, 'ksa67', 'Karen', 'Ackroyd', systimestamp, 'icat-test-records', 'icat-test-records', 'N') ;

insert into facility_user (facility_user_id, federal_id, first_name, last_name, mod_time, mod_id, create_id, deleted)
values
(1230078, 'shk78', 'Steve', 'Kinder', systimestamp, 'icat-test-records', 'icat-test-records', 'N') ;

insert into facility_user (facility_user_id, federal_id, first_name, last_name, mod_time, mod_id, create_id, deleted)
values
(123025, 'awa25', 'Alun', 'Ashton', systimestamp, 'icat-test-records', 'icat-test-records', 'N') ;


insert into facility_user (facility_user_id, federal_id, first_name, last_name, mod_time, mod_id, create_id, deleted)
values
(123027, 'sas27', 'Shoaib', 'Sufi', systimestamp, 'icat-test-records', 'icat-test-records', 'N') ;

insert into facility_user (facility_user_id, federal_id, first_name, last_name, mod_time, mod_id, create_id, deleted)
values
(123078, 'sic78', 'Stuart', 'Cambell', systimestamp, 'icat-test-records', 'icat-test-records', 'N') ;

insert into facility_user (facility_user_id, federal_id, first_name, last_name, mod_time, mod_id, create_id, deleted)
values
(123073, 'rd73', 'Roger', 'Downing', systimestamp, 'icat-test-records', 'icat-test-records', 'N') ;

insert into facility_user (facility_user_id, federal_id, first_name, last_name, mod_time, mod_id, create_id, deleted)
values
(123023, 'rpt23', 'Rik', 'Tyer', systimestamp, 'icat-test-records', 'icat-test-records', 'N') ;

insert into facility_user (facility_user_id, federal_id, first_name, last_name, mod_time, mod_id, create_id, deleted)
values
(123089113, 'vsc89113', 'James', 'Healy', systimestamp, 'icat-test-records', 'icat-test-records', 'N') ;


-- investigator 

insert into investigator(investigation_id, facility_user_id, mod_time, mod_id, create_id, deleted)
values ((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'nessie'),
123063, systimestamp, 'icat-test-records', 'icat-test-records', 'N') ;

insert into investigator(investigation_id, facility_user_id, mod_time, mod_id, create_id, deleted)
values ((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'nessie'),
123037, systimestamp, 'icat-test-records', 'icat-test-records', 'N') ;

insert into investigator(investigation_id, facility_user_id, mod_time, mod_id, create_id, deleted)
values ((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'nessie'),
123067, systimestamp, 'icat-test-records', 'icat-test-records', 'N') ;

insert into investigator(investigation_id, facility_user_id, mod_time, mod_id, create_id, deleted)
values ((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'nessie'),
123078, systimestamp, 'icat-test-records', 'icat-test-records', 'N') ;

insert into investigator(investigation_id, facility_user_id, mod_time, mod_id, create_id, deleted)
values ((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'nessie'),
1230078, systimestamp, 'icat-test-records', 'icat-test-records', 'N') ;

insert into investigator(investigation_id, facility_user_id, mod_time, mod_id, create_id, deleted)
values ((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'nessie'),
123025, systimestamp, 'icat-test-records', 'icat-test-records', 'N') ;

insert into investigator(investigation_id, facility_user_id, mod_time, mod_id, create_id, deleted)
values ((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'nessie'),
123027, systimestamp, 'icat-test-records', 'icat-test-records', 'N') ;

insert into investigator(investigation_id, facility_user_id, mod_time, mod_id, create_id, deleted)
values ((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'nessie'),
123073, systimestamp, 'icat-test-records', 'icat-test-records', 'N') ;

insert into investigator(investigation_id, facility_user_id, mod_time, mod_id, create_id, deleted)
values ((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'nessie'),
123023, systimestamp, 'icat-test-records', 'icat-test-records', 'N') ;

insert into investigator(investigation_id, facility_user_id, mod_time, mod_id, create_id, deleted)
values ((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'nessie'),
123089113, systimestamp, 'icat-test-records', 'icat-test-records', 'N') ;

-- shift

--creating a monster shift for testing
insert into shift (investigation_id, start_date, end_date, mod_time, mod_id, create_id, deleted) values 
((select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'nessie'),
systimestamp-0.1, systimestamp+8.1, systimestamp, 'icat-test-records', 'icat-test-records', 'N') ;

-- data set (just one for experiment raw)

insert into dataset (id, investigation_id, name, dataset_type, mod_time, mod_id, create_id, deleted) 
values
(dataset_id_seq.nextval,
 (select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'nessie'),
 'my_sample-X-at-100K',
 'experiment_raw',
 systimestamp,
 'icat-test-records', 'icat-test-records', 'N') ;
 
 
-- data file (from roger e-mail)

insert into datafile (id, dataset_id, name, location, mod_time, mod_id, create_id, deleted)
values (datafile_id_seq.nextval,
(select id from dataset where investigation_id = (select id from investigation where inv_number = 'sp123' and visit_id = '1' and instrument = 'nessie') and dataset_type = 'experiment_raw'),
'file-1.nxs', 'srb://mombasa.esc.rl.ac.uk:5640/nessie/data/data/2007/sp123-1/SRB-3.4.2-r2.tar.gz',
systimestamp, 'icat-test-records', 'icat-test-records', 'N') ;



commit ;

