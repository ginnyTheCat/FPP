sealed class Nachricht(val wire: String) {
    class AnAlle(val inhalt: String) : Nachricht("B:$inhalt")

    override fun toString(): String {
        return when (this) {
            is AnAlle -> "AnAlle($inhalt)"
        }
    }

    companion object {
        fun parse(s: String): Nachricht? {
            val parts = s.split(":")
            return when (parts[0]) {
                "B" -> AnAlle(parts[1])
                else -> null
            }
        }
    }
}