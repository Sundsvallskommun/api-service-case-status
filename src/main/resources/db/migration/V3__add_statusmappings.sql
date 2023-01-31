ALTER TABLE CaseManagementStatus
    add column if not exists description varchar(255) null;

INSERT INTO CaseManagementStatus (ID, caseManagementID, caseManagementStatus,
                                  creationDate, updateDate)
VALUES (1, 'ANM', 'Inskickat', '2021-09-27 19:12:56', '2021-09-27 19:12:56')
     , (2, 'ANSÖKAN', 'Inskickat', '2021-09-27 19:12:56', '2021-09-27 19:12:56')
     , (3, 'Kv2', 'Tilldelat för handläggning', '2021-09-27 19:12:56', '2022-05-20 09:23:26')
     , (4, 'UTSKICK', 'Under behandling', '2021-09-27 19:12:56', '2021-09-27 19:12:56')
     , (5, 'UNDER', 'Under behandling', '2021-09-27 19:12:56', '2021-09-27 19:12:56')
     , (6, 'KOMP', 'Komplettering behövs', '2021-09-27 19:12:56', '2022-05-20 09:23:27')
     , (7, 'KOMP1', 'Påminnelse om komplettering', '2021-09-27 19:12:56', '2022-05-20 09:23:27')
     , (8, 'KOMPL', 'Kompletering inkommen, behandling fortsätter', '2021-09-27 19:12:56',
        '2022-05-20 09:23:27')
     , (9, 'SLU', 'Klart', '2021-09-27 19:12:56', '2021-09-27 19:12:56')
     , (10, 'UAB', 'Klart', '2021-09-27 19:12:56', '2021-09-27 19:12:56')
     , (11, 'Avslutat', 'Ärendet arkiveras', '2021-09-27 19:12:56', '2021-09-27 19:12:56')
     , (12, 'Anmälan', 'Inskickat', '2022-02-18 13:46:51', '2022-02-18 13:46:52')
     , (13, 'Begäran om komplettering', 'Väntar på komplettering', '2022-02-18 13:54:31',
        '2022-02-18 13:54:32')
     , (14, 'Komplettering', 'Kompletterad', '2022-02-18 13:55:10', '2022-02-18 13:55:11')
     , (15, 'Nämndbeslut', 'Klart', '2022-02-24 10:38:41', '2022-02-24 10:38:42')
     , (16, 'KOMPBYGG', 'Komplettering inkommen, behandling fortsätter', '2022-05-20 09:20:02',
        '2022-05-20 09:45:09')
     , (17, 'KOMPTEK', 'Komplettering inkommen, behandling fortsätter', '2022-05-20 09:21:07',
        '2022-05-20 09:45:09')
     , (18, 'KOMPREV', 'Beslut finns, se separat information', '2022-05-20 09:21:07',
        '2022-05-20 09:23:27')
     , (19, 'Ärende inkommit', 'Ärende inkommit', '2022-11-08 15:20:19', '2022-11-08 15:20:19')
     , (20, 'Väntar på komplettering', 'Väntar på komplettering', '2022-11-08 15:20:19',
        '2022-11-08 15:20:19')
     , (21, 'Komplettering inkommen', 'Komplettering inkommen', '2022-11-08 15:20:19',
        '2022-11-08 15:20:19')
     , (22, 'Under utredning', 'Under utredning', '2022-11-08 15:20:19', '2022-11-08 15:20:19')
     , (23, 'Under Beslut', 'Under Beslut', '2022-11-08 15:20:19', '2022-11-08 15:20:19')
     , (24, 'Handläggare tilldelad', 'Handläggare tilldelad', '2022-11-08 15:20:19',
        '2022-11-08 15:20:19')
     , (25, 'Beslutad', 'Beslutad', '2022-11-08 15:20:19', '2022-11-08 15:20:19')
     , (26, 'Beslut verkställt', 'Beslut verkställt', '2023-01-27 09:08:11', '2023-01-27 09:08:11')
     , (27, 'Beslut avvisas', 'Beslut avvisas', '2023-01-27 09:08:11', '2023-01-27 09:08:11')
ON DUPLICATE KEY UPDATE ID=ID;

INSERT INTO IncidentStatus (ID, incidentID, incidentStatus, creationDate, updateDate)
VALUES (1, 1, 'Inskickat', '2021-09-27 19:15:38', '2021-09-27 19:15:38')
     , (2, 2, 'Klart', '2021-09-27 19:15:38', '2021-09-27 19:15:38')
     , (3, 3, 'Kompletterad', '2021-09-27 19:15:38', '2021-09-27 19:15:38')
     , (4, 4, 'Sparat ärende', '2021-09-27 19:15:38', '2021-09-27 19:15:38')
     , (5, 5, 'Under behandling', '2021-09-27 19:15:38', '2021-09-27 19:15:38')
     , (6, 6, 'Väntar på komplettering', '2021-09-27 19:15:38', '2021-09-27 19:15:38')
     , (7, 7, 'Ärendet arkiveras', '2021-09-27 19:15:38', '2021-09-27 19:15:38')
ON DUPLICATE KEY UPDATE ID=ID;


INSERT INTO OpenEStatus (ID, openeID, openeStatus, creationDate, updateDate)
VALUES (1, 'Inskickat', 'Inskickat', '2021-09-27 19:17:51', '2021-09-27 19:17:51')
     , (2, 'Klart', 'Klart', '2021-09-27 19:17:51', '2021-09-27 19:17:51')
     , (3, 'Kompletterad', 'Kompletterad', '2021-09-27 19:17:51', '2021-09-27 19:17:51')
     , (4, 'Sparat ärende', 'Sparat ärende', '2021-09-27 19:17:51', '2021-09-27 19:17:51')
     , (5, 'Under behandling', 'Under behandling', '2021-09-27 19:17:51', '2021-09-27 19:17:51')
     , (6, 'Väntar på komplettering', 'Väntar på komplettering', '2021-09-27 19:17:51',
        '2021-09-27 19:17:51')
     , (7, 'Ärendet arkiveras', 'Ärendet arkiveras', '2021-09-27 19:17:51', '2021-09-27 19:17:51')
     , (8, 'Tilldelat för handläggning', 'Tilldelat för handläggning', '2022-05-20 09:08:28',
        '2022-05-20 09:08:28')
     , (15, 'Komplettering behövs', 'Komplettering behövs', '2022-05-20 09:14:32',
        '2022-05-20 09:14:32')
     , (16, 'Påminnelse om komplettering', 'Påminnelse om komplettering', '2022-05-20 09:14:32',
        '2022-05-20 09:14:32')
     , (17, 'Beslut finns, se separat information', 'Beslut finns, se separat information',
        '2022-05-20 09:14:32', '2022-05-20 09:14:32')
     , (18, 'Komplettering inkommen, behandling fortsätter',
        'Komplettering inkommen, behandling fortsätter', '2022-05-20 09:14:32',
        '2022-05-20 09:45:20')
ON DUPLICATE KEY UPDATE ID=ID;


INSERT INTO RefCaseManagementOpenE (ID, caseManagementPK, openePK, creationDate, updateDate)
VALUES (1, 1, 1, '2021-09-27 19:22:31', '2021-09-27 19:22:31')
     , (2, 2, 1, '2021-09-27 19:22:31', '2021-09-27 19:22:31')
     , (3, 3, 8, '2021-09-27 19:22:31', '2022-05-20 09:20:20')
     , (4, 4, 5, '2021-09-27 19:22:31', '2021-09-27 19:22:31')
     , (5, 5, 5, '2021-09-27 19:22:31', '2021-09-27 19:22:31')
     , (6, 6, 15, '2021-09-27 19:22:31', '2022-05-20 09:32:32')
     , (7, 7, 16, '2021-09-27 19:22:31', '2022-05-20 09:33:56')
     , (8, 8, 18, '2021-09-27 19:22:31', '2022-05-20 09:20:20')
     , (9, 9, 2, '2021-09-27 19:22:31', '2021-09-27 19:22:31')
     , (10, 10, 2, '2021-09-27 19:22:31', '2021-09-27 19:22:31')
     , (11, 11, 7, '2021-09-27 19:22:31', '2021-09-27 19:22:31')
     , (12, 12, 1, '2022-02-18 13:47:46', '2022-02-18 13:47:47')
     , (13, 13, 6, '2022-02-18 13:54:50', '2022-02-18 13:54:50')
     , (14, 14, 3, '2022-02-18 13:55:26', '2022-02-18 13:55:27')
     , (15, 15, 2, '2022-02-24 10:39:04', '2022-02-24 10:39:04')
     , (16, 16, 18, '2022-05-20 09:20:20', '2022-05-20 09:20:20')
     , (17, 17, 18, '2022-05-20 09:21:55', '2022-05-20 09:21:55')
     , (18, 18, 17, '2022-05-20 09:21:55', '2022-05-20 09:21:55')
     , (19, 19, 1, '2022-11-08 15:23:29', '2022-11-08 15:23:29')
     , (20, 20, 6, '2022-11-08 15:23:29', '2022-11-08 15:23:29')
     , (21, 21, 3, '2022-11-08 15:23:29', '2022-11-08 15:23:29')
     , (22, 22, 5, '2022-11-08 15:23:29', '2022-11-08 15:23:29')
     , (23, 23, 5, '2022-11-08 15:23:29', '2022-11-08 15:23:29')
     , (24, 24, 5, '2022-11-08 15:23:29', '2022-11-08 15:23:29')
     , (25, 25, 2, '2022-11-08 15:23:29', '2022-11-08 15:23:29')
     , (26, 26, 2, '2023-01-27 09:09:02', '2023-01-27 09:09:02')
     , (27, 27, 2, '2023-01-30 10:18:48', '2023-01-30 10:18:59')
ON DUPLICATE KEY UPDATE ID=ID;


INSERT INTO RefIncidentOpenE (ID, incidentPK, openePK, creationDate, updateDate)
VALUES (1, 1, 1, '2021-09-27 19:25:48', '2021-09-27 19:25:48')
     , (2, 2, 2, '2021-09-27 19:25:48', '2021-09-27 19:25:48')
     , (3, 3, 3, '2021-09-27 19:25:48', '2021-09-27 19:25:48')
     , (4, 4, 4, '2021-09-27 19:25:48', '2021-09-27 19:25:48')
     , (5, 5, 5, '2021-09-27 19:25:48', '2021-09-27 19:25:48')
     , (6, 6, 6, '2021-09-27 19:25:48', '2021-09-27 19:25:48')
     , (7, 7, 7, '2021-09-27 19:25:48', '2021-09-27 19:25:48')
ON DUPLICATE KEY UPDATE ID=ID;