INSERT INTO review_cycle(
start_date,
end_date,
publish,
self_review_start_date,
self_review_end_date,
manager_review_start_date,
manager_review_end_date,
check_in_start_date,
check_in_end_date,
organisation_id
)
VALUES
(
    :start_date,
    :end_date,
    :publish,
    :self_review_start_date,
    :self_review_end_date,
    :manager_review_start_date,
    :manager_review_end_date,
    :check_in_start_date,
    :check_in_end_date,
    :organisation_id
    ) RETURNING *;