SELECT
  COUNT(employees.emp_id) as employee_count
FROM
  employees
  LEFT JOIN employees_department_mapping_view ON employees.id = employees_department_mapping_view.emp_id
  AND employees_department_mapping_view.organisation_id = :organisationId
  LEFT JOIN employees_team_mapping_view ON employees.id = employees_team_mapping_view.emp_id
  AND employees_team_mapping_view.organisation_id = :organisationId
  LEFT JOIN employees_designation_mapping_view ON employees.id = employees_designation_mapping_view.emp_id
  AND employees_designation_mapping_view.organisation_id = :organisationId
  JOIN employees_role_mapping_view ON employees.id = employees_role_mapping_view.emp_id
  AND employees_role_mapping_view.organisation_id = :organisationId
  JOIN employee_manager_mapping_view AS firstManagerData ON employees.id = firstManagerData.emp_id
  AND firstManagerData.type = 1
  LEFT JOIN employee_manager_mapping_view AS secondManagerData ON employees.id = secondManagerData.emp_id
  AND secondManagerData.type = 2
WHERE
  employees_role_mapping_view.id IN (SELECT DISTINCT(role_id) FROM module_permissions WHERE module_id = ANY(:moduleId::INT[]) AND edit=true)
  AND employees.organisation_id = :organisationId ;