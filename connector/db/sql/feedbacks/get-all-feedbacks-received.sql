SELECT
  feedbacks.sr_no,
  feedbacks.updated_at,
  feedbacks.feedback_to AS feedback_to_id,
  emp_to.emp_id AS feedback_to_employee_id,
  feedbacks.feedback_from AS feedback_from_id,
  emp_to.organisation_id,
  COALESCE(emp_from.emp_id, null) AS feedback_from_employee_id,
  COALESCE(emp_from.first_name, null) AS feedback_from_first_name,
  COALESCE(emp_from.last_name, null) AS feedback_from_last_name,
  COALESCE(roles.role_name, null) AS feedback_from_role,
  COALESCE(external_email.email_id, null) AS external_feedback_from_email,
  feedbacks.feedback,
  feedbacks.feedback_type_id,
  feedback_types.name,
  feedbacks.is_draft
FROM
  feedbacks
  LEFT JOIN employees AS emp_from ON feedbacks.feedback_from = emp_from.id AND emp_from.organisation_id = :organisationId
  INNER JOIN employees AS emp_to ON feedbacks.feedback_to = emp_to.id AND emp_to.organisation_id = :organisationId
  LEFT JOIN external_feedback_emails AS external_email
      ON feedbacks.feedback_from_external_id = external_email.id
      AND external_email.organisation_id = :organisationId
  INNER JOIN feedback_types ON feedbacks.feedback_type_id = feedback_types.id
  LEFT JOIN employees_role_mapping ON employees_role_mapping.emp_id = emp_from.id
  LEFT JOIN roles ON roles.id = employees_role_mapping.role_id
  LEFT JOIN review_cycle ON DATE(feedbacks.updated_at) BETWEEN review_cycle.start_date AND review_cycle.end_date
  AND review_cycle.organisation_id = :organisationId
WHERE
  feedbacks.is_draft = false
  AND feedbacks.feedback_to = :feedbackToId
  AND (:feedbackFromId::INT[] = '{-99}' OR feedbacks.feedback_from = ANY (:feedbackFromId::INT[]))
  AND (:feedbackTypeId::INT[] = '{-99}' OR feedbacks.feedback_type_id = ANY(:feedbackTypeId::INT[]))
  AND (:reviewCycleId::INT[] = '{-99}' OR review_cycle.id = ANY(:reviewCycleId::INT[]))
ORDER BY
  CASE WHEN :sortBy = 'dateDesc' THEN feedbacks.updated_at END DESC,
  CASE WHEN :sortBy = 'dateAsc' THEN feedbacks.updated_at END ASC
OFFSET (:offset::INT)
LIMIT (:limit::INT);