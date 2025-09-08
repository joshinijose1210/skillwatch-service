package scalereal.core.emails

import jakarta.inject.Singleton
import scalereal.core.models.AppConfig
import scalereal.core.models.Constants
import java.util.Base64

// TODO("Uncomment line:35,36 in (UserService) if complete registration mail is required!")
@Singleton
class CompleteRegistrationMail(
    private val emailSenderService: EmailSenderService,
    private val appConfig: AppConfig,
) {
    fun sendMail(
        firstName: String,
        emailId: String,
    ) {
        val encodedEmailId = Base64.getEncoder().encodeToString(emailId.toByteArray())
        emailSenderService.sendEmail(
            receiver = emailId,
            subject = "Welcome to ${Constants.APPLICATION_NAME}",
            htmlBody = completeRegistrationHTML(firstName, encodedEmailId),
            textBody = completeRegistrationTEXT(),
        )
    }

    fun completeRegistrationHTML(
        firstName: String,
        encodedEmailId: String,
    ) = "<!DOCTYPE html>" +
        "<html>" +
        "<head><style> a > div {display:none} </style> </head>" +
        "<body>" +
        "<a href=''>" +
        "<img src='$welcomeImage' alt='Welcome Image' width='250' height='300' style='display: block; margin: auto;'>" +
        "</a>" +
        "<P style='color:black;'>Hi $firstName,</P>" +
        "<P style='color:black;'>We're glad to have you on board." +
        " With ${Constants.APPLICATION_NAME}, you can give and receive reviews, feedback, and appreciation from other employees.</P>" +
        "<P style='color:black;'>" +
        "To complete the registration process, please click on the below mentioned button “Complete Registration” " +
        "to enter the Company Information and Set Password for your account.</P>" +
        "<button type='button' style='display: block; margin: auto;'>" +
        "<a href='$completeRegistrationURL?$encodedEmailId'>Complete Registration</a></button>" +
        "<P style='color:black;'>Thank you & Regards,<br>" +
        "${Constants.APPLICATION_NAME}</P>" +
        "</body>" +
        "</html>"

    private fun completeRegistrationTEXT() = "Welcome and complete registration"

    private val instanceUrl = appConfig.getInstanceUrl()

    private val welcomeImage = "https://drive.google.com/uc?export=view&id=19NSyIFGzHq65iAtvejHrScuYCiAiFzXS"
    private val completeRegistrationURL = "$instanceUrl/sign-up/org-admin/company-info"
}
