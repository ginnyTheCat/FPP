import org.apache.commons.mail2.javax.SimpleEmail
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.concurrent.thread


data class Nutzer(
    val email: String,
    val name: String,
    var passwort: String,
)

class Server(
    private val uniUser: String?,
    private val uniPassword: String?
) {
    private var serverSocket: ServerSocket? = null
    private var users: HashMap<String, Nutzer> = HashMap()
    private var pool: HashSet<ServerConnection> = HashSet()

    fun start() {
        val socket = ServerSocket(9876)
        println("Hört auf Port :${socket.localPort}")
        this.serverSocket = socket
        thread { this.hintergrund(socket) }
    }

    private fun hintergrund(socket: ServerSocket) {
        while (!socket.isClosed) {
            val con = socket.accept()
            val handler = ServerConnection(this, con)

            this.pool.add(handler)
            println("Client ${con.remoteSocketAddress} verbunden")
            thread {
                handler.handle()
                this.pool.remove(handler)
                println("Client ${con.remoteSocketAddress} getrennt")
            }
        }
    }

    fun stop() {
        this.serverSocket?.close()
    }

    fun anAlleSenden(msg: Nachricht) {
        this.pool.forEach { it.sende(msg) }
    }

    fun registrieren(email: String, name: String) {
        val alphabet = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        val passwort = String(CharArray(12) { alphabet.random() })

        if (this.uniUser != null && this.uniPassword != null) {
            val mail = SimpleEmail()
            // mail.isDebug = true

            mail.hostName = "smtp.uni-jena.de"
            mail.isStartTLSEnabled = true
            mail.setAuthentication("${this.uniUser}@uni-jena.de", this.uniPassword)
            mail.setFrom("${this.uniUser}@uni-jena.de", "FPP")

            mail.addTo(email, name)

            mail.subject = "FPP Registrierung - $name"
            mail.setMsg("Ihr Passwort lautet: $passwort")
            mail.send()
        } else {
            println("register $email with password $passwort")
        }

        val nutzer = Nutzer(email, name, passwort)
        this.users[email] = nutzer
    }

    fun anmelden(con: ServerConnection, email: String, passwort: String) {
        val nutzer = this.users[email]
        if (nutzer?.passwort == passwort) {
            con.nutzer = nutzer
            this.anAlleSenden(Nachricht.Verbinden(nutzer.name))
            con.sende(Nachricht.Success("an"))
        } else {
            con.sende(Nachricht.Fail("an"))
        }
    }

    fun passwortAendern(con: ServerConnection, alt: String, neu: String) {
        val nutzer = con.nutzer
        if (nutzer?.passwort == alt) {
            nutzer.passwort = neu
            con.sende(Nachricht.Success("chpwd"))
        } else {
            con.sende(Nachricht.Fail("chpwd"))
        }
    }
}

class ServerConnection(private val server: Server, private val socket: Socket) {
    private val reader: Scanner = Scanner(socket.getInputStream())
    private val writer: OutputStream = socket.getOutputStream()

    var nutzer: Nutzer? = null

    fun sende(msg: Nachricht) {
        writer.write("${msg.wire}\n".toByteArray(StandardCharsets.UTF_8))
    }

    fun handle() {
        while (true) {
            var line: String
            try {
                line = reader.nextLine()
            } catch (e: NoSuchElementException) {
                break;
            }

            this.handleNachricht(Nachricht.parse(line))
        }

        val nutzer = this.nutzer
        if (nutzer != null) {
            this.server.anAlleSenden(Nachricht.Trennen(nutzer.name))
        }
    }

    private fun handleNachricht(msg: Nachricht) {
        println("Nachricht $msg erhalten")

        when (msg) {
            is Nachricht.Registrieren -> this.server.registrieren(msg.email, msg.name)
            is Nachricht.Anmelden -> this.server.anmelden(this, msg.email, msg.passwort)
            is Nachricht.PasswortAendern -> this.server.passwortAendern(this, msg.alt, msg.neu)
            is Nachricht.Text -> this.server.anAlleSenden(Nachricht.Text(nutzer?.name, msg.content))
            else -> {}
        }
    }
}
