SELECT
  employees.organisation_id,
  employees.id,
  employees.first_name,
  employees.last_name,
  employees.email_id,
  employees.contact_no,
  employees.emp_id,
  employees.onboarding_flow,
  employees_department_mapping_view.department_name,
  employees_designation_mapping_view.designation_name,
  employees_team_mapping_view.team_name,
  employees_role_mapping_view.role_name,
  employees_role_mapping_view.id AS role_id,
  COALESCE(first_manager_data.manager_id, null) AS first_manager_id,
  COALESCE(second_manager_data.manager_id, null) AS second_manager_id,
  CASE WHEN is_manager_details.manager_id IS NOT NULL THEN true ELSE false END AS is_or_was_manager
FROM
  employees
  JOIN employees_department_mapping_view ON employees.id = employees_department_mapping_view.emp_id
  JOIN employees_team_mapping_view ON employees.id = employees_team_mapping_view.emp_id
  JOIN employees_designation_mapping_view ON employees.id = employees_designation_mapping_view.emp_id
  JOIN employees_role_mapping_view ON employees.id = employees_role_mapping_view.emp_id
  LEFT JOIN employee_manager_mapping_view AS first_manager_data ON employees.id = first_manager_data.emp_id
    AND first_manager_data.type = 1
  LEFT JOIN employee_manager_mapping_view AS second_manager_data ON employees.id = second_manager_data.emp_id
    AND second_manager_data.type = 2
  LEFT JOIN employee_manager_mapping AS is_manager_details ON employees.id = is_manager_details.manager_id
WHERE
  LOWER(employees.email_id) = LOWER(:emailId) ;
