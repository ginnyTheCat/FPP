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

    /*
    //erste koordinate ist zeile, die zweite ist spalte
    private fun kannWaagerecht(x: Int, y: Int, computer: Spieler): Boolean {
        //wenn man nicht links anbauen kann...
        if (x == 0 || (y > 0 && feld.matrix[x][y - 1] != null)) {
            //kann man rechts anbauen?
            if (x == feld.breite - 1) {
                return false
            } else {
                //rechts checken
                if (feld.matrix[x][y] == null && feld.matrix[x][y - 1] != null) {
                }
            }

        }
    }
     */

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
            print("In Spalte setzen: ")
            val x = readln().toInt()

            if (x <= 0) {
                println("Höheren Spaltenindex wählen")
            } else if (x > this.feld.matrix[0].size) {
                println("Niedrigeren Spaltenindex wählen")
            } else if (einwerfen(x - 1, selbst)) {
                break
            } else {
                println("Spalte schon voll, andere Spalte wählen")
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
            this.feld.matrix[y - diagonal][x - 1] != null &&
            this.feld.matrix[y - diagonal][x - 1] != computer &&
            this.feld.matrix[y - diagonal][x - 1] == this.feld.matrix[y + diagonal][x + 1]
        ) {
            return x
        }

        // Links frei testen
        drunterFrei = y - diagonal + 1 >= this.feld.hoehe || this.feld.matrix[y - diagonal + 1][x - 1] != null
        if (drunterFrei &&
            this.feld.matrix[y][x] != null &&
            this.feld.matrix[y][x] != computer &&
            this.feld.matrix[y][x] == this.feld.matrix[y + diagonal][x + 1]
        ) {
            return x - 1
        }

        return null
    }

    private fun umgebung_checken(x: Int, y: Int, computer: Spieler): Int? {
        var spalte = this.maske_testen(x, y, 0, computer)
        if (spalte != null) {
            return spalte
        }

        if (y != this.feld.hoehe - 1) {
            spalte = this.maske_testen(x, y, 1, computer)
            if (spalte != null) {
                return spalte
            }


            spalte = this.maske_testen(x, y, -1, computer)
            if (spalte != null) {
                return spalte
            }
        }

        /*
        //gibt true oder false zurpück, je nachdem, ob ein match gefunden wurde für einen strategisch gute zug
        val aktuellesFeld = this.feld.matrix[y][x]
        var x_in_reihe = 0
        //nach  unten schauen
        if (aktuellesFeld != null) {
            if (this.feld.matrix[y + 1][x] != null && this.feld.matrix[y + 1][x] != computer) {
                x_in_reihe += 1
                umgebung_checken(y + 1, x, computer)
            }

            if (x_in_reihe == 2) {

                if (this.kannEinwerfen(x) == true) {
                    this.einwerfen(x, computer);
                    return true;
                }
            }

            //liste hier mit tupeln aus i und j füllen, damit man darauf zugreifen kann
        }

        //waagerechte linien checken


        //zuerst rausfinden, ob es diese linien gibt,
        // und dann, ob sie verhinderbar sind durch draufstapeln.
        // wenn draufstapeln nicht möglich, weitersuchen.
        //hier speichern, wo der anfang der reihe ist, diesen dann übergeben

        if (aktuellesFeld != null) {
            //speichern das startfeld ein für waagerechtes checken
            val xGefunden = y
            val yGefunden = x;
            if (this.feld.matrix[y][x + 1] != null && this.feld.matrix[y][x + 1] != computer) {
                x_in_reihe += 1
                umgebung_checken(y, x + 1, computer)
            }
            if (x_in_reihe == 2) {

                if (this.kannEinwerfen(x) == true) {
                    this.einwerfen(x, computer);
                    return true;
                }
            }
        }

         */

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

    fun defaultZug(spieler: Spieler) {
        this.einwerfen(0, spieler)
    }
}



