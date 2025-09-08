INSERT INTO organisations(
    sr_no,
    admin_id,
    name,
    is_active,
    organisation_size
)
VALUES
    (
        1, 1, 'Scalereal', true, 50
    );

INSERT INTO employees (
id,
emp_id,
first_name,
last_name,
email_id,
contact_no,
status,
onboarding_flow,
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
    false,
    1
);
INSERT INTO departments(organisation_id, department_id, department_name, status)
VALUES(1, 1, 'Engineering',true);

INSERT INTO teams(organisation_id, team_id, team_name, status)
VALUES(1, 1, 'Devops Team',true);

INSERT INTO department_team_mapping(department_id, team_id)
VALUES(1,1);
