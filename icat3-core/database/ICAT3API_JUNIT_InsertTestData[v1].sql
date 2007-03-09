Insert into INVESTIGATION
   (ID, INV_NUMBER, VISIT_ID, INSTRUMENT, TITLE, INV_TYPE, BCAT_INV_STR, RELEASE_DATE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, '32', '1', 'SXD', 'SrF2 calibration  w=-25.3', 'experiment', '/CCW - / RAL', TO_TIMESTAMP('02/01/2009 00:00:00.0','DD/MM/YYYY HH24:MI:SS.FF'), TO_TIMESTAMP('02/01/2007 11:39:59.1','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');


Insert into INVESTIGATION
   (ID, INV_NUMBER, VISIT_ID, INSTRUMENT, TITLE, INV_TYPE, BCAT_INV_STR, RELEASE_DATE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (3, '12345', '1', 'SXD', 'Investigation without any investigators', 'experiment', 'damian', TO_TIMESTAMP('02/01/2009 00:00:00.0','DD/MM/YYYY HH24:MI:SS.FF'), TO_TIMESTAMP('02/01/2007 11:39:59.1','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');


Insert into FACILITY_USER
   (FACILITY_USER_ID, FEDERAL_ID, TITLE, INITIALS, FIRST_NAME, LAST_NAME, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   ('9932', 'dwf64', 'Mr', 'DW', 'Damian', 'Flannery', TO_TIMESTAMP('16/01/2007 11:16:55.1','DD/MM/YYYY HH24:MI:SS.FF'), 'JAMES', 'FIRST PROPAGATION');
Insert into FACILITY_USER
   (FACILITY_USER_ID, FEDERAL_ID, TITLE, INITIALS, FIRST_NAME, LAST_NAME, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   ('JAMES', 'JAMES-JAMES', 'Mr', 'JWH', 'JAMES', 'HEALY', TO_TIMESTAMP('16/01/2007 09:00:15.2','DD/MM/YYYY HH24:MI:SS.FF'), 'JAMES', 'FIRST PROPAGATION');


Insert into INVESTIGATOR
   (INVESTIGATION_ID, FACILITY_USER_ID, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, '9932', TO_TIMESTAMP('16/01/2007 09:00:17.7','DD/MM/YYYY HH24:MI:SS.FF'), '9932', 'FIRST PROPAGATION');
Insert into INVESTIGATOR
   (INVESTIGATION_ID, FACILITY_USER_ID, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'JAMES', TO_TIMESTAMP('16/01/2007 09:00:17.7','DD/MM/YYYY HH24:MI:SS.FF'), 'JAMES', 'FIRST PROPAGATION');


Insert into KEYWORD
   (INVESTIGATION_ID, NAME, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, '(00l)', TO_TIMESTAMP('02/01/2007 11:40:13.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into KEYWORD
   (INVESTIGATION_ID, NAME, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, '1989', TO_TIMESTAMP('02/01/2007 11:40:13.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into KEYWORD
   (INVESTIGATION_ID, NAME, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'calibration', TO_TIMESTAMP('02/01/2007 11:40:13.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into KEYWORD
   (INVESTIGATION_ID, NAME, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'ccw', TO_TIMESTAMP('02/01/2007 11:40:13.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into KEYWORD
   (INVESTIGATION_ID, NAME, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'ccwilson', TO_TIMESTAMP('02/01/2007 11:40:13.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into KEYWORD
   (INVESTIGATION_ID, NAME, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'harwell', TO_TIMESTAMP('02/01/2007 11:40:13.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into KEYWORD
   (INVESTIGATION_ID, NAME, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'jgoff', TO_TIMESTAMP('02/01/2007 11:40:13.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into KEYWORD
   (INVESTIGATION_ID, NAME, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'position', TO_TIMESTAMP('02/01/2007 11:40:13.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into KEYWORD
   (INVESTIGATION_ID, NAME, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'ral', TO_TIMESTAMP('02/01/2007 11:40:13.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into KEYWORD
   (INVESTIGATION_ID, NAME, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'shull', TO_TIMESTAMP('02/01/2007 11:40:13.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into KEYWORD
   (INVESTIGATION_ID, NAME, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'srf2', TO_TIMESTAMP('02/01/2007 11:40:13.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into KEYWORD
   (INVESTIGATION_ID, NAME, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'sxd', TO_TIMESTAMP('02/01/2007 11:40:13.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into KEYWORD
   (INVESTIGATION_ID, NAME, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'sxd01064.raw', TO_TIMESTAMP('02/01/2007 11:40:13.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into KEYWORD
   (INVESTIGATION_ID, NAME, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'sxd01256.raw', TO_TIMESTAMP('02/01/2007 11:40:13.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into KEYWORD
   (INVESTIGATION_ID, NAME, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'sxd01300.raw', TO_TIMESTAMP('02/01/2007 11:40:13.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into KEYWORD
   (INVESTIGATION_ID, NAME, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'w=-25.3', TO_TIMESTAMP('02/01/2007 11:40:13.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');


Insert into SAMPLE
   (ID, INVESTIGATION_ID, NAME, SAFETY_INFORMATION, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 2, 'SrF2 calibration  w=-25.3', '0', TO_TIMESTAMP('02/01/2007 11:44:00.4','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');



Insert into DATASET
   (ID, SAMPLE_ID, INVESTIGATION_ID, NAME, DATASET_TYPE, DATASET_STATUS, DESCRIPTION, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 2, 2, 'Default', 'experiment_raw', 'complete', 'These files were processed retrospectively using application ''writeRaw'' v1.6'' on Tue Apr 25 00:56:38 2006', TO_TIMESTAMP('02/01/2007 11:44:18.6','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');


Insert into DATAFILE
   (ID, DATASET_ID, NAME, DESCRIPTION, FILE_SIZE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 2, 'SXD01064.RAW', 'SrF2 calibration w=-25.3', 2430976, TO_TIMESTAMP('02/01/2007 11:44:45.8','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE
   (ID, DATASET_ID, NAME, DESCRIPTION, FILE_SIZE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (58, 2, 'SXD01256.RAW', 'SrF2 calibration (00l)', 6024704, TO_TIMESTAMP('02/01/2007 11:44:45.8','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE
   (ID, DATASET_ID, NAME, DESCRIPTION, FILE_SIZE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (101, 2, 'SXD01300.RAW', 'SrF2 position calibration', 3169280, TO_TIMESTAMP('02/01/2007 11:44:45.8','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE
   (ID, DATASET_ID, NAME, FILE_SIZE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (17434, 2, 'SXD01300.LOG', 227, TO_TIMESTAMP('02/01/2007 11:44:45.8','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');



Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'finish_date', 'yyyy-MM-dd HH:mm:ss', '1989-05- 8 11:12:17', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'good_frames', 'pulses', 189541, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'good_proton_charge', 'uAmp hours', '389.443', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'monitor_sum1', 'neutrons', 119262801, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'monitor_sum2', 'neutrons', 0, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'monitor_sum3', 'neutrons', 0, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'notes', 'N/A', '138', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'number_of_periods', 'decimal', 1, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'number_of_spectra', 'decimal', 4100, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'number_of_time_channels', 'decimal', 137, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'run_duration', 'seconds', 5322, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'run_header', 'N/A', 'SXD01064                /CCWSrF2 calibration  w=-25. 8-MAY-1989 08:40:05   389.4', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'run_number', 'decimal', 1064, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'run_title', 'N/A', 'SrF2 calibration  w=-25.3', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'short_title', 'N/A', 'SrF2 calibration  w=-25.', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'start_date', 'yyyy-MM-dd HH:mm:ss', '1989-05- 8 08:40:05', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'time_channel_parameters', 'N/A', '1800 7000 0.01', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (2, 'total_proton_charge', 'uAmp hours', '389.445', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');



Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (58, 'finish_date', 'yyyy-MM-dd HH:mm:ss', '1989-09-12 15:43:21', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (58, 'good_frames', 'pulses', 1185970, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (58, 'good_proton_charge', 'uAmp hours', '24.9172', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (58, 'monitor_sum1', 'neutrons', 12679, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (58, 'monitor_sum2', 'neutrons', 0, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (58, 'monitor_sum3', 'neutrons', 0, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (58, 'notes', 'N/A', '357', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (58, 'number_of_periods', 'decimal', 1, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (58, 'number_of_spectra', 'decimal', 4100, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (58, 'number_of_time_channels', 'decimal', 356, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (58, 'run_duration', 'seconds', 1234, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (58, 'run_header', 'N/A', 'SXD01256SHull/JGoff/CCWilsonSrF2 calibration (00l)  12-SEP-1989 08:13:09    24.9', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (58, 'run_number', 'decimal', 1256, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (58, 'run_title', 'N/A', 'SrF2 calibration (00l)', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (58, 'short_title', 'N/A', 'SrF2 calibration (00l)', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (58, 'start_date', 'yyyy-MM-dd HH:mm:ss', '1989-09-12 08:13:09', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (58, 'time_channel_parameters', 'N/A', '1000 12000 0.007', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (58, 'total_proton_charge', 'uAmp hours', '24.9172', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');


Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (101, 'finish_date', 'yyyy-MM-dd HH:mm:ss', '1989-10- 2 08:16:50', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (101, 'good_frames', 'pulses', 167033, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (101, 'good_proton_charge', 'uAmp hours', '89.6272', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (101, 'monitor_sum1', 'neutrons', 1031, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (101, 'monitor_sum2', 'neutrons', 0, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (101, 'monitor_sum3', 'neutrons', 0, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (101, 'notes', 'N/A', '183', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (101, 'number_of_periods', 'decimal', 1, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (101, 'number_of_spectra', 'decimal', 4100, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (101, 'number_of_time_channels', 'decimal', 182, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (101, 'run_duration', 'seconds', 3627, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (101, 'run_header', 'N/A', 'SXD01300                /ccwSrF2 position calibratio 2-OCT-1989 07:16:12    89.6', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, NUMERIC_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (101, 'run_number', 'decimal', 1300, TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (101, 'run_title', 'N/A', 'SrF2 position calibration', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (101, 'short_title', 'N/A', 'SrF2 position calibratio', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (101, 'start_date', 'yyyy-MM-dd HH:mm:ss', '1989-10- 2 07:16:12', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (101, 'time_channel_parameters', 'N/A', '1800 7000 0.0075', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');
Insert into DATAFILE_PARAMETER
   (DATAFILE_ID, NAME, UNITS, STRING_VALUE, MOD_TIME, MOD_ID, CREATE_ID)
 Values
   (101, 'total_proton_charge', 'uAmp hours', '89.6272', TO_TIMESTAMP('02/01/2007 11:52:56.3','DD/MM/YYYY HH24:MI:SS.FF'), 'FIRST PROPAGATION', 'FIRST PROPAGATION');