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

    private fun spielzug_computer(computer: Spieler) {
        //gewinnstrategie: quadrate vermeiden
        //wennn quadratisch -> symmetrisch
        //Sonst anzahl der kästchen gerade halten durch randomisierte kästchen,
        //bei denen gecheckt wird, ob die übrig bleibdende Anzahl gerade bleibt


        if (feld.breite == feld.hoehe) {
            //quadratische Strategie
            //wenn die position 1,1 noch nicht belegt ist, dann belegen
            //um ein quadrat in der mitte zu bauen, und dann immer kopieren,
            //was der Vorgänger tut
//---------------QUADRATISCH------------------------
            if (feld.matrix[1][1] == null) {
                this.setzen(1, 1, computer)
            } else {
                //checken, ob die erste zeile und die erste spalte gleich lang sind
                var nachUnten = 0
                var nachRechts = 0
                for (i in 0..<feld.hoehe) {
                    if (feld.matrix[i][0] == null) {
                        nachUnten += 1
                    }
                }
                for (j in 0..<feld.breite) {
                    if (feld.matrix[0][j] == null) {
                        nachRechts += 1
                    }
                }
                //erste koordinate ist zeile, die zweite ist spalte
                if (nachUnten == nachRechts) {
                    this.setzen(nachUnten, 0, computer)

                } else {
                    if (nachUnten < nachRechts) {
                        var differenz = nachRechts - nachUnten;
                        this.setzen(0, feld.breite - differenz, computer)
                    } else {
                        var differenz = nachUnten - nachRechts;
                        this.setzen(feld.hoehe - differenz, 0, computer)
                    }

                }
            }
//-------------UNQUADRATISCH-------------------
        } else {
            //unquadratische strategie
            var leereFelder = 0;
            for (i in 0..<feld.hoehe) {
                for (j in 0..<feld.breite) {

                    val aktuellesFeld = this.feld.matrix[i][j]
                    if (aktuellesFeld == null) {
                        leereFelder += 1
                    }
                }
            }
            if (leereFelder % 2 == 0) {
                generierenUndChecken(true, computer)
            } else {
                generierenUndChecken(false, computer)
            }
        }
    }

    //erste koordinate ist zeile, die zweite ist spalte
    private fun randomPlatz(): Pair<Int, Int> {
        val zeile = (0..<feld.hoehe).random()
        val spalte = (0..<feld.breite).random()

        if (feld.matrix[zeile][spalte] == null)
        //ist das schon belegt?
            return Pair(zeile, spalte)
        else {

            return randomPlatz()
        }
    }

    fun generierenUndChecken(gerade: Boolean, computer: Spieler) {
        val (zeile, spalte) = randomPlatz()
        var anzahlVerbleiben = 0
        for (i in 0..<zeile) {
            for (j in 0..<spalte) {
                if (feld.matrix[i][j] == null) {
                    anzahlVerbleiben += 1
                }

            }
            if (anzahlVerbleiben % 2 == 0 && gerade == true) {
                this.setzen(zeile, spalte, computer)
            }
            if (anzahlVerbleiben % 2 == 1 && gerade == false) {
                this.setzen(zeile, spalte, computer)

            }

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