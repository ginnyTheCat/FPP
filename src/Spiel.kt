abstract class Spiel(
    val spieler: Array<Spieler>,
    open val feld: Spielfeld,
) {
    abstract fun spielzug(): Ausgang?
    abstract fun durchgang(): Ausgang?
}