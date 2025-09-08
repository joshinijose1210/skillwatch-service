INSERT INTO employees_role_mapping(emp_id,role_id)
VALUES (
  :id,
  :role_id
  ) RETURNING *;
