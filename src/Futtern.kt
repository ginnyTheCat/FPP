class Futtern(
    spieler: Array<Spieler>,
    override val feld: MatrixSpielfeld
) : Spiel(spieler, feld) {

    constructor(spieler: Array<Spieler>) : this(
        spieler,
        MatrixSpielfeld({
            print("Bitte Höhe eingeben: ")
            readln().toInt()
        }(), {
            print("Bitte Breite eingeben: ")
            readln().toInt()
        }())
    )

    var amZugIndex = 0

    override fun spielzug() {
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
    }

    fun spielzug_spieler(selbst: Spieler) {
        while (true) {
            print("An X-Position setzen: ")
            val x = readln().toInt()

            print("An Y-Position setzen: ")
            val y = readln().toInt()

            if (feld.matrix[y][x] == null) {
                feld.matrix[y][x] = selbst
                break
            } else {
                println("Feld schon belegt, anderes Feld wählen")
            }
        }
    }

    override fun durchgang() {
        for (s in spieler) {
            spielzug()
        }
    }
}