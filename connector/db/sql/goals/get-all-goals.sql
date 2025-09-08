SELECT
  gl.id,
  gl.description,
  gl.target_date,
  gl.progress_id,
  gl.created_by,
  CONCAT(cb.first_name, ' ', cb.last_name) AS created_by_name,
  gl.assigned_to,
  CONCAT(ab.first_name, ' ', ab.last_name) AS assigned_to_name,
  gl.type_id,
  CONCAT('G', gl.goal_id) AS goal_id
FROM
  goals gl
  LEFT JOIN review_cycle
    ON DATE(gl.target_date) BETWEEN review_cycle.start_date AND review_cycle.end_date
  LEFT JOIN employees cb ON gl.created_by = cb.id
  LEFT JOIN employees ab ON gl.assigned_to = ab.id
WHERE
  gl.organisation_id = :organisationId
  AND (:assignedTo::INT[] = '{-99}' OR gl.assigned_to = ANY(:assignedTo::INT[]))
  AND (:reviewCycleId::INT[] = '{-99}' OR review_cycle.id = ANY(:reviewCycleId::INT[]))
  AND (:progressId::INT[] = '{-99}' OR gl.progress_id = ANY(:progressId::INT[]))
  AND (:typeId::INT[] = '{-99}' OR gl.type_id = ANY(:typeId::INT[]))
ORDER BY
  gl.goal_id DESC
OFFSET (:offset::INT)
LIMIT (:limit::INT);