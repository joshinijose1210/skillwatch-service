SELECT COUNT(review_cycle.id)
as review_cycle_count
FROM review_cycle
WHERE
review_cycle.organisation_id = :organisationId ;