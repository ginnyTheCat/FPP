class VierGewinnt(
    spieler: Spieler,
    override val feld: MatrixSpielfeld = MatrixSpielfeld(run {
        print("Bitte Höhe eingeben: ")
        readln().toInt()
    }, run {
        print("Bitte Breite eingeben: ")
        readln().toInt()
    })
) :
    Spiel(spieler, feld) {

    override fun spielzug() {
        this.feld.darstellen();
        print("In Spalte setzen: ")
    }

    override fun durchgang() {
        TODO("Not yet implemented")
    }
}



