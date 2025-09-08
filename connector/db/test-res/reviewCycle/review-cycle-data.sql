INSERT INTO organisations(sr_no, admin_id, name, is_active, organisation_size)
    VALUES (1,1,'ScaleReal Technologies Pvt. Ltd.', true, 50);

INSERT INTO modules (name)
  SELECT new_module
    FROM (VALUES
    ('Review Cycles')) AS new_modules(new_module)
    LEFT JOIN modules ON new_modules.new_module = modules.name
    WHERE modules.name IS NULL;

INSERT INTO employees(id, emp_id, first_name, last_name, email_id, contact_no, status, organisation_id)
    VALUES
           (1, 'SR0043', 'Syed Ubed', 'Ali', 'syed.ali@scalereal.com', '+916262209099', true, 1),
           (2, 'SR0051', 'Rushad', 'Shaikh', 'rushad.shaikh@scalereal.com', '+918265079426', true, 1),
           (3, 'SR0006', 'Yogesh', 'Jadhav', 'yogesh.jadhav@scalereal.com', '+919876543210', true, 1);

INSERT INTO teams(organisation_id, team_id, team_name, status) VALUES(1, 1, 'Backend',true);
INSERT INTO employees_team_mapping(emp_id, team_id, joined_at, is_active)
    VALUES
    (1, 1, now(), true),
    (2, 1, now(), true),
    (3, 1, now(), true);

INSERT INTO employee_manager_mapping(emp_id, manager_id, type, is_active) VALUES(1,3,1,true),(2,3,1,true), (3,3,1,true);

INSERT INTO review_types
     VALUES
            (1, 'Self Review'),
            (2, 'Manager Review'),
            (3, 'Summary');

INSERT INTO review_cycle(
    organisation_id,
    start_date,
    end_date,
    publish,
    self_review_start_date,
    self_review_end_date,
    manager_review_start_date,
    manager_review_end_date,
    check_in_start_date,
    check_in_end_date)
VALUES (1, '2022-01-01','2022-06-30',false,'2022-06-01','2022-06-10','2022-06-11','2022-06-20','2022-06-21','2022-06-30');

INSERT INTO review_details(id, review_cycle_id, review_to, review_from, updated_at, draft, published, review_type_id, average_rating)
VALUES
    (1, 1, 2, 2, now(), false, true, 1, 3.50),
    (2, 1, 2, 3, now(), false, true, 2, 3.50),
    (3, 1, 2, 3, now(), true, false, 3, null);
