import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Casino
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
import java.io.File
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.concurrent.thread


data class Nutzer(
    val email: String,
    val name: String,
    var passwort: String,
)

class Server(
    private val email: String?,
    private val emailPassword: String?
) {
    private var serverSocket: ServerSocket? = null
    private var users = mutableStateMapOf<String, Nutzer>()
    private var pool = mutableStateMapOf<ServerConnection, Unit>()
    private var log = mutableStateListOf<String>()

    fun start() {
        this.load()
        val socket = ServerSocket(9876)
        log.add("Hört auf Port :${socket.localPort}")
        this.serverSocket = socket
        thread { this.hintergrund(socket) }
    }

    private fun load() {
        try {
            File("users.csv").useLines { lines ->
                lines.forEach {
                    val parts = it.split(",").map { s -> URLDecoder.decode(s, Charsets.UTF_8) }
                    users[parts[0]] = Nutzer(parts[0], parts[1], parts[2])
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun save() {
        try {
            File("users.csv").writeText(
                users.values.joinToString("\n") {
                    arrayOf(it.email, it.name, it.passwort).joinToString(",") { s ->
                        URLEncoder.encode(s, Charsets.UTF_8)
                    }
                })
        } catch (_: Exception) {
        }
    }

    private fun hintergrund(socket: ServerSocket) {
        while (!socket.isClosed) {
            val con = socket.accept()
            val handler = ServerConnection(this, con)

            this.pool[handler] = Unit
            log.add("Client ${con.remoteSocketAddress} verbunden")
            thread {
                this.sendMemberUpdates(handler)
                this.sendRoomUpdates()
                handler.handle()
                this.quitGame(handler)
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
                Card(Modifier.fillMaxSize().weight(1f)) {
                    Text("Aktuelle Spiele", Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                    LazyColumn {
                        val games = pool.keys.mapNotNull { it.room.value }.toSet().toList()
                        items(games) { game ->
                            ListItem(
                                leadingContent = { Icon(Icons.Default.Casino, null) },
                                headlineContent = { Text(game.spieler.joinToString(" gegen ") { it.name }) },
                                supportingContent = { Text("${game.feld.breite}x${game.feld.hoehe} ${if (game as? VierGewinnt == null) "Futtern" else "Vier gewinnt"}") },
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

    private fun sendMemberUpdates(client: ServerConnection) {
        this.pool.keys.mapNotNull { it.nutzer.value }.forEach { client.sende(Nachricht.Verbinden(it.name, false)) }
    }

    private fun sendRoomUpdates() {
        val games = pool.keys.mapNotNull { it.room.value }.toSet().toList()
        val nameGames = games.map { it.spieler.joinToString(" gegen ") }
        this.anAlleSenden(Nachricht.Room(nameGames.toTypedArray()))
    }

    fun anAlleSenden(msg: Nachricht) {
        this.pool.keys.forEach { it.sende(msg) }
    }

    fun registrieren(email: String, name: String) {
        val alphabet = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        val passwortLen = if (this.email == null) 2 else 12
        val passwort = String(CharArray(passwortLen) { alphabet.random() })

        if (this.email != null && this.emailPassword != null) {
            val mail = SimpleEmail()
            // mail.isDebug = true

            mail.hostName = "mail.gmx.net"
            mail.isStartTLSEnabled = false
            mail.isSSLOnConnect = true
            mail.setAuthentication(this.email, this.emailPassword)
            mail.setFrom(this.email, "FPP")

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
        this.save()

        log.add("Nutzer $email mit Name \"$name\" registriert")
    }

    fun anmelden(con: ServerConnection, email: String, passwort: String) {
        val nutzer = this.users[email]
        if (nutzer?.passwort == passwort) {
            con.nutzer.value = nutzer
            this.pool.keys.forEach { it.sende(Nachricht.Verbinden(nutzer.name, it.nutzer.value == nutzer)) }
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
            this.save()
            con.sende(Nachricht.Success("chpwd"))
            log.add("Erfolgreiche Passwortänderung von ${nutzer.email}")
        } else {
            con.sende(Nachricht.Fail("chpwd"))
            log.add("Fehlgeschlagene Passwortänderung von ${nutzer?.email}")
        }
    }

    fun message(nutzer: Nutzer?, game: Spiel?, content: String) {
        log.add("Nachricht von ${nutzer?.name}: $content")
        this.pool.keys.filter { it.room.value == game }.forEach { it.sende(Nachricht.Text(nutzer?.name, content)) }
    }

    private fun findUser(name: String): ServerConnection? = this.pool.keys.find { it.nutzer.value?.name == name }

    fun invite(name: String, from: Nutzer, game: Game, width: Int, height: Int) {
        log.add("Einladung zu ${width}x${height} ${game.plain} von ${from.name} an $name")
        this.findUser(name)?.sende(Nachricht.Einladen(from.name, game, width, height))
    }

    fun quitGame(user: ServerConnection) {
        val game = user.room.value ?: return
        log.add("${user.nutzer.value!!.name} gibt auf")
        this.pool.keys.filter { it.room.value == game }.forEach {
            it.sende(Nachricht.End(Ausgang.Verloren(Spieler(user.nutzer.value!!.name, SpielerArt.MENSCH))))
            it.room.value = null
        }
        sendRoomUpdates()
    }

    fun createRoom(user1: ServerConnection, user2: String?, game: Game, width: Int, height: Int) {
        log.add("Erstelle ${width}x${height} ${game.plain} Raum mit ${user1.nutzer.value!!.name} und ${user2 ?: "dem Computer"}")
        val user2 = user2?.let { this.findUser(it)!! }

        val spiel = game.create(
            width, height, arrayOf(
                Spieler(user1.nutzer.value!!.name, SpielerArt.MENSCH),
                Spieler(
                    user2?.nutzer?.value?.name ?: "Computer",
                    if (user2 == null) SpielerArt.COMPUTER else SpielerArt.MENSCH
                ),
            )
        ) { x, y, spieler ->
            // log.add("Setze ${x + 1}, ${y + 1} auf ${spieler.name}")
            user1.sende(Nachricht.Setzen(spieler.name, x, y))
            user2?.sende(Nachricht.Setzen(spieler.name, x, y))
        }
        user1.room.value = spiel
        user2?.room?.value = spiel

        user1.sende(Nachricht.Beitreten(user2?.nutzer?.value?.name ?: "Computer", game, width, height))
        user2?.sende(Nachricht.Beitreten(user1.nutzer.value!!.name, game, width, height))
    }

    fun setzen(spieler: Spieler, spiel: Spiel, x: Int, y: Int) {
        if (spiel.amZug().name == spieler.name) {
            val ausgang = spiel.zug(x, y)
            if (ausgang != null) {
                this.log.add("Nach Zug von ${spieler.name}: $ausgang")
                this.pool.keys.filter { it.room.value == spiel }.forEach {
                    it.sende(Nachricht.End(ausgang))
                    it.room.value = null
                }
                this.sendRoomUpdates()
            }
        }
    }
}

class ServerConnection(private val server: Server, private val socket: Socket) {
    private val reader: Scanner = Scanner(socket.getInputStream())
    private val writer: OutputStream = socket.getOutputStream()

    val room = mutableStateOf<Spiel?>(null)
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
            is Nachricht.Text -> this.server.message(this.nutzer.value, this.room.value, msg.content)
            is Nachricht.Einladen -> this.server.invite(msg.name, this.nutzer.value!!, msg.game, msg.width, msg.height)
            is Nachricht.Beitreten -> if (msg.width == 0 || msg.height == 0) {
                this.server.quitGame(this)
            } else {
                this.server.createRoom(
                    this,
                    if (msg.name == "") null else msg.name,
                    msg.game,
                    msg.width,
                    msg.height
                )
            }

            is Nachricht.Setzen -> this.server.setzen(
                Spieler(this.nutzer.value!!.name, SpielerArt.MENSCH),
                this.room.value!!,
                msg.x,
                msg.y,
            )

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