SELECT COUNT(*)
FROM feedbacks f
LEFT JOIN review_cycle ON DATE(f.updated_at) BETWEEN review_cycle.start_date AND review_cycle.end_date
  AND review_cycle.organisation_id = :organisationId
WHERE f.feedback_to = :feedbackToId
    AND (:feedbackFromId::INT[] = '{-99}' OR f.feedback_from = ANY (:feedbackFromId::INT[]))
    AND (:feedbackTypeId::INT[] = '{-99}' OR f.feedback_type_id = ANY(:feedbackTypeId::INT[]))
    AND (:reviewCycleId::INT[] = '{-99}' OR review_cycle.id = ANY(:reviewCycleId::INT[]))
    AND f.is_read = FALSE
    AND f.is_draft = FALSE ;