package scalereal.core.emails

import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeRepository
import scalereal.core.models.AppConfig
import scalereal.core.organisations.OrganisationRepository

@Singleton
class FeedbackReceivedMail(
    private val emailSenderService: EmailSenderService,
    private val employeeRepository: EmployeeRepository,
    private val organisationRepository: OrganisationRepository,
    appConfig: AppConfig,
) {
    fun sendFeedbackReceivedMail(
        feedbackToId: Long,
        feedbackFromId: Long,
    ) {
        val feedbackToDetails = employeeRepository.getEmployeeById(feedbackToId)
        val feedbackFromDetails = employeeRepository.getEmployeeById(feedbackFromId)
        val feedbackToName = feedbackToDetails.firstName
        val feedbackFromData = "${feedbackFromDetails.firstName} ${feedbackFromDetails.lastName} (${feedbackFromDetails.employeeId})"
        val organisationDetails = organisationRepository.getOrganisationDetails(feedbackToDetails.organisationId)
        emailSenderService.sendEmail(
            receiver = feedbackToDetails.emailId,
            subject = "You have received Feedback from $feedbackFromData!",
            htmlBody =
                feedbackReceivedMailHTML(
                    feedbackToName,
                    feedbackFromData,
                    organisationDetails.name,
                ),
            textBody = feedbackReceivedMailTEXT(),
        )
    }

    private fun feedbackReceivedMailTEXT() = "Feedback received mail"

    private fun feedbackReceivedMailHTML(
        feedbackToName: String?,
        feedbackFromName: String,
        organisationName: String,
    ) = "<html>" +
        "<head><style> a > div {display:none} </style> </head>" +
        "<body>" +
        "<a href=''>" +
        "<img src='$feedbackGif' alt='Feedback Image' width='500' height='400' style='display: block; margin: auto;'>" +
        "</a>" +
        "<P style='color:black;'>Hi $feedbackToName,</P>" +
        "<P style='color:black;'>" +
        "We are pleased to inform you that feedback has been shared by $feedbackFromName regarding your performance during " +
        "the current review cycle.</P>" +
        "<P style='color:black;'>To view the detailed feedback, please click on the below mentioned link - <br>" +
        "<a href='$feedbackURL'>360-Degree Feedback</a>" +
        "<P style='color:black;'>Thank you & Regards,<br>" +
        "$organisationName</P>" +
        "</body>" +
        "</html>"

    private val feedbackURL = appConfig.getMyFeedbackUrl()
    private val feedbackGif = "http://d3rhjv3990jhk1.cloudfront.net/feedback.gif"
}
