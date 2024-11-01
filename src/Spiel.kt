abstract class Spiel(
    val spieler: Spieler,
    open val feld: Spielfeld,
) {
    abstract fun spielzug()
    abstract fun durchgang()
}