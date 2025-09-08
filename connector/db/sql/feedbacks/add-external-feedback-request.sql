INSERT INTO feedback_request( requested_by, feedback_to, feedback_from_external_id, request, is_submitted, is_external_request)
VALUES(:requestedBy, :feedbackToId, :feedbackFromId, :request, false, :isExternalRequest) RETURNING * ;
