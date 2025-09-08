SELECT
    suggestions.id,
    suggestions.suggestion,
    suggestions.suggested_by,
    suggestions.is_draft,
    suggestions.is_anonymous
FROM suggestions
WHERE suggestions.id = :id ;
