#################### CaseStatus ####################

alter table `ms-casestatus`.MapCaseTypeEnums
    change ENUM Enum varchar(255) not null;

rename table `ms-casestatus`.caseManagementStatus to CaseManagementStatus;
rename table `ms-casestatus`.incidentStatus to IncidentStatus;
rename table `ms-casestatus`.openeStatus to OpenEStatus;
rename table `ms-casestatus`.refCaseManagementOpene to RefCaseManagementOpenE;
rename table `ms-casestatus`.refIncidentOpene to RefIncidentOpenE;


CREATE OR REPLACE ALGORITHM = UNDEFINED VIEW `ms-casestatus`.`vStatusCaseManagementOpenE` AS
select `cs`.`caseManagementID` AS `caseManagementID`,
       `os`.`openEID`          AS `openeID`
from ((`ms-casestatus`.`RefCaseManagementOpenE` `ref`
    join `ms-casestatus`.`OpenEStatus` `os` on
    (`ref`.`openEPK` = `os`.`ID`))
    join `ms-casestatus`.`CaseManagementStatus` `cs` on
    (`ref`.`caseManagementPK` = `cs`.`ID`));

CREATE OR REPLACE ALGORITHM = UNDEFINED VIEW `ms-casestatus`.`vStatusIncidentOpenE` AS
select `incS`.`incidentID` AS `incidentID`,
       `os`.`openEID`      AS `openEID`
from ((`ms-casestatus`.`RefIncidentOpenE` `ref`
    join `ms-casestatus`.`OpenEStatus` `os` on
    (`ref`.`openEPK` = `os`.`ID`))
    join `ms-casestatus`.`IncidentStatus` `incS` on
    (`ref`.`incidentPK` = `incS`.`ID`));