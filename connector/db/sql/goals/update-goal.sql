UPDATE goals
SET
    description = COALESCE(:description, description),
    type_id = COALESCE(:typeId, type_id)
WHERE id = :id
RETURNING
    id,
    'G' || goal_id AS goal_id,
    type_id,
    description,
    created_at,
    target_date,
    progress_id,
    created_by,
    assigned_to;
