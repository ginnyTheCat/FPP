fun main() {
    var spielIndex: Int?
    while (true) {
        print("Spiel auswählen (1 = Vier gewinnt, 2 = Futtern): ")
        spielIndex = readln().toIntOrNull()
        if (spielIndex == 1 || spielIndex == 2) {
            break
        }
    }

    print("Bitte Höhe eingeben: ")
    val hoehe = readln().toInt()

    print("Bitte Breite eingeben: ")
    val breite = readln().toInt()

    val spieler = arrayOf(
        Spieler("X", SpielerArt.MENSCH),
        Spieler("O", SpielerArt.COMPUTER),
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
}
