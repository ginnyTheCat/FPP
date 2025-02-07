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
                                9876
                            ) { text, actionLabel, action ->
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        text,
                                        actionLabel,
                                        withDismissAction = action != null,
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        action?.invoke()
                                    }
                                }
                            }.also { it.start() }
                        }
                        client.ui()
                    }

                    App.Server -> {
                        val server =
                            remember { Server("fpp.uni@gmx.de", "fppunijena").also { it.start() } }
                        server.ui()
                    }
                }
            }
        }
    }
}
