fun main() {
    val vierGewinnt = Futtern(
        arrayOf(
            Spieler("X", SpielerArt.MENSCH),
            Spieler("O", SpielerArt.COMPUTER),
        )
    )
    vierGewinnt.durchgang()
}