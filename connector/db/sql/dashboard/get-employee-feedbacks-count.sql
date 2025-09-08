SELECT COUNT(feedback_types.name) AS employee_feedbacks_count
FROM
  (
    SELECT
      employees.id,
      employees.emp_id,
      employees.first_name,
      employees.last_name,
      review_cycle.id AS review_cycle_id,
      review_cycle.start_date AS start_date,
      review_cycle.end_date AS end_date
    FROM
      employees
      INNER JOIN review_cycle ON employees.organisation_id = review_cycle.organisation_id
    WHERE
      review_cycle.id = ANY(:reviewCycleId::INT[])
      AND employees.organisation_id = :organisationId
  ) AS review
  LEFT JOIN feedbacks ON review.id = feedbacks.feedback_to
    AND DATE(feedbacks.updated_at) BETWEEN review.start_date AND review.end_date
  JOIN feedback_types ON feedbacks.feedback_type_id = feedback_types.id
  LEFT JOIN employees_role_mapping AS feedback_to_role_mapping ON feedback_to_role_mapping.emp_id = review.id
  LEFT JOIN roles AS feedback_to_role ON feedback_to_role.id = feedback_to_role_mapping.role_id
  LEFT JOIN employees AS feedback_from ON feedbacks.feedback_from = feedback_from.id
  LEFT JOIN employees_role_mapping AS feedback_from_role_mapping ON feedback_from_role_mapping.emp_id = feedback_from.id
  LEFT JOIN roles AS feedback_from_role ON feedback_from_role.id = feedback_from_role_mapping.role_id
  LEFT JOIN external_feedback_emails AS external_email ON feedbacks.feedback_from_external_id = external_email.id
  WHERE
    feedbacks.is_draft = false
    AND CASE WHEN :id::INT[] = '{-99}' THEN 1 = 1 ELSE feedbacks.feedback_to = ANY (:id::INT[]) END
    AND feedbacks.feedback_type_id = ANY (:feedbackTypeId::INT[])
    AND DATE(feedbacks.updated_at) BETWEEN review.start_date AND review.end_date ;
