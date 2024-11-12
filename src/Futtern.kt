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
            SpielerArt.MENSCH -> spielzug_spieler(amZugSpieler)
            SpielerArt.COMPUTER -> spielzug_computer(amZugSpieler)
        }

        amZugIndex++;
        if (amZugIndex == spieler.size) {
            amZugIndex = 0
        }

        return this.feld.matrix[0][0]?.let { Ausgang.Verloren(it) }
    }

    private fun spielzug_spieler(selbst: Spieler) {
        while (true) {
            var x: Int
            while (true) {
                print("In Spalte setzen: ")
                x = readln().toInt()
                if (0 < x && x <= this.feld.breite) {
                    break
                }
            }

            var y: Int
            while (true) {
                print("In Zeile setzen: ")
                y = readln().toInt()
                if (0 < y && y <= this.feld.hoehe) {
                    break;
                }
            }

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
        //bei denen gecheckt wird, ob die übrig bleibende Anzahl gerade bleibt

        if (feld.breite == feld.hoehe) {
            //quadratische Strategie
            //wenn die position 1,1 noch nicht belegt ist, dann belegen
            //um ein quadrat in der mitte zu bauen, und dann immer kopieren,
            //was der Vorgänger tut
//---------------QUADRATISCH------------------------
            if (!setzen(1, 1, computer)) {
                val nachUnten = feld.matrix.indexOfLast { it[0] == null }
                val nachRechts = feld.matrix[0].indexOfLast { it == null }

                //checken, ob die erste zeile und die erste spalte gleich lang sind
                if (nachUnten == nachRechts) {
                    this.setzen(0, nachUnten, computer)
                } else {
                    if (nachUnten < nachRechts) {
                        // geht, da das Feld quadratisch ist
                        this.setzen(nachUnten + 1, 0, computer)
                    } else {
                        // geht, da das Feld quadratisch ist
                        this.setzen(0, nachRechts + 1, computer)
                    }
                }
            }
//-------------UNQUADRATISCH-------------------
        } else {
            //unquadratische strategie

            generierenUndChecken(computer)
        }
    }

    //erste koordinate ist zeile, die zweite ist spalte
    private fun randomPlatz(): Pair<Int, Int> {
        while (true) {
            val x = (0..<feld.breite).random()
            val y = (0..<feld.hoehe).random()

            if (x == 0 && y == 0) {
                continue
            }

            //ist das schon belegt?
            if (feld.matrix[y][x] == null) {
                return Pair(x, y)
            }
        }
    }

    fun generierenUndChecken(computer: Spieler) {
        val voll = feld.matrix[1][0] != null && feld.matrix[0][1] != null
        if (voll) {
            this.setzen(0, 0, computer)
        }

        while (true) {
            val (x, y) = randomPlatz()

            val anzahlVerbleiben =
                feld.matrix.indices.sumOf { y_i ->
                    feld.matrix[y_i].indices.count { x_i ->
                        feld.matrix[y_i][x_i] == null && (x > x_i || y > y_i)
                    }
                }

            if (anzahlVerbleiben % 2 != 0) {
                this.setzen(x, y, computer)
                return
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