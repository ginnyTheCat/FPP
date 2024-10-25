interface Protokollierbar {
    fun zugHinzufuegen(spielzug: Int, hoehe: Int, breite: Int)
     abstract fun entferneZug(spielzug: Int);
}