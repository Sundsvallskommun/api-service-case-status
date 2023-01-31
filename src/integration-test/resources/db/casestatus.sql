CREATE TABLE `MapCaseTypeEnums`
(
    `ID`   INT(6)       NOT NULL AUTO_INCREMENT,
    `ENUM` VARCHAR(255) NOT NULL,
    `Text` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`ID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE `caseManagementStatus`
(
    `ID`                   INT(11)      NOT NULL AUTO_INCREMENT,
    `caseManagementID`     VARCHAR(30)  NOT NULL,
    `caseManagementStatus` VARCHAR(250) NOT NULL,
    `creationDate`         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    `updateDate`           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
    PRIMARY KEY (`ID`),
    UNIQUE KEY `caseManagementID` (`caseManagementID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE `incidentStatus`
(
    `ID`             INT(11)     NOT NULL AUTO_INCREMENT,
    `incidentID`     INT(6)      NOT NULL,
    `incidentStatus` VARCHAR(30) NOT NULL,
    `creationDate`   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    `updateDate`     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
    PRIMARY KEY (`ID`),
    UNIQUE KEY `incidentID` (`incidentID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE `openeStatus`
(
    `ID`           INT(11)      NOT NULL AUTO_INCREMENT,
    `openeID`      VARCHAR(250) NOT NULL,
    `openeStatus`  VARCHAR(250) NOT NULL,
    `creationDate` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    `updateDate`   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
    PRIMARY KEY (`ID`),
    UNIQUE KEY `openeID` (`openeID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE `refCaseManagementOpene`
(
    `ID`               INT(11)   NOT NULL AUTO_INCREMENT,
    `caseManagementPK` INT(11)   NOT NULL,
    `openePK`          INT(11)   NOT NULL,
    `creationDate`     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    `updateDate`       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
    PRIMARY KEY (`ID`),
    UNIQUE KEY `caseManagementPK` (`caseManagementPK`, `openePK`),
    KEY `openePK` (`openePK`),
    CONSTRAINT `refCaseManagementOpene_ibfk_1` FOREIGN KEY (`caseManagementPK`) REFERENCES `caseManagementStatus` (`ID`),
    CONSTRAINT `refCaseManagementOpene_ibfk_2` FOREIGN KEY (`openePK`) REFERENCES `openeStatus` (`ID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE `refIncidentOpene`
(
    `ID`           INT(11)   NOT NULL AUTO_INCREMENT,
    `incidentPK`   INT(11)   NOT NULL,
    `openePK`      INT(11)   NOT NULL,
    `creationDate` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    `updateDate`   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
    PRIMARY KEY (`ID`),
    UNIQUE KEY `incidentPK` (`incidentPK`, `openePK`),
    KEY `openePK` (`openePK`),
    CONSTRAINT `refIncidentOpene_ibfk_1` FOREIGN KEY (`incidentPK`) REFERENCES `incidentStatus` (`ID`),
    CONSTRAINT `refIncidentOpene_ibfk_2` FOREIGN KEY (`openePK`) REFERENCES `openeStatus` (`ID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE OR REPLACE ALGORITHM = UNDEFINED VIEW `vStatusCaseManagementOpenE` AS
SELECT `cs`.`caseManagementID` AS `caseManagementID`,
       `os`.`openeID`          AS `openeID`
FROM ((`refCaseManagementOpene` `ref`
    JOIN `openeStatus` `os` ON
    (`ref`.`openePK` = `os`.`ID`))
    JOIN `caseManagementStatus` `cs` ON
    (`ref`.`caseManagementPK` = `cs`.`ID`));

CREATE OR REPLACE ALGORITHM = UNDEFINED VIEW `vStatusIncidentOpenE` AS
SELECT `incS`.`incidentID` AS `incidentID`,
       `os`.`openeID`      AS `openeID`
FROM ((`refIncidentOpene` `ref`
    JOIN `openeStatus` `os` ON
    (`ref`.`openePK` = `os`.`ID`))
    JOIN `incidentStatus` `incS` ON
    (`ref`.`incidentPK` = `incS`.`ID`));


CREATE TABLE `Companies`
(
    `FlowInstanceID`     varchar(30)  NOT NULL,
    `OrganisationNumber` varchar(30)  NOT NULL,
    `FamilyID`           varchar(30)  NOT NULL,
    `Status`             varchar(100) NOT NULL,
    `ErrandType`         varchar(100) NOT NULL,
    `ContentType`        varchar(100) NOT NULL,
    `FirstSubmitted`     varchar(30)  NOT NULL,
    `LastStatusChange`   varchar(30)  NOT NULL,
    `SysStartTime`       timestamp(6) GENERATED ALWAYS AS ROW START,
    `SysEndtime`         timestamp(6) GENERATED ALWAYS AS ROW END,
    PRIMARY KEY (`FlowInstanceID`, `SysEndtime`),
    PERIOD FOR SYSTEM_TIME (`SysStartTime`, `SysEndtime`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  WITH SYSTEM VERSIONING;

INSERT INTO `caseManagementStatus` (ID, caseManagementID, caseManagementStatus, creationDate,
                                    updateDate)
VALUES (1, 'ANM', 'Inskickat', '2021-09-27 17:12:56.0', '2021-09-27 17:12:56.0'),
       (2, 'ANSÖKAN', 'Inskickat', '2021-09-27 17:12:56.0', '2021-09-27 17:12:56.0'),
       (3, 'Kv2', 'Tilldelat för handläggning', '2021-09-27 17:12:56.0', '2022-05-20 07:23:26.0'),
       (4, 'UTSKICK', 'Under behandling', '2021-09-27 17:12:56.0', '2021-09-27 17:12:56.0'),
       (5, 'UNDER', 'Under behandling', '2021-09-27 17:12:56.0', '2021-09-27 17:12:56.0'),
       (6, 'KOMP', 'Komplettering behövs', '2021-09-27 17:12:56.0', '2022-05-20 07:23:27.0'),
       (7, 'KOMP1', 'Påminnelse om komplettering', '2021-09-27 17:12:56.0',
        '2022-05-20 07:23:27.0'),
       (8, 'KOMPL', 'Kompletering inkommen, behandling fortsätter', '2021-09-27 17:12:56.0',
        '2022-05-20 07:23:27.0'),
       (9, 'SLU', 'Klart', '2021-09-27 17:12:56.0', '2021-09-27 17:12:56.0'),
       (10, 'UAB', 'Klart', '2021-09-27 17:12:56.0', '2021-09-27 17:12:56.0'),
       (11, 'Avslutat', 'Ärendet arkiveras', '2021-09-27 17:12:56.0', '2021-09-27 17:12:56.0'),
       (12, 'Anmälan', 'Inskickat', '2022-02-18 12:46:51.0', '2022-02-18 12:46:52.0'),
       (13, 'Begäran om komplettering', 'Väntar på komplettering', '2022-02-18 12:54:31.0',
        '2022-02-18 12:54:32.0'),
       (14, 'Komplettering', 'Kompletterad', '2022-02-18 12:55:10.0', '2022-02-18 12:55:11.0'),
       (15, 'Nämndbeslut', 'Klart', '2022-02-24 09:38:41.0', '2022-02-24 09:38:42.0'),
       (16, 'KOMPBYGG', 'Komplettering inkommen, behandling fortsätter', '2022-05-20 07:20:02.0',
        '2022-05-20 07:45:09.0'),
       (17, 'KOMPTEK', 'Komplettering inkommen, behandling fortsätter', '2022-05-20 07:21:07.0',
        '2022-05-20 07:45:09.0'),
       (18, 'KOMPREV', 'Beslut finns, se separat information', '2022-05-20 07:21:07.0',
        '2022-05-20 07:23:27.0');

INSERT INTO `MapCaseTypeEnums` (ID, `ENUM`, `Text`)
VALUES (1, 'ANMALAN_ANDRING_AVLOPPSANLAGGNING', 'Anmälan om ändring för avloppsanläggning'),
       (2, 'ANMALAN_ANDRING_AVLOPPSANORDNING', 'Anmälan om ändring för avloppsanordning'),
       (3, 'ANMALAN_ATTEFALL', 'Anmälan attefall'),
       (4, 'ANMALAN_HALSOSKYDDSVERKSAMHET', 'Anmälan hälsoskyddsverksamhet'),
       (5, 'ANMALAN_INSTALLATION_VARMEPUMP', 'Anmälan om installation av värmepump'),
       (6, 'ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC',
        'Anmälan om installation för enskilt avlopp utan WC'),
       (7, 'ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP', 'Ansökan om tillstånd för enskilt avlopp'),
       (8, 'ANSOKAN_TILLSTAND_VARMEPUMP', 'Ansökan om tillstånd för värmepump'),
       (9, 'NYBYGGNAD_ANSOKAN_OM_BYGGLOV', 'Nybyggnad - Ansökan om bygglov'),
       (10, 'REGISTRERING_AV_LIVSMEDEL', 'Registrering av livsmedel');

INSERT INTO `incidentStatus` (incidentID, incidentStatus, creationDate, updateDate)
VALUES (1, 'Inskickat', '2021-09-27 17:15:38.0', '2021-09-27 17:15:38.0'),
       (2, 'Klart', '2021-09-27 17:15:38.0', '2021-09-27 17:15:38.0'),
       (3, 'Kompletterad', '2021-09-27 17:15:38.0', '2021-09-27 17:15:38.0'),
       (4, 'Sparat ärende', '2021-09-27 17:15:38.0', '2021-09-27 17:15:38.0'),
       (5, 'Under behandling', '2021-09-27 17:15:38.0', '2021-09-27 17:15:38.0'),
       (6, 'Väntar på komplettering', '2021-09-27 17:15:38.0', '2021-09-27 17:15:38.0'),
       (7, 'Ärendet arkiveras', '2021-09-27 17:15:38.0', '2021-09-27 17:15:38.0');

INSERT INTO `openeStatus` (ID, openeID, openeStatus, creationDate, updateDate)
VALUES (1, 'Inskickat', 'Inskickat', '2021-09-27 17:17:51.0', '2021-09-27 17:17:51.0'),
       (2, 'Klart', 'Klart', '2021-09-27 17:17:51.0', '2021-09-27 17:17:51.0'),
       (3, 'Kompletterad', 'Kompletterad', '2021-09-27 17:17:51.0', '2021-09-27 17:17:51.0'),
       (4, 'Sparat ärende', 'Sparat ärende', '2021-09-27 17:17:51.0', '2021-09-27 17:17:51.0'),
       (5, 'Under behandling', 'Under behandling', '2021-09-27 17:17:51.0',
        '2021-09-27 17:17:51.0'),
       (6, 'Väntar på komplettering', 'Väntar på komplettering', '2021-09-27 17:17:51.0',
        '2021-09-27 17:17:51.0'),
       (7, 'Ärendet arkiveras', 'Ärendet arkiveras', '2021-09-27 17:17:51.0',
        '2021-09-27 17:17:51.0'),
       (8, 'Tilldelat för handläggning', 'Tilldelat för handläggning', '2022-05-20 07:08:28.0',
        '2022-05-20 07:08:28.0'),
       (15, 'Komplettering behövs', 'Komplettering behövs', '2022-05-20 07:14:32.0',
        '2022-05-20 07:14:32.0'),
       (16, 'Påminnelse om komplettering', 'Påminnelse om komplettering', '2022-05-20 07:14:32.0',
        '2022-05-20 07:14:32.0'),
       (17, 'Beslut finns, se separat information', 'Beslut finns, se separat information',
        '2022-05-20 07:14:32.0',
        '2022-05-20 07:14:32.0'),
       (18, 'Komplettering inkommen, behandling fortsätter',
        'Komplettering inkommen, behandling fortsätter',
        '2022-05-20 07:14:32.0', '2022-05-20 07:45:20.0');

INSERT INTO `refCaseManagementOpene` (caseManagementPK, openePK, creationDate, updateDate)
VALUES (1, 1, '2021-09-27 17:22:31.0', '2021-09-27 17:22:31.0'),
       (2, 1, '2021-09-27 17:22:31.0', '2021-09-27 17:22:31.0'),
       (3, 8, '2021-09-27 17:22:31.0', '2022-05-20 07:20:20.0'),
       (4, 5, '2021-09-27 17:22:31.0', '2021-09-27 17:22:31.0'),
       (5, 5, '2021-09-27 17:22:31.0', '2021-09-27 17:22:31.0'),
       (6, 15, '2021-09-27 17:22:31.0', '2022-05-20 07:32:32.0'),
       (7, 16, '2021-09-27 17:22:31.0', '2022-05-20 07:33:56.0'),
       (8, 18, '2021-09-27 17:22:31.0', '2022-05-20 07:20:20.0'),
       (9, 2, '2021-09-27 17:22:31.0', '2021-09-27 17:22:31.0'),
       (10, 2, '2021-09-27 17:22:31.0', '2021-09-27 17:22:31.0'),
       (11, 7, '2021-09-27 17:22:31.0', '2021-09-27 17:22:31.0'),
       (12, 1, '2022-02-18 12:47:46.0', '2022-02-18 12:47:47.0'),
       (13, 6, '2022-02-18 12:54:50.0', '2022-02-18 12:54:50.0'),
       (14, 3, '2022-02-18 12:55:26.0', '2022-02-18 12:55:27.0'),
       (15, 2, '2022-02-24 09:39:04.0', '2022-02-24 09:39:04.0'),
       (16, 18, '2022-05-20 07:20:20.0', '2022-05-20 07:20:20.0'),
       (17, 18, '2022-05-20 07:21:55.0', '2022-05-20 07:21:55.0'),
       (18, 17, '2022-05-20 07:21:55.0', '2022-05-20 07:21:55.0');

INSERT INTO `refIncidentOpene` (incidentPK, openePK, creationDate, updateDate)
VALUES (1, 1, '2021-09-27 17:25:48.0', '2021-09-27 17:25:48.0'),
       (2, 2, '2021-09-27 17:25:48.0', '2021-09-27 17:25:48.0'),
       (3, 3, '2021-09-27 17:25:48.0', '2021-09-27 17:25:48.0'),
       (4, 4, '2021-09-27 17:25:48.0', '2021-09-27 17:25:48.0'),
       (5, 5, '2021-09-27 17:25:48.0', '2021-09-27 17:25:48.0'),
       (6, 6, '2021-09-27 17:25:48.0', '2021-09-27 17:25:48.0'),
       (7, 7, '2021-09-27 17:25:48.0', '2021-09-27 17:25:48.0');



INSERT INTO `Companies` (FlowInstanceID, OrganisationNumber, FamilyID, Status, ErrandType,
                         ContentType, FirstSubmitted,
                         LastStatusChange, SysStartTime, SysEndtime)
VALUES ('100757', '1234561235', '439', 'Inskickat', 'Ansökan - strandskyddsdispens', 'SUBMITTED',
        '2022-05-11 14:24',
        '2022-05-11 14:24', DEFAULT, DEFAULT),
       ('89767', '1234561233', '253', 'Inskickat', 'Beställa nybyggnadskarta', 'SUBMITTED',
        '2022-02-09 09:51',
        '2022-02-09 09:51', DEFAULT, DEFAULT);

