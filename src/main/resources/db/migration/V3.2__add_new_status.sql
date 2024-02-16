INSERT INTO CaseManagementStatus(ID, caseManagementID, caseManagementStatus, creationDate, updateDate)
VALUES (28, 'Under granskning', 'Under granskning', '2024-02-15 16:18:01', '2024-02-15 16:18:02'),
       (29, 'Ärendet avslutat', 'Ärendet avslutat', '2024-02-15 16:18:03', '2024-02-15 16:18:04');

INSERT INTO RefCaseManagementOpenE (ID, caseManagementPK, openePK, creationDate, updateDate)
VALUES (28, 28, 5, '2024-02-15 16:18:05', '2024-02-15 16:18:06'),
       (29, 29, 2, '2024-02-15 16:18:07', '2024-02-15 16:18:08');