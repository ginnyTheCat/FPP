fun main() {
    val vierGewinnt = VierGewinnt(
        arrayOf(
            Spieler("X", SpielerArt.MENSCH),
            Spieler("O", SpielerArt.MENSCH),
        )
    )
    while (true) {
        vierGewinnt.durchgang()
    }
}