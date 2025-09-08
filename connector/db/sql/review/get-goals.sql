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
WHERE
  review_details_id = :reviewDetailsId ;
