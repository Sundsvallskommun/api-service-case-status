alter table if exists execution_information
    drop primary key;

alter table if exists execution_information
    drop column if exists family_id;

alter table if exists execution_information
    add primary key (municipality_id);
