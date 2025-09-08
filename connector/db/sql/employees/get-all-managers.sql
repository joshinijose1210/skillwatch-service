SELECT
  employees.organisation_id,
  employees.id,
  employees.emp_id,
  employees.first_name,
  employees.last_name,
  employees.email_id,
  employees.contact_no,
  employees.status,
  employees_department_mapping_view.department_name,
  employees_team_mapping_view.team_name,
  employees_designation_mapping_view.designation_name,
  employees_role_mapping_view.role_name,
  firstManagerData.manager_id AS first_manager_id,
  firstManagerData.manager_employee_id AS first_manager_employee_id,
  COALESCE(secondManagerData.manager_id, null) AS second_manager_id,
  COALESCE(secondManagerData.manager_employee_id, null) AS second_manager_employee_id
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
  AND employees.organisation_id = :organisationId
  ORDER BY employees.emp_id
OFFSET (:offset::INT)
LIMIT (:limit::INT) ;
