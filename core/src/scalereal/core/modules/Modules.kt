package scalereal.core.modules

enum class Modules(
    val moduleName: String,
) {
    RECEIVED_SUGGESTIONS("Received Suggestions"),
    TEAM_GOALS("Team Goals"),
    EMPLOYEE_FEEDBACK("Feedback"),
    PERFORMANCE_REVIEWS("Performance Review"),
    ANALYTICS("Analytics"),
    DEPARTMENTS("Departments"),
    TEAMS("Teams"),
    DESIGNATIONS("Designations"),
    ROLES_AND_PERMISSIONS("Roles & Permissions"),
    EMPLOYEES("Employees"),
    KRAs("KRAs"),
    KPIs("KPIs"),
    REVIEW_FOR_TEAM("Reviews for Team Members"),
    CHECK_IN_WITH_TEAM("Check-in with Team Members"),
    REVIEW_CYCLE("Review Cycles"),
    COMPANY_INFORMATION("Company Information"),
    SETTINGS("Settings"),
    INTEGRATIONS("Integrations"),
    USER_ACTIVITY_LOG("User Activity Log"),
    TUTORIAL_VIDEOS("Tutorial Videos"),
}

enum class MainModules(
    val moduleName: String,
) {
    REPORTS("Reports"),
    REVIEW_TIMELINE("Review Timeline"),
    CONFIGURATION("Configuration"),
    SUGGESTION_BOX("Suggestion Box"),
    HELP_AND_TRAINING("Help and Training"),
    GOALS("Goals"),
}

enum class SuperAdminModules(
    val moduleName: String,
) {
    ORGANISATIONS("Organisations"),
}
