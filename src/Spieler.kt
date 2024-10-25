data class Spieler(
    val name: String,
    val art: SpielerArt,
)

enum class SpielerArt {
    MENSCH,
    COMPUTER,
}