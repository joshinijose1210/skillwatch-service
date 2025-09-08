SELECT
  employees.organisation_id,
  employees.id,
  employees.emp_id,
  employees.first_name,
  employees.last_name,
  employees.email_id,
  employees.contact_no,
  employees.gender_id,
  employees.date_of_joining,
  employees.date_of_birth,
  employees.experience,
  employees.status,
  employees.is_consultant,
  COALESCE(employees_department_mapping_view.id, null) AS department_id,
  COALESCE(employees_department_mapping_view.department_name, null) AS department_name,
  COALESCE(employees_team_mapping_view.team_id, null) AS team_id,
  COALESCE(employees_team_mapping_view.team_name, null) AS team_name,
  COALESCE(employees_designation_mapping_view.id, null) AS designation_id,
  COALESCE(employees_designation_mapping_view.designation_name, null) AS designation_name,
  COALESCE(employees_role_mapping_view.id, null) AS role_id,
  COALESCE(employees_role_mapping_view.role_name, null) AS role_name,
  COALESCE(first_manager_data.id, null) AS first_manager_id,
  COALESCE(first_manager_data.emp_id, null) AS first_manager_employee_id,
  COALESCE(first_manager_data.first_name, null) AS first_manager_first_name,
  COALESCE(first_manager_data.last_name, null) AS first_manager_last_name,
  COALESCE(second_manager_data.id, null) AS second_manager_id,
  COALESCE(second_manager_data.emp_id, null) AS second_manager_employee_id,
  COALESCE(second_manager_data.first_name, null) AS second_manager_first_name,
  COALESCE(second_manager_data.last_name, null) AS second_manager_last_name
FROM employees
  LEFT JOIN employees_department_mapping_view ON employees.id = employees_department_mapping_view.emp_id
  LEFT JOIN employees_team_mapping_view ON employees.id = employees_team_mapping_view.emp_id
  LEFT JOIN employees_designation_mapping_view ON employees.id = employees_designation_mapping_view.emp_id
  LEFT JOIN employees_role_mapping_view ON employees.id = employees_role_mapping_view.emp_id
  LEFT JOIN review_cycle AS rc
    ON rc.id = :review_cycle_id
  LEFT JOIN employee_manager_mapping AS first_manager_mapping
    ON employees.id = first_manager_mapping.emp_id
    AND first_manager_mapping.type = 1
    AND (
      (rc.id IS NOT NULL AND
        first_manager_mapping.created_at::date <= rc.self_review_end_date
        AND (first_manager_mapping.updated_at::date IS NULL OR first_manager_mapping.updated_at::date >= rc.end_date))
      OR
      (rc.id IS NULL AND first_manager_mapping.is_active = TRUE)
    )
  LEFT JOIN employees AS first_manager_data ON first_manager_mapping.manager_id = first_manager_data.id
  LEFT JOIN employee_manager_mapping AS second_manager_mapping
    ON employees.id = second_manager_mapping.emp_id
    AND second_manager_mapping.type = 2
    AND (
      (rc.id IS NOT NULL AND
        second_manager_mapping.created_at::date <= rc.self_review_end_date
        AND (second_manager_mapping.updated_at::date IS NULL OR second_manager_mapping.updated_at::date >= rc.end_date))
      OR
      (rc.id IS NULL AND second_manager_mapping.is_active = TRUE)
    )
  LEFT JOIN employees AS second_manager_data ON second_manager_mapping.manager_id = second_manager_data.id
WHERE employees.id = :id;
