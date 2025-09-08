package scalereal.core.emails

import io.micronaut.email.Email
import io.micronaut.email.EmailSender
import io.micronaut.email.MultipartBody
import jakarta.inject.Inject
import jakarta.inject.Singleton
import scalereal.core.linkHandling.LinkHandlingService
import scalereal.core.models.AppConfig
import scalereal.core.models.Constants

@Singleton
class EmailSenderService(
    @Inject private var emailSender: EmailSender<Any, Any>,
    @Inject private var appConfig: AppConfig,
    @Inject private var linkHandlingService: LinkHandlingService,
) {
    private val instanceUrl = appConfig.getInstanceUrl()

    fun sendEmail(
        receiver: String,
        subject: String,
        htmlBody: String,
        textBody: String,
    ) {
        emailSender.send(
            Email
                .builder()
                .to(receiver)
                .subject(subject)
                .body(MultipartBody(htmlBody, textBody)),
        )
    }

    fun welcomeSubject() = "Welcome to ${Constants.APPLICATION_NAME}"

    fun welcomeHTML(
        name: String?,
        emailId: String,
        organisationName: String,
    ) = "<html>" +
        "<head><style> a > div {display:none} </style> </head>" +
        "<body>" +
        "<a href=''>" +
        "<img src='$welcomeImage' alt='Welcome Image' width='250' height='300' style='display: block; margin: auto;'>" +
        "</a>" +
        "<h1 style='font-size:20px; font-weight:bold; text-align:center; color:black;'>Welcome to ${Constants.APPLICATION_NAME}</h1>" +
        "<P style='color:black;'>Dear $name,</P>" +
        "<P style='color:black;'>Welcome to ${Constants.APPLICATION_NAME}, your go-to platform for employee performance management! " +
        "We're thrilled to have you on board and excited for you to start using our features to enhance your team's performance.</P>" +
        "<P style='color:black;'>With ${Constants.APPLICATION_NAME}, " +
        "you can easily Give and Receive Feedback, show Appreciation to your colleagues, generate Reports, and monitor progress on our " +
        "Dashboard. We also offer Performance Review Cycles, including Self Review, Manager Review, and Check-In with Manager, " +
        "to help you set goals, track progress, and improve performance. " +
        "And, we're constantly adding new features to make performance management even easier for you!</P>" +
        "<P style='color:black;'>" +
        "As a new user, we recommend taking some time to explore our platform and familiarise yourself with all the features. " +
        "Our support team is always available to assist you with any questions or concerns you may have.</P>" +
        "<P style='color:black;'>" +
        "Thank you for choosing ${Constants.APPLICATION_NAME}, we look forward to supporting your team's success!</P>" +
        "<P style='color:black;'>Kindly click on the below mentioned link to Set Password and achieve your performance goals.</P>" +
        "<a href='$setPasswordURL?id=${insertLinkDetailsAndGetUniqueId("Set Password")}&emailId=$emailId'>Set Password</a>" +
        "<P style='color:black;'>Thank you & Regards,<br>" +
        "$organisationName</P>" +
        "</body>" +
        "</html>"

    fun welcomeTEXT() = "Welcome to ${Constants.APPLICATION_NAME}"

    fun resetPasswordHTML(emailId: String) =
        "<html>" +
            "<head><style> a > div {display:none} </style> </head>" +
            " <body>" +
            "<a href=''>" +
            "<img src='$resetPasswordGif' alt='Reset Password Image' width='250' height='250' style='display: block; margin: auto;'>" +
            "</a>" +
            "<P style='text-align:center;'>" +
            "We have received a request to reset the password of your ${Constants.APPLICATION_NAME} account." +
            " To reset password, please click on the below mentioned link and set new password</P>" +
            "<div style='text-align:center;'>" +
            "<a href='$resetPasswordURL?id=${insertLinkDetailsAndGetUniqueId("Reset Password")}&emailId=$emailId'>Reset Password</a>" +
            "</div>" +
            "<P style='color:gray; text-align:center;'>" +
            "If you didn't request a password reset, you can ignore this email. Your password will not be changed.</P>" +
            "</body>" +
            "</html>"

    fun resetPasswordTEXT() = "Reset password email"

    private val welcomeImage = "http://d3rhjv3990jhk1.cloudfront.net/welcome-email.jpg"
    private val resetPasswordGif = "http://d3rhjv3990jhk1.cloudfront.net/reset-password.gif"

    private val setPasswordURL = "$instanceUrl/signup/set-password"
    private val resetPasswordURL = "$instanceUrl/login/reset-password"

    fun insertLinkDetailsAndGetUniqueId(purpose: String): String = linkHandlingService.insertLinkDetails(purpose).linkId
}
