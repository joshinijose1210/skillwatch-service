UPDATE organisations
SET
  is_manager_review_mandatory = :managerReviewMandatory,
  is_anonymous_suggestion_allowed = :anonymousSuggestionAllowed,
  is_biweekly_feedback_reminder_enabled = :isBiWeeklyFeedbackReminderEnabled
WHERE sr_no = :id;
