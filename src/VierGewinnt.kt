import androidx.compose.runtime.Composable

class VierGewinnt(
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
        if (!this.einwerfen(x, amZugSpieler)) {
            return null;
        }

        while (true) {
            if (this.unentschieden()) {
                return Ausgang.Unentschieden
            } else {
                val gewinner = this.gewinner()
                if (gewinner != null) {
                    return Ausgang.Gewonnen(gewinner)
                }
            }

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

        return null
    }

    private fun kannEinwerfen(x: Int): Boolean {
        return this.feld.get(x, 0) == null
    }

    private fun einwerfen(x: Int, spieler: Spieler): Boolean {
        for (y in (0..<this.feld.hoehe).reversed()) {
            if (this.feld.get(x, y) == null) {
                this.feld.set(x, y, spieler)
                return true
            }
        }
        return false
    }

    private fun unentschieden(): Boolean {
        return this.feld.entries().all { it != null }
    }

    private fun gewinner(): Spieler? {
        // Reihen prüfen
        for (reihe in this.feld.reihen()) {
            for (i in 0..reihe.size - 4) {
                if (reihe[i] != null &&
                    reihe[i] == reihe[i + 1] &&
                    reihe[i] == reihe[i + 2] &&
                    reihe[i] == reihe[i + 3]
                ) {
                    return reihe[i]
                }
            }
        }

        // Spalten prüfen
        for (x in 0..<this.feld.breite) {
            for (y in 0..this.feld.hoehe - 4) {
                val erstes = this.feld.get(x, y)
                if (
                    erstes != null &&
                    erstes == this.feld.get(x, y + 1) &&
                    erstes == this.feld.get(x, y + 2) &&
                    erstes == this.feld.get(x, y + 3)
                ) {
                    return erstes
                }
            }
        }

        // Diagonale \ prüfen
        for (y in 0..this.feld.hoehe - 4) {
            for (x in 0..this.feld.breite - 4) {
                val erstes = this.feld.get(x, y)
                if (
                    erstes != null &&
                    erstes == this.feld.get(x + 1, y + 1) &&
                    erstes == this.feld.get(x + 2, y + 2) &&
                    erstes == this.feld.get(x + 3, y + 3)
                ) {
                    return erstes
                }
            }
        }

        // Diagonale / prüfen
        for (y in 0..this.feld.hoehe - 4) {
            for (x in 0..this.feld.breite - 4) {
                val erstes = this.feld.get(x + 3, y)
                if (
                    erstes != null &&
                    erstes == this.feld.get(x + 2, y + 1) &&
                    erstes == this.feld.get(x + 1, y + 2) &&
                    erstes == this.feld.get(x, y + 3)
                ) {
                    return erstes
                }
            }
        }

        return null
    }

    private fun spielzug_computer(computer: Spieler) {
        //berechne die zweierketten mit freiem platz daneben
        for (y in 1..<feld.hoehe) {
            for (x in 1..<feld.breite - 1) {
                val spalte = umgebung_checken(x, y, computer)
                if (spalte != null) {
                    this.einwerfen(spalte, computer);
                    return;
                }
            }
        }

        this.randomZug(computer)
    }

    private fun maske_testen(x: Int, y: Int, diagonal: Int, computer: Spieler): Int? {
        // Mitte frei testen
        var drunterFrei = y + 1 >= this.feld.hoehe || this.feld.get(x, y + 1) != null
        if (drunterFrei &&
            this.feld.get(x, y) == null &&
            this.feld.get(x - 1, y - diagonal) != null &&
            this.feld.get(x - 1, y - diagonal) != computer &&
            this.feld.get(x - 1, y - diagonal) == this.feld.get(x + 1, y + diagonal)
        ) {
            return x
        }

        // Links frei testen
        drunterFrei = y - diagonal + 1 >= this.feld.hoehe || this.feld.get(x - 1, y - diagonal + 1) != null
        if (drunterFrei &&
            this.feld.get(x - 1, y - diagonal) == null &&
            this.feld.get(x, y) != null &&
            this.feld.get(x, y) != computer &&
            this.feld.get(x, y) == this.feld.get(x + 1, y + diagonal)
        ) {
            return x - 1
        }

        return null
    }

    private fun umgebung_checken(x: Int, y: Int, computer: Spieler): Int? {
        // Nebeneinander verhindern
        var spalte = this.maske_testen(x, y, 0, computer)
        if (spalte != null) {
            return spalte
        }

        if (y != this.feld.hoehe - 1) {
            // Diagonal \ verhindern
            spalte = this.maske_testen(x, y, 1, computer)
            if (spalte != null) {
                return spalte
            }

            // Diagonal / verhindern
            spalte = this.maske_testen(x, y, -1, computer)
            if (spalte != null) {
                return spalte
            }

            // Übereinander verhindern
            if (this.feld.get(x, y - 1) == null &&
                this.feld.get(x, y) != null &&
                this.feld.get(x, y) != computer &&
                this.feld.get(x, y) == this.feld.get(x, y + 1)
            ) {
                return x
            }
        }

        return null
    }

    fun randomZug(computer: Spieler) {
        while (true) {
            val rnds = (0..<feld.breite).random()
            if (this.einwerfen(rnds, computer)) {
                break
            }
        }
    }

    @Composable
    override fun ui(onSet: (x: Int, y: Int) -> Unit) {
        this.feld.ui(onSet)
    }
}