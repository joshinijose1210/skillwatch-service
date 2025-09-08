package scalereal.core.user

import jakarta.inject.Singleton
import scalereal.core.emails.CompleteRegistrationMail
import scalereal.core.emails.SuperAdminNotificationMail
import scalereal.core.employees.EmployeeRepository
import scalereal.core.exception.DuplicateDataException
import scalereal.core.exception.UserNotFoundException
import scalereal.core.models.InvalidDomains
import scalereal.core.models.domain.ModulePermission
import scalereal.core.models.domain.SuperAdminResponse
import scalereal.core.models.domain.User
import scalereal.core.modules.SuperAdminModules

@Singleton
class UserService(
    private val userRepository: UserRepository,
    private val employeeRepository: EmployeeRepository,
    private val completeRegistrationMail: CompleteRegistrationMail,
    private val superAdminNotificationMail: SuperAdminNotificationMail,
) {
    fun getById(id: Long): User = userRepository.getById(id) ?: throw UserNotFoundException("User not found")

    fun getUser(emailId: String): User = userRepository.getUser(emailId)

    fun isUserExist(emailId: String): Boolean = userRepository.isUserExist(emailId)

    fun createUser(user: User) {
        try {
            val domainName = user.emailId.substring(user.emailId.indexOf('@') + 1)
            when {
                (InvalidDomains.values().any { it.domain.lowercase() == domainName.lowercase() }) ->
                    throw Exception("Invalid email. Please enter your organisation’s email.")
                (employeeRepository.isEmailIdExists(user.emailId) || isUserExist(user.emailId)) ->
                    throw DuplicateDataException("This Email ID already exists")
            }
            userRepository.createUser(user)
            sendNotification(user)
        } catch (e: Exception) {
            throw e
        }
    }

    private fun sendNotification(user: User) {
//        TODO(reason = "Uncomment when complete registration mail is required!")
//        val firstName = getUser(user.emailId).firstName
//        completeRegistrationMail.sendMail(firstName = firstName, emailId = user.emailId)

        // Sending email to super admin
        superAdminNotificationMail.userRegisteredEmail(user)
    }

    fun getRole(userName: Any): List<String> = userRepository.getRole(userName)

    // TODO - Need to move these function to superAdmin package after refactoring
    fun isSuperAdmin(email: String): Boolean = userRepository.isSuperAdmin(email)

    fun getSecret(email: String): String = userRepository.getSecret(email)

    fun fetchSuperAdminDetails(emailId: String): SuperAdminResponse {
        if (!isSuperAdmin(emailId)) {
            throw UserNotFoundException("Unauthorized access! Please contact System Admin.")
        }

        val superAdmin = userRepository.getUser(emailId)
        val modulePermissions =
            SuperAdminModules.values().map { module ->
                ModulePermission(
                    moduleId = module.ordinal + 1,
                    moduleName = module.moduleName,
                    view = true,
                    edit = true,
                )
            }

        return SuperAdminResponse(
            id = superAdmin.id,
            emailId = emailId,
            firstName = superAdmin.firstName,
            lastName = superAdmin.lastName,
            modulePermission = modulePermissions,
        )
    }
}
