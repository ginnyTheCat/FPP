abstract class Spielfeld(
    val hoehe: Int,
    val breite: Int,
) : Protokollierbar {
    abstract fun darstellen()

    private val zuege = ArrayList<String>()
    override fun zugHinzufuegen(spielzug: Int, hoehe: Int, breite: Int) {
        zuege.add("$spielzug: setze ($hoehe, $breite)")
    }

    override fun zugEntfernen(spielzug: Int) {
        zuege.removeAt(spielzug)
    }
}