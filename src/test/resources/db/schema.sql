
    create table case_type (
        id integer not null,
        description varchar(255),
        enum varchar(255),
        municipality_id varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table company (
        content_type varchar(255),
        errand_type varchar(255),
        family_id varchar(255),
        first_submitted varchar(255),
        flow_instance_id varchar(255) not null,
        last_status_change varchar(255),
        municipality_id varchar(255),
        organisation_number varchar(255),
        status varchar(255),
        primary key (flow_instance_id)
    ) engine=InnoDB;

    create table private (
        content_type varchar(255),
        errand_type varchar(255),
        family_id varchar(255),
        first_submitted varchar(255),
        flow_instance_id varchar(255) not null,
        last_status_change varchar(255),
        municipality_id varchar(255),
        person_id varchar(255),
        status varchar(255),
        primary key (flow_instance_id)
    ) engine=InnoDB;

    create table unknown (
        content_type varchar(255),
        errand_type varchar(255),
        family_id varchar(255),
        first_submitted varchar(255),
        flow_instance_id varchar(255) not null,
        last_status_change varchar(255),
        municipality_id varchar(255),
        status varchar(255),
        primary key (flow_instance_id)
    ) engine=InnoDB;

    create table v_status_case_management_opene (
        case_management_id varchar(255) not null,
        opene_id varchar(255),
        primary key (case_management_id)
    ) engine=InnoDB;

    create table v_status_incident_opene (
        incident_id integer not null,
        opene_id varchar(255),
        primary key (incident_id)
    ) engine=InnoDB;

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
