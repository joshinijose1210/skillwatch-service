SELECT EXISTS (
  SELECT 1 FROM goals WHERE
      id = :id
);
