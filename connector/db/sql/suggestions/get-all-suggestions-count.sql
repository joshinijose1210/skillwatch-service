SELECT
  COUNT(suggestions.id) as suggestion_count
FROM
  suggestions
  JOIN employees AS suggested_by_details ON suggested_by_details.id = suggestions.suggested_by AND suggested_by_details.organisation_id = :organisationId
  LEFT JOIN review_cycle ON DATE(suggestions.updated_at) BETWEEN review_cycle.start_date AND review_cycle.end_date
  AND review_cycle.organisation_id = :organisationId
WHERE
  (:suggestedById::INT[] = '{-99}' OR suggestions.suggested_by = ANY (:suggestedById::INT[]))
  AND (:reviewCycleId::INT[] = '{-99}' OR review_cycle.id = ANY(:reviewCycleId::INT[]))
  AND (:isDraft::BOOL[] = '{true,false}'
      OR :isDraft::BOOL[] = '{false,true}'
      OR suggestions.is_draft = ANY(:isDraft::BOOL[]))
  AND (:progressIds::INT[] = '{-99}' OR suggestions.progress_id = ANY(:progressIds::INT[]));
