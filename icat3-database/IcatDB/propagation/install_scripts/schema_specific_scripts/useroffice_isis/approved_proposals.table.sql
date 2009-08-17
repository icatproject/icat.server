
DROP TABLE approved_proposals;

CREATE TABLE approved_proposals(
  title                     VARCHAR2(255),
  rbno                      NUMBER,
  principal_investigator    VARCHAR2(255),
  access_route              VARCHAR2(255),
  requested_time            NUMBER,
  country                   VARCHAR2(255),
  round                     VARCHAR2(255),
  instrument                VARCHAR2(255),
  alternative_instruments   VARCHAR2(255),
  organisation              VARCHAR2(255),
  department                VARCHAR2(255),
  user_number               NUMBER,
  proposal                  VARCHAR2(255),
  programme_access_ref      VARCHAR2(255),
  allocated_time            NUMBER,
  instrument_allocated      VARCHAR2(255),
  experiment_contact        VARCHAR2(255),
  experiment_contact_unno   NUMBER,
  science_programme         VARCHAR2(255),
  previous_experiment       VARCHAR2(255),
  abstract                  VARCHAR2(4000),
  mod_user                  VARCHAR2(30) DEFAULT USER NOT NULL,
  mod_time                  TIMESTAMP DEFAULT systimestamp NOT NULL,
  CONSTRAINT approved_proposals_pk PRIMARY KEY(rbno)
);


COMMENT ON COLUMN approved_proposals.title       IS 'Proposal Title';
COMMENT ON COLUMN approved_proposals.rbno        IS 'Proposal Reference Number';
COMMENT ON COLUMN approved_proposals.user_number IS 'PI User Number';


CREATE TRIGGER approved_proposals_biur
BEFORE INSERT OR UPDATE ON approved_proposals
FOR EACH ROW
BEGIN
  :NEW.mod_user := USER;
  :NEW.mod_time := systimestamp;
END;
/

GRANT SELECT, UPDATE ON approved_proposals TO useroffice_isis_feed;
