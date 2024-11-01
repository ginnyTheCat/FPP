abstract class Spiel(
    val spieler: Array<Spieler>,
    open val feld: Spielfeld,
) {
    abstract fun spielzug()
    abstract fun durchgang()
}