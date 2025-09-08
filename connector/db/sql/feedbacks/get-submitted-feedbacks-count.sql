SELECT COUNT(feedbacks.feedback_to) as feedback_count
FROM
  feedbacks
  INNER JOIN employees AS emp_to ON feedbacks.feedback_to = emp_to.id AND emp_to.organisation_id = :organisationId
  INNER JOIN employees AS emp_from ON feedbacks.feedback_from = emp_from.id AND emp_from.organisation_id = :organisationId
  INNER JOIN feedback_types ON feedbacks.feedback_type_id = feedback_types.id
  JOIN employees_role_mapping ON employees_role_mapping.emp_id = emp_to.id
  JOIN roles ON roles.id = employees_role_mapping.role_id
  LEFT JOIN review_cycle ON DATE(feedbacks.updated_at) BETWEEN review_cycle.start_date AND review_cycle.end_date
  AND review_cycle.organisation_id = :organisationId
WHERE
  feedbacks.feedback_from = :feedbackFromId
  AND NOT (feedbacks.is_draft = true AND emp_to.status = false)
  AND (:feedbackToId::INT[] = '{-99}' OR feedbacks.feedback_to = ANY(:feedbackToId::INT[]))
  AND (:feedbackTypeId::INT[] = '{-99}' OR feedbacks.feedback_type_id = ANY(:feedbackTypeId::INT[]))
  AND (:reviewCycleId::INT[] = '{-99}' OR review_cycle.id = ANY(:reviewCycleId::INT[]))
  AND (feedbacks.request_id IS NULL OR (feedbacks.request_id IS NOT NULL AND feedbacks.is_draft = false));
