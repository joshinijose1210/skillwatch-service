SELECT
  SUM(CASE WHEN self_review.draft = true AND self_review.published = false THEN 1 ELSE 0 END) AS in_progress,
  SUM(CASE WHEN self_review.published = true AND self_review.draft = false THEN 1 ELSE 0 END) AS completed
FROM
  review_cycle
  INNER JOIN employees ON employees.organisation_id = review_cycle.organisation_id
  JOIN employees_history ON employees_history.employee_id = employees.id
    AND (DATE(employees_history.activated_at) <= review_cycle.end_date)
    AND (DATE(employees_history.deactivated_at) IS NULL OR DATE(employees_history.deactivated_at) >= review_cycle.start_date)
  LEFT JOIN review_details AS self_review ON self_review.review_cycle_id = review_cycle.id
  AND self_review.review_to = employees.id
  AND self_review.review_type_id = 1
WHERE
  review_cycle.organisation_id = :organisationId
  AND review_cycle.id = :reviewCycleId;