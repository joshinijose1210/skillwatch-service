INSERT INTO organisations(sr_no, admin_id, name, is_active, organisation_size )
VALUES(1, 1, 'ScaleReal Technologies Pvt. Ltd.', true, 50);

INSERT INTO review_cycle (
    organisation_id, id, start_date, end_date, publish, self_review_start_date, self_review_end_date, manager_review_start_date,
     manager_review_end_date, check_in_start_date, check_in_end_date)
VALUES
    (1, 1, '12/12/2022', '02/02/2023', true,'12/13/2022','12/14/2022','01/01/2023','10/01/2023','01/20/2023','01/25/2023'),
    (1, 2,'2023-04-01', '2023-04-30', false, '2023-04-01', '2023-04-07', '2023-04-01', '2023-04-15', '2023-04-16', '2023-04-30'),
    (1, 3,'2023-03-01', '2023-03-30', false, '2023-03-01', '2023-03-07', '2023-03-01', '2023-03-15', '2023-03-16', '2023-03-30');

INSERT INTO employees(id, emp_id, first_name, last_name, email_id, contact_no, status, organisation_id)
VALUES
    (1, 'SR0043', 'Syed Ubed', 'Ali', 'syed.ali@scalereal.com', 6262209099, true, 1),
    (2, 'SR0051', 'Rushad', 'Shaikh', 'rushad.shaikh@scalereal.com', 8265079426, true, 1),
    (3, 'SR0050', 'Moly', 'Agarwal', 'moly.agarwal@scalereal.com', 9876543210, true, 1),
    (4, 'SR0006', 'Yogesh', 'Jadhav', 'yogesh.jadhav@scalereal.com', 9123456780, true, 1),
    (5, 'SR0039', 'Amir', 'Islam', 'amir.islam@scalereal.com', 7878787878, true, 1);

INSERT INTO kpi (organisation_id, id, kpi_id, title, description, status)
VALUES
    (1, 1, 1, '1st KPI Title', 'Its a detailed description of 1st kpi', true),
    (1, 2, 2, '2nd KPI Title', 'Its a detailed description of 2nd kpi', true);

INSERT INTO review_types (name)
      SELECT review_type
      FROM (VALUES
      ('Self Review'),
      ('Manager Review'),
      ('Summary')) AS new_review_types(review_type)
      LEFT JOIN review_types ON new_review_types.review_type = review_types.name
      WHERE review_types.name IS NULL;

-- Data for Avg Ratings for multiple Review Cycles
INSERT INTO employee_manager_mapping(emp_id, manager_id, type, created_at, updated_at, is_active)
VALUES (2, 1, 1, '2023-04-01', '2023-05-01', false),
       (2, 5, 2, '2023-04-01', '2023-05-01', false),
       (2, 3, 1, '2023-03-01', '2023-04-01', false),
       (2, 4, 2, '2023-03-01', '2023-04-01', false);

INSERT INTO review_details(id, review_cycle_id, review_to, review_from, updated_at, draft, published, review_type_id, average_rating)
values (10, 2, 2, 2,'2023-04-06', false, true, 1, 3),
       (11, 2, 2, 1,'2023-04-10', false, true, 2, 3),
       (12, 2, 2, 5,'2023-04-10', false, true, 2, 3),
       (13, 2, 2, 1,'2023-04-10', false, true, 3, 3),
       (14, 3, 2, 2,'2023-03-06', false, true, 1, 4),
       (15, 3, 2, 3,'2023-03-10', false, true, 2, 4),
       (16, 3, 2, 4,'2023-03-10', false, true, 2, 4),
       (17, 3, 2, 4,'2023-03-10', false, true, 3, 4);

