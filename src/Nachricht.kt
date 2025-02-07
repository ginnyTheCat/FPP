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
    class Einladen(val name: String, val game: Game, val width: Int, val height: Int) :
        Nachricht("inv", name, game.wire, width.toString(), height.toString())

    class Beitreten(val name: String, val game: Game, val width: Int, val height: Int) :
        Nachricht("join", name, game.wire, width.toString(), height.toString())

    class Setzen(val name: String?, val x: Int, val y: Int) : Nachricht("set", name, x.toString(), y.toString())
    class End(val ausgang: Ausgang) : Nachricht(
        "end",
        when (ausgang) {
            Ausgang.Unentschieden -> "un"
            is Ausgang.Gewonnen -> "win"
            is Ausgang.Verloren -> "lose"
        },
        when (ausgang) {
            Ausgang.Unentschieden -> null
            is Ausgang.Gewonnen -> ausgang.gewinner.name
            is Ausgang.Verloren -> ausgang.verlierer.name
        },
    )

    class Verbinden(val name: String, val self: Boolean) : Nachricht("con", name, if (self) "1" else "0")
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
            is Einladen -> "Einladen($name, $game)"
            is Beitreten -> "Beitreten($name, $game)"

            is Setzen -> "Set($name, $x, $y)"
            is End -> "End($ausgang)"

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
                first == "inv" && args.size == 4 -> Einladen(
                    args[0],
                    if (args[1] == Game.VierGewinnt.wire) Game.VierGewinnt else Game.Futtern,
                    args[2].toInt(),
                    args[3].toInt(),
                )

                first == "join" && args.size == 4 -> Beitreten(
                    args[0],
                    if (args[1] == Game.VierGewinnt.wire) Game.VierGewinnt else Game.Futtern,
                    args[2].toInt(),
                    args[3].toInt(),
                )


                first == "set" && args.size == 2 -> Setzen(null, args[0].toInt(), args[1].toInt())
                first == "set" && args.size == 3 -> Setzen(args[0], args[1].toInt(), args[2].toInt())
                first == "end" && (args.size == 1 || args.size == 2) -> End(
                    when (args[0]) {
                        "un" -> Ausgang.Unentschieden
                        "win" -> Ausgang.Gewonnen(Spieler(args[1], SpielerArt.MENSCH))
                        "lose" -> Ausgang.Verloren(Spieler(args[1], SpielerArt.MENSCH))
                        else -> throw IllegalStateException(args[0])
                    }
                )

                first == "con" && args.size == 2 -> Verbinden(args[0], args[1] == "1")
                first == "dis" && args.size == 1 -> Trennen(args[0])
                first == "succ" && args.size == 1 -> Success(args[0])
                first == "fail" && args.size == 1 -> Fail(args[0])

                else -> Unknown(*parts.toTypedArray())
            }
        }
    }
}

enum class Game(val wire: String, val plain: String) {
    VierGewinnt("v", "Vier gewinnt"),
    Futtern("f", "Futtern");

    fun create(
        width: Int,
        height: Int,
        spieler: Array<Spieler>,
        onSet: (x: Int, y: Int, spieler: Spieler) -> Unit = { _, _, _ -> },
    ): Spiel {
        return when (this) {
            VierGewinnt -> VierGewinnt(height, width, spieler, onSet)
            Futtern -> Futtern(height, width, spieler, onSet)
        }
    }
}