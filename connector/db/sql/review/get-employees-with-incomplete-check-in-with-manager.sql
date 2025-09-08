SELECT
  employees.organisation_id,
  employees.id,
  employees.first_name,
  employees.last_name,
  employees.emp_id,
  employees.email_id,
  employees.contact_no
FROM
  employees
  JOIN employee_manager_mapping_view AS firstManagerMapping ON employees.id = firstManagerMapping.emp_id
  AND firstManagerMapping.type = 1
  LEFT JOIN employee_manager_mapping_view AS secondManagerMapping ON employees.id = secondManagerMapping.emp_id
  AND secondManagerMapping.type = 2
  LEFT JOIN review_details ON employees.id = review_details.review_to
  AND review_details.review_type_id = 3
  AND review_details.review_cycle_id = :reviewCycleId
WHERE
  ((firstManagerMapping.manager_id = :managerId AND firstManagerMapping.emp_id != :managerId) OR
  (secondManagerMapping.manager_id = :managerId AND secondManagerMapping.emp_id != :managerId))
  AND employees.status = TRUE
  AND (review_details.published IS NULL OR review_details.published = FALSE) ;