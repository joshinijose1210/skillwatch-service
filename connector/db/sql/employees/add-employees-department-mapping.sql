INSERT INTO employees_department_mapping(emp_id,department_id)
VALUES ( :id, :departmentId) RETURNING *;