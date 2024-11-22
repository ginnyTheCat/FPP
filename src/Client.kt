import java.io.OutputStream
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.concurrent.thread

class Client(
    private val adresse: String,
    private val port: Int
) {

    private var socket: Socket? = null
    private var ausgang: OutputStream? = null

    private var chatting: Boolean = false

    private val online: HashSet<String> = HashSet()

    fun start() {
        val socket = Socket(adresse, port)
        this.socket = socket
        this.ausgang = socket.getOutputStream()

        thread { this.anmeldeDialog() }
        this.hintergrund(socket)
    }

    fun stop() {
        this.socket?.close()
    }

    private fun anmeldeDialog() {
        val schritt = intInput("Herzlich Willkommen! (1 = Anmelden, 2 = Registrieren)", 1..2)

        println()
        print("E-Mail-Adresse: ")
        val email = readln()

        if (schritt == 2) {
            print("Name: ")
            val name = readln()

            this.sende(Nachricht.Registrieren(email, name))

            println()
            println("Sie sollten ihr Passwort nur per E-Mail zugeschickt bekommen.")
        }

        print("Passwort: ")
        val passwort = readln()

        this.sende(Nachricht.Anmelden(email, passwort))
        println()
    }

    private fun chattenAnfangen() {
        this.chatting = true

        println("Sie sind fertig angemeldet und verbunden.")
        println("Benutzen sie den /? Befehl um die Hilfe zu sehen, oder benutzen sie die Nachrichteneingaben um eine Nachricht zu schicken.")
        println()

        while (true) {
            print("> ")
            val text = readln()

            when (text.trim()) {
                "" -> {}
                "/?" -> {
                    println("Diese Befehle stehen ihnen zur Verfügung:")
                    println("  /?         - Zeigt diese Hilfe an")
                    println("  /passwort  - Erlaubt ihnen ihr Passwort zu ändern")
                    println("  /online    - Zeigt an wer im Moment online ist")
                }

                "/passwort" -> {
                    println("Passwort ändern: ")

                    while (true) {
                        print("Geben sie zuerst ihr aktuelles/altes Passwort ein: ")
                        val alt = readln()

                        print("Geben sie nun ihr neues Passwort ein: ")
                        val neu = readln()

                        print("Geben sie ihr neues Passwort nochmal ein: ")
                        val neu2 = readln()

                        if (neu == neu2) {
                            this.sende(Nachricht.PasswortAendern(alt, neu))
                            break;
                        } else {
                            println()
                            println("Sie haben sich beim neuen Passwort vertippt. Bitte versuchen sie es noch einmal.")
                        }
                    }
                }

                "/online" -> {
                    val online = this.online.toList()
                    if (online.size > 1) {
                        print(online.dropLast(1).joinToString(", "))
                        println(" und ${online.last()} sind online.")
                    } else {
                        println("${online.first()} ist online.")
                    }
                }

                else -> this.sende(Nachricht.Text(null, text))
            }
        }
    }

    private fun sende(msg: Nachricht) {
        this.ausgang!!.write("${msg.wire}\n".toByteArray(StandardCharsets.UTF_8))
    }

    private fun hintergrund(socket: Socket) {
        val eingang = Scanner(socket.getInputStream())

        while (true) {
            val line = eingang.nextLine()
            this.handleNachricht(Nachricht.parse(line))
        }
    }

    private fun schreiben(text: String) {
        if (this.chatting) {
            print("\u0033[2K\r${text}\n> ")
        }
    }

    private fun handleNachricht(msg: Nachricht) {
        when (msg) {
            is Nachricht.Verbinden -> {
                this.online.add(msg.name)
                this.schreiben("${msg.name} ist dem Raum beigetreten.")
            }

            is Nachricht.Trennen -> {
                this.online.remove(msg.name)
                this.schreiben("${msg.name} hat den Raum verlassen.")
            }

            is Nachricht.Text -> this.schreiben("${msg.from} schreibt: ${msg.content}")

            is Nachricht.Success -> when (msg.where) {
                "an" -> thread { this.chattenAnfangen() }
                "chpwd" -> this.schreiben("Passwort erfolgreich geändert!")
            }

            is Nachricht.Fail -> when (msg.where) {
                "an" -> {
                    println("Anmeldung fehlgeschlagen! Bitte überprüfen sie ihre E-Mail-Adresse und ihr Passwort.")
                    println()
                    thread { this.anmeldeDialog() }
                }

                "chpwd" -> this.schreiben("Passwort ändern fehlgeschlagen.")
            }

            else -> this.schreiben(msg.toString())
        }
    }
}