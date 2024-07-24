DROP VIEW IF EXISTS `vStatusCaseManagementOpenE`;
DROP VIEW IF EXISTS `vStatusIncidentOpenE`;

CREATE OR REPLACE ALGORITHM = UNDEFINED VIEW `vStatusCaseManagementOpenE` AS
select `cs`.`caseManagementID` AS `caseManagementID`,
       `os`.`openEID`          AS `openeID`
from ((`RefCaseManagementOpenE` `ref`
    join `OpenEStatus` `os` on
    (`ref`.`openEPK` = `os`.`ID`))
    join `CaseManagementStatus` `cs` on
    (`ref`.`caseManagementPK` = `cs`.`ID`));

CREATE OR REPLACE ALGORITHM = UNDEFINED VIEW `vStatusIncidentOpenE` AS
select `incS`.`incidentID` AS `incidentID`,
       `os`.`openEID`      AS `openEID`
from ((`RefIncidentOpenE` `ref`
    join `OpenEStatus` `os` on
    (`ref`.`openEPK` = `os`.`ID`))
    join `IncidentStatus` `incS` on
    (`ref`.`incidentPK` = `incS`.`ID`));