SELECT
  COALESCE(AVG(self_review.average_rating), 0.0)::NUMERIC(10,2) AS self_average_rating,
  COALESCE(AVG(first_manager_review.average_rating), 0.0)::NUMERIC(10,2) AS first_manager_average_rating,
  COALESCE(AVG(second_manager_review.average_rating), 0.0)::NUMERIC(10,2) AS second_manager_average_rating,
  COALESCE(AVG(check_in_review.average_rating), 0.0)::NUMERIC(10,2) AS check_in_average_rating
FROM review_cycle
  INNER JOIN employees ON employees.organisation_id = review_cycle.organisation_id
  LEFT JOIN employee_manager_mapping AS firstManagerMapping ON (
    firstManagerMapping.emp_id = employees.id
    AND firstManagerMapping.type = 1
    AND firstManagerMapping.created_at::date <= review_cycle.self_review_end_date
    AND (firstManagerMapping.updated_at::date IS NULL OR firstManagerMapping.updated_at::date >= review_cycle.check_in_end_date)
  )
  LEFT JOIN employee_manager_mapping AS secondManagerMapping ON (
    secondManagerMapping.emp_id = employees.id
    AND secondManagerMapping.type = 2
    AND secondManagerMapping.created_at::date <= review_cycle.self_review_end_date
    AND (secondManagerMapping.updated_at::date IS NULL OR secondManagerMapping.updated_at::date >= review_cycle.check_in_end_date)
  )
  LEFT JOIN review_details AS self_review ON (
    self_review.review_cycle_id = review_cycle.id
    AND self_review.review_to = employees.id
    AND self_review.review_type_id = 1
  )
  LEFT JOIN review_details AS first_manager_review ON (
    first_manager_review.review_cycle_id = review_cycle.id
    AND first_manager_review.review_to = employees.id
    AND first_manager_review.review_type_id = 2
    AND first_manager_review.review_from = firstManagerMapping.manager_id
    AND first_manager_review.updated_at BETWEEN firstManagerMapping.created_at AND COALESCE(firstManagerMapping.updated_at, now())
  )
  LEFT JOIN review_details AS second_manager_review ON (
    second_manager_review.review_cycle_id = review_cycle.id
    AND second_manager_review.review_to = employees.id
    AND second_manager_review.review_type_id = 2
    AND second_manager_review.review_from = secondManagerMapping.manager_id
    AND second_manager_review.updated_at BETWEEN secondManagerMapping.created_at AND COALESCE(secondManagerMapping.updated_at, now())
  )
  LEFT JOIN review_details AS check_in_review ON (
    check_in_review.review_cycle_id = review_cycle.id
    AND check_in_review.review_to = employees.id
    AND check_in_review.review_type_id = 3
  )
WHERE
  employees.organisation_id = :organisationId
  AND employees.id = :reviewToId
  AND review_cycle.id = ANY(:reviewCycleId::INT[]);
