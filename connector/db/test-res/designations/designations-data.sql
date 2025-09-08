INSERT INTO organisations(
    sr_no,
    admin_id,
    name,
    is_active,
    organisation_size
)
VALUES
    (
        1, 1, 'Scalereal',true, 50
    );

INSERT INTO employees (
id,
emp_id,
first_name,
last_name,
email_id,
contact_no,
status,
organisation_id
)
VALUES (
    1,
    'SR0049',
    'grv',
    'abc',
    'gp.aarush@gmail.com',
    7389543155,
    true,
    1
);

INSERT INTO departments(organisation_id, department_id, department_name, status)
VALUES(1, 1, 'Engineering',true);

INSERT INTO teams(organisation_id, team_id, team_name, status)
VALUES(1, 1, 'Backend',true),
      (1, 2, 'Frontend',true);

INSERT INTO department_team_mapping (department_id, team_id)
VALUES(1,1),
      (1,2);

INSERT INTO kra (sr_no, name, weightage, organisation_id, version_number)
VALUES (1, 'Results', 60, 1, 1),
       (2, 'Skills', 30, 1, 1),
       (3, 'Attitude & Fitment', 10, 1, 1);

INSERT INTO kpi (title, description, status, kpi_id, organisation_id)
VALUES ('KPI-1', 'Description for KPI-1', true, 1, 1),
       ('KPI-2', 'Description for KPI-2', true, 2, 1),
       ('KPI-3', 'Description for KPI-3', true, 3, 1) ;

INSERT INTO kra_kpi_mapping(kra_id, kpi_id)
VALUES (1, 1),
       (2, 2),
       (3, 3);