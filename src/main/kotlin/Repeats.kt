class Repeats(symbols: Node? = null, val lowBorder: Int, val highBorder: Int = -1) : UnaryOperator(symbols, 'R') {
    override fun createNFA(start: Boolean, end: Boolean, nameDigit: MutableList<Int>): NFA {
        startNode = NFA(start, end, nameDigit[0])
        startNode.transitions.add('@')
        nameDigit[0] = nameDigit[0] + 1
        endNode = NFA(end, start, nameDigit[0])
        nameDigit[0] = nameDigit[0] + 1
        var i = 0
        var repeatsChild = child
        while (repeatsChild is SimpleGroup) repeatsChild = repeatsChild.child
        var flag = false
        var lastEndNode = endNode
        var newNFA: NFA
        var childList = startNode.nfaChildren
        if ((lowBorder == 1) and (highBorder == -1)) {
            startNode.nfaChildren.add(PlusNode(repeatsChild).createNFA(start = false, end = false, nameDigit))
            nameDigit[0] = nameDigit[0] + 1
            while (childList[0].nfaChildren.size != 0) childList = childList[0].nfaChildren
            childList[0].nfaChildren.add(endNode)
            childList[0].transitions.add('@')

            return startNode
        }
//        {4,}
//        while (i < lowBorder - 1) { // cat until highBorder
//            if (!flag) {
//                newNFA = repeatsChild!!.createNFA(start = false, end = false, nameDigit = nameDigit)
//                nameDigit[0] = nameDigit[0] + 1
//                startNode.NFAchildren.add(newNFA)
////                while (childList[0].NFAchildren.size != 0) childList = childList[0].NFAchildren
//                repeatsChild.endNode.NFAchildren.add(endNode)
//                repeatsChild.endNode.transitions.add('@')
////                childList[0].NFAchildren.add(endNode)
////                childList[0].transitions.add('@')
//                //lastEndNode = childList[0]
//                flag = true
//            } else {
//                //lastEndNode.NFAchildren.remove(endNode)
//                repeatsChild!!.endNode.NFAchildren.remove(endNode)
//                newNFA = repeatsChild!!.createNFA(start = false, end = false, nameDigit = nameDigit)
//               // lastEndNode.NFAchildren.add(newNFA)
//                nameDigit[0] = nameDigit[0] + 1
//                repeatsChild.endNode.NFAchildren.add(newNFA)
//                //while (childList[0].NFAchildren.size != 0) childList = childList[0].NFAchildren
//                repeatsChild.endNode.NFAchildren.add(endNode)
//                repeatsChild.endNode.transitions.add('@')
////                childList[0].NFAchildren.add(endNode)
////                childList[0].transitions.add('@')
////                lastEndNode = childList[0]
//            }
//            i++
//        }
//        if (highBorder == -1) { //Clini
//            lastEndNode.NFAchildren.remove(endNode)
//            newNFA = repeatsChild!!.createNFA(start = false, end = false, nameDigit = nameDigit)
//            lastEndNode.NFAchildren.add(newNFA)
//            nameDigit[0] = nameDigit[0] + 1
//            while (childList[0].NFAchildren.size != 0) childList = childList[0].NFAchildren
//            childList[0].NFAchildren.add(endNode)
//            childList[0].NFAchildren.add(repeatsChild.startNode)
//            childList[0].transitions.add('@')
//        } else {
//            while (i < highBorder) {
//                lastEndNode.NFAchildren.remove(endNode)
//                newNFA = repeatsChild!!.createNFA(start = false, end = false, nameDigit = nameDigit)
//                nameDigit[0] = nameDigit[0] + 1
//                if (endNode !in newNFA.NFAchildren) newNFA.NFAchildren.add(endNode)
//                while (childList[0].NFAchildren.size != 0) childList = childList[0].NFAchildren
//                childList[0].NFAchildren.add(lastEndNode)
//                childList[0].NFAchildren.add(repeatsChild.startNode)
//                childList[0].transitions.add('@')
//                lastEndNode.NFAchildren.add(newNFA)
//                repeatsChild.endNode.NFAchildren.add(endNode)
//                repeatsChild.endNode.transitions.add('@')
//                lastEndNode = newNFA.NFAchildren.last()
//                i++
//            }
//        }
//        childList[0].NFAchildren.removeLast()
//
//        return startNode
//    }
//}
//{4,}
        while (i < lowBorder - 1) { // cat until highBorder
            if (!flag) {
                newNFA = repeatsChild!!.createNFA(start = false, end = false, nameDigit = nameDigit)
                nameDigit[0] = nameDigit[0] + 1
                startNode.nfaChildren.add(newNFA)
                while (childList[0].nfaChildren.size != 0) childList = childList[0].nfaChildren
                childList[0].nfaChildren.add(endNode)
                childList[0].transitions.add('@')
                lastEndNode = childList[0]
                flag = true
            } else {
                lastEndNode.nfaChildren.remove(endNode)
                newNFA = repeatsChild!!.createNFA(start = false, end = false, nameDigit = nameDigit)
                lastEndNode.nfaChildren.add(newNFA)
                nameDigit[0] = nameDigit[0] + 1
                while (childList[0].nfaChildren.size != 0) childList = childList[0].nfaChildren
                childList[0].nfaChildren.add(endNode)
                childList[0].transitions.add('@')
                lastEndNode = childList[0]
            }
            i++
        }
        if (highBorder == -1) { //Clini
            lastEndNode.nfaChildren.remove(endNode)
            newNFA = repeatsChild!!.createNFA(start = false, end = false, nameDigit = nameDigit)
            lastEndNode.nfaChildren.add(newNFA)
            nameDigit[0] = nameDigit[0] + 1
            while (childList[0].nfaChildren.size != 0) childList = childList[0].nfaChildren
            childList[0].nfaChildren.add(endNode)
            childList[0].nfaChildren.add(repeatsChild.startNode)
            childList[0].transitions.add('@')
        } else {
            while (i < highBorder) {
                lastEndNode.nfaChildren.remove(endNode)
                newNFA = repeatsChild!!.createNFA(start = false, end = false, nameDigit = nameDigit)
                nameDigit[0] = nameDigit[0] + 1
                if (endNode !in newNFA.nfaChildren) newNFA.nfaChildren.add(endNode)
                newNFA.transitions.add('@')
                lastEndNode.nfaChildren.add(newNFA)
                repeatsChild.endNode.nfaChildren.add(endNode)
                repeatsChild.endNode.transitions.add('@')
                lastEndNode = newNFA.nfaChildren.last()
                i++
            }
        }
        childList[0].nfaChildren.removeLast()

        return startNode
    }
}