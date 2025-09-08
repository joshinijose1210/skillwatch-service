SELECT
  COUNT(CASE WHEN feedback_type_id = 1 AND feedback_to = employees.id THEN 1 END) AS positive_count,
  COUNT(CASE WHEN feedback_type_id = 2 AND feedback_to = employees.id THEN 1 END) AS improvement_count,
  COUNT(CASE WHEN feedback_type_id = 3 AND feedback_to = employees.id THEN 1 END) AS appreciation_count
FROM
  feedbacks
  LEFT JOIN employees
  ON employees.id = feedbacks.feedback_to
WHERE
  DATE(feedbacks.updated_at) >= :startDate
  AND DATE(feedbacks.updated_at) <= :endDate
  AND (feedbacks.is_draft IS NULL OR feedbacks.is_draft = false)
  AND employees.organisation_id = :organisationId;
