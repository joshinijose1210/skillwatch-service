SELECT
  review_cycle.id AS review_cycle_id,
  review_cycle.start_date,
  review_cycle.end_date,
  review_cycle.manager_review_start_date,
  review_cycle.manager_review_end_date,
  review_cycle.publish,
  employees.id AS review_to_id,
  employees.emp_id AS review_to_employee_id,
  employees.first_name,
  employees.last_name,
  employees_team_mapping_view.team_name,
  review_details.draft,
  review_details.published,
  review_details.average_rating
FROM
  review_cycle
  LEFT JOIN employee_manager_mapping AS emm
    ON ((emm.manager_id = :reviewFromId AND type = 1)
        OR (emm.manager_id = :reviewFromId AND type = 2))
  LEFT JOIN employees ON employees.id = emm.emp_id
  LEFT JOIN review_details ON review_cycle.id = review_details.review_cycle_id
    AND review_details.review_type_id = :reviewTypeId
    AND review_details.review_from = :reviewFromId
    AND review_details.review_to = emm.emp_id
  LEFT JOIN employees_team_mapping_view ON employees.id = employees_team_mapping_view.emp_id
WHERE
  review_cycle.organisation_id = :organisationId
  AND emm.emp_id != :reviewFromId
  AND (emm.created_at::date <= review_cycle.self_review_end_date
        AND (emm.updated_at::date IS NULL OR emm.updated_at::date >= review_cycle.end_date))
  AND employees.status = true
  AND employees.created_at::date <= review_cycle.end_date
  AND (:reviewToId::INT[] = '{-99}' OR emm.emp_id = ANY (:reviewToId::INT[]))
  AND (:reviewCycleId::INT[] = '{-99}' OR review_cycle.id = ANY (:reviewCycleId::INT[]))
  AND (:managerReviewDraft::BOOLEAN IS NULL OR (COALESCE(review_details.draft, false) = :managerReviewDraft
  AND COALESCE(review_details.published, false) = :managerReviewPublished))
ORDER BY
  review_cycle.publish DESC,
  daterange(review_cycle.start_date, review_cycle.end_date) DESC
OFFSET (:offset::INT)
LIMIT (:limit::INT);