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
    val passwort: String,
)

class Server(
    private val uniUser: String?,
    private val uniPassword: String?
) {
    private var serverSocket: ServerSocket? = null
    private var users: HashMap<String, Nutzer> = HashMap()
    private var pool: HashSet<ServerHandler> = HashSet()

    fun start() {
        val socket = ServerSocket(9876)
        println("Hört auf Port :${socket.localPort}")
        this.serverSocket = socket
        thread {
            while (!socket.isClosed) {
                val handler = ServerHandler(this, socket.accept())
                this.pool.add(handler)
                println("Neuer Client verbunden")
                thread {
                    handler.handle()
                    this.pool.remove(handler)
                    this.sendeOnline()
                }
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
        val passwort = String(CharArray(10) { alphabet.random() })

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

    fun anmelden(handler: ServerHandler, email: String, passwort: String) {
        val nutzer = this.users[email]
        if (nutzer?.passwort == passwort) {
            handler.nutzer = nutzer
            this.sendeOnline()
        }
    }

    private fun sendeOnline() {
        val names = this.pool.mapNotNull { it.nutzer }.map { it.name }
        val msg = Nachricht.NowOnline(*names.toTypedArray())
        this.anAlleSenden(msg)
    }
}

class ServerHandler(private val server: Server, private val socket: Socket) {
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
    }

    fun handleNachricht(msg: Nachricht) {
        println("Nachricht $msg erhalten")

        when (msg) {
            is Nachricht.Registrieren -> this.server.registrieren(msg.email, msg.name)
            is Nachricht.Anmelden -> this.server.anmelden(this, msg.email, msg.passwort)
            is Nachricht.Text -> this.server.anAlleSenden(Nachricht.Text(nutzer?.name, msg.content))
            else -> {}
        }
    }
}
