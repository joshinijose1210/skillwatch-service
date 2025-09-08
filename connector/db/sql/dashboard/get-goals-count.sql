SELECT COUNT(g.id) AS goals_count
FROM
  goals g
  JOIN review_details ON review_details.id = g.review_details_id
  JOIN (
    SELECT start_date, end_date
    FROM review_cycle
    WHERE review_cycle.id = :reviewCycleId AND review_cycle.organisation_id = :organisationId
  ) AS data ON data.start_date BETWEEN g.created_at AND g.target_date
  JOIN review_cycle ON review_cycle.id = review_details.review_cycle_id AND review_cycle.organisation_id = :organisationId
WHERE
  review_details.review_to = :reviewToId;
