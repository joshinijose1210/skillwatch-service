package goals

import io.kotest.core.spec.Spec
import io.kotest.matchers.shouldBe
import norm.executeCommand
import scalereal.core.models.domain.Goal
import scalereal.db.goals.GoalRepositoryImpl
import util.StringSpecWithDataSource
import java.sql.Date
import java.sql.Timestamp

class GoalRepositoryImplTest : StringSpecWithDataSource() {
    private lateinit var goalRepositoryImpl: GoalRepositoryImpl

    init {

        "should fetch goals for a given reviewTo and reviewCycleId" {
            val itemsForCycle3 =
                goalRepositoryImpl.getGoalsForCycle(
                    goalToId = 1L,
                    reviewCycleId = 3L,
                )

            itemsForCycle3 shouldBe
                listOf(
                    Goal(
                        id = 4L,
                        goalId = "G4",
                        typeId = 1,
                        description = "Item 3.1",
                        createdAt = Timestamp.valueOf("2023-04-01 10:00:00"),
                        targetDate = Date.valueOf("2023-06-01"),
                        progressId = 1,
                        createdBy = 2,
                        assignedTo = 1,
                    ),
                )

            val itemsForCycle1 =
                goalRepositoryImpl.getGoalsForCycle(
                    goalToId = 1L,
                    reviewCycleId = 1L,
                )

            itemsForCycle1 shouldBe
                listOf(
                    Goal(
                        id = 1L,
                        goalId = "G1",
                        typeId = 1,
                        description = "Item 1.1",
                        createdAt = Timestamp.valueOf("2022-06-01 10:00:00"),
                        targetDate = Date.valueOf("2022-06-01"),
                        progressId = 1,
                        progressName = null,
                        createdBy = 2,
                        assignedTo = 1,
                    ),
                    Goal(
                        id = 2L,
                        goalId = "G2",
                        typeId = 1,
                        description = "Item 1.2",
                        createdAt = Timestamp.valueOf("2022-06-15 10:00:00"),
                        targetDate = Date.valueOf("2022-06-01"),
                        progressId = 1,
                        progressName = null,
                        createdBy = 2,
                        assignedTo = 1,
                    ),
                )
        }

        "should update progress of an existing goal" {
            val originalId = 4L
            val newProgressId = 2

            val result = goalRepositoryImpl.updateGoalProgress(originalId, newProgressId)

            result shouldBe
                Goal(
                    id = originalId,
                    goalId = "G4",
                    typeId = 1,
                    description = "Item 3.1",
                    createdAt = Timestamp.valueOf("2023-04-01 10:00:00"),
                    targetDate = Date.valueOf("2023-06-01"),
                    progressId = newProgressId,
                    progressName = null,
                    createdBy = 2,
                    assignedTo = 1,
                )
        }

        "should return true if goal exists" {
            val existingId = 1L
            val result = goalRepositoryImpl.isGoalExists(existingId)
            result shouldBe true
        }

        "should return false if goal does not exist" {
            val nonExistingId = 999L
            val result = goalRepositoryImpl.isGoalExists(nonExistingId)
            result shouldBe false
        }
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        dataSource.connection.use {
            it.executeCommand(
                """
                INSERT INTO organisations(sr_no, admin_id, name, is_active, organisation_size)
                VALUES (1,1,'ScaleReal Technologies Pvt. Ltd.', true, 50);

                INSERT INTO employees(id, emp_id, first_name, last_name, email_id, contact_no, status, onboarding_flow, organisation_id)
                VALUES
                    (1, 'SR0051', 'Rushad', 'Shaikh', 'rushad.shaikh@scalereal.com', 8265079426, true, false, 1),
                    (2, 'SR0006', 'Yogesh', 'Jadhav', 'yogesh.jadhav@scalereal.com', 9876543210, true, false, 1);

                INSERT INTO roles(role_id, role_name, status, organisation_id)
                VALUES (1, 'Manager', true, 1), (2, 'Employee', true, 1);

                INSERT INTO employees_role_mapping(emp_id, role_id)
                VALUES (1, 2), (2, 1);

                INSERT INTO review_types(name)
                SELECT val FROM (VALUES ('Self Review'), ('Manager Review'), ('Summary')) AS vals(val)
                WHERE NOT EXISTS (SELECT 1 FROM review_types WHERE name = vals.val);

                INSERT INTO review_cycle(
                    id, organisation_id, start_date, end_date, publish,
                    self_review_start_date, self_review_end_date,
                    manager_review_start_date, manager_review_end_date,
                    check_in_start_date, check_in_end_date)
                VALUES
                    (1, 1, '2022-01-01','2022-06-30', false, '2022-01-05','2022-01-15','2022-01-20','2022-01-25','2022-02-01','2022-02-10'),
                    (2, 1, '2022-07-01','2022-12-31', false, '2022-07-05','2022-07-15','2022-07-20','2022-07-25','2022-08-01','2022-08-10'),
                    (3, 1, '2023-01-01','2023-06-30', false, '2023-01-05','2023-01-15','2023-01-20','2023-01-25','2023-02-01','2023-02-10'),
                    (4, 1, '2023-07-01','2023-12-31', true, '2023-07-05','2023-07-15','2023-07-20','2023-07-25','2023-08-01','2023-08-10');

                INSERT INTO review_details(id, review_cycle_id, review_to, review_from, updated_at, draft, published, review_type_id)
                VALUES
                    (1, 1, 1, 2, '2022-02-15 10:00:00+00', false, true, 3),
                    (2, 2, 1, 2, '2022-08-10 10:00:00+00', false, true, 3),
                    (3, 3, 1, 2, '2023-03-15 10:00:00+00', false, true, 3);

                INSERT INTO goals(id, goal_id, organisation_id, assigned_to, created_by, description, type_id, progress_id, created_at, target_date)
                VALUES
                    (1, 1, 1, 1, 2, 'Item 1.1', 1, 1, '2022-06-01 10:00:00+00', '2022-06-01'),
                    (2, 2, 1, 1, 2, 'Item 1.2', 1, 1, '2022-06-15 10:00:00+00', '2022-06-01'),
                    (3, 3, 1, 1, 2, 'Item 2.1', 1, 1, '2022-10-01 10:00:00+00', '2022-12-01'),
                    (4, 4, 1, 1, 2, 'Item 3.1', 1, 1, '2023-04-01 10:00:00+00', '2023-06-01');
                """.trimIndent(),
            )
        }
        goalRepositoryImpl = GoalRepositoryImpl(dataSource)
    }
}
