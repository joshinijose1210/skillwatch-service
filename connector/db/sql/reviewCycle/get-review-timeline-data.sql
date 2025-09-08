SELECT
  review_cycle.id,
  review_cycle.organisation_id AS organisation_id,
  review_cycle.start_date,
  review_cycle.end_date,
  review_cycle.publish,
  review_cycle.self_review_start_date,
  review_cycle.self_review_end_date,
  review_cycle.manager_review_start_date,
  review_cycle.manager_review_end_date,
  review_cycle.check_in_start_date,
  review_cycle.check_in_end_date,
  self_review_details.draft AS self_review_draft,
  self_review_details.published AS self_review_publish,
  self_review_details.updated_at AS self_review_date,
  self_review_details.average_rating AS self_average_rating,
  first_manager_review_details.draft AS first_manager_review_draft,
  first_manager_review_details.published AS first_manager_review_publish,
  first_manager_review_details.updated_at AS first_manager_review_date,
  first_manager_review_details.average_rating AS first_manager_average_rating,
  second_manager_review_details.draft AS second_manager_review_draft,
  second_manager_review_details.published AS second_manager_review_publish,
  second_manager_review_details.updated_at AS second_manager_review_date,
  second_manager_review_details.average_rating AS second_manager_average_rating,
  check_in_with_manager.review_from AS check_in_from_id,
  check_in_with_manager.draft AS check_in_with_manager_draft,
  check_in_with_manager.published AS check_in_with_manager_publish,
  check_in_with_manager.updated_at AS check_in_with_manager_date,
  check_in_with_manager.average_rating AS check_in_with_manager_average_rating,
  COALESCE(check_in_from_details.emp_id, null) AS check_in_from_employee_id,
  COALESCE(check_in_from_details.first_name, null) AS check_in_from_first_name,
  COALESCE(check_in_from_details.last_name, null) AS check_in_from_last_name,
  COALESCE(first_manager_details.id, null) as first_manager_id,
  COALESCE(first_manager_details.emp_id, null) as first_manager_employee_id,
  COALESCE(first_manager_details.first_name, null) as first_manager_first_name,
  COALESCE(first_manager_details.last_name, null) as first_manager_last_name,
  COALESCE(second_manager_details.id, null) as second_manager_id,
  COALESCE(second_manager_details.emp_id, null) as second_manager_employee_id,
  COALESCE(second_manager_details.first_name, null) as second_manager_firstname,
  COALESCE(second_manager_details.last_name, null) as second_manager_last_name,
  COALESCE((SELECT true FROM employee_manager_mapping WHERE employee_manager_mapping.manager_id = :reviewToId LIMIT 1), false) AS is_or_was_manager
FROM
  review_cycle
  LEFT JOIN employees ON employees.id = :reviewToId
  LEFT JOIN employee_manager_mapping_view AS firstManagerMapping ON firstManagerMapping.emp_id = :reviewToId
  AND firstManagerMapping.type = 1
  LEFT JOIN employee_manager_mapping_view AS secondManagerMapping ON secondManagerMapping.emp_id = :reviewToId
  AND secondManagerMapping.type = 2
  LEFT JOIN review_details AS self_review_details
  ON self_review_details.review_cycle_id = review_cycle.id
  AND self_review_details.review_type_id = 1
  AND self_review_details.review_to = :reviewToId
  LEFT JOIN review_details AS first_manager_review_details
  ON first_manager_review_details.review_cycle_id = review_cycle.id
  AND first_manager_review_details.review_type_id = 2
  AND first_manager_review_details.review_to = :reviewToId
  AND first_manager_review_details.review_from = firstManagerMapping.manager_id
  LEFT JOIN review_details AS second_manager_review_details
  ON second_manager_review_details.review_cycle_id = review_cycle.id
  AND second_manager_review_details.review_type_id = 2
  AND second_manager_review_details.review_to = :reviewToId
  AND second_manager_review_details.review_from = secondManagerMapping.manager_id
  LEFT JOIN review_details AS check_in_with_manager
  ON check_in_with_manager.review_cycle_id = review_cycle.id
  AND check_in_with_manager.review_type_id = 3
  AND check_in_with_manager.review_to = :reviewToId
  LEFT JOIN employees AS first_manager_details
  ON first_manager_details.id = firstManagerMapping.manager_id
  AND first_manager_details.status = true
  AND first_manager_details.organisation_id = :organisationId
  LEFT JOIN employees AS second_manager_details
  ON second_manager_details.id = secondManagerMapping.manager_id
  AND second_manager_details.status = true
  AND second_manager_details.organisation_id = :organisationId
  LEFT JOIN employees AS check_in_from_details
  ON check_in_from_details.id = check_in_with_manager.review_from
  AND check_in_from_details.organisation_id = :organisationId
WHERE
  review_cycle.organisation_id = :organisationId
  AND employees.status = true
  AND review_cycle.publish = true
ORDER BY
  review_cycle.start_date DESC limit 1;
