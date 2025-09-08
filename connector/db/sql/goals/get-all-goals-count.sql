SELECT
  COUNT(*) AS goal_count
FROM
  goals gl
  LEFT JOIN review_cycle
    ON DATE(gl.target_date) BETWEEN review_cycle.start_date AND review_cycle.end_date
WHERE
  gl.organisation_id = :organisationId
  AND (:assignedTo::INT[] = '{-99}' OR gl.assigned_to = ANY(:assignedTo::INT[]))
  AND (:reviewCycleId::INT[] = '{-99}' OR review_cycle.id = ANY(:reviewCycleId::INT[]))
  AND (:goalStatus::INT[] = '{-99}' OR gl.progress_id = ANY(:goalStatus::INT[]))
  AND (:goalType::INT[] = '{-99}' OR gl.type_id = ANY(:goalType::INT[]));