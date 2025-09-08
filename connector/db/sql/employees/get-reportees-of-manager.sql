SELECT
  employees.organisation_id,
  employees.id,
  employees.first_name,
  employees.last_name,
  employees.emp_id,
  employees.email_id,
  COALESCE(firstManagerMapping.manager_id, null) AS first_manager_id,
  COALESCE(secondManagerMapping.manager_id, null) AS second_manager_id
FROM
  employees
  JOIN employee_manager_mapping_view AS firstManagerMapping ON employees.id = firstManagerMapping.emp_id
  AND firstManagerMapping.type = 1
  LEFT JOIN employee_manager_mapping_view AS secondManagerMapping ON employees.id = secondManagerMapping.emp_id
  AND secondManagerMapping.type = 2
WHERE
  employees.organisation_id = :organisationId
  AND employees.status = true
  AND ((firstManagerMapping.manager_id = :managerId AND firstManagerMapping.emp_id != :managerId)
  OR (secondManagerMapping.manager_id = :managerId AND secondManagerMapping.emp_id != :managerId))
ORDER BY
  employees.first_name
OFFSET (:offset::INT)
LIMIT (:limit::INT);