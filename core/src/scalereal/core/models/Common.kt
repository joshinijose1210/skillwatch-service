package scalereal.core.models

fun String.removeExtraSpaces(): String {
    val extraSpaces = Regex("\\s{2,}")
    return this.replace(extraSpaces, " ").trim()
}

fun String.containsSpecialChars(): Boolean {
    val regex = Regex("^[a-zA-Z0-9\\s-]+$")
    return !regex.matches(this)
}
