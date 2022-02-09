class DFA(val nodes: MutableList<Node>, val nameDigit: Int = 0) {
    var nodeName = "DNode $nameDigit"
    val transitions = mutableListOf<Char>()
    val DFAchildren = mutableListOf<DFA>()
    var startNode = false
    var endNode = false
    var error = false

    fun createDFA(start_: Node, alphabet: MutableSet<Char>, nameDigit: Int) {
        var start = start_
        var currentStart = start.startNode
        while (currentStart.start == false) {
            start = (start as Group).child!!
            currentStart = start.startNode
        }
        val dNodes = mutableListOf<Node>()

    }

    fun nextNode(ch: Char): DFA? {
        val predicate: (Char) -> Boolean = { it == ch }
        if (transitions.count(predicate) == 0) return null
        var i = 0
        for (cur in transitions) {
            if (cur == ch) return DFAchildren[i]
            i += 1
        }

        return null
    }

    fun epsilonTrans(NFAnode: NFA): MutableList<NFA> {
        val dNodes = mutableListOf<NFA>()
        dNodes.add(NFAnode)
        var i = 0
        while (i < dNodes.size) {
            var k = 0
            while (k < dNodes[i].NFAchildren.size) {
                val predicate: (NFA) -> Boolean = { it == dNodes[i].NFAchildren[k] }
                if (dNodes[i].transitions.size != 0) {
                    val tmp = dNodes[i].transitions[0]
                    if (tmp == '@') {
                        // add child if it is not in dNodes
                        if (dNodes.count(predicate) == 0) dNodes.add(dNodes[i].NFAchildren[k])
                        k++
                    }
                } else {
                    if (dNodes.count(predicate) == 0) dNodes.add(dNodes[i].NFAchildren[k])
                    k++
                }
                i++
            }
        }

        return dNodes
    }

    fun printDFA(dNodes: MutableList<DFA>){
        for (eachNode in dNodes){
            println("Name: ${eachNode.nodeName}\nstart: ${eachNode.startNode}\nend: ${eachNode.endNode}")
            println(eachNode.nodes)
            var i = 0
            while (i < eachNode.DFAchildren.size){
                println("transition: ${eachNode.transitions[i]} child: ${eachNode.DFAchildren[i].nodeName}")
                i++
            }
        }
    }
}


