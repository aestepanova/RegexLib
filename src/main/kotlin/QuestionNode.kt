class QuestionNode(child_: Node?) : UnaryOperator(child_, '?') {
    override fun clone(): QuestionNode {
        return QuestionNode(c, child, startNode, endNode)
    }

    constructor(ch: Char, child_: Node?, start: NFA, end: NFA) : this(child_) {
        c = ch
        startNode = start
        endNode = end
    }

    ///TODO: own NFA for k?
    override fun createNFA(start: Boolean, end: Boolean, nameDigit: MutableList<Int>): NFA {
        startNode = NFA(start, end, nameDigit[0])
        nameDigit[0] = nameDigit[0] + 1
        startNode.transitions.add('@')
        endNode = NFA(end, start, nameDigit[0])
        nameDigit[0] = nameDigit[0] + 1
        startNode.NFAchildren.add(endNode)

        startNode.NFAchildren.add(child!!.createNFA(start = false, end = false, nameDigit = nameDigit))
        nameDigit[0] = nameDigit[0] + 1
        startNode.transitions.add(child!!.c)

        child!!.endNode.NFAchildren.add(endNode)
        child!!.endNode.transitions.add('@')

        return startNode
    }
}