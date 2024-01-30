
SET @@system_versioning_alter_history = 1;
ALTER TABLE Companies
    DROP SYSTEM VERSIONING,
    DROP COLUMN sysStartTime,
    DROP COLUMN sysEndTime;

TRUNCATE table Companies;

ALTER TABLE Companies
    ADD SYSTEM VERSIONING,
    ADD COLUMN sysStartTime TIMESTAMP(6) GENERATED ALWAYS AS ROW START,
    ADD COLUMN sysEndTime TIMESTAMP(6) GENERATED ALWAYS AS ROW END,
    ADD PERIOD FOR SYSTEM_TIME(sysStartTime, sysEndTime);

