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
  AND employees.organisation_id = :organisationId;