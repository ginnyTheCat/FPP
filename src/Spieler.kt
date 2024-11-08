data class Spieler(
    val name: String,
    val art: SpielerArt,
) {
    override fun toString(): String {
        return this.name
    }
}

enum class SpielerArt {
    MENSCH,
    COMPUTER,
}