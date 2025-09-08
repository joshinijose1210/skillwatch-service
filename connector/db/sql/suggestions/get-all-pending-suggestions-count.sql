SELECT
  COUNT(suggestions.id) as suggestion_count
FROM
  suggestions
  JOIN employees AS suggested_by_details ON suggested_by_details.id = suggestions.suggested_by AND suggested_by_details.organisation_id = :organisationId
  LEFT JOIN review_cycle ON DATE(suggestions.updated_at) BETWEEN review_cycle.start_date AND review_cycle.end_date
  AND review_cycle.organisation_id = :organisationId
WHERE
  progress_id = 1 AND is_draft = false
  AND (:reviewCycleId::INT[] = '{-99}' OR review_cycle.id = ANY(:reviewCycleId::INT[]));