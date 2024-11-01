class VierGewinnt(
    spieler: Array<Spieler>,
    override val feld: MatrixSpielfeld = MatrixSpielfeld(run {
        print("Bitte Höhe eingeben: ")
        readln().toInt()
    }, run {
        print("Bitte Breite eingeben: ")
        readln().toInt()
    })
) : Spiel(spieler, feld) {

    var amZugIndex = 0

    override fun spielzug() {
        feld.darstellen()

        val amZugSpieler = spieler[amZugIndex]
        println("${amZugSpieler.name} am Zug")
        when (amZugSpieler.art) {
            SpielerArt.MENSCH -> {}
            SpielerArt.COMPUTER -> spielzug_computer(amZugSpieler)
        }

        amZugIndex++;
        if (amZugIndex == spieler.size) {
            amZugIndex = 0
        }
    }

    override fun durchgang() {
        TODO("Not yet implemented")
    }

    fun spielzug_computer(selbst: Spieler) {
        //berechne die zweierketten mit freiem platz daneben
        for (i in 0..feld.hoehe - 1)
            for (j in 0..feld.breite - 1)
            //x für ersten spieler,o für zweiten
            //wenn man gegen computer spielt, spielt der immer o
                if (this.feld.matrix[i][j])

                    this.feld.matrix
        //wenn nicht, dann random


    }
}



