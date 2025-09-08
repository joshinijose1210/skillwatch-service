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
JOIN
  review_cycle rc
  ON g.organisation_id = rc.organisation_id
WHERE
  g.assigned_to = :goalToId
  AND rc.id = :reviewCycleId
  AND g.target_date BETWEEN rc.start_date AND rc.end_date;
