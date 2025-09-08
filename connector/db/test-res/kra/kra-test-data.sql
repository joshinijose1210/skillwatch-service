INSERT INTO organisations (
sr_no,
admin_id,
name,
is_active,
organisation_size )
VALUES (1, 1, 'ScaleReal', true, 50),
       (2, 2, 'ABC Pvt. Ltd', true, 80);

INSERT INTO kra (sr_no, name, weightage, organisation_id, version_number)
VALUES (1, 'Results', 60, 2, 1),
       (2, 'Skills', 30, 2, 1),
       (3, 'Attitude & Fitment', 10, 2, 1);

INSERT INTO kpi (title, description, status, kpi_id, organisation_id)
VALUES ('KPI-1', 'Description for KPI-1', true, 1, 2),
       ('KPI-2', 'Description for KPI-2', true, 2, 2),
       ('KPI-3', 'Description for KPI-3', true, 3, 2) ;

INSERT INTO kra_kpi_mapping(kra_id, kpi_id)
VALUES (1, 1),
       (2, 2),
       (3, 3);