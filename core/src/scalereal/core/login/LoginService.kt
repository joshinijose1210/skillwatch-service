package scalereal.core.login

import jakarta.inject.Singleton
import scalereal.core.emails.EmailSenderService
import scalereal.core.encryption.EncryptionService
import scalereal.core.exception.EmployeeNotFoundException
import scalereal.core.exception.PasswordFormatException
import scalereal.core.linkHandling.LinkHandlingService
import scalereal.core.models.Constants
import scalereal.core.user.UserService
import java.util.Base64

@Singleton
class LoginService(
    private val repository: LoginRepository,
    private val emailSenderService: EmailSenderService,
    private val encryptionService: EncryptionService,
    private val employeeRepository: scalereal.core.employees.EmployeeRepository,
    private val userService: UserService,
    private val linkHandlingService: LinkHandlingService,
) {
    fun setPassword(
        password: String,
        emailId: String,
        linkId: String,
    ) {
        try {
            linkHandlingService.checkLinkValidity(linkId = linkId)
            when {
                (password.length < 8) ->
                    throw PasswordFormatException("Password should contain minimum 8 characters")

                (password.length > 20) ->
                    throw PasswordFormatException("Password should contain maximum 20 characters")

                (!password.matches(Regex(".*[0-9].*"))) ->
                    throw PasswordFormatException("Password should contain at least 1 number")

                (!password.matches(Regex(".*[A-Z].*"))) ->
                    throw PasswordFormatException("Password should contain at least 1 capital letter")

                (!password.matches(Regex(".*[a-z].*"))) ->
                    throw PasswordFormatException("Password should contain at least 1 small letter")

                (!password.matches(Regex("(?=.*[^a-zA-Z0-9]).*"))) ->
                    throw PasswordFormatException("Password should contain at least 1 special character")

                else -> {
                    val encryptedPassword = encryptionService.encryptPassword(password = password)
                    repository.setPassword(encryptedPassword, emailId)
                    linkHandlingService.updateLinkDetails(linkId)
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }

    fun resetPasswordEmail(emailId: String) {
        if (employeeRepository.isEmployeeExists(emailId)) {
            val encodedEmailId = Base64.getEncoder().encodeToString(emailId.toByteArray())
            emailSenderService.sendEmail(
                receiver = emailId,
                subject = "Reset password",
                htmlBody = emailSenderService.resetPasswordHTML(encodedEmailId),
                textBody = emailSenderService.resetPasswordTEXT(),
            )
        } else {
            throw EmployeeNotFoundException("Invalid Email Id")
        }
    }

    fun resendWelcomeEmail(emailId: String) {
        if (employeeRepository.isEmployeeExists(emailId)) {
            val encodedEmailId = Base64.getEncoder().encodeToString(emailId.toByteArray())
            val firstName = userService.getUser(emailId).firstName
            emailSenderService.sendEmail(
                receiver = emailId,
                subject = emailSenderService.welcomeSubject(),
                htmlBody = emailSenderService.welcomeHTML(firstName, encodedEmailId, Constants.APPLICATION_NAME),
                textBody = emailSenderService.welcomeTEXT(),
            )
        } else {
            throw EmployeeNotFoundException("User not found")
        }
    }
}
