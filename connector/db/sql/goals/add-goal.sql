INSERT INTO goals(
  description, target_date, goal_id, type_id, assigned_to, created_by, organisation_id
)
VALUES(
   :description, :targetDate, :goalId, :typeId, :assignedTo, :createdBy, :organisationId
) RETURNING id;
