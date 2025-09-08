SELECT DISTINCT
  employees.organisation_id,
  employees.id,
  employees.emp_id,
  employees.first_name,
  employees.last_name,
  employees.email_id,
  employees.contact_no,
  employees.status
FROM employees
  JOIN employee_manager_mapping_view
  ON ((employees.id = employee_manager_mapping_view.manager_id AND employee_manager_mapping_view.type = 1)
  OR (employees.id = employee_manager_mapping_view.manager_id AND employee_manager_mapping_view.type = 2))
WHERE
  employees.status = true
  AND employees.organisation_id = :organisationId ;
