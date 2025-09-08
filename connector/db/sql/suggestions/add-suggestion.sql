INSERT INTO suggestions(
    suggestion,
    suggested_by,
    is_draft,
    is_anonymous,
    created_at,
    updated_at
) VALUES(
    :suggestion,
    :suggested_by,
    :is_draft,
    :is_anonymous,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) RETURNING id, suggestion, suggested_by, is_draft, is_anonymous ;
