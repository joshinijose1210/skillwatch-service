SELECT
  feedbacks.sr_no,
  feedbacks.updated_at,
  feedbacks.feedback_from AS feedback_from_id,
  emp_from.emp_id AS feedback_from_employee_id,
  feedbacks.feedback_to AS feedback_to_id,
  emp_to.emp_id AS feedback_to_employee_id,
  emp_to.organisation_id,
  emp_to.first_name,
  emp_to.last_name,
  roles.role_name,
  feedbacks.feedback,
  feedbacks.feedback_type_id,
  feedback_types.name,
  feedbacks.is_draft,
  feedbacks.is_read
FROM
  feedbacks
  INNER JOIN employees AS emp_to ON feedbacks.feedback_to = emp_to.id AND emp_to.organisation_id = :organisationId
  INNER JOIN employees AS emp_from ON feedbacks.feedback_from = emp_from.id AND emp_from.organisation_id = :organisationId
  INNER JOIN feedback_types ON feedbacks.feedback_type_id = feedback_types.id
  JOIN employees_role_mapping ON employees_role_mapping.emp_id = emp_to.id
  JOIN roles ON roles.id = employees_role_mapping.role_id
WHERE
  feedbacks.sr_no = :feedbackId;