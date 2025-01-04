import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.launch

enum class App {
    Client,
    Server,
}

fun main() = application {
    var app by remember { mutableStateOf<App?>(null) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Window(
        title = when (app) {
            null -> "FPP"
            App.Client -> "FPP - Client"
            App.Server -> "FPP - Server"
        },
        onCloseRequest = ::exitApplication,
    ) {
        MaterialTheme {
            Scaffold(
                Modifier.padding(8.dp),
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) {
                when (app) {
                    null -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button({ app = App.Client }) { Text("Client") }
                        Button({ app = App.Server }) { Text("Server") }
                    }

                    App.Client -> {
                        val client = remember {
                            Client(
                                "localhost",
                                9876,
                                { scope.launch { snackbarHostState.showSnackbar(it) } }
                            ).also { it.start() }
                        }
                        client.ui()
                    }

                    App.Server -> {
                        val server = remember { Server(null, null).also { it.start() } }
                        server.ui()
                    }
                }
            }
        }
    }

    /*
    if (args.isNotEmpty()) {
        Client("localhost", 9876).start()
    } else {
        Server(null, null).start()
    }
    */

    /*
    val spielIndex = intInput("Spiel auswählen (1 = Vier gewinnt, 2 = Futtern)", 1..2)

    val breite = intInput("Bitte Breite eingeben", 1..50)
    val hoehe = intInput("Bitte Höhe eingeben", 1..50)

    val gegner = intInput("Gegner auswählen (1 = Mensch, 2 = Computer)", 1..2)
    val gegnerType = if (gegner == 1) {
        SpielerArt.MENSCH
    } else {
        SpielerArt.COMPUTER
    }

    val spieler = arrayOf(
        Spieler("X", SpielerArt.MENSCH),
        Spieler("O", gegnerType),
    )

    val spiel: Spiel = if (spielIndex == 1) {
        VierGewinnt(hoehe, breite, spieler)
    } else {
        Futtern(hoehe, breite, spieler)
    }

    while (true) {
        val ausgang = spiel.durchgang()
        if (ausgang != null) {
            println()
            spiel.feld.darstellen()
            println(ausgang)
            break
        }
    }
     */
}
