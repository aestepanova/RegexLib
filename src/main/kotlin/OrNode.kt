class OrNode(leftSymbols: Node? = null, rightSymbols: Node? = null) : BinaryOperator(leftSymbols, rightSymbols, '|') {

    override fun clone(): OrNode {
        return OrNode(c, left, right, startNode, endNode)
    }
    constructor(ch: Char, leftchild_: Node?, rightChild_: Node?, start: NFA, end: NFA): this(leftchild_, rightChild_) {
        c = ch
        startNode = start
        endNode = end
    }

    override fun createNFA(start: Boolean, end: Boolean, nameDigit: MutableList<Int>): NFA {
        startNode = NFA(start, end, nameDigit[0])
        nameDigit[0] = nameDigit[0] + 1
        endNode = NFA(end, start, nameDigit[0])
        nameDigit[0] = nameDigit[0] + 1
        startNode.transitions.add('@')
        nameDigit[0] = nameDigit[0] + 1
        startNode.NFAchildren.add(left!!.createNFA(start = false, end = false, nameDigit = nameDigit))
        startNode.NFAchildren.add(right!!.createNFA(start = false, end = false, nameDigit = nameDigit))

        if ((left !is SimpleGroup) and (right !is SimpleGroup)) {
            left!!.endNode.NFAchildren.add(endNode)
            left!!.endNode.transitions.add('@')
            right!!.endNode.NFAchildren.add(endNode)
            right!!.endNode.transitions.add('@')
        } else if ((left !is SimpleGroup) and (right is SimpleGroup)) {
            left!!.endNode.NFAchildren.add(endNode)
            left!!.endNode.transitions.add('@')
            (right as SimpleGroup).child!!.endNode.NFAchildren.add(endNode)
            (right as SimpleGroup).child!!.endNode.transitions.add('@')
        } else if ((left is SimpleGroup) and (right !is SimpleGroup)) {
            (left as SimpleGroup).child!!.endNode.NFAchildren.add(endNode)
            (left as SimpleGroup).child!!.endNode.transitions.add('@')
            right!!.endNode.NFAchildren.add(endNode)
            right!!.endNode.transitions.add('@')
        } else {
            (left as SimpleGroup).child!!.endNode.NFAchildren.add(endNode)
            (left as SimpleGroup).child!!.endNode.transitions.add('@')
            (right as SimpleGroup).child!!.endNode.NFAchildren.add(endNode)
            (right as SimpleGroup).child!!.endNode.transitions.add('@')
        }
        return startNode
    }
}