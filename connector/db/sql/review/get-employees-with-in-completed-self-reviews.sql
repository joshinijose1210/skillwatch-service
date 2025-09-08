SELECT
    employees.organisation_id,
    employees.id,
    employees.first_name,
    employees.last_name,
    employees.emp_id,
    employees.email_id,
    employees.contact_no
FROM employees
LEFT JOIN review_details ON
    employees.id  = review_details.review_to
    AND employees.id = review_details.review_from
    AND review_details.review_type_id = 1
    AND review_details.review_cycle_id = :review_cycle_id
WHERE
    employees.organisation_id = :organisation_id
    AND employees.status = TRUE
    AND ( review_details.published IS NULL OR review_details.published = FALSE ) ;