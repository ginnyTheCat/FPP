class Futtern(
    spieler: Array<Spieler>,
    override val feld: MatrixSpielfeld = MatrixSpielfeld(run {
        print("Bitte Höhe eingeben: ")
        readln().toInt()
    }, run {
        print("Bitte Breite eingeben: ")
        readln().toInt()
    })
) : Spiel(spieler, feld) {

    val turn = 0

    override fun spielzug() {
        val spieler = spieler[turn]
        println("${spieler.name} am Zug")
        when (spieler.art) {
            SpielerArt.MENSCH -> {
                TODO()
            }

            SpielerArt.COMPUTER -> {
                TODO()
            }
        }
    }

    override fun durchgang() {
        TODO("Not yet implemented")
    }
}