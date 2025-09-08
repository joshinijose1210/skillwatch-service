SELECT
  feedbacks.sr_no,
  feedbacks.feedback,
  feedbacks.feedback_type_id,
  feedback_types.name AS feedback_type,
  feedbacks.is_draft
FROM
  feedbacks
  JOIN feedback_types ON feedbacks.feedback_type_id = feedback_types.id
WHERE
  feedbacks.request_id = :request_id;