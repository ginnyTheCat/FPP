data class Spieler(
    val name: String,
    val art: SpielerArt,
)

enum class SpielerArt {
    MENSCH,
    COMPUTER,
}

abstract class Spielfeld(
    val höhe: Int,
    val breite: Int,
) {
    abstract fun darstellen(): String
}

abstract class Spiel(
    val spieler: Spieler,
    val feld: Spielfeld,
) {
    abstract fun spielzug()
    abstract fun durchgang()
}