SELECT
  review_cycle.id,
  review_cycle.start_date,
  review_cycle.end_date,
  review_cycle.publish,
  review_cycle.self_review_start_date,
  review_cycle.self_review_end_date,
  review_details.draft,
  review_details.published,
  review_details.updated_at,
  review_details.average_rating
FROM
  review_cycle
  LEFT JOIN review_details ON review_cycle.id = review_details.review_cycle_id
  AND review_details.review_type_id = ANY (:reviewTypeId::INT[])
  AND review_details.review_to = ANY (:reviewToId::INT[])
  AND review_details.review_from = ANY (:reviewFromId::INT[])
  LEFT JOIN employees ON employees.id = ANY(:reviewToId::INT[])
WHERE
  review_cycle.organisation_id = :organisationId
  AND (:reviewCycleId::INT[] = '{-99}' OR review_cycle.id = ANY (:reviewCycleId::INT[]))
  AND employees.created_at::date <= review_cycle.end_date
ORDER BY
  review_cycle.publish DESC,
  daterange(start_date, end_date) DESC
OFFSET (:offset::INT)
LIMIT (:limit::INT);