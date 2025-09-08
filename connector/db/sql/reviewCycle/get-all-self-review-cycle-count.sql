SELECT
  COUNT(review_cycle.start_date) as review_cycle_count
FROM
  review_cycle
  LEFT JOIN review_details ON review_cycle.id = review_details.review_cycle_id
  AND review_details.review_type_id = ANY (:reviewTypeId::INT[])
  AND review_details.review_to = ANY (:reviewToId::INT[])
  AND review_details.review_from = ANY (:reviewFromId::INT[])
  LEFT JOIN employees ON employees.id = ANY(:reviewToId::INT[])
WHERE review_cycle.organisation_id = :organisationId
  AND (:reviewCycleId::INT[] = '{-99}' OR review_cycle.id = ANY (:reviewCycleId::INT[]))
  AND employees.created_at::date <= review_cycle.end_date ;