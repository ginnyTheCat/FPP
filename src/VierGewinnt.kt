class VierGewinnt(spieler: Spieler) :
    Spiel(spieler, MatrixSpielfeld(run {
        print("Bitte Höhe eingeben: ")
        readln().toInt()
    }, run {
        print("Bitte Breite eingeben: ")
        readln().toInt()
    })) {


    override fun spielzug() {
        TODO("Not yet implemented")
    }

    override fun durchgang() {
        TODO("Not yet implemented")
    }


}



