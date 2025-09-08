SELECT
    organisations.sr_no,
    organisations.is_biweekly_feedback_reminder_enabled,
    COALESCE(frs.last_sent_at, NULL) AS last_sent_at,
    COALESCE(frs.last_reminder_index, NULL) AS last_reminder_index
FROM feedback_reminder_schedule frs
RIGHT JOIN organisations ON
    organisations.sr_no = frs.organisation_id
WHERE organisations.sr_no = :organisationId ;
