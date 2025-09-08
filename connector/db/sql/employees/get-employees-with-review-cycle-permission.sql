SELECT
  employees.organisation_id,
  employees.id,
  employees.first_name,
  employees.last_name,
  employees.email_id,
  employees.contact_no,
  employees.emp_id
FROM employees
JOIN employees_role_mapping_view ON employees_role_mapping_view.emp_id = employees.id
JOIN module_permissions ON
     module_permissions.role_id = employees_role_mapping_view.id
     AND module_permissions.module_id = :module_id
     AND edit = true
WHERE employees.organisation_id = :organisation_id
     AND employees.status = true;


