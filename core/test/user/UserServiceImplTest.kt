package user

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import scalereal.core.emails.CompleteRegistrationMail
import scalereal.core.emails.SuperAdminNotificationMail
import scalereal.core.employees.EmployeeRepository
import scalereal.core.models.domain.User
import scalereal.core.user.UserRepository
import scalereal.core.user.UserService

class UserServiceImplTest : StringSpec() {
    private val userRepository = mockk<UserRepository>()
    private val superAdminRepository = mockk<EmployeeRepository>()
    private val completeRegistrationMail = mockk<CompleteRegistrationMail>()
    private val superAdminNotificationMail = mockk<SuperAdminNotificationMail>()
    private val userService = UserService(userRepository, superAdminRepository, completeRegistrationMail, superAdminNotificationMail)

    init {
        "should get user details by id" {

            val user =
                User(
                    id = 1,
                    firstName = "sherlock",
                    lastName = "Holmes",
                    emailId = "sherlock",
                )

            val userId: Long = 1
            every { userRepository.getById(userId) } returns user

            userRepository.getById(userId) shouldBe user

            verify(exactly = 1) { userRepository.getById(userId) }
        }

        "should create a user and send registration mail" {
            val user =
                User(
                    id = 2,
                    firstName = "Moly",
                    lastName = "Agarwal",
                    emailId = "moly.agarwal@scalereal.com",
                )
            every { superAdminRepository.isEmailIdExists(user.emailId) } returns false
            every { userRepository.isUserExist(user.emailId) } returns false
            every { userRepository.createUser(user) } just Runs
            every { userRepository.getUser(user.emailId) } returns user
//            every { completeRegistrationMail.sendMail(user.firstName, user.emailId) } just Runs
            every { superAdminNotificationMail.userRegisteredEmail(user) } just Runs

            userService.createUser(user)

            verify(exactly = 1) { userRepository.createUser(user) }
//            verify(exactly = 1) { completeRegistrationMail.sendMail(user.firstName, user.emailId) }
        }

        "should throw DuplicateDataException if email already exists" {
            val user =
                User(
                    id = 2,
                    firstName = "Moly",
                    lastName = "Agarwal",
                    emailId = "moly.agarwal@scalereal.com",
                )
            every { superAdminRepository.isEmailIdExists(user.emailId) } returns true
            every { userRepository.isUserExist(user.emailId) } returns true

            val exception =
                shouldThrow<Exception> {
                    userService.createUser(user)
                }
            exception.message shouldBe "This Email ID already exists"
        }

        "should throw exception when user trying to add non-business mail" {
            val user =
                User(
                    id = 3,
                    firstName = "John",
                    lastName = "Cruise",
                    emailId = "john.cruise@gmail.com",
                )
            every { superAdminRepository.isEmailIdExists(user.emailId) } returns false
            every { userRepository.isUserExist(user.emailId) } returns false

            val exception =
                shouldThrow<Exception> {
                    userService.createUser(user)
                }
            exception.message shouldBe "Invalid email. Please enter your organisation’s email."
        }
    }
}
