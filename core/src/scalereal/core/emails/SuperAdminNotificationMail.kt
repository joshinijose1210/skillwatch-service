package scalereal.core.emails

import jakarta.inject.Singleton
import scalereal.core.models.domain.OrganisationData
import scalereal.core.models.domain.User
import scalereal.core.user.UserRepository

@Singleton
class SuperAdminNotificationMail(
    private val emailSenderService: EmailSenderService,
    private val userRepository: UserRepository,
) {
    fun userRegisteredEmail(user: User) {
        val superAdminEmail = userRepository.fetchSuperAdminEmailIds()
        val userName = "${user.firstName} ${user.lastName}"
        superAdminEmail.forEach {
            emailSenderService.sendEmail(
                receiver = it,
                subject = "SkillWatch SuperAdmin | $userName user has signed up",
                htmlBody = userRegisteredHTML(userName, user.emailId),
                textBody = "New user has signed up",
            )
        }
    }

    private fun userRegisteredHTML(
        userName: String,
        userEmailId: String,
    ) = "<!DOCTYPE html>" +
        "<html>" +
        "<body>" +
        "<P style = 'color:black;'>Dear SuperAdmin,</P>" +
        "<P style = 'color:black;'>" +
        "We are thrilled to inform you that a new user has recently signed up for SkillWatch. " +
        "Below are the details of the newly registered Org Admin:</P>" +
        "<P style = 'color:black;'>" +
        "Name: $userName<br>" +
        "Email: $userEmailId</P>" +
        "<P style = 'color:black;'>Thank you & Regards,<br>" +
        "SkillWatch Team</P>" +
        "</body>" +
        "</html>"

    fun organisationDetailsAddedEmail(organisationData: OrganisationData) {
        val superAdminEmail = userRepository.fetchSuperAdminEmailIds()
        superAdminEmail.forEach {
            emailSenderService.sendEmail(
                receiver = it,
                subject = "SkillWatch SuperAdmin | ${organisationData.organisationName} organisation details added",
                htmlBody = organisationDetailsAddedHTML(organisationData),
                textBody = "Organisation details has been added",
            )
        }
    }

    private fun organisationDetailsAddedHTML(organisationData: OrganisationData): String =
        "<!DOCTYPE html>" +
            "<html>" +
            "<body>" +
            "<P style = 'color:black;'>Dear SuperAdmin,</P>" +
            "<P style = 'color:black;'>We are pleased to inform you that new organisation details have been added to SkillWatch. " +
            "Here are the particulars of the new organisation:</P>" +
            "<P style = 'color:black;'>" +
            "Admin Name: ${organisationData.adminFirstName} ${organisationData.adminLastName}<br>" +
            "Admin Email: ${organisationData.adminEmailId}<br>" +
            "Organisation Name: ${organisationData.organisationName}<br>" +
            "Organisation Size: ${organisationData.organisationSize}<br>" +
            "Contact No: ${organisationData.contactNo}</P>" +
            "<P style = 'color:black;'>Thank you & Regards,<br>" +
            "SkillWatch Team</P>" +
            "</body>" +
            "</html>"
}
