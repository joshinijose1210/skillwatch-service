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
    COALESCE(employees_department_mapping.department_id, null) AS department_id,
    employees_team_mapping_view.team_id,
    employees_designation_mapping.designation_id,
    employees_role_mapping.role_id,
    firstManager.manager_id AS first_manager_id,
    COALESCE(secondManager.manager_id, null) AS second_manager_id
FROM
  employees
  LEFT JOIN employees_department_mapping ON employees.id = employees_department_mapping.emp_id
  LEFT JOIN employees_team_mapping_view ON employees.id = employees_team_mapping_view.emp_id
  LEFT JOIN employees_designation_mapping ON employees.id = employees_designation_mapping.emp_id
  LEFT JOIN employees_role_mapping ON employees.id = employees_role_mapping.emp_id
  LEFT JOIN employee_manager_mapping_view AS firstManager ON employees.id = firstManager.emp_id
  AND firstManager.type = 1
  LEFT JOIN employee_manager_mapping_view AS secondManager ON employees.id = secondManager.emp_id
  AND secondManager.type = 2
WHERE
  employees.id = :id ;