class MatrixSpielfeld(hoehe: Int, breite: Int) : Spielfeld(hoehe, breite) {
    val matrix: Array<Array<Spieler?>> = Array(hoehe) { Array(breite) { null } }

    override fun darstellen() {
        print("-");
        for (j in 0..<breite) {
            print("--");
        }
        println();

        for (i in 0..<hoehe) {
            print("|")
            for (j in 0..<breite) {
                if (matrix[i][j] != null) {
                    print(matrix[i][j]);
                } else {
                    print(" ");
                }
                print("|");
            }
            println();

            print("-");
            for (j in 0..<breite) {
                print("--");
            }
            println();
        }
    }
}
