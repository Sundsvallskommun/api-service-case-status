
    create table case_status (
        content_type varchar(255),
        errand_type varchar(255),
        family_id varchar(255),
        first_submitted varchar(255),
        flow_instance_id varchar(255) not null,
        last_status_change varchar(255),
        municipality_id varchar(255),
        organisation_number varchar(255),
        person_id varchar(255),
        status varchar(255),
        primary key (flow_instance_id)
    ) engine=InnoDB;

    create table case_type (
        id integer not null,
        description varchar(255),
        enum varchar(255),
        municipality_id varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table v_status_case_management_opene (
        case_management_id varchar(255) not null,
        opene_id varchar(255),
        primary key (case_management_id)
    ) engine=InnoDB;
