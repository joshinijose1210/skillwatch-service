INSERT INTO feedbacks(
  feedback,
  feedback_to,
  feedback_from,
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
   :isDraft,
   CURRENT_TIMESTAMP
  ) RETURNING *;

