class QuestionNode(child_: Node? = null) : UnaryOperator(child_, '?') {
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

        val emptyNode = NFA(false, false, nameDigit[0])
        nameDigit[0] = nameDigit[0] + 1
        emptyNode.nfaChildren.add(endNode)
        emptyNode.transitions.add('@')
        startNode.nfaChildren.add(emptyNode)

        emptyNode.nfaChildren.add(child!!.createNFA(start = true, end = false, nameDigit = nameDigit))
        nameDigit[0] = nameDigit[0] + 1

        child!!.endNode.nfaChildren.add(endNode)
        child!!.endNode.transitions.add('@')

        return startNode
    }
}