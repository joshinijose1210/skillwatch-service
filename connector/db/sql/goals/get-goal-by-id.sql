SELECT
  id,
  description,
  created_at,
  target_date,
  progress_id,
  created_by,
  assigned_to,
  goal_id,
  type_id
FROM
  goals
WHERE
goals.id = :id;