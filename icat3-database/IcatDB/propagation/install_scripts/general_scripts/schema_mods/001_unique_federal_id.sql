PROMPT Ensure Federal Id is unique regardless of case.

CREATE  INDEX facility_user_uk1 ON facility_user(Upper(federal_id));

