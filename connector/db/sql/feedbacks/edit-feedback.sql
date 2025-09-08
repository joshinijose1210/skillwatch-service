UPDATE
  feedbacks
SET
  feedback = :feedback,
  feedback_to = :feedbackToId,
  feedback_type_id = :feedbackTypeId,
  request_id = :requestId,
  is_draft = :idDraft,
  updated_at = CURRENT_TIMESTAMP
WHERE
  sr_no = :id
  AND is_draft = true
  RETURNING *;