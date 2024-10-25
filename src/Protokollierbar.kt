interface Protokollierbar {
    fun zugHinzufuegen(spielzug: Int, hoehe: Int, breite: Int)
    fun zugEntfernen(spielzug: Int);
}