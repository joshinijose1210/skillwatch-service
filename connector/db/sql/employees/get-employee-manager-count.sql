SELECT COUNT(employees.emp_id) AS employee_manager_count
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
  AND employees.organisation_id = :organisationId;