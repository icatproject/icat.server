ALTER TABLE DataCollectionParameter MODIFY NUMERIC_VALUE NUMBER;
ALTER TABLE DataCollectionParameter MODIFY  rangeTop NUMBER;
ALTER TABLE DataCollectionParameter  MODIFY rangeBottom NUMBER;
ALTER TABLE DataCollectionParameter  MODIFY error NUMBER;

ALTER TABLE DatafileParameter  MODIFY NUMERIC_VALUE NUMBER;
ALTER TABLE DatafileParameter  MODIFY rangeTop NUMBER;
ALTER TABLE DatafileParameter  MODIFY rangeBottom NUMBER;
ALTER TABLE DatafileParameter  MODIFY error NUMBER;

ALTER TABLE DatasetParameter  MODIFY NUMERIC_VALUE NUMBER;
ALTER TABLE DatasetParameter  MODIFY rangeTop NUMBER;
ALTER TABLE DatasetParameter  MODIFY rangeBottom NUMBER;
ALTER TABLE DatasetParameter  MODIFY error NUMBER;

ALTER TABLE InvestigationParameter  MODIFY NUMERIC_VALUE NUMBER;
ALTER TABLE InvestigationParameter  MODIFY rangeTop NUMBER;
ALTER TABLE InvestigationParameter  MODIFY rangeBottom NUMBER;
ALTER TABLE InvestigationParameter  MODIFY error NUMBER;

ALTER TABLE SampleParameter  MODIFY NUMERIC_VALUE NUMBER;
ALTER TABLE SampleParameter  MODIFY rangeTop NUMBER;
ALTER TABLE SampleParameter  MODIFY rangeBottom NUMBER;
ALTER TABLE SampleParameter  MODIFY error NUMBER;

ALTER TABLE ParameterType MODIFY maximumNumericValue NUMBER;
ALTER TABLE ParameterType MODIFY minimumNumericValue NUMBER;
