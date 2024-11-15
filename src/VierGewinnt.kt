class VierGewinnt(
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

        return if (this.unentschieden()) {
            Ausgang.Unentschieden
        } else {
            this.gewinner()?.let { Ausgang.Gewonnen(it) }
        }
    }

    private fun kannEinwerfen(x: Int): Boolean {
        return this.feld.matrix[0][x] == null
    }

    private fun einwerfen(x: Int, spieler: Spieler): Boolean {
        for (i in this.feld.matrix.indices.reversed()) {
            if (this.feld.matrix[i][x] == null) {
                this.feld.matrix[i][x] = spieler
                return true
            }
        }
        return false
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

    private fun unentschieden(): Boolean {
        return this.feld.matrix.all { reihe ->
            reihe.all { feld ->
                feld != null
            }
        }
    }

    private fun gewinner(): Spieler? {
        // Reihen prüfen
        for (reihe in this.feld.matrix) {
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
                val erstes = this.feld.matrix[y][x]
                if (
                    erstes != null &&
                    erstes == this.feld.matrix[y + 1][x] &&
                    erstes == this.feld.matrix[y + 2][x] &&
                    erstes == this.feld.matrix[y + 3][x]
                ) {
                    return erstes
                }
            }
        }

        // Diagonale \ prüfen
        for (y in 0..this.feld.hoehe - 4) {
            for (x in 0..this.feld.breite - 4) {
                val erstes = this.feld.matrix[y][x]
                if (
                    erstes != null &&
                    erstes == this.feld.matrix[y + 1][x + 1] &&
                    erstes == this.feld.matrix[y + 2][x + 2] &&
                    erstes == this.feld.matrix[y + 3][x + 3]
                ) {
                    return erstes
                }
            }
        }

        // Diagonale / prüfen
        for (y in 0..this.feld.hoehe - 4) {
            for (x in 0..this.feld.breite - 4) {
                val erstes = this.feld.matrix[y][x + 3]
                if (
                    erstes != null &&
                    erstes == this.feld.matrix[y + 1][x + 2] &&
                    erstes == this.feld.matrix[y + 2][x + 1] &&
                    erstes == this.feld.matrix[y + 3][x]
                ) {
                    return erstes
                }
            }
        }

        return null
    }

    private fun spielzug_spieler(selbst: Spieler) {
        while (true) {
            val x = intInput("In Spalte setzen", 1..this.feld.breite)

            if (!einwerfen(x - 1, selbst)) {
                println("Spalte schon voll, andere Spalte wählen.")
            } else {
                break
            }
        }
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
        var drunterFrei = y + 1 >= this.feld.hoehe || this.feld.matrix[y + 1][x] != null
        if (drunterFrei &&
            this.feld.matrix[y][x] == null &&
            this.feld.matrix[y - diagonal][x - 1] != null &&
            this.feld.matrix[y - diagonal][x - 1] != computer &&
            this.feld.matrix[y - diagonal][x - 1] == this.feld.matrix[y + diagonal][x + 1]
        ) {
            return x
        }

        // Links frei testen
        drunterFrei = y - diagonal + 1 >= this.feld.hoehe || this.feld.matrix[y - diagonal + 1][x - 1] != null
        if (drunterFrei &&
            this.feld.matrix[y - diagonal][x - 1] == null &&
            this.feld.matrix[y][x] != null &&
            this.feld.matrix[y][x] != computer &&
            this.feld.matrix[y][x] == this.feld.matrix[y + diagonal][x + 1]
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
            if (this.feld.matrix[y - 1][x] == null &&
                this.feld.matrix[y][x] != null &&
                this.feld.matrix[y][x] != computer &&
                this.feld.matrix[y][x] == this.feld.matrix[y + 1][x]
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
}



