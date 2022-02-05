class NFA(var start: Boolean = false, var end: Boolean = false, var nameDigit: Int = 0){
    val nodeName = "Node $nameDigit"
    val transitions = mutableListOf<Char>()
    val NFAchildren = mutableListOf<NFA>()
}