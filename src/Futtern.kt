class Futtern(
    hoehe: Int,
    breite: Int,
    spieler: Array<Spieler>,
    override val feld: MatrixSpielfeld = MatrixSpielfeld(hoehe, breite)
) : Spiel(spieler, feld) {

    var amZugIndex = 0

    override fun spielzug(): Ausgang? {
        println()
        feld.darstellen()

        val amZugSpieler = spieler[amZugIndex]
        println("${amZugSpieler.name} am Zug")
        when (amZugSpieler.art) {
            SpielerArt.MENSCH -> {
                spielzug_spieler(amZugSpieler)
            }

            SpielerArt.COMPUTER -> {
                TODO()
            }
        }

        amZugIndex++;
        if (amZugIndex == spieler.size) {
            amZugIndex = 0
        }

        return this.feld.matrix[0][0]?.let { Ausgang.Verloren(it) }
    }

    private fun spielzug_spieler(selbst: Spieler) {
        while (true) {
            print("In Spalte setzen: ")
            val x = readln().toInt()

            print("In Zeile setzen: ")
            val y = readln().toInt()

            if (this.setzen(x - 1, y - 1, selbst)) {
                break
            } else {
                println("Feld schon belegt, anderes Feld wählen")
            }
        }
    }

    private fun spielzug_computer(selbst: Spieler) {
        //gewinnstrategie: quadrate vermeiden
        //wennn quadratisch -> symmetrisch
        //Sonst anzahl der kästchen gerade halten durch randomisierte kästchen,
        //bei denen gecheckt wird, ob die übrig bleibdende Anzahl gerade bleibt

        if (feld.breite == feld.hoehe) {
            //quadratische Strategie
        } else {
            //unquadratische strategie

        }

    }


    override fun durchgang(): Ausgang? {
        for (s in spieler) {
            val ausgang = spielzug()
            if (ausgang != null) {
                return ausgang
            }
        }
        return null
    }

    private fun setzen(x: Int, y: Int, spieler: Spieler): Boolean {
        if (feld.matrix[y][x] != null) {
            return false
        }

        for (row in this.feld.matrix.drop(y)) {
            for (i in row.indices.drop(x)) {
                if (row[i] == null) {
                    row[i] = spieler
                }
            }
        }
        return true
    }
}