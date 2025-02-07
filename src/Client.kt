import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
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

    private val online = mutableStateMapOf<String, Boolean>()

    fun start() {
        val socket = Socket(adresse, port)
        this.socket = socket
        this.ausgang = socket.getOutputStream()
        thread { this.hintergrund(socket) }
    }

    fun stop() {
        this.socket?.close()
    }

    @Composable
    fun ui() {
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
        Box(Modifier.fillMaxSize()) {
            Box(Modifier.align(Alignment.Center)) {
                spiel.value!!.ui { x, y ->
                    sende(Nachricht.Setzen(null, x, y))
                }
            }
        }

        if (ende.value != null) {
            Dialog(onDismissRequest = {
                spiel.value = null
                ende.value = null
            }) {
                Card {
                    Text("${ende.value}", Modifier.padding(64.dp))
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

        var input by remember { mutableStateOf("") }
        var invite by remember { mutableStateOf<String?>(null) }

        fun send() {
            if (input.isNotEmpty()) {
                sende(Nachricht.Text(null, input))
                input = ""
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LazyColumn(Modifier.fillMaxHeight().weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(nachrichten) {
                        when (it) {
                            is Nachricht.Text -> Row {
                                Text("${it.from!!}:")
                                Spacer(Modifier.width(4.dp))
                                Card(shape = CircleShape.copy(bottomStart = ZeroCornerSize)) {
                                    Text(it.content, Modifier.padding(horizontal = 12.dp))
                                }
                            }

                            is Nachricht.Verbinden -> Text("${it.name} ist dem Raum beigetreten.")
                            is Nachricht.Trennen -> Text("${it.name} hat den Raum verlassen.")

                            else -> Text(it.toString())
                        }
                    }
                }
                TextField(
                    input, { input = it },
                    Modifier.fillMaxWidth(),
                    placeholder = { Text("Nachricht senden...") },
                    trailingIcon = {
                        IconButton(
                            { send() },
                            enabled = input.isNotEmpty()
                        ) { Icon(Icons.AutoMirrored.Default.Send, null) }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { send() }),
                    shape = CircleShape,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,
                    )
                )
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
                Card(Modifier.fillMaxHeight()) {
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
            }
        }

        var width by remember { mutableIntStateOf(7) }
        var height by remember { mutableIntStateOf(7) }
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
                                width.toString(), { width = it.toIntOrNull() ?: width },
                                Modifier.fillMaxWidth().weight(1f),
                                label = { Text("Breite") },
                            )
                            Icon(Icons.Default.Close, null)
                            OutlinedTextField(
                                height.toString(), { height = it.toIntOrNull() ?: height },
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

                        Button({
                            if (invite == "") {
                                sende(Nachricht.Beitreten("", game, width, height))
                            } else {
                                sende(Nachricht.Einladen(invite!!, game, width, height))
                            }
                            invite = null
                        }) { Text(if (invite == "") "Bot herausfordern" else "Einladen") }
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

            is Nachricht.Verbinden -> this.online[msg.name] = msg.self
            is Nachricht.Trennen -> this.online.remove(msg.name)

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