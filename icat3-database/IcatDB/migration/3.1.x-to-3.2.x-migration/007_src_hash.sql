REM new column on investigation to store a hash of the identifying fields from
REM each of the 4 tables in Duodesk which make up the record in ICAT.  May also
REM be used in other installations.

PROMPT New column src_hash on investigator table

alter table investigation add SRC_HASH VARCHAR2(32);

ALTER TABLE INVESTIGATION
ADD CONSTRAINT INVESTIGATION_UK3 UNIQUE
(
SRC_HASH
)
 ENABLE
;


