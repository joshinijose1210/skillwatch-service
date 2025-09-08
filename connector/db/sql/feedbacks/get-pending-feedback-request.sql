SELECT
    feedback_request.id,
    feedback_request.is_external_request,
    feedback_request.requested_by AS requested_by_id,
    requested_by_details.emp_id AS requested_by_emp_id,
    requested_by_details.first_name AS requested_by_first_name,
    requested_by_details.last_name AS requested_by_last_name,
    COALESCE(feedback_from_details.id, null) AS feedback_from_id,
    COALESCE(feedback_from_details.first_name, null) AS feedback_from_first_name,
    COALESCE(feedback_from_details.last_name, null) AS feedback_from_last_name,
    COALESCE(feedback_from_details.email_id, null) AS feedback_from_email_id,
    COALESCE(external_email.email_id, null) AS external_feedback_from_email_id,
    DATE(feedback_request.created_at),
    organisation_details.name AS organisation_name,
    organisation_details.time_zone AS organisation_time_zone
FROM feedback_request
JOIN employees AS requested_by_details ON requested_by_details.id = feedback_request.requested_by
JOIN employees AS feedback_to_details ON feedback_to_details.id = feedback_request.feedback_to
JOIN organisations AS organisation_details ON organisation_details.sr_no = feedback_to_details.organisation_id
LEFT JOIN employees AS feedback_from_details ON feedback_from_details.id = feedback_request.feedback_from
    AND feedback_from_details.status = TRUE
LEFT JOIN external_feedback_emails AS external_email ON feedback_request.feedback_from_external_id = external_email.id
WHERE feedback_request.is_submitted = FALSE
    AND feedback_to_details.status = TRUE ;
