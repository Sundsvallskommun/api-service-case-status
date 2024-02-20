create table if not exists MapCaseTypeEnums
(
    ID   int(6) auto_increment
        primary key,
    ENUM varchar(255) not null,
    Text varchar(255) not null
)
    charset = latin1;

create table if not exists caseManagementStatus
(
    ID                   int auto_increment
        primary key,
    caseManagementID     varchar(30)                           not null,
    caseManagementStatus varchar(250)                          not null,
    creationDate         timestamp default current_timestamp() not null,
    updateDate           timestamp default current_timestamp() not null on update current_timestamp(),
    constraint caseManagementID
        unique (caseManagementID)
)
    charset = latin1;

create table if not exists incidentStatus
(
    ID             int auto_increment
        primary key,
    incidentID     int(6)                                not null,
    incidentStatus varchar(30)                           not null,
    creationDate   timestamp default current_timestamp() not null,
    updateDate     timestamp default current_timestamp() not null on update current_timestamp(),
    constraint incidentID
        unique (incidentID)
)
    charset = latin1;

create table if not exists openeStatus
(
    ID           int auto_increment
        primary key,
    openeID      varchar(250)                          not null,
    openeStatus  varchar(250)                          not null,
    creationDate timestamp default current_timestamp() not null,
    updateDate   timestamp default current_timestamp() not null on update current_timestamp(),
    constraint openeID
        unique (openeID)
)
    charset = latin1;

create table if not exists refCaseManagementOpene
(
    ID               int auto_increment
        primary key,
    caseManagementPK int                                   not null,
    openePK          int                                   not null,
    creationDate     timestamp default current_timestamp() not null,
    updateDate       timestamp default current_timestamp() not null on update current_timestamp(),
    constraint caseManagementPK
        unique (caseManagementPK, openePK),
    constraint refCaseManagementOpene_ibfk_1
        foreign key (caseManagementPK) references caseManagementStatus (ID),
    constraint refCaseManagementOpene_ibfk_2
        foreign key (openePK) references openeStatus (ID)
)
    charset = latin1;

create index if not exists openePK
    on refCaseManagementOpene (openePK);

create table if not exists refIncidentOpene
(
    ID           int auto_increment
        primary key,
    incidentPK   int                                   not null,
    openePK      int                                   not null,
    creationDate timestamp default current_timestamp() not null,
    updateDate   timestamp default current_timestamp() not null on update current_timestamp(),
    constraint incidentPK
        unique (incidentPK, openePK),
    constraint refIncidentOpene_ibfk_1
        foreign key (incidentPK) references incidentStatus (ID),
    constraint refIncidentOpene_ibfk_2
        foreign key (openePK) references openeStatus (ID)
)
    charset = latin1;

create index if not exists openePK
    on refIncidentOpene (openePK);

drop table if exists Companies;
create table if not exists Companies
(
    flowInstanceID     varchar(30)  not null,
    organisationNumber varchar(30)  not null,
    familyID           varchar(30)  not null,
    status             varchar(100) not null,
    errandType         varchar(100) not null,
    contentType        varchar(100) not null,
    firstSubmitted     varchar(30)  not null,
    lastStatusChange   varchar(30)  not null,
    sysStartTime       timestamp(6) generated always as ROW START,
    sysEndTime         timestamp(6) generated always as ROW END,
    primary key (flowInstanceID, sysEndTime),
    period for system_time(sysStartTime, sysEndTime)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = latin1
    WITH SYSTEM VERSIONING;

drop table if exists ETjanster;

drop table if exists Private;
create table if not exists Private
(
    flowInstanceID   varchar(30)  not null,
    personID         char(36)     not null,
    familyID         varchar(30)  not null,
    status           varchar(100) not null,
    errandType       varchar(100) not null,
    contentType      varchar(100) not null,
    firstSubmitted   varchar(30)  not null,
    lastStatusChange varchar(30)  not null,
    sysStartTime     timestamp(6) generated always as ROW START,
    sysEndTime       timestamp(6) generated always as ROW END,
    primary key (flowInstanceID, sysEndTime),
    period for system_time(sysStartTime, sysEndTime)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = latin1
    WITH SYSTEM VERSIONING;

drop table if exists StageCompanies;
create table StageCompanies
(
    flowInstanceID     varchar(30)  not null,
    organisationNumber varchar(30)  null,
    familyID           varchar(30)  not null,
    status             varchar(100) not null,
    errandType         varchar(100) not null,
    contentType        varchar(100) not null,
    firstSubmitted     varchar(30)  not null,
    lastStatusChange   varchar(30)  not null
)
    DEFAULT CHARSET = latin1;

drop table if exists StagePrivate;
create table StagePrivate
(
    flowInstanceID   varchar(30)  not null,
    personID         char(36)     not null,
    familyID         varchar(30)  not null,
    status           varchar(100) not null,
    errandType       varchar(100) not null,
    contentType      varchar(100) not null,
    firstSubmitted   varchar(30)  not null,
    lastStatusChange varchar(30)  not null
)
    DEFAULT CHARSET = latin1;

drop table if exists StageUnknown;
create table StageUnknown
(
    flowInstanceID   varchar(30)  not null,
    familyID         varchar(30)  not null,
    status           varchar(100) not null,
    errandType       varchar(100) not null,
    contentType      varchar(100) not null,
    firstSubmitted   varchar(30)  not null,
    lastStatusChange varchar(30)  not null
)
    DEFAULT CHARSET = latin1;

drop table if exists Unknown;
create table Unknown
(
    flowInstanceID   varchar(30)  not null,
    familyID         varchar(30)  not null,
    status           varchar(100) not null,
    errandType       varchar(100) not null,
    contentType      varchar(100) not null,
    firstSubmitted   varchar(30)  not null,
    lastStatusChange varchar(30)  not null,
    sysStartTime     timestamp(6) generated always as ROW START,
    sysEndTime       timestamp(6) generated always as ROW END,
    primary key (flowInstanceID, sysEndTime),
    period for system_time(sysStartTime, sysEndTime)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = latin1
    WITH SYSTEM VERSIONING;



