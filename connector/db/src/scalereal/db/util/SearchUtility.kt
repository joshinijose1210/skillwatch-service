package scalereal.db.util

private const val WILD_CARD_CHARACTER = "%"

fun getWildCardedString(searchString: String): String = (WILD_CARD_CHARACTER + searchString.trim() + WILD_CARD_CHARACTER)
