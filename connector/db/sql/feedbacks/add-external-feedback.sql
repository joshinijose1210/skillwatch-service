INSERT INTO feedbacks(
  feedback,
  feedback_to,
  feedback_from_external_id,
  feedback_type_id,
  request_id,
  is_draft,
  updated_at
)
VALUES
  (
   :feedback,
   :feedbackToId,
   :feedbackFromId,
   :feedbackTypeId,
   :requestId,
   false,
   CURRENT_TIMESTAMP
  ) RETURNING *;
