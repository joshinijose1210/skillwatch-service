SELECT
    is_manager_review_mandatory,
    is_anonymous_suggestion_allowed,
    is_biweekly_feedback_reminder_enabled,
    time_zone
FROM organisations
WHERE sr_no = :id ;
