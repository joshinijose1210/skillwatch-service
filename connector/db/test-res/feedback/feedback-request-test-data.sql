INSERT INTO organisations (sr_no, admin_id, name, is_active, organisation_size )
VALUES (1, 1, 'ScaleReal', true, 50);
INSERT INTO employees(id, emp_id, first_name, last_name, email_id, contact_no, status, organisation_id)
VALUES (1, 'SR0043', 'Syed Ubed', 'Ali', 'syed.ali@scalereal.com', 6262209099, true, 1),
    (2, 'SR0050', 'Moly', 'Agarwal', 'moly.agarwal@scalereal.com', 9090909090, true, 1),
    (3, 'SR0035', 'Twinkle', 'Dahiya', 'twinkle.dahiya@scalereal.com',9099990999, true, 1),
    (4, 'SR0051', 'Rushad', 'Shaikh', 'rushad.shaikh@scalereal.com', 6262626262, true, 1);

INSERT INTO review_cycle (
    organisation_id, id, start_date, end_date, publish, self_review_start_date, self_review_end_date, manager_review_start_date,
     manager_review_end_date, check_in_start_date, check_in_end_date)
VALUES
    (1, 1, '12/12/2022', '02/02/2023', true,'12/13/2022','12/14/2022','01/01/2023','10/01/2023','01/20/2023','01/25/2023'),
    (1, 2,'2023-03-01', '2023-03-30', false, '2023-03-01', '2023-03-07', '2023-03-01', '2023-03-15', '2023-03-16', '2023-03-30');

INSERT INTO review_types
VALUES (1, 'Self Review'),
  (2, 'Manager Review'),
  (3, 'Summary Review');

INSERT INTO employee_manager_mapping(emp_id, manager_id, type, created_at, updated_at, is_active)
VALUES (2, 1, 1, '2023-04-01', '2023-05-01', false),
       (2, 4, 2, '2023-04-01', '2023-05-01', false),
       (2, 3, 1, '2023-03-01', '2023-04-01', false),
       (2, 4, 2, '2023-03-01', '2023-04-01', false);

INSERT INTO teams(id, team_id, organisation_id, team_name, status)
VALUES (1, 1, 1, 'Finance',true),
       (2, 2, 1, 'Backend',true);

INSERT INTO employees_team_mapping(emp_id, team_id, joined_at, is_active)
VALUES (1, 2, now(), true),
       (2, 2, now(), true),
       (3, 1, now(), true),
       (4, 2, now(), true);

INSERT INTO review_details(id, review_cycle_id, review_to, review_from, updated_at, draft, published, review_type_id, average_rating)
values (5, 2, 2, 2,'2023-04-06', false, true, 1, 3),
       (6, 2, 2, 1,'2023-04-10', false, true, 2, 3),
       (7, 2, 2, 1,'2023-04-10', false, true, 3, 3);

INSERT INTO goals(id, goal_id, organisation_id, assigned_to, created_by, description, type_id, progress_id, created_at, target_date)
VALUES(1, 1, 1, 2, 1, 'Please improve your coding skills', 1, 1, '2023-04-10 15:06:35.142179+05:30', '2023-10-10');

INSERT INTO feedback_types VALUES
    (1, 'Positive'),
    (2, 'Improvement'),
    (3, 'Appreciation');
INSERT INTO feedback_request(requested_by, feedback_to, feedback_from, request, is_submitted, goal_id)
VALUES (1, 1, 2, 'Please submit feedback for me based on my performance in PTA', true, 1),
       (3, 2, 1, 'Submit feedback for Moly based on her performance in PTA', false, null),
       (3, 4, 1, 'Submit feedback for Rushad based on his performance in PTA', false, null) ;
INSERT INTO feedbacks(feedback, feedback_to, feedback_from, feedback_type_id, request_id, is_draft, updated_at)
VALUES ('He performed upto the mark and completed all of his tasks on time.', 4, 1, 1, 1, false, CURRENT_TIMESTAMP) ;
