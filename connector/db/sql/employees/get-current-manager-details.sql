SELECT
  first_manager.manager_id AS first_manager_id,
  first_manager.manager_employee_id AS first_manager_employee_id,
  first_manager.first_name AS first_manager_first_name,
  first_manager.last_name AS first_manager_last_name,
  COALESCE(second_manager.manager_id, null) AS second_manager_id,
  COALESCE(second_manager.manager_employee_id, null) AS second_manager_employee_id,
  COALESCE(second_manager.first_name, null) AS second_manager_first_name,
  COALESCE(second_manager.last_name, null) AS second_manager_last_name
FROM
  employees
  JOIN employee_manager_mapping_view AS first_manager ON employees.id = first_manager.emp_id
  AND first_manager.type = 1
  LEFT JOIN employee_manager_mapping_view AS second_manager ON employees.id = second_manager.emp_id
  AND second_manager.type = 2
WHERE
  employees.status = true
  AND employees.organisation_id = :organisationId
  AND employees.id = :id;