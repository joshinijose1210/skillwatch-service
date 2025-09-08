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
  employees_department_mapping_view.department_name,
  employees_team_mapping_view.team_name,
  employees_designation_mapping_view.designation_name,
  employees_role_mapping_view.role_name,
  firstManagerData.manager_id AS first_manager_id,
  firstManagerData.manager_employee_id AS first_manager_employee_id,
  firstManagerData.first_name AS first_manager_first_name,
  firstManagerData.last_name AS first_manager_last_name,
  COALESCE(secondManagerData.manager_id, null) AS second_manager_id,
  COALESCE(secondManagerData.manager_employee_id, null) AS second_manager_employee_id,
  COALESCE(secondManagerData.first_name, null) AS second_manager_first_name,
  COALESCE(secondManagerData.last_name, null) AS second_manager_last_name
FROM
  employees
  LEFT JOIN employees_department_mapping_view ON employees.id = employees_department_mapping_view.emp_id
  AND employees_department_mapping_view.organisation_id = :organisationId
  LEFT JOIN employees_team_mapping_view ON employees.id = employees_team_mapping_view.emp_id
  AND employees_team_mapping_view.organisation_id = :organisationId
  LEFT JOIN employees_designation_mapping_view ON employees.id = employees_designation_mapping_view.emp_id
  AND employees_designation_mapping_view.organisation_id = :organisationId
  LEFT JOIN employees_role_mapping_view ON employees.id = employees_role_mapping_view.emp_id
  AND employees_role_mapping_view.organisation_id = :organisationId
  LEFT JOIN employee_manager_mapping_view AS firstManagerData ON employees.id = firstManagerData.emp_id
  AND firstManagerData.type = 1
  LEFT JOIN employee_manager_mapping_view AS secondManagerData ON employees.id = secondManagerData.emp_id
  AND secondManagerData.type = 2
 WHERE
  ((cast(:search as text) IS NULL)
       OR UPPER(employees.emp_id) LIKE UPPER(:search)
       OR UPPER(employees.first_name) LIKE UPPER(:search)
       OR UPPER(employees.last_name) LIKE UPPER(:search)
       OR UPPER(employees.first_name) || ' ' || UPPER(employees.last_name) LIKE UPPER(:search)
       OR UPPER(employees.email_id) LIKE UPPER(:search)
  )
  AND (:departmentId::INT[] = '{-99}' OR employees_department_mapping_view.id = ANY (:departmentId::INT[]))
  AND (:teamId::INT[] = '{-99}' OR employees_team_mapping_view.team_id = ANY (:teamId::INT[]))
  AND (:designationId::INT[] = '{-99}' OR employees_designation_mapping_view.id = ANY (:designationId::INT[]))
  AND (:roleId::INT[] = '{-99}' OR employees_role_mapping_view.id = ANY (:roleId::INT[]))
  AND employees.organisation_id = :organisationId
  ORDER BY CASE WHEN :sortOrder = 'ASC' THEN employees.emp_id END ASC,
           CASE WHEN :sortOrder = 'DESC' THEN employees.emp_id END DESC
OFFSET (:offset::INT)
LIMIT (:limit::INT);
