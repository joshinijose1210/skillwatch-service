INSERT INTO link_details(id, generation_time, no_of_hit, purpose)
 VALUES (:linkId, :generationTime, :noOfHit, :purpose) RETURNING *;
