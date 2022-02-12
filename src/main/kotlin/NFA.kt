class NFA(var start: Boolean = false, var end: Boolean = false, var nameDigit: Int = 0) : Automata() {
    val nodeName = "Node $nameDigit"
    val transitions = mutableListOf<Char>()
    val nfaChildren = mutableListOf<NFA>()

    override fun toString(): String {
        return "NFA: $nodeName"
    }

}