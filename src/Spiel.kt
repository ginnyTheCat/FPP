import androidx.compose.runtime.Composable

abstract class Spiel(
    val spieler: Array<Spieler>,
    open val feld: Spielfeld,
) {
    abstract fun amZug(): Spieler
    abstract fun zug(x: Int, y: Int): Ausgang?

    @Composable
    abstract fun ui(onSet: (x: Int, y: Int) -> Unit)
}