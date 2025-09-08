SELECT
  g.id,
  'G' || g.goal_id AS goal_id,
  g.type_id,
  g.description,
  g.created_at,
  g.target_date,
  g.progress_id,
  g.created_by,
  g.assigned_to
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
  g.assigned_to = :reviewToId
OFFSET (:offset::INT)
LIMIT (:limit::INT) ;
