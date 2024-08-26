SET @@system_versioning_alter_history = 1;

ALTER TABLE StageUnknown
    ADD COLUMN municipalityId VARCHAR(255);

ALTER TABLE StageCompanies
    ADD COLUMN municipalityId VARCHAR(255);

ALTER TABLE StagePrivate
    ADD COLUMN municipalityId VARCHAR(255);

ALTER TABLE Companies
    ADD COLUMN municipalityId VARCHAR(255);


ALTER TABLE Private
    ADD COLUMN municipalityId VARCHAR(255);

ALTER TABLE Unknown
    ADD COLUMN municipalityId VARCHAR(255);

SET @@system_versioning_alter_history = 0;
