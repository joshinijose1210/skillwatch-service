SELECT
  employees.id,
  employees.emp_id AS employee_id,
  employees.first_name,
  employees.last_name,
  firstManagerMapping.manager_id AS first_manager_id,
  COALESCE(secondManagerMapping.manager_id, null) AS second_manager_id,
  self_review_details.draft AS self_review_draft,
  self_review_details.published AS self_review_publish,
  self_review_details.updated_at::DATE AS self_review_date,
  first_manager_review_details.draft AS first_manager_review_draft,
  first_manager_review_details.published AS first_manager_review_published,
  first_manager_review_details.updated_at::DATE AS first_manager_review_date,
  second_manager_review_details.draft AS second_manager_review_draft,
  second_manager_review_details.published AS second_manager_review_published,
  second_manager_review_details.updated_at::DATE AS second_manager_review_date,
  check_in_with_manager.review_from AS check_in_from_id,
  COALESCE(check_in_from_details.emp_id, null) AS check_in_from_employee_id,
  COALESCE(check_in_from_details.first_name, null) AS check_in_from_first_name,
  COALESCE(check_in_from_details.last_name, null) AS check_in_from_last_name,
  check_in_with_manager.draft AS check_in_with_manager_draft,
  check_in_with_manager.published AS check_in_with_manager_publish,
  check_in_with_manager.updated_at::DATE AS check_in_with_manager_date
FROM
  employees
  JOIN employee_manager_mapping_view AS firstManagerMapping ON employees.id = firstManagerMapping.emp_id
  AND firstManagerMapping.type = 1
  LEFT JOIN employee_manager_mapping_view AS secondManagerMapping ON employees.id = secondManagerMapping.emp_id
  AND secondManagerMapping.type = 2
  LEFT JOIN review_details AS first_manager_review_details
  ON first_manager_review_details.review_cycle_id = :reviewCycleId
  AND first_manager_review_details.review_type_id = 2
  AND first_manager_review_details.review_to = employees.id
  AND first_manager_review_details.review_from = firstManagerMapping.manager_id
  LEFT JOIN review_details AS second_manager_review_details
  ON second_manager_review_details.review_cycle_id = :reviewCycleId
  AND second_manager_review_details.review_type_id = 2
  AND second_manager_review_details.review_to = employees.id
  AND second_manager_review_details.review_from = secondManagerMapping.manager_id
  LEFT JOIN review_details AS check_in_with_manager
  ON check_in_with_manager.review_cycle_id = :reviewCycleId
  AND check_in_with_manager.review_type_id = 3
  AND check_in_with_manager.review_to = employees.id
  LEFT JOIN review_details AS self_review_details
  ON self_review_details.review_cycle_id = :reviewCycleId
  AND self_review_details.review_type_id = 1
  AND self_review_details.review_to = employees.id
  AND self_review_details.review_from = employees.id
  LEFT JOIN employees AS check_in_from_details
  ON check_in_from_details.id = check_in_with_manager.review_from
  AND check_in_from_details.organisation_id = :organisationId
WHERE
  (firstManagerMapping.manager_id = :reviewFromId OR secondManagerMapping.manager_id = :reviewFromId)
  AND (firstManagerMapping.emp_id != :reviewFromId OR secondManagerMapping.emp_id != :reviewFromId)
  AND employees.organisation_id = :organisationId
  AND employees.status = true;
