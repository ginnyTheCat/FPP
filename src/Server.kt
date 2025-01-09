import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
    private var users = mutableStateMapOf<String, Nutzer>()
    private var pool = mutableStateMapOf<ServerConnection, Unit>()
    private var log = mutableStateListOf<String>()

    fun start() {
        val socket = ServerSocket(9876)
        log.add("Hört auf Port :${socket.localPort}")
        this.serverSocket = socket
        thread { this.hintergrund(socket) }
    }

    private fun hintergrund(socket: ServerSocket) {
        while (!socket.isClosed) {
            val con = socket.accept()
            val handler = ServerConnection(this, con)

            this.pool[handler] = Unit
            log.add("Client ${con.remoteSocketAddress} verbunden")
            thread {
                handler.handle()
                this.pool.remove(handler)
                log.add("Client ${con.remoteSocketAddress} getrennt")
            }
        }
    }

    fun stop() {
        this.serverSocket?.close()
    }

    @Composable
    fun ui() {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(Modifier.fillMaxSize().weight(1f)) {
                    Text("Momentan online", Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                    LazyColumn {
                        items(pool.keys.toList()) {
                            it.ui()
                        }
                    }
                }
                Card(Modifier.fillMaxSize().weight(1f)) {
                    Text("Registrierte Benutzer", Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                    LazyColumn {
                        items(users.values.toList()) {
                            ListItem(
                                leadingContent = { Icon(Icons.Default.AccountCircle, null) },
                                headlineContent = { Text(it.name) },
                                supportingContent = { Text(it.email) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            )
                        }
                    }
                }
            }
            Card(Modifier.fillMaxSize().weight(1f)) {
                Text("Logbuch", Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                LazyColumn() {
                    items(log) {
                        Text(it, Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }

    fun anAlleSenden(msg: Nachricht) {
        this.pool.keys.forEach { it.sende(msg) }
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

            log.add("E-Mail mit Password versendet an $email")
        } else {
            log.add("E-Mail-Versand ist nicht aktiviert: $email hat Passwort $passwort")
        }

        val nutzer = Nutzer(email, name, passwort)
        this.users[email] = nutzer

        log.add("Nutzer $email mit Name \"$name\" registriert")
    }

    fun anmelden(con: ServerConnection, email: String, passwort: String) {
        val nutzer = this.users[email]
        if (nutzer?.passwort == passwort) {
            con.nutzer.value = nutzer
            this.anAlleSenden(Nachricht.Verbinden(nutzer.name))
            con.sende(Nachricht.Success("an"))
            log.add("Erfolgreiche Anmeldung von $email")
        } else {
            con.sende(Nachricht.Fail("an"))
            log.add("Anmeldeversuch von $email")
        }
    }

    fun passwortAendern(con: ServerConnection, alt: String, neu: String) {
        val nutzer = con.nutzer.value
        if (nutzer?.passwort == alt) {
            nutzer.passwort = neu
            con.sende(Nachricht.Success("chpwd"))
            log.add("Erfolgreiche Passwortänderung von ${nutzer.email}")
        } else {
            con.sende(Nachricht.Fail("chpwd"))
            log.add("Fehlgeschlagene Passwortänderung von ${nutzer?.email}")
        }
    }

    fun message(nutzer: Nutzer?, content: String) {
        log.add("Nachricht von ${nutzer?.name}: $content")
        this.anAlleSenden(Nachricht.Text(nutzer?.name, content))
    }
}

class ServerConnection(private val server: Server, private val socket: Socket) {
    private val reader: Scanner = Scanner(socket.getInputStream())
    private val writer: OutputStream = socket.getOutputStream()

    val nutzer = mutableStateOf<Nutzer?>(null)

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

        val nutzer = this.nutzer.value
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
            is Nachricht.Text -> this.server.message(this.nutzer.value, msg.content)
            else -> {}
        }
    }

    @Composable
    fun ui() {
        ListItem(
            leadingContent = { Icon(Icons.Default.Person, null) },
            headlineContent = { Text(socket.remoteSocketAddress.toString()) },
            supportingContent = nutzer.value?.let { { Text("${it.name} - ${it.email}") } },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}
