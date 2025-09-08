package userActivity

import io.kotest.core.spec.Spec
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.shouldBe
import norm.executeCommand
import scalereal.core.models.domain.UserActivity
import scalereal.core.userActivity.UserActivityService
import scalereal.db.userActivity.UserActivityRepositoryImpl
import util.StringSpecWithDataSource
import java.sql.Timestamp

class UserActivityRepositoryImplTest : StringSpecWithDataSource() {
    private lateinit var userActivityRepositoryImpl: UserActivityRepositoryImpl
    private lateinit var userActivityService: UserActivityService

    init {

        val now = Timestamp(System.currentTimeMillis())

        "should return count and fetch user activity details by organisation id" {
            val organisationId = 1L
            val expectedDetails =
                listOf(
                    UserActivity(
                        firstName = "Rushad",
                        lastName = "Shaikh",
                        employeeId = "SR0051",
                        activity = "Review Cycle Created",
                        createdAt = now,
                    ),
                    UserActivity(
                        firstName = "Yogesh",
                        lastName = "Jadhav",
                        employeeId = "SR0006",
                        activity = "Review Cycle Updated",
                        createdAt = now,
                    ),
                )
            val count = userActivityRepositoryImpl.userActivitiesCount(organisationId)
            val actualDetails = userActivityRepositoryImpl.fetchUserActivities(organisationId = organisationId, offset = 0, limit = 10)
            count shouldBe 2
            actualDetails.size shouldBe count
            actualDetails.mapIndexed { index, userActivity ->
                userActivity.activity shouldBe expectedDetails[index].activity
                userActivity.createdAt shouldBeAfter expectedDetails[index].createdAt
                userActivity.firstName shouldBe expectedDetails[index].firstName
                userActivity.lastName shouldBe expectedDetails[index].lastName
                userActivity.employeeId shouldBe expectedDetails[index].employeeId
            }
        }

        "should add new user activity" {
            userActivityRepositoryImpl.addActivity(
                actionBy = 1,
                moduleId = 1,
                activity = "Loreum Ipsum",
                description = "Loreum Ipsum",
                ipAddress = "127.0.0.1",
            ) shouldBe Unit
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
                VALUES (1, 'SR0051', 'Rushad', 'Shaikh', 'rushad.shaikh@scalereal.com', 8265079426, true, false, 1),
                       (2, 'SR0006', 'Yogesh', 'Jadhav', 'yogesh.jadhav@scalereal.com', 9876543210, true, false, 1);
                       
                INSERT INTO modules (name)
                  SELECT new_module
                    FROM (VALUES 
                    ('Review Cycles'),
                    ('User Activity Log')) AS new_modules(new_module)
                    LEFT JOIN modules ON new_modules.new_module = modules.name
                    WHERE modules.name IS NULL;
                      
                 INSERT INTO user_activity (sr_no, employee_id, module_id, activity, description, ip_address, created_at)
                 VALUES ( 2, 1, 1, 'Review Cycle Created','01/04/2023 - 30/04/2023','191.0.0.1',now()),
                        ( 3, 2, 1, 'Review Cycle Updated','01/04/2023 - 25/04/2023','191.0.0.1',now());
                        
                """.trimIndent(),
            )
        }
        userActivityRepositoryImpl = UserActivityRepositoryImpl(dataSource)
        userActivityService = UserActivityService(userActivityRepositoryImpl)
    }
}
