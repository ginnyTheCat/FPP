import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.io.OutputStream
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.concurrent.thread

class Client(
    private val adresse: String,
    private val port: Int,
    private val snackbarAction: (String, String?, (() -> Unit)?) -> Unit,
) {
    private var socket: Socket? = null
    private var ausgang: OutputStream? = null

    private fun snackbar(msg: String, actionLabel: String? = null, onClick: (() -> Unit)? = null) =
        snackbarAction(msg, actionLabel, onClick)

    private val ende = mutableStateOf<Ausgang?>(null)
    private val chatting = mutableStateOf(false)
    private var spiel = mutableStateOf<Spiel?>(null)
    private val nachrichten = mutableStateListOf<Nachricht>()
    private val rooms = mutableStateOf<List<String>>(listOf())
    private val amZug = mutableStateOf("")

    private val online = mutableStateMapOf<String, Boolean>()

    fun start() {
        val socket = Socket(adresse, port)
        this.socket = socket
        this.ausgang = socket.getOutputStream()
    }

    fun stop() {
        this.socket?.close()
    }

    @Composable
    fun ui() {
        LaunchedEffect(true) {
            thread { hintergrund(socket!!) }
        }

        if (spiel.value != null) {
            spielDialog()
        } else if (chatting.value) {
            chatDialog()
        } else {
            anmeldeDialog()
        }
    }

    @Composable
    private fun spielDialog() {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.fillMaxSize().weight(1f)) {
                Box(Modifier.align(Alignment.Center)) {
                    spiel.value!!.ui { x, y -> sende(Nachricht.Setzen(null, x, y)) }
                }
            }
            Column(Modifier.fillMaxWidth().weight(1f)) {
                Button(
                    { sende(Nachricht.Beitreten("", Game.VierGewinnt, 0, 0)) },
                    Modifier.fillMaxWidth(),
                ) {
                    Text("Aufgeben")
                }
                Text(
                    "${amZug.value} ist am Zug",
                    color = colors[amZug.value.hashCode().mod(colors.size)],
                    textAlign = TextAlign.Center,
                )
                ChatView(nachrichten) {
                    sende(Nachricht.Text(null, it))
                }
            }
        }

        fun backToMenu() {
            spiel.value = null
            ende.value = null
        }

        if (ende.value != null) {
            Dialog(onDismissRequest = { backToMenu() }) {
                Card {
                    Column(
                        Modifier.width(512.dp).padding(32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text("${ende.value}")
                        Button({ backToMenu() }) {
                            Text("Zurück zur Lobby")
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun anmeldeDialog() {
        var anmelden by remember { mutableStateOf(true) }

        var email by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        Box(Modifier.fillMaxSize()) {
            Column(Modifier.width(512.dp).align(Alignment.Center), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    email,
                    { email = it },
                    Modifier.fillMaxWidth(),
                    label = { Text("E-Mail-Adresse") },
                )

                if (anmelden) {
                    OutlinedTextField(
                        password, { password = it },
                        Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        label = { Text("Passwort") },
                    )

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        TextButton({ anmelden = false }) { Text("Registrieren") }
                        Button({
                            sende(Nachricht.Anmelden(email, password))
                        }) { Text("Anmelden") }
                    }
                } else {
                    OutlinedTextField(
                        name,
                        { name = it },
                        Modifier.fillMaxWidth(),
                        label = { Text("Name") },
                    )

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        TextButton({ anmelden = true }) { Text("Anmelden") }
                        Button({
                            sende(Nachricht.Registrieren(email, name))
                            password = ""
                            anmelden = true
                            snackbar("Sie sollten ihr Passwort nun per E-Mail zugeschickt bekommen.")
                        }) { Text("Registrieren") }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun chatDialog() {
        var changePassword by remember { mutableStateOf(false) }

        var invite by remember { mutableStateOf<String?>(null) }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChatView(nachrichten, Modifier.weight(1f)) {
                sende(Nachricht.Text(null, it))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button({ changePassword = true }, Modifier.fillMaxWidth().weight(1f)) {
                        Text("Passwort ändern")
                    }
                    Button({ invite = "" }, Modifier.fillMaxWidth().weight(1f)) {
                        Text("Bot herausfordern")
                    }
                }
                Card(Modifier.fillMaxSize().weight(1f)) {
                    Text(
                        "Momentan Online",
                        Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    LazyColumn {
                        items(online.toList()) { (name, self) ->
                            ListItem(
                                leadingContent = { Icon(Icons.Default.Person, null) },
                                headlineContent = { Text(name) },
                                trailingContent = if (self) ({ }) else ({
                                    Button({
                                        invite = name
                                    }) { Text("Einladen") }
                                }),
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            )
                        }
                    }
                }
                Card(Modifier.fillMaxSize().weight(1f)) {
                    Text(
                        "Aktuelle Spiele",
                        Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    LazyColumn {
                        items(rooms.value) {
                            ListItem(
                                leadingContent = { Icon(Icons.Default.Casino, null) },
                                headlineContent = { Text(it) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            )
                        }
                    }
                }
            }
        }

        var width by remember { mutableStateOf("7") }
        var height by remember { mutableStateOf("7") }
        var game by remember { mutableStateOf(Game.VierGewinnt) }
        if (invite != null) {
            Dialog(onDismissRequest = { invite = null }) {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                width, { width = it },
                                Modifier.fillMaxWidth().weight(1f),
                                label = { Text("Breite") },
                            )
                            Icon(Icons.Default.Close, null)
                            OutlinedTextField(
                                height, { height = it },
                                Modifier.fillMaxWidth().weight(1f),
                                label = { Text("Höhe") },
                            )
                        }

                        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                            Game.entries.forEachIndexed { i, entry ->
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(i, Game.entries.size),
                                    selected = game == entry,
                                    onClick = { game = entry },
                                ) { Text(entry.plain) }
                            }
                        }

                        val enabled by derivedStateOf {
                            val width = width.toIntOrNull() ?: 0
                            val height = height.toIntOrNull() ?: 0
                            width in 1..20 && height in 1..20
                        }

                        Button(
                            enabled = enabled,
                            onClick = {
                                if (invite == "") {
                                    sende(Nachricht.Beitreten("", game, width.toInt(), height.toInt()))
                                } else {
                                    sende(Nachricht.Einladen(invite!!, game, width.toInt(), height.toInt()))
                                }
                                invite = null
                            },
                        ) { Text(if (invite == "") "Bot herausfordern" else "Raum erstellen") }
                    }
                }
            }
        }

        var oldPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        if (changePassword) {
            Dialog(onDismissRequest = { changePassword = false }) {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            oldPassword, { oldPassword = it },
                            Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            label = { Text("Altes Passwort") },
                        )

                        OutlinedTextField(
                            newPassword, { newPassword = it },
                            Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            label = { Text("Neues Passwort") },
                        )

                        Button({
                            sende(Nachricht.PasswortAendern(oldPassword, newPassword))
                            changePassword = false
                        }) { Text("Passwort ändern") }
                    }
                }
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

    private fun handleNachricht(msg: Nachricht) {
        when (msg) {
            is Nachricht.Einladen -> {
                snackbar(
                    "${msg.name} hat dich zu ${msg.width}x${msg.height} ${msg.game.plain} eingeladen!",
                    "Beitreten"
                ) { sende(Nachricht.Beitreten(msg.name, msg.game, msg.width, msg.height)) }
                return
            }

            is Nachricht.Beitreten -> {
                this.spiel.value = msg.game.create(msg.width, msg.height, arrayOf())
                return
            }

            is Nachricht.Setzen -> {
                this.spiel.value?.feld?.set(msg.x, msg.y, Spieler(msg.name!!, SpielerArt.COMPUTER))
                return
            }

            is Nachricht.End -> {
                this.ende.value = msg.ausgang
                return
            }

            is Nachricht.Verbinden -> {
                this.online[msg.name] = msg.self
                if (msg.update) return
            }

            is Nachricht.Trennen -> this.online.remove(msg.name)

            is Nachricht.Room -> {
                this.rooms.value = msg.names.toList()
                return
            }

            is Nachricht.AmZug -> {
                this.amZug.value = msg.name
                return
            }

            is Nachricht.Success -> {
                when (msg.where) {
                    "an" -> chatting.value = true
                    "chpwd" -> snackbar("Passwort erfolgreich geändert!")
                }
                return
            }

            is Nachricht.Fail -> {
                when (msg.where) {
                    "an" -> {
                        snackbar("Anmeldung fehlgeschlagen! Bitte überprüfen sie ihre E-Mail-Adresse und ihr Passwort.")
                        chatting.value = false
                    }

                    "chpwd" -> snackbar("Passwort ändern fehlgeschlagen.")
                }
                return
            }

            else -> {}
        }

        if (this.chatting.value) {
            this.nachrichten.add(msg)
        }
    }
}