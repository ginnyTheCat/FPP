abstract class Spiel(
    val spieler: Spieler,
    val feld: Spielfeld,
) {
    abstract fun spielzug()
    abstract fun durchgang()
}