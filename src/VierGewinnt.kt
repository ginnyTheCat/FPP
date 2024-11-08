class VierGewinnt(
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
            SpielerArt.MENSCH -> {}
            SpielerArt.COMPUTER -> spielzug_computer(amZugSpieler)
        }

        amZugIndex++;
        if (amZugIndex == spieler.size) {
            amZugIndex = 0
        }
    }

    override fun durchgang() {
        for (s in spieler) {
            spielzug()
        }
    }

    fun spielzug_computer(selbst: Spieler) {
        //berechne die zweierketten mit freiem platz daneben


        for (i in 0..<feld.hoehe){
            for (j in 0..<feld.breite) {
                val aktuellesFeld = this.feld.matrix[i][j]

                if (aktuellesFeld != null && aktuellesFeld == selbst){
                    umgebung_checken(i, j, selbst)}
            }
    }}
    //erste koordinate ist zeile, die zweite ist spalte
    private fun umgebung_checken(i : Int, j: Int, computer:Spieler){
        //gibt true oder false zurpück, je nachdem, ob ein match gefunden wurde für einen strategisch gute zug
        val aktuellesFeld = this.feld.matrix[i][j]
        var x_in_reihe=0
      //nach  unten schauen
        if (aktuellesFeld != null) {
            if  ( this.feld.matrix[i+1][j]!= null && this.feld.matrix[i+1][j]!=  computer){
                x_in_reihe+=1
                umgebung_checken(i+1,j,computer)}

        if (x_in_reihe==2){

            if (this.einwerfen(x, computer)== false){

            }
        }

            //liste hier mit tupeln aus i und j füllen, damit man darauf zugreifen kann
        }

        if (aktuellesFeld != null) {
            if  ( this.feld.matrix[i][j+1]!= null && this.feld.matrix[i][j+1]!=  computer){
                x_in_reihe+=1
                umgebung_checken(i, j + 1, computer)}
        }

        fun defaultZug(breite: Int){
            this.einwerfen(breite, )

        }

    }
}



