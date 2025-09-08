SELECT
  COUNT(CASE WHEN feedback_type_id = 1 AND feedback_from = :id THEN 1 END) AS submitted_positive_count,
  COUNT(CASE WHEN feedback_type_id = 2 AND feedback_from = :id THEN 1 END) AS submitted_improvement_count,
  COUNT(CASE WHEN feedback_type_id = 3 AND feedback_from = :id THEN 1 END) AS submitted_appreciation_count,
  COUNT(CASE WHEN feedback_type_id = 1 AND feedback_to = :id THEN 1 END) AS received_positive_count,
  COUNT(CASE WHEN feedback_type_id = 2 AND feedback_to = :id THEN 1 END) AS received_improvement_count,
  COUNT(CASE WHEN feedback_type_id = 3 AND feedback_to = :id THEN 1 END) AS received_appreciation_count
FROM
  feedbacks
WHERE
  DATE(updated_at) >= :startDate
  AND DATE(updated_at) <= :endDate
  AND (feedbacks.is_draft IS NULL OR feedbacks.is_draft = false)
  AND (feedbacks.feedback_to = :id OR feedbacks.feedback_from = :id) ;
