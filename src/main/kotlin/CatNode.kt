class CatNode(leftSymbols: Node? = null, rightSymbols: Node? = null) : BinaryOperator(leftSymbols, rightSymbols, '.') {
    override fun createNFA(start: Boolean, end: Boolean, nameDigit: MutableList<Int>): NFA {
        startNode = NFA(start, end, nameDigit[0])
        nameDigit[0] = nameDigit[0] + 1
        endNode = NFA(end, start, nameDigit[0])
        nameDigit[0] = nameDigit[0] + 1
        startNode.transitions.add('@')
        startNode.NFAchildren.add(left!!.createNFA(start = false, end = false, nameDigit = nameDigit))
        if ((left !is SimpleGroup) and (right !is SimpleGroup)) {
            left!!.endNode.NFAchildren.add(right!!.createNFA(false, false, nameDigit))
            left!!.endNode.transitions.add('@')
            right!!.endNode.NFAchildren.add(endNode)
            right!!.endNode.transitions.add('@')
        } else if ((left !is SimpleGroup) and (right is SimpleGroup)) {
            left!!.endNode.NFAchildren.add(right!!.createNFA(false, false, nameDigit))
            left!!.endNode.transitions.add('@')
            (right as SimpleGroup).child!!.endNode.NFAchildren.add(endNode)
            (right as SimpleGroup).child!!.endNode.transitions.add('@')
        } else if ((left is SimpleGroup) and (right !is SimpleGroup)) {
            (left as SimpleGroup).child!!.endNode.NFAchildren.add(right!!.createNFA(false, false, nameDigit))
            (left as SimpleGroup).child!!.endNode.transitions.add('@')
            right!!.endNode.NFAchildren.add(endNode)
            right!!.endNode.transitions.add('@')
        } else {
            (left as SimpleGroup).child!!.endNode.NFAchildren.add(right!!.createNFA(false, false, nameDigit))
            (left as SimpleGroup).child!!.endNode.transitions.add('@')
            (right as SimpleGroup).child!!.endNode.NFAchildren.add(endNode)
            (right as SimpleGroup).child!!.endNode.transitions.add('@')
        }
        return startNode
    }
}