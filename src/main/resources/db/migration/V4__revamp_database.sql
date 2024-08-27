SET @@system_versioning_alter_history = 1;

DROP TABLE IF EXISTS StageCompanies, StagePrivate, StageUnknown;

ALTER TABLE Companies
    DROP SYSTEM VERSIONING,
    DROP COLUMN sysStartTime,
    DROP COLUMN sysEndTime,
    CHANGE flowInstanceID flow_instance_id VARCHAR(30) NOT NULL,
    CHANGE organisationNumber organisation_number VARCHAR(30) NOT NULL,
    CHANGE familyID family_id VARCHAR(30) NOT NULL,
    CHANGE errandType errand_type VARCHAR(100) NOT NULL,
    CHANGE contentType content_type VARCHAR(100) NOT NULL,
    CHANGE firstSubmitted first_submitted VARCHAR(30) NOT NULL,
    CHANGE lastStatusChange last_status_change VARCHAR(30) NOT NULL,
    ADD COLUMN municipality_id VARCHAR(255);

ALTER TABLE Private
    DROP SYSTEM VERSIONING,
    DROP COLUMN sysStartTime,
    DROP COLUMN sysEndTime,
    CHANGE flowInstanceID flow_instance_id VARCHAR(30) NOT NULL,
    CHANGE personID person_id VARCHAR(36) NOT NULL,
    CHANGE familyID family_id VARCHAR(30) NOT NULL,
    CHANGE errandType errand_type VARCHAR(100) NOT NULL,
    CHANGE contentType content_type VARCHAR(100) NOT NULL,
    CHANGE firstSubmitted first_submitted VARCHAR(30) NOT NULL,
    CHANGE lastStatusChange last_status_change VARCHAR(30) NOT NULL,
    ADD COLUMN municipality_id VARCHAR(255);


ALTER TABLE Unknown
    DROP SYSTEM VERSIONING,
    DROP COLUMN sysStartTime,
    DROP COLUMN sysEndTime,
    CHANGE flowInstanceID flow_instance_id VARCHAR(30) NOT NULL,
    CHANGE familyID family_id VARCHAR(30) NOT NULL,
    CHANGE errandType errand_type VARCHAR(100) NOT NULL,
    CHANGE contentType content_type VARCHAR(100) NOT NULL,
    CHANGE firstSubmitted first_submitted VARCHAR(30) NOT NULL,
    CHANGE lastStatusChange last_status_change VARCHAR(30) NOT NULL,
    ADD COLUMN municipality_id VARCHAR(255);


alter table CaseManagementStatus
    change ID id int auto_increment,
    change caseManagementID case_management_id varchar(30) not null,
    change caseManagementStatus case_management_status varchar(250) not null,
    change creationDate creation_date timestamp default current_timestamp() not null,
    change updateDate update_date timestamp default current_timestamp() not null on update current_timestamp();

ALTER TABLE MapCaseTypeEnums
    change ID id int auto_increment,
    CHANGE ENUM enum VARCHAR(255) NOT NULL,
    CHANGE Text description VARCHAR(255) NOT NULL,
    ADD COLUMN municipality_id VARCHAR(255);

ALTER TABLE OpenEStatus
    change ID id int auto_increment,
    change openEID opene_id varchar(250) not null,
    change openEStatus opene_status varchar(250) not null,
    change creationDate creation_date timestamp default current_timestamp() not null,
    change updateDate update_date timestamp default current_timestamp() not null on update current_timestamp();

ALTER TABLE IncidentStatus
    change ID id int auto_increment,
    change incidentID incident_id int(6) not null,
    change incidentStatus incident_status varchar(250) not null,
    change creationDate creation_date timestamp default current_timestamp() not null,
    change updateDate update_date timestamp default current_timestamp() not null on update current_timestamp();

ALTER TABLE RefCaseManagementOpenE
    change ID id int auto_increment,
    change caseManagementPK case_management_pk int not null,
    change openePK opene_pk int not null,
    change creationDate creation_date timestamp default current_timestamp() not null,
    change updateDate update_date timestamp default current_timestamp() not null on update current_timestamp();

ALTER TABLE RefIncidentOpenE
    change ID id int auto_increment,
    change incidentPK incident_pk int not null,
    change openePK opene_pk int not null,
    change creationDate creation_date timestamp default current_timestamp() not null,
    change updateDate update_date timestamp default current_timestamp() not null on update current_timestamp();

RENAME TABLE Companies TO company, Private TO private, Unknown TO unknown, MapCaseTypeEnums TO case_type,
    OpenEStatus to opene_status, CaseManagementStatus to case_management_status, IncidentStatus to incident_status,
    RefCaseManagementOpenE to ref_case_management_opene, RefIncidentOpenE to ref_incident_opene;

DROP VIEW IF EXISTS vStatusCaseManagementOpenE, vStatusIncidentOpenE;

CREATE OR REPLACE ALGORITHM = UNDEFINED VIEW v_status_case_management_opene AS
SELECT cs.case_management_id AS case_management_id, os.opene_id AS opene_id
FROM ref_case_management_opene ref
         JOIN opene_status os ON ref.opene_pk = os.id
         JOIN case_management_status cs ON ref.case_management_pk = cs.id;

CREATE OR REPLACE ALGORITHM = UNDEFINED VIEW v_status_incident_opene AS
SELECT incS.incident_id AS incident_id, os.opene_id AS opene_id
FROM ref_incident_opene ref
         JOIN opene_status os ON ref.opene_pk = os.id
         JOIN incident_status incS ON ref.incident_pk = incS.id;


create index idx_company_organisation_number
    on company (organisation_number);

create index idx_company_municipality_id
    on company (municipality_id);

create index idx_private_person_id
    on private (person_id);

create index idx_private_municipality_id
    on private (municipality_id);

create index idx_unknown_municipality_id
    on unknown (municipality_id);

SET @@system_versioning_alter_history = 0;
