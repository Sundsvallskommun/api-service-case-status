CREATE TABLE IF NOT EXISTS statuses
(
	id INT AUTO_INCREMENT PRIMARY KEY,
	case_management_display_name varchar(100),
	case_management_status varchar(50),
	external_display_name varchar(100),
	external_status varchar(50),
	oep_display_name varchar(100),
	oep_status varchar(50),
	support_management_display_name varchar(100),
	support_management_status varchar(50)
);

CREATE INDEX idx_oep_status
	ON statuses (oep_status);

CREATE index idx_support_management_status
	ON statuses (support_management_status);

CREATE index idx_case_management_status
	ON statuses (case_management_status);


INSERT INTO statuses (case_management_status, case_management_display_name, support_management_status, support_management_display_name, oep_status, oep_display_name, external_status, external_display_name)
VALUES
	 ('ANM',NULL,NULL,NULL,'Inskickat',NULL,NULL,NULL),
	 ('ANSÖKAN',NULL,NULL,NULL,'Inskickat',NULL,NULL,NULL),
	 ('Kv2',NULL,NULL,NULL,'Tilldelat för handläggning',NULL,NULL,NULL),
	 ('UTSKICK',NULL,NULL,NULL,'Under behandling',NULL,NULL,NULL),
	 ('UNDER',NULL,NULL,NULL,'Under behandling',NULL,NULL,NULL),
	 ('KOMP',NULL,NULL,NULL,'Komplettering behövs',NULL,NULL,NULL),
	 ('KOMP1',NULL,NULL,NULL,'Påminnelse om komplettering',NULL,NULL,NULL),
	 ('KOMPL',NULL,NULL,NULL,'Kompletering inkommen, behandling fortsätter',NULL,NULL,NULL),
	 ('SLU',NULL,NULL,NULL,'Klart',NULL,NULL,NULL),
	 ('UAB',NULL,NULL,NULL,'Klart',NULL,NULL,NULL);
INSERT INTO statuses (case_management_status, case_management_display_name, support_management_status, support_management_display_name, oep_status, oep_display_name, external_status, external_display_name)
VALUES
	 ('Anmälan',NULL,NULL,NULL,'Inskickat',NULL,NULL,NULL),
	 ('Begäran om komplettering',NULL,NULL,NULL,'Väntar på komplettering',NULL,NULL,NULL),
	 ('Väntar på komplettering',NULL,NULL,NULL,'Väntar på komplettering',NULL,"Komplettering behövs","Komplettering behövs"),
	 ('Nämndbeslut',NULL,NULL,NULL,'Klart',NULL,NULL,NULL),
	 ('KOMPBYGG',NULL,NULL,NULL,'Komplettering inkommen, behandling fortsätter',NULL,NULL,NULL),
	 ('KOMPTEK',NULL,NULL,NULL,'Komplettering inkommen, behandling fortsätter',NULL,NULL,NULL),
	 ('KOMPREV',NULL,NULL,NULL,'Beslut finns, se separat information',NULL,NULL,NULL);
INSERT INTO statuses (case_management_status, case_management_display_name, support_management_status, support_management_display_name, oep_status, oep_display_name, external_status, external_display_name)
VALUES
	 ('Komplettering inkommen',NULL,NULL,NULL,'Kompletterad',NULL,NULL,NULL),
	 ('Under utredning',NULL,NULL,NULL,'Under behandling',NULL,NULL,NULL),
	 ('Under beslut',NULL,NULL,NULL,'Under behandling',NULL,NULL,NULL),
	 ('Beslutad',NULL,NULL,NULL,'Klart',NULL,'Avslutat',NULL),
	 ('Beslut verkställt',NULL,NULL,NULL,'Klart',NULL,'Avslutat',NULL),
	 ('Ärendet avvisas',NULL,NULL,NULL,'Klart',NULL,'Avslutat',NULL),
	 ('Under granskning',NULL,NULL,NULL,'Under behandling',NULL,NULL,NULL),
	 ('Intern komplettering',NULL,NULL,NULL,'Under behandling',NULL,NULL,NULL);
INSERT INTO statuses (case_management_status, case_management_display_name, support_management_status, support_management_display_name, oep_status, oep_display_name, external_status, external_display_name)
VALUES
	 ('Under remiss',NULL,NULL,NULL,'Under behandling',NULL,NULL,NULL),
	 ('Återkoppling remiss',NULL,NULL,NULL,'Under behandling',NULL,NULL,NULL),
	 ('Intern återkoppling',NULL,NULL,NULL,'Under behandling',NULL,NULL,NULL),
	 ('Under överklagan',NULL,NULL,NULL,'Under överklagan',NULL,NULL,NULL),
	 (NULL,NULL,NULL,NULL,'Preliminär',NULL,'Inskickat',NULL),
	 (NULL,NULL,NULL,NULL,'Arkiverat',NULL,'Avslutat',NULL),
	 ('Handläggare tilldelad',NULL,'ASSIGNED','ASSIGNED','Under behandling',NULL,NULL,NULL),
	 (NULL,NULL,'AWAITING_INTERNAL_RESPONSE','AWAITING_INTERNAL_RESPONSE',NULL,NULL,NULL,NULL),
	 ('Ärende inkommit',NULL,"NEW",NULL,'Inskickat',NULL,"Inskickat","Inskickat"),
	 (NULL,NULL,'ONGOING','ONGOING',NULL,NULL,NULL,NULL),
	 (NULL,NULL,'PENDING','PENDING',NULL,NULL,NULL,NULL),
	 ('Ärende avslutat','SOLVED','SOLVED',NULL,'Klart',NULL,'Avslutat',NULL);
INSERT INTO statuses (case_management_status, case_management_display_name, support_management_status, support_management_display_name, oep_status, oep_display_name, external_status, external_display_name)
VALUES
	 (NULL,NULL,'SUSPENDED','SUSPENDED',NULL,NULL,NULL,NULL),
	 (NULL,NULL,'UPSTART','Uppstart',NULL,NULL,NULL,NULL),
	 (NULL,NULL,'PUBLISH_SELECTION','Publicera och urval',NULL,NULL,NULL,NULL),
	 (NULL,NULL,'INTERNAL_CONTROL_AND_INTERVIEWS','Intern kontroll och intervjuer',NULL,NULL,NULL,NULL),
	 (NULL,NULL,'REFERENCE_CHECK','Referenstagning',NULL,NULL,NULL,NULL),
	 (NULL,NULL,'REVIEW','Avstämning',NULL,NULL,NULL,NULL),
	 (NULL,NULL,'SECURITY_CLEARENCE','Säkerhetsprövning',NULL,NULL,NULL,NULL),
	 (NULL,NULL,'FEEDBACK_CLOSURE','Återkoppling och avslut',NULL,NULL,NULL,NULL);


