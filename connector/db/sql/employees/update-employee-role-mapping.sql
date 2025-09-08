UPDATE
  employees_role_mapping
SET
  emp_id = :id,
  role_id = :roleId
WHERE
  emp_id = :id;