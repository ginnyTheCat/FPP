import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable
@Preview
fun App() {
    var counter by remember { mutableStateOf(0) }

    MaterialTheme {
        Button(onClick = { counter++ }) {
            Text("${counter}x FPP!")
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
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
