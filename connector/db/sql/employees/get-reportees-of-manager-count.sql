SELECT COUNT(employees.id) AS reportees_count
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
  OR (secondManagerMapping.manager_id = :managerId AND secondManagerMapping.emp_id != :managerId));