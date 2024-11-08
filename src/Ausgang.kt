sealed class Ausgang {
    class Gewonnen(val gewinner: Spieler) : Ausgang()
    class Verloren(val verlierer: Spieler) : Ausgang()
    object Unentschieden : Ausgang()

    override fun toString(): String {
        return when (this) {
            is Gewonnen -> "${this.gewinner} hat gewonnen"
            is Verloren -> "${this.verlierer} hat verloren"
            Unentschieden -> "Unentschieden"
        }
    }
}
