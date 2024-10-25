interface Protokollierbar {
    fun zugHinzufuegen(spielzug: Int, hoehe: Int, breite: Int)
     abstract fun entferne(spielzug: Int);
}