SELECT COUNT(feedback_request.id) AS feedback_request_count
FROM
  feedback_request
  INNER JOIN employees AS requested_by
    ON feedback_request.requested_by = requested_by.id
    AND requested_by.organisation_id = :organisationId
    AND requested_by.status = true
  INNER JOIN employees AS feedback_to
    ON feedback_request.feedback_to = feedback_to.id
    AND feedback_to.organisation_id = :organisationId
    AND feedback_to.status = true
  LEFT JOIN employees AS feedback_from
    ON feedback_request.feedback_from = feedback_from.id
    AND feedback_from.organisation_id = :organisationId
  LEFT JOIN external_feedback_emails AS external_email
    ON feedback_request.feedback_from_external_id = external_email.id
    AND external_email.organisation_id = :organisationId
  LEFT JOIN review_cycle ON DATE(feedback_request.created_at) BETWEEN review_cycle.start_date AND review_cycle.end_date
    AND review_cycle.organisation_id = :organisationId
WHERE
  (:requestedById::INT[] = '{-99}' OR feedback_request.requested_by = ANY(:requestedById::INT[]))
  AND (:feedbackFromId::INT[] = '{-99}' OR feedback_request.feedback_from = ANY(:feedbackFromId::INT[]))
  AND (:feedbackToId::INT[] = '{-99}' OR feedback_request.feedback_to = ANY(:feedbackToId::INT[]))
  AND (:isSubmitted::BOOL[] = '{true,false}' OR feedback_request.is_submitted = ANY(:isSubmitted::BOOL[]))
  AND (:reviewCycleId::INT[] = '{-99}' OR review_cycle.id = ANY(:reviewCycleId::INT[]));