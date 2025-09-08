DELETE FROM feedbacks
WHERE sr_no = :id
AND is_draft = TRUE;
