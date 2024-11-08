fun main() {
    print("Bitte Höhe eingeben: ")
    val hoehe = readln().toInt()

    print("Bitte Breite eingeben: ")
    val breite = readln().toInt()

    val spiel: Spiel = Futtern(
        hoehe, breite,
        arrayOf(
            Spieler("X", SpielerArt.MENSCH),
            Spieler("O", SpielerArt.MENSCH),
        )
    )

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