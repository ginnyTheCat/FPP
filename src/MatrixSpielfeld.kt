class MatrixSpielfeld(hoehe: Int, breite: Int) : Spielfeld(hoehe, breite) {
    private val felder: Array<Array<Spieler?>> = Array(hoehe) { Array(breite) { null } }

    override fun darstellen(): String {
        // TODO
        return "";
    }
}