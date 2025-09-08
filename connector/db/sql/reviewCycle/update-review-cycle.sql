UPDATE
review_cycle
SET
start_date = :start_date,
end_date = :end_date,
publish = :publish,
last_modified = CURRENT_TIMESTAMP,
self_review_start_date = :self_review_start_date,
self_review_end_date = :self_review_end_date,
manager_review_start_date = :manager_review_start_date,
manager_review_end_date = :manager_review_end_date,
check_in_start_date = :check_in_start_date,
check_in_end_date = :check_in_end_date
WHERE
organisation_id = :organisation_id
AND id = :id RETURNING *;