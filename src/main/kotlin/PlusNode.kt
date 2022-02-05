class PlusNode(symbols: Node? = null) : UnaryOperator(symbols, '+') {
    override fun createNFA(start: Boolean, end: Boolean, nameDigit: MutableList<Int>): NFA {
        startNode = NFA(start, end, nameDigit[0])
        nameDigit[0] = nameDigit[0] + 1
        endNode = NFA(end, start, nameDigit[0])
        nameDigit[0] = nameDigit[0] + 1
        startNode.transitions.add('@')
        var plusChild = child
        while (plusChild is SimpleGroup) plusChild = plusChild.child
        startNode.NFAchildren.add(plusChild!!.createNFA(start = false, end = false, nameDigit = nameDigit))
        nameDigit[0] = nameDigit[0] + 1
        var childList = startNode.NFAchildren
        while (childList[0].NFAchildren.size != 0) childList = childList[0].NFAchildren
        childList[0].NFAchildren.add(endNode)
        childList[0].transitions.add(plusChild.c)
        childList[0].transitions.add('@')
        return startNode
    }
}