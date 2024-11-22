import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Nachricht(kind: String, vararg args: String?) {

    val wire: String =
        "$kind ${args.filterNotNull().joinToString(" ") { URLEncoder.encode(it, StandardCharsets.UTF_8) }}"

    class Registrieren(val email: String, val name: String) : Nachricht("reg", email, name)
    class Anmelden(val email: String, val passwort: String) : Nachricht("an", email, passwort)
    class PasswortAendern(val alt: String, val neu: String) : Nachricht("chpwd", alt, neu)
    class Text(val from: String?, val content: String) : Nachricht("msg", from, content)
    class Verbinden(val name: String) : Nachricht("con", name)
    class Trennen(val name: String) : Nachricht("dis", name)

    class Success(val where: String) : Nachricht("succ", where)
    class Fail(val where: String) : Nachricht("fail", where)

    class Unknown(
        vararg val inhalt: String
    ) : Nachricht("unknown", *inhalt)

    override fun toString(): String {
        val zensieren = true

        return when (this) {
            is Registrieren -> "Registrieren($email, $name)"
            is Anmelden -> "Anmelden($email, ${
                if (zensieren) {
                    "***"
                } else {
                    passwort
                }
            })"

            is PasswortAendern -> if (zensieren) {
                "PasswortAendern(***, ***)"
            } else {
                "PasswortAendern($alt, $neu)"
            }

            is Text -> "Text($from, $content)"
            is Verbinden -> "Verbinden($name)"
            is Trennen -> "Trennen($name)"

            is Success -> "Success($where)"
            is Fail -> "Fail($where)"

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
                first == "chpwd" && args.size == 2 -> PasswortAendern(args[0], args[1])
                first == "msg" && args.size == 2 -> Text(args[0], args[1])
                first == "msg" && args.size == 1 -> Text(null, args[0])
                first == "con" && args.size == 1 -> Verbinden(args[0])
                first == "dis" && args.size == 1 -> Trennen(args[0])
                first == "succ" && args.size == 1 -> Success(args[0])
                first == "fail" && args.size == 1 -> Fail(args[0])

                else -> Unknown(*parts.toTypedArray())
            }
        }
    }
}
