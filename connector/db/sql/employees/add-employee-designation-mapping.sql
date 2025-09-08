INSERT INTO employees_designation_mapping(emp_id,designation_id)
VALUES (
  :id,
  :designation_id
  ) RETURNING *;
