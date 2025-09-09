
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

    create table execution_information (
        municipality_id varchar(4) not null,
        last_successful_execution datetime(6),
        primary key (municipality_id)
    ) engine=InnoDB;

    create table statuses (
        id integer not null auto_increment,
        case_management_display_name varchar(255),
        case_management_status varchar(255),
        external_display_name varchar(255),
        external_status varchar(255),
        oep_display_name varchar(255),
        oep_status varchar(255),
        support_management_display_name varchar(255),
        support_management_status varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table support_management_status (
        id integer not null auto_increment,
        generic_status varchar(255),
        system_status varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table v_status_case_management_opene (
        case_management_id varchar(255) not null,
        opene_id varchar(255),
        primary key (case_management_id)
    ) engine=InnoDB;

    create index idx_flow_instance_id_municipality_id 
       on case_status (flow_instance_id, municipality_id);

    create index idx_organisation_number_municipality_id 
       on case_status (organisation_number, municipality_id);

    create index idx_person_id_municipality_id 
       on case_status (person_id, municipality_id);

    create index idx_enum_value_municipality_id
       on case_type (enum, municipality_id);

	create index idx_oep_status
		on statuses (oep_status);

	create index idx_support_management_status
		on statuses (support_management_status);

	create index idx_case_management_status
		on statuses (case_management_status);

    create index idx_system_status
       on support_management_status (system_status);
