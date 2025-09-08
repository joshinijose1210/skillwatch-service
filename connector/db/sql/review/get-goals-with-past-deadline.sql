SELECT
    g.id,
    'G' || g.goal_id AS goal_id,
    g.assigned_to,
    g.created_by
FROM goals g
WHERE DATE(g.target_date) = CURRENT_DATE - INTERVAL '1 day';
