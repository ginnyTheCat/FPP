import androidx.compose.runtime.Composable

class Futtern(
    hoehe: Int,
    breite: Int,
    spieler: Array<Spieler>,
    onSet: (x: Int, y: Int, spieler: Spieler) -> Unit,
    override val feld: MatrixSpielfeld = MatrixSpielfeld(hoehe, breite, onSet)
) : Spiel(spieler, feld) {

    private var amZugIndex = 0

    override fun amZug(): Spieler {
        return spieler[amZugIndex]
    }

    override fun zug(x: Int, y: Int): Ausgang? {
        val amZugSpieler = spieler[amZugIndex]
        if (!this.setzen(x, y, amZugSpieler)) {
            return null;
        }

        while (this.feld.get(0, 0) == null) {
            amZugIndex++;
            if (amZugIndex == spieler.size) {
                amZugIndex = 0
            }

            val amZugSpieler = spieler[amZugIndex]
            if (spieler[amZugIndex].art == SpielerArt.COMPUTER) {
                spielzug_computer(amZugSpieler)
            } else {
                break
            }
        }

        return this.feld.get(0, 0)?.let { Ausgang.Verloren(it) }
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
                val nachUnten = feld.reihen().indexOfLast { it[0] == null }
                val nachRechts = feld.reihen()[0].indexOfLast { it == null }

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
            if (this.feld.get(x, y) == null) {
                return Pair(x, y)
            }
        }
    }

    fun generierenUndChecken(computer: Spieler) {
        val voll = this.feld.get(0, 1) != null && this.feld.get(1, 0) != null
        if (voll) {
            this.setzen(0, 0, computer)
            return
        }

        while (true) {
            val (x, y) = randomPlatz()

            val anzahlVerbleiben =
                feld.reihen().indices.sumOf { y_i ->
                    feld.reihen()[y_i].indices.count { x_i ->
                        this.feld.get(x_i, y_i) == null && (x > x_i || y > y_i)
                    }
                }

            if (anzahlVerbleiben % 2 != 0) {
                this.setzen(x, y, computer)
                return
            }
        }
    }

    private fun setzen(x: Int, y: Int, spieler: Spieler): Boolean {
        if (this.feld.get(x, y) != null) {
            return false
        }

        for (iy in this.feld.reihen().indices.drop(y)) {
            for (ix in this.feld.spalten().indices.drop(x)) {
                if (this.feld.get(ix, iy) == null) {
                    this.feld.set(ix, iy, spieler)
                }
            }
        }
        return true
    }

    @Composable
    override fun ui(onSet: (x: Int, y: Int) -> Unit) {
        this.feld.ui(onSet)
    }
}