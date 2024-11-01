class MatrixSpielfeld(hoehe: Int, breite: Int) : Spielfeld(hoehe, breite) {
    private val matrix: Array<Array<Spieler?>> = Array(hoehe) { Array(breite) { null } }

    override fun darstellen() {
        for (i in 0..hoehe - 1) {
            for (j in 0..breite - 1) {
                print(matrix[i][j]);
                print(" ");
            }
            println();
        }
    }
}
