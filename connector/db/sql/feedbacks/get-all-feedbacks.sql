SELECT
  feedbacks.updated_at::DATE ,
  COALESCE(feedbacks.feedback_from, null) AS feedback_from_id ,
  COALESCE(feedback_from_details.emp_id, null) AS feedback_from_employee_id ,
  COALESCE(feedback_from_details.first_name, null) AS feedback_from_first_name ,
  COALESCE(feedback_from_details.last_name, null) AS feedback_from_last_name ,
  COALESCE(feedback_from_role.role_name, null) AS feedback_from_role ,
  COALESCE(external_email.email_id, null) AS external_feedback_from_email,
  feedbacks.feedback_to AS feedback_to_id ,
  feedback_to_details.emp_id AS feedback_to_employee_id ,
  COALESCE(feedback_to_details.first_name, null) AS feedback_to_first_name ,
  COALESCE(feedback_to_details.last_name, null) AS feedback_to_last_name ,
  feedback_to_role.role_name AS feedback_to_role ,
  feedbacks.feedback,
  feedbacks.feedback_type_id,
  feedbacks.is_draft,
  feedback_types.name,
  feedback_to_details.organisation_id
FROM
  feedbacks
  JOIN employees AS feedback_to_details ON feedback_to_details.id = feedbacks.feedback_to
  JOIN employees_role_mapping_view AS feedback_to_role ON feedback_to_details.id = feedback_to_role.emp_id
  LEFT JOIN employees AS feedback_from_details ON feedback_from_details.id = feedbacks.feedback_from
  LEFT JOIN employees_role_mapping_view AS feedback_from_role ON feedback_from_details.id = feedback_from_role.emp_id
  LEFT JOIN external_feedback_emails AS external_email ON external_email.id = feedbacks.feedback_from_external_id
  INNER JOIN feedback_types ON feedbacks.feedback_type_id = feedback_types.id
  LEFT JOIN review_cycle ON DATE(feedbacks.updated_at) BETWEEN review_cycle.start_date AND review_cycle.end_date
  AND review_cycle.organisation_id = :organisationId
WHERE
  feedbacks.is_draft = false
  AND( (CAST (:fromDate  AS text) IS  NULL OR  CAST(:toDate  AS text)IS NULL) OR
        feedbacks.updated_at::DATE BETWEEN :fromDate::DATE AND :toDate::DATE)
  AND( (CAST(:search as text) IS NULL)
            OR feedbacks.feedback_to IN
            (SELECT id FROM employees WHERE
              (UPPER(employees.emp_id) LIKE  UPPER(:search)
              OR UPPER(employees.first_name) LIKE  UPPER(:search)
              OR UPPER(employees.last_name) LIKE  UPPER(:search)
              OR UPPER(employees.first_name) || ' ' || UPPER(employees.last_name) LIKE  UPPER(:search) )
            )
            OR feedbacks.feedback_from IN
            (SELECT id FROM employees WHERE
              (UPPER(employees.emp_id) LIKE  UPPER(:search)
              OR UPPER(employees.first_name) LIKE UPPER(:search)
              OR UPPER(employees.last_name) LIKE UPPER(:search)
              OR UPPER(employees.first_name) || ' ' || UPPER(employees.last_name) LIKE UPPER(:search) )
            )
            OR feedbacks.feedback_from_external_id IN
            (SELECT id FROM external_feedback_emails WHERE
                (UPPER(external_feedback_emails.email_id) LIKE UPPER(:search))
            )
        )
  AND (:feedbackTypeId::INT[] = '{-99}' OR feedbacks.feedback_type_id = ANY(:feedbackTypeId::INT[]))
  AND feedback_to_details.organisation_id = :organisationId
  AND (:reviewCycleId::INT[] = '{-99}' OR review_cycle.id = ANY(:reviewCycleId::INT[]))
ORDER BY
  CASE WHEN :sortBy = 'dateDesc' THEN feedbacks.updated_at END DESC,
  CASE WHEN :sortBy = 'dateAsc' THEN feedbacks.updated_at END ASC
OFFSET (:offset::INT)
LIMIT (:limit::INT);