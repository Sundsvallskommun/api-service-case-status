CREATE TABLE IF NOT EXISTS support_management_status
(
    id             INT AUTO_INCREMENT PRIMARY KEY,
    system_status  VARCHAR(255) NOT NULL,
    generic_status VARCHAR(255) NOT NULL
);

CREATE INDEX idx_system_status
    ON support_management_status (system_status);

INSERT INTO support_management_status (system_status, generic_status)
VALUES ('SOLVED', 'Klart'),
       ('NEW', 'Inskickat'),
       ('PENDING', 'Väntar på komplettering'),
       ('SUSPENDED', 'Pågående'),
       ('ONGOING', 'Pågående'),
       ('ASSIGNED', 'Pågående'),
       ('AWAITING_INTERNAL_RESPONSE', 'Pågående');

