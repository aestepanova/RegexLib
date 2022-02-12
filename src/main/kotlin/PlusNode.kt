class PlusNode(symbols: Node? = null) : UnaryOperator(symbols, '+') {
    override fun clone(): PlusNode {
        return PlusNode(c, child, startNode, endNode)
    }

    constructor(ch: Char, child_: Node?, start: NFA, end: NFA) : this(child_) {
        c = ch
        startNode = start
        endNode = end
    }

    override fun createNFA(start: Boolean, end: Boolean, nameDigit: MutableList<Int>): NFA {
        startNode = NFA(start, end, nameDigit[0])
        nameDigit[0] = nameDigit[0] + 1
        startNode.transitions.add('@')
        endNode = NFA(end, start, nameDigit[0])
        nameDigit[0] = nameDigit[0] + 1

        var plusChild = child
        while (plusChild is SimpleGroup) plusChild = plusChild.child
        startNode.nfaChildren.add(plusChild!!.createNFA(start = false, end = false, nameDigit = nameDigit))
        nameDigit[0] = nameDigit[0] + 1

        plusChild.endNode.nfaChildren.add(endNode)
        plusChild.endNode.transitions.add('@')
        plusChild.endNode.nfaChildren.add(plusChild.startNode)

        return startNode
    }
}