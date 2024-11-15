fun intInput(prompt: String, range: IntRange): Int {
    while (true) {
        print("$prompt: ")
        val n = readln().toIntOrNull()
        if (n == null) {
            println("Bitte gib eine Zahl ein.")
        } else if (n < range.first) {
            println("Zahl zu niedrig.")
        } else if (range.endInclusive < n) {
            println("Zahl zu groß.")
        } else {
            return n
        }
    }
}