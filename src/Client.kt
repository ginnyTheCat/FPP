import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import java.io.OutputStream
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.concurrent.thread

class Client(
    private val adresse: String,
    private val port: Int,
    private val snackbar: (String) -> Unit,
) {
    private var socket: Socket? = null
    private var ausgang: OutputStream? = null

    private val chatting = mutableStateOf(false)
    private val nachrichten = mutableStateListOf<Nachricht>()

    private val online = mutableStateMapOf<String, Unit>()

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
        if (chatting.value) {
            chatDialog()
        } else {
            anmeldeDialog()
        }
    }

    @Composable
    private fun anmeldeDialog() {
        var anmelden by remember { mutableStateOf(true) }

        var email by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TextField(email, { email = it }, label = { Text("E-Mail-Adresse") })

            if (anmelden) {
                TextField(password, { password = it }, label = { Text("Passwort") })

                Row {
                    TextButton({ anmelden = false }) { Text("Registrieren") }
                    Button({
                        sende(Nachricht.Anmelden(email, password))
                    }) { Text("Anmelden") }
                }
            } else {
                TextField(name, { name = it }, label = { Text("Name") })

                Row {
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

    @Composable
    private fun chatDialog() {
        var input by remember { mutableStateOf("") }

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
            Card(Modifier.fillMaxHeight().weight(0.5f)) {
                Text(
                    "Momentan Online",
                    Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                )
                LazyColumn {
                    items(online.keys.toList()) {
                        ListItem(
                            leadingContent = { Icon(Icons.Default.Person, null) },
                            headlineContent = { Text(it) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        )
                    }
                }
            }
        }

        /*
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
         */
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
            is Nachricht.Verbinden -> this.online[msg.name] = Unit
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