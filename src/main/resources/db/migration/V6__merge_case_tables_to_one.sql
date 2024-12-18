CREATE TABLE IF NOT EXISTS case_status
(
    flow_instance_id    VARCHAR(255) NOT NULL,
    family_id           VARCHAR(255),
    status              VARCHAR(255),
    errand_type         VARCHAR(255),
    content_type        VARCHAR(255),
    first_submitted     VARCHAR(255),
    last_status_change  VARCHAR(255),
    municipality_id     VARCHAR(255),
    organisation_number VARCHAR(255),
    person_id           VARCHAR(255),
    PRIMARY KEY (flow_instance_id)
) ENGINE = InnoDB;

INSERT IGNORE INTO case_status (flow_instance_id, family_id, status, errand_type, content_type, first_submitted,
                                last_status_change, municipality_id, person_id)
SELECT flow_instance_id,
       family_id,
       status,
       errand_type,
       content_type,
       first_submitted,
       last_status_change,
       municipality_id,
       person_id
FROM private;

INSERT IGNORE INTO case_status (flow_instance_id, family_id, status, errand_type, content_type, first_submitted,
                                last_status_change, municipality_id)
SELECT flow_instance_id,
       family_id,
       status,
       errand_type,
       content_type,
       first_submitted,
       last_status_change,
       municipality_id
FROM unknown;

INSERT IGNORE INTO case_status (flow_instance_id, family_id, status, errand_type, content_type, first_submitted,
                                last_status_change, municipality_id, organisation_number)
SELECT flow_instance_id,
       family_id,
       status,
       errand_type,
       content_type,
       first_submitted,
       last_status_change,
       municipality_id,
       organisation_number
FROM company;

DROP TABLE IF EXISTS private;
DROP TABLE IF EXISTS unknown;
DROP TABLE IF EXISTS company;

CREATE INDEX IF NOT EXISTS idx_case_organisation_number ON case_status (organisation_number);
CREATE INDEX IF NOT EXISTS idx_case_person_id ON case_status (person_id);
CREATE INDEX IF NOT EXISTS idx_case_municipality_id ON case_status (municipality_id);
