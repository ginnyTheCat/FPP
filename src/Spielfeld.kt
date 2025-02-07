import androidx.compose.runtime.Composable

abstract class Spielfeld(
    val breite: Int,
    val hoehe: Int,
    private val onSet: (x: Int, y: Int, spieler: Spieler) -> Unit
) {
    open fun set(x: Int, y: Int, spieler: Spieler) {
        this.onSet(x, y, spieler)
    }

    abstract fun get(x: Int, y: Int): Spieler?

    fun reihen() = (0..<hoehe).map { y -> (0..<breite).map { x -> this.get(x, y) } }
    fun spalten() = (0..<breite).map { x -> (0..<hoehe).map { y -> this.get(x, y) } }

    fun entries() = (0..<breite).flatMap { x -> (0..<hoehe).map { y -> this.get(x, y) } }

    @Composable
    abstract fun ui(onSet: (x: Int, y: Int) -> Unit)
}