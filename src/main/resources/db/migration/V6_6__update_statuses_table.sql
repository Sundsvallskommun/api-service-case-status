TRUNCATE TABLE statuses;

INSERT INTO statuses (case_management_status, case_management_display_name, support_management_status, support_management_display_name, oep_status, oep_display_name, external_status, external_display_name)
VALUES
	 ('ANM',NULL,NULL,NULL,'Inskickat','Inskickat',NULL,NULL),
	 ('ANSÖKAN',NULL,NULL,NULL,'Inskickat','Inskickat',NULL,NULL),
	 ('Kv2',NULL,NULL,NULL,'Tilldelat för handläggning','Tilldelat för handläggning',NULL,NULL),
	 ('UTSKICK',NULL,NULL,NULL,'Under behandling','Under behandling',NULL,NULL),
	 ('UNDER',NULL,NULL,NULL,'Under behandling','Under behandling',NULL,NULL),
	 ('KOMP',NULL,NULL,NULL,'Komplettering behövs','Komplettering behövs',NULL,NULL),
	 ('KOMP1',NULL,NULL,NULL,'Påminnelse om komplettering','Påminnelse om komplettering',NULL,NULL),
	 ('KOMPL',NULL,NULL,NULL,'Kompletering inkommen, behandling fortsätter','Kompletering inkommen, behandling fortsätter',NULL,NULL),
	 ('SLU',NULL,NULL,NULL,'Klart','Klart',NULL,NULL),
	 ('UAB',NULL,NULL,NULL,'Klart','Klart',NULL,NULL);
INSERT INTO statuses (case_management_status, case_management_display_name, support_management_status, support_management_display_name, oep_status, oep_display_name, external_status, external_display_name)
VALUES
	 ('Anmälan',NULL,NULL,NULL,'Inskickat','Inskickat',NULL,NULL),
	 ('Begäran om komplettering',NULL,NULL,NULL,'Väntar på komplettering','Väntar på komplettering',NULL,NULL),
	 ('Väntar på komplettering',NULL,NULL,NULL,'Väntar på komplettering','Väntar på komplettering',"Komplettering behövs","Komplettering behövs"),
	 ('Nämndbeslut',NULL,NULL,NULL,'Klart','Klart',NULL,NULL),
	 ('KOMPBYGG',NULL,NULL,NULL,'Komplettering inkommen, behandling fortsätter','Komplettering inkommen, behandling fortsätter',NULL,NULL),
	 ('KOMPTEK',NULL,NULL,NULL,'Komplettering inkommen, behandling fortsätter','Komplettering inkommen, behandling fortsätter',NULL,NULL),
	 ('KOMPREV',NULL,NULL,NULL,'Beslut finns, se separat information','Beslut finns, se separat information',NULL,NULL);
INSERT INTO statuses (case_management_status, case_management_display_name, support_management_status, support_management_display_name, oep_status, oep_display_name, external_status, external_display_name)
VALUES
	 ('Komplettering inkommen',NULL,NULL,NULL,'Kompletterad',NULL,NULL,NULL),
	 ('Komplettering',NULL,NULL,NULL,'Kompletterad',NULL,NULL,NULL),
	 ('Under utredning',NULL,NULL,NULL,'Under behandling','Under behandling',NULL,NULL),
	 ('Under beslut',NULL,NULL,NULL,'Under behandling','Under behandling',NULL,NULL),
	 ('Beslutad',NULL,NULL,NULL,'Klart','Klart','Avslutat',NULL),
	 ('Beslut verkställt',NULL,NULL,NULL,'Klart','Klart','Avslutat',NULL),
	 ('Ärendet avvisas',NULL,NULL,NULL,'Klart','Klart','Avslutat',NULL),
	 ('Under granskning',NULL,NULL,NULL,'Under behandling','Under behandling',NULL,NULL),
	 ('Intern komplettering',NULL,NULL,NULL,'Under behandling','Under behandling',NULL,NULL);
INSERT INTO statuses (case_management_status, case_management_display_name, support_management_status, support_management_display_name, oep_status, oep_display_name, external_status, external_display_name)
VALUES
	 ('Under remiss',NULL,NULL,NULL,'Under behandling','Under behandling',NULL,NULL),
	 ('Återkoppling remiss',NULL,NULL,NULL,'Under behandling','Under behandling',NULL,NULL),
	 ('Intern återkoppling',NULL,NULL,NULL,'Under behandling','Under behandling',NULL,NULL),
	 ('Under överklagan',NULL,NULL,NULL,'Under överklagan','Under överklagan',NULL,NULL),
	 (NULL,NULL,NULL,NULL,'Preliminär',NULL,'Inskickat',NULL),
	 (NULL,NULL,NULL,NULL,'Arkiverat',NULL,'Avslutat',NULL),
	 (NULL,NULL,'ASSIGNED','ASSIGNED','Pågående','Pågående',NULL,NULL),
	 ('Handläggare tilldelad',NULL,'ASSIGNED','ASSIGNED','Under behandling','Under behandling',NULL,NULL),
	 (NULL,NULL,'AWAITING_INTERNAL_RESPONSE','AWAITING_INTERNAL_RESPONSE',"Pågående",NULL,NULL,NULL),
	 ('Ärende inkommit',NULL,"NEW",NULL,'Inskickat','Inskickat',"Inskickat","Inskickat"),
	 (NULL,NULL,'ONGOING','ONGOING','Pågående','Pågående',NULL,NULL),
	 (NULL,NULL,'PENDING','PENDING',"Väntar på komplettering","Väntar på komplettering",NULL,NULL),
	 ('Avslutat',NULL,NULL,NULL,'Ärendet arkiveras','Ärendet arkiveras',"Avslutat",NULL),
	 ('Ärende avslutat',NULL,'SOLVED',NULL,'Klart','Klart','Avslutat',NULL);
INSERT INTO statuses (case_management_status, case_management_display_name, support_management_status, support_management_display_name, oep_status, oep_display_name, external_status, external_display_name)
VALUES
	 (NULL,NULL,'SUSPENDED','SUSPENDED','Pågående','Pågående',NULL,NULL),
	 (NULL,NULL,'UPSTART','Uppstart','Pågående','Pågående',NULL,NULL),
	 (NULL,NULL,'PUBLISH_SELECTION','Publicera och urval','Pågående','Pågående',NULL,NULL),
	 (NULL,NULL,'INTERNAL_CONTROL_AND_INTERVIEWS','Intern kontroll och intervjuer','Pågående','Pågående',NULL,NULL),
	 (NULL,NULL,'REFERENCE_CHECK','Referenstagning','Pågående','Pågående',NULL,NULL),
	 (NULL,NULL,'REVIEW','Avstämning','Pågående','Pågående',NULL,NULL),
	 (NULL,NULL,'SECURITY_CLEARENCE','Säkerhetsprövning','Pågående','Pågående',NULL,NULL),
	 (NULL,NULL,'FEEDBACK_CLOSURE','Återkoppling och avslut','Pågående','Pågående',NULL,NULL);


