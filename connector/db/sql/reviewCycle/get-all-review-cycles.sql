SELECT
review_cycle.organisation_id,
review_cycle.id,
review_cycle.start_date,
review_cycle.end_date,
review_cycle.publish,
review_cycle.last_modified,
review_cycle.self_review_start_date,
review_cycle.self_review_end_date,
review_cycle.manager_review_start_date,
review_cycle.manager_review_end_date,
review_cycle.check_in_start_date,
review_cycle.check_in_end_date
FROM
review_cycle
WHERE
review_cycle.organisation_id = :organisationId
ORDER BY
  review_cycle.publish DESC,
  (daterange(review_cycle.start_date, review_cycle.end_date)) DESC
OFFSET (:offset::INT)
LIMIT (:limit::INT);