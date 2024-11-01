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
        for (i in 0..<feld.hoehe)
            for (j in 0..<feld.breite)
            //x für ersten spieler,o für zweiten
            //wenn man gegen computer spielt, spielt der immer o
           val aktuellesFeld = this.feld.matrix[i][j]

            if (aktuellesFeld != null && aktuellesFeld.art != SpielerArt.COMPUTER)
                umgebung_checken(i, j  )

    }
    fun umgebung_checken( i : Int, j: Int){
        val aktuelles_feld = this.feld.matrix[i][j]
        var x_in_reihe=0
      //nach  unten schauen
        if (aktuelles_feld != null) {
            if  ( this.feld.matrix[i+1][j]!= null && aktuelles_feld.art != SpielerArt.COMPUTER)
                x_in_reihe+=1
                umgebung_checken(i+1,j)
        }
            umgebung_checken(i+1,j)
        if (aktuelles_feld != null) {
            if  ( this.feld.matrix[i][j+1]!= null && aktuelles_feld.art != SpielerArt.COMPUTER)
                x_in_reihe+=1
                umgebung_checken(i,j+1)
        }


    }
}



