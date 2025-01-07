create index idx_flow_instance_id_municipality_id
    on case_status (flow_instance_id, municipality_id);

create index idx_organisation_number_municipality_id
    on case_status (organisation_number, municipality_id);

create index idx_person_id_municipality_id
    on case_status (person_id, municipality_id);

create index idx_enum_value_municipality_id
    on case_type (enum, municipality_id);
