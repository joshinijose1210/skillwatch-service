SELECT
  employees.organisation_id,
  employees.id,
  employees.emp_id AS employee_id,
  employees.first_name,
  employees.last_name,
  employees.email_id,
  COALESCE(firstManagerMappingData.manager_id, null) AS first_manager_id,
  COALESCE(firstManagerMappingData.manager_employee_id, null) AS first_manager_employee_id,
  COALESCE(firstManagerMappingData.first_name, null) AS first_manager_first_name,
  COALESCE(firstManagerMappingData.last_name,null) AS first_manager_last_name,
  COALESCE(secondManagerMappingData.manager_id, null) AS second_manager_id,
  COALESCE(secondManagerMappingData.manager_employee_id, null) AS second_manager_employee_id,
  COALESCE(secondManagerMappingData.first_name, null) AS second_manager_first_name,
  COALESCE(secondManagerMappingData.last_name, null) AS second_manager_last_name
FROM
  employees
  LEFT JOIN employee_manager_mapping_view AS firstManagerMappingData ON employees.id = firstManagerMappingData.emp_id
  AND firstManagerMappingData.type = 1
  LEFT JOIN employee_manager_mapping_view AS secondManagerMappingData ON employees.id = secondManagerMappingData.emp_id
  AND secondManagerMappingData.type = 2
WHERE
  (:id::INT[] = '{-99}' OR employees.id = ANY (:id::INT[]))
  AND (:firstManagerId::INT[] = '{-99}'
  OR (firstManagerMappingData.manager_id = ANY (:firstManagerId::INT[]) AND firstManagerMappingData.emp_id != ANY (:firstManagerId::INT[])))
  AND (:secondManagerId::INT[] = '{-99}'
  OR (secondManagerMappingData.manager_id = ANY (:secondManagerId::INT[]) AND secondManagerMappingData.emp_id != ANY (:secondManagerId::INT[])))
  AND employees.status = true
  AND employees.organisation_id = :organisationId
ORDER BY employees.emp_id
OFFSET (:offset::INT)
LIMIT (:limit::INT);