import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Nachricht(kind: String, vararg args: String) {

    val wire: String = "$kind ${args.joinToString(" ") { URLEncoder.encode(it, StandardCharsets.UTF_8) }}"

    class Registrieren(val email: String, val name: String) : Nachricht("reg", email, name)
    class Anmelden(val email: String, val passwort: String) : Nachricht("an", email, passwort)
    class Text(val from: String?, val content: String) : Nachricht("msg", from ?: "Anonymous", content)
    class NowOnline(vararg val names: String) : Nachricht("nowOnline", *names)

    class Unknown(vararg val inhalt: String) : Nachricht("unknown", *inhalt)

    override fun toString(): String {
        return when (this) {
            is Registrieren -> "Registrieren($email, $name)"
            is Anmelden -> "Anmelden($email, $passwort)"
            is Text -> "Text($from, $content)"
            is NowOnline -> "NowOnline($names)"

            is Unknown -> "Unknown($inhalt)"
        }
    }

    companion object {
        fun parse(s: String): Nachricht {
            val parts = s.split(" ").map { URLDecoder.decode(it, StandardCharsets.UTF_8) }

            val first = parts.firstOrNull()
            val args = parts.subList(1, parts.size)
            return when {
                first == "reg" && args.size == 2 -> Registrieren(args[0], args[1])
                first == "an" && args.size == 2 -> Anmelden(args[0], args[1])
                first == "msg" && args.size == 1 -> Text(null, args[0])
                first == "nowOnline" -> NowOnline(*args.toTypedArray())

                else -> Unknown(*parts.toTypedArray())
            }
        }
    }
}