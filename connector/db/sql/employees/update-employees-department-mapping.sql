UPDATE
  employees_department_mapping
SET
  emp_id = :id,
  department_id = :departmentId
WHERE
  emp_id = :id;