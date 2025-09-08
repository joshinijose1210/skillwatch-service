INSERT INTO external_feedback_emails (email_id, organisation_id)
VALUES (:emailId, :organisationId)
ON CONFLICT (email_id, organisation_id) DO UPDATE SET email_id = EXCLUDED.email_id
RETURNING id;
