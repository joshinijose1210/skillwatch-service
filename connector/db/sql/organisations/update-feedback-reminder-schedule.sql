INSERT INTO feedback_reminder_schedule (
    organisation_id,
    last_sent_at,
    last_reminder_index
) VALUES (
    :organisationId,
    :lastSentAt,
    :lastReminderIndex
)
ON CONFLICT (organisation_id) DO UPDATE
SET
    last_sent_at = EXCLUDED.last_sent_at,
    last_reminder_index = EXCLUDED.last_reminder_index;
