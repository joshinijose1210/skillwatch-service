SELECT
  review_cycle.id as review_cycle_id,
  employees.emp_id AS employee_id,
  employees.id AS id,
  employees.first_name,
  employees.last_name,
  check_in_review.average_rating AS check_in_average_rating
FROM
  review_cycle
  INNER JOIN employees ON employees.organisation_id = review_cycle.organisation_id
  JOIN employees_history ON employees_history.employee_id = employees.id
    AND (DATE(employees_history.activated_at) <= review_cycle.end_date)
    AND (DATE(employees_history.deactivated_at) IS NULL OR DATE(employees_history.deactivated_at) >= review_cycle.start_date)
  LEFT JOIN review_details AS check_in_review ON check_in_review.review_cycle_id = review_cycle.id
  AND check_in_review.review_to = employees.id
  AND check_in_review.review_type_id = 3
  AND check_in_review.draft = false
  AND check_in_review.published = true
WHERE
  review_cycle.organisation_id = :organisationId
  AND review_cycle.id = :reviewCycleId
  AND (check_in_review.average_rating BETWEEN :minRange::NUMERIC(10,2) AND :maxRange::NUMERIC(10,2)
  OR (:minRange::NUMERIC(10,2) IS NULL OR :maxRange::NUMERIC(10,2) IS NULL))
  AND (:employeeId::INT[] = '{-99}' OR employees.id = ANY (:employeeId::INT[]))
ORDER BY
  check_in_review.average_rating DESC
OFFSET (:offset::INT)
LIMIT (:limit::INT) ;