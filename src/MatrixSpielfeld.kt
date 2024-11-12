class MatrixSpielfeld(hoehe: Int, breite: Int) : Spielfeld(hoehe, breite) {
    val matrix: Array<Array<Spieler?>> = Array(hoehe) { Array(breite) { null } }

    override fun darstellen() {
        print("   ");
        for (j in 1..breite) {
            print("$j ")
        }
        println()

        for (i in 0..<hoehe) {
            print("${i + 1} |")
            for (j in 0..<breite) {
                print(matrix[i][j] ?: " ");
                print("|");
            }
            println();
        }
    }
}
