package login

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.Spec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import norm.executeCommand
import scalereal.core.emails.EmailSenderService
import scalereal.core.encryption.EncryptionService
import scalereal.core.linkHandling.LinkHandlingService
import scalereal.core.login.LoginService
import scalereal.core.user.UserService
import scalereal.db.login.LoginRepositoryImpl
import util.StringSpecWithDataSource

class LoginRepositoryImplTest : StringSpecWithDataSource() {
    private lateinit var service: LoginService
    private lateinit var repositoryImpl: LoginRepositoryImpl
    private lateinit var encryptionService: EncryptionService
    private val employeeRepository = mockk<scalereal.core.employees.EmployeeRepository>()
    private val emailSenderService = mockk<EmailSenderService>()
    private val userService = mockk<UserService>()
    private val linkHandlingService = mockk<LinkHandlingService>()

    init {
        "should set password for user" {
            val encryptedSecret = encryptionService.encryptPassword("Ubed@123")
            repositoryImpl.setPassword(encryptedSecret, "syed.ali@scalereal.com")
        }

        "should throw an exception when password format is incorrect" {
            every { linkHandlingService.checkLinkValidity(any()) } returns Unit
            val exception =
                shouldThrow<Exception> {
                    service.setPassword("rushad@123", "rushad.shaikh@scalereal.com", "dummy_link-id")
                }
            exception.message shouldBe "Password should contain at least 1 capital letter"
        }
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        dataSource.connection.use {
            it.executeCommand(
                """
                 INSERT INTO organisations(sr_no, admin_id, name, is_active, organisation_size)
                 VALUES (1,1,'Scalereal', true, 50);
                 
                INSERT INTO employees(emp_id, first_name, last_name, email_id, contact_no, status, password, organisation_id)
                VALUES
                      ('SR0043', 'Syed Ubed', 'Ali', 'syed.ali@scalereal.com', 6262209099, true, null, 1),
                      ('SR0051', 'Rushad', 'Shaikh', 'rushad.shaikh@scalereal.com', 8265079426, true, null, 1);
                
                """.trimIndent(),
            )
        }
        repositoryImpl = LoginRepositoryImpl(dataSource)
        encryptionService = EncryptionService()
        service =
            LoginService(
                repositoryImpl,
                emailSenderService,
                encryptionService,
                employeeRepository,
                userService,
                linkHandlingService,
            )
    }
}
