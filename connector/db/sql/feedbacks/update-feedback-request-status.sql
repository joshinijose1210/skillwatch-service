UPDATE feedback_request
SET
  is_submitted = true
WHERE
  feedback_request.id = :requestId;
