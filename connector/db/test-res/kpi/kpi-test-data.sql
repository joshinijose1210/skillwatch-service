INSERT INTO organisations (
sr_no,
admin_id,
name,
is_active,
organisation_size )
VALUES (1, 1, 'Scalereal', true, 50);

INSERT INTO kra (sr_no, name, weightage, organisation_id, version_number)
VALUES (1, 'Results', 60, 1, 1),
       (2, 'Skills', 30, 1, 1),
       (3, 'Attitude & Fitment', 10, 1, 1);

INSERT INTO employees(id, emp_id, first_name, last_name, email_id, contact_no, status, organisation_id)
VALUES
    (1, 'SR0043', 'Syed Ubed', 'Ali', 'syed.ali@scalereal.com', 6262209099, true, 1);

INSERT INTO departments(id, department_id, organisation_id, department_name, status)
VALUES (1, 1, 1, 'Engineering',true);

INSERT INTO teams(id, team_id, organisation_id, team_name, status)
VALUES (1, 1, 1, 'Devops Team',true),
       (2, 2, 1, 'HR Team',true);

INSERT INTO designations(id, designation_id, organisation_id, designation_name, status)
VALUES (4, 1, 1, 'Designation-1',true),
       (5, 2, 1, 'Designation-2',true);

INSERT INTO kpi (id, title, description, status, kpi_id, organisation_id)
VALUES
      (10, 'Communication', 'test', true, 4, 1),
      (11, 'Clean Code', 'test', true, 5, 1);

INSERT INTO kpi_version_mapping(kpi_id, version_number)
VALUES (10, 1), (11, 1);

INSERT INTO employees_department_mapping(emp_id, department_id)
VALUES (1, 1);

INSERT INTO employees_team_mapping(emp_id, team_id, joined_at, is_active)
VALUES (1, 2, now(), true);

INSERT INTO employees_designation_mapping(emp_id, designation_id)
VALUES (1, 4);

INSERT INTO department_team_mapping (department_id, team_id)
VALUES (1, 1), (1, 2);

INSERT INTO team_designation_mapping(team_id, designation_id)
VALUES (1, 4),
       (2, 5);

INSERT INTO kpi_department_team_designation_mapping (kpi_id, department_id, team_id, designation_id)
VALUES ( 10, 1, 1, 4),
       ( 11, 1, 2, 4);
