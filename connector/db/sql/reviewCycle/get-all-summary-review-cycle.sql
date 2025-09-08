SELECT
  review_cycle.id as review_cycle_id,
  review_cycle.start_date,
  review_cycle.end_date,
  review_cycle.publish,
  review_cycle.check_in_start_date,
  review_cycle.check_in_end_date,
  employees.emp_id AS employeeId,
  employees.id AS review_to_id,
  employees.emp_id AS review_to_employee_id,
  employees.first_name,
  employees.last_name,
  self_review.draft AS self_review_draft,
  self_review.published AS self_review_publish,
  self_review.average_rating AS self_average_rating,
  first_manager_review.draft AS first_manager_review_draft,
  first_manager_review.published AS first_manager_review_publish,
  first_manager_review.average_rating AS first_manager_average_rating,
  second_manager_review.draft AS second_manager_review_draft,
  second_manager_review.published AS second_manager_review_publish,
  second_manager_review.average_rating AS second_manager_average_rating,
  check_in_review.review_from AS check_in_from_id,
  check_in_review.draft AS check_in_draft,
  check_in_review.published AS check_in_publish,
  check_in_review.average_rating AS check_in_average_rating,
  COALESCE(first_manager_details.id, null) AS first_manager_id,
  COALESCE(first_manager_details.emp_id, null) AS first_manager_employee_id,
  COALESCE(first_manager_details.first_name, null) AS first_manager_first_name,
  COALESCE(first_manager_details.last_name, null) AS first_manager_last_name,
  COALESCE(second_manager_details.id, null) AS second_manager_id,
  COALESCE(second_manager_details.emp_id, null) AS second_manager_employee_id,
  COALESCE(second_manager_details.first_name, null) AS second_manager_first_name,
  COALESCE(second_manager_details.last_name, null) AS second_manager_last_name
FROM
  review_cycle
  INNER JOIN employees ON employees.organisation_id = review_cycle.organisation_id
  LEFT JOIN employee_manager_mapping AS firstManagerData ON
    (firstManagerData.emp_id = employees.id AND firstManagerData.type = 1
    AND firstManagerData.created_at::date <= review_cycle.self_review_end_date
    AND (firstManagerData.updated_at::date IS NULL OR firstManagerData.updated_at::date >= review_cycle.check_in_end_date))
  LEFT JOIN employee_manager_mapping AS secondManagerData ON
    (secondManagerData.emp_id = employees.id AND secondManagerData.type = 2
    AND secondManagerData.created_at::date <= review_cycle.self_review_end_date
    AND (secondManagerData.updated_at::date IS NULL OR secondManagerData.updated_at::date >= review_cycle.check_in_end_date))
  LEFT JOIN employees AS current_first_manager ON current_first_manager.id = firstManagerData.manager_id
  LEFT JOIN employees AS current_second_manager ON current_second_manager.id = secondManagerData.manager_id
  LEFT JOIN review_details AS self_review ON self_review.review_cycle_id = review_cycle.id
  AND self_review.review_to = employees.id
  AND self_review.review_type_id = 1
  LEFT JOIN review_details AS check_in_review ON check_in_review.review_cycle_id = review_cycle.id
  AND check_in_review.review_to = employees.id
  AND check_in_review.review_type_id = 3
  LEFT JOIN review_details AS first_manager_review ON first_manager_review.review_cycle_id = review_cycle.id
  AND first_manager_review.review_to = employees.id
  AND first_manager_review.review_type_id = 2
  AND first_manager_review.review_from IN (SELECT manager_id FROM employee_manager_mapping WHERE emp_id = employees.id AND type = 1
  AND first_manager_review.updated_at BETWEEN created_at AND COALESCE(updated_at, now()))
  LEFT JOIN review_details AS second_manager_review ON second_manager_review.review_cycle_id = review_cycle.id
  AND second_manager_review.review_to = employees.id
  AND second_manager_review.review_type_id = 2
  AND second_manager_review.review_from IN (SELECT manager_id FROM employee_manager_mapping WHERE emp_id = employees.id AND type = 2
  AND second_manager_review.updated_at BETWEEN created_at AND COALESCE(updated_at, now()))
  LEFT JOIN employees AS first_manager_details ON first_manager_details.id = first_manager_review.review_from
  LEFT JOIN employees AS second_manager_details ON second_manager_details.id = second_manager_review.review_from
WHERE
  review_cycle.organisation_id = :organisationId
  AND employees.status = true
  AND employees.created_at::date <= review_cycle.end_date
  AND COALESCE (check_in_review.average_rating, 0.0) BETWEEN :minRange AND :maxRange
  AND (:managerId::INT[] = '{-99}' OR (current_first_manager.id = ANY (:managerId::INT[])
  OR current_second_manager.id = ANY (:managerId::INT[]))
  AND ((firstManagerData.manager_id IS NULL OR firstManagerData.emp_id != firstManagerData.manager_id)
  AND (secondManagerData.manager_id IS NULL OR secondManagerData.emp_id != secondManagerData.manager_id)))
  AND (:reviewToId::INT[] = '{-99}' OR employees.id = ANY (:reviewToId::INT[]))
  AND (:reviewCycleId::INT[] = '{-99}' OR review_cycle.id = ANY (:reviewCycleId::INT[]))
  AND (:selfReviewDraft::BOOLEAN IS NULL
  OR (COALESCE(self_review.draft, false) = :selfReviewDraft AND COALESCE(self_review.published, false) = :selfReviewPublished))
  AND (:firstManagerReviewDraft::BOOLEAN IS NULL OR (COALESCE(first_manager_review.draft, false) = :firstManagerReviewDraft
  AND COALESCE(first_manager_review.published, false) = :firstManagerReviewPublished))
  AND (:secondManagerReviewDraft::BOOLEAN IS NULL OR (COALESCE(second_manager_review.draft, false) = :secondManagerReviewDraft
  AND COALESCE(second_manager_review.published, false) = :secondManagerReviewPublished))
  AND (:checkInDraft::BOOLEAN IS NULL
  OR (COALESCE(check_in_review.draft, false) = :checkInDraft AND COALESCE(check_in_review.published, false) = :checkInPublished))
ORDER BY
  review_cycle.publish DESC,
  (daterange(review_cycle.start_date, review_cycle.end_date)) DESC;