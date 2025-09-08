UPDATE suggestions SET
    suggestion = :suggestion,
    is_draft = :is_draft,
    is_anonymous = :is_anonymous,
    updated_at = CURRENT_TIMESTAMP
WHERE id = :id AND is_draft = TRUE
RETURNING id, suggestion, suggested_by, is_draft, is_anonymous ;
