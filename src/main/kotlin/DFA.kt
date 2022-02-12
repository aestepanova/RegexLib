open class Automata

@Suppress("UNCHECKED_CAST")
class DFA(private val nameDigit: Int = 0) : Automata() {
    var nodeName = "DNode $nameDigit"
    private var nodes = mutableListOf<NFA>()
    private var dNodes = mutableListOf<DFA>()
    private val transitions = mutableListOf<Char>()
    private var dfaChildren = mutableListOf<DFA>()
    var start = false
    var end = false
    var error = false

    constructor(nd: MutableList<NFA>, i: Int = 0, dfa: MutableList<DFA>? = null) : this() {
        nodes = nd
        nodeName = "DNode $i"
        if (dfa != null) dfaChildren = dfa
    }

    constructor(nds: MutableList<DFA>) : this() {
        dNodes = nds
    }


    fun createDFA(start_: Node, language: MutableSet<Char>, nameDigit_: Int): MutableList<DFA> {
        var start = start_
        var dName = nameDigit_

        var currentStart = start.startNode
        while (!currentStart.start) {
            start = (start as Group).child!!
            currentStart = start.startNode
        }
        val dfaNodes = mutableListOf<DFA>()
        val digits = mutableListOf<Int>()
        digits.add(dName)

        val dfaStartNode = DFA(epsilonTrans(currentStart), digits[dName])
        dName += 1
        digits.add(dName)
        dfaStartNode.start = true

        for (each in dfaStartNode.nodes) if (each.end) dfaStartNode.end = true
        dfaNodes.add(dfaStartNode)
        var k = 0
        var nodes = mutableListOf<NFA>()
        while (k < dfaNodes.size) {
            for (ch in language) {
                for (nNode in ((dfaNodes[k] as DFA).nodes)) {
                    for ((j, t) in nNode.transitions.withIndex()) {
                        if (t == ch) nodes.add(nNode.nfaChildren[j])
                        // добавили того ребенка, в которого попали по переходу по ch
                    }
                }
                val newNodes = mutableListOf<NFA>()
                for (each in nodes) {
                    val tmp = epsilonTrans(each)
                    for (tmp2 in tmp) {
                        if (tmp2 !in newNodes) newNodes.add(tmp2)
                    }
                }
                nodes = mutableListOf()
                val dNodesflag = inDNodes(newNodes, dfaNodes)
                if (dNodesflag.first) {
                    (dfaNodes[k] as DFA).transitions.add(ch)
                    (dfaNodes[k] as DFA).dfaChildren.add(dfaNodes[dNodesflag.second] as DFA)
                    continue
                }

                val newDFAnode = DFA(newNodes, digits[dName])
                if (newDFAnode.nodes.size == 0) newDFAnode.error = true
                else {
                    dName += 1
                    digits.add(dName)
                    for (each in newDFAnode.nodes) if (each.end) newDFAnode.end = true
                    (dfaNodes[k] as DFA).dfaChildren.add(newDFAnode)
                    (dfaNodes[k] as DFA).transitions.add(ch)
                    dfaNodes.add(newDFAnode)
                }
            }
            k++
        }
        return dfaNodes as MutableList<DFA>
    }

    private fun inDNodes(nNodes: MutableList<NFA>, dNodes: MutableList<DFA>): Pair<Boolean, Int> {
        var f = false
        var k = 0
        for ((j, d) in dNodes.withIndex()) {
            if (d.nodes == nNodes) f = true
            if (f) return Pair(true, j)
            f = false
        }
        return Pair(false, -1)
    }

    private fun epsilonTrans(NFAnode: NFA): MutableList<NFA> {
        val nodes = mutableListOf<NFA>()
        nodes.add(NFAnode)
        var i = 0
        while (i < nodes.size) {
            var k = 0
            while (k < nodes[i].nfaChildren.size) {
                val predicate: (NFA) -> Boolean = { it == nodes[i].nfaChildren[k] }
                if (nodes[i].transitions.size != 0) {
                    val tmp = nodes[i].transitions[0]
                    if (tmp == '@') {
                        // add child if it is not in dNodes
                        if (nodes.count(predicate) == 0) nodes.add(nodes[i].nfaChildren[k])
                    }
                } else {
                    if (nodes.count(predicate) == 0) nodes.add(nodes[i].nfaChildren[k])
                }
                k++
            }
            i++
        }

        return nodes
    }

    fun printDFA(dNodes: MutableList<DFA>) {
        for (eachNode in dNodes) {
            println("\nName: ${eachNode.nodeName}\nstart: ${eachNode.start}\nend: ${eachNode.end}")
            println(eachNode.nodes)
            var i = 0
            while (i < eachNode.dfaChildren.size) {
                print("Transition: ${eachNode.transitions[i]} ---> ${eachNode.dfaChildren[i].nodeName}\n")
                i++
            }
        }
    }

    fun minimize(dNodes: MutableList<DFA>, language: MutableSet<Char>): MutableList<DFA> {
        val groups = mutableListOf<MutableList<DFA>>()
        val receiving = mutableListOf<DFA>()
        val nonReceiving = mutableListOf<DFA>()

        if (dNodes.size <= 2) return dNodes

        for (dNode in dNodes) if (dNode.end) receiving.add(dNode) else nonReceiving.add(dNode)
        groups.add(receiving)
        if (nonReceiving.size != 0) groups.add(nonReceiving)

        var flag = true

        while (flag) {
            var i = 0
            flag = false
            while (i < groups.size) {
                val curGroup = groups[i]
                for (ch in language) {
                    val grMap = mutableMapOf<Char, MutableList<MutableList<DFA>>>()
                    val newGroup = mutableListOf<MutableList<DFA>>()
                    for (dfaNode in curGroup) {
                        for ((k, eachTr) in dfaNode.transitions.withIndex()) {
                            if (eachTr == ch) newGroup.add(mutableListOf(dfaNode, dfaNode.dfaChildren[k]))
                        }

                    }
                    grMap[ch] = newGroup
                    val sameGroupNum = sameId(groups, grMap[ch]!![0][1])
                    receiving.clear()
                    nonReceiving.clear()
                    var piFlag = true
                    for (each in grMap[ch]!!) {
                        if (sameId(groups, each[0]) != sameGroupNum) {
                            receiving.add(each[0])
                            flag = true
                            piFlag = false
                        } else nonReceiving.add(each[0])
                    }
                    if (piFlag) continue
                    groups[i] = nonReceiving
                    groups[i+1] = receiving
                    i--
                    break
                }
                i++
            }
        }
        val mDFA = mutableListOf<DFA>()

        for (group in groups) mDFA.add(DFA(group))
        for ((k, group) in groups.withIndex()) {
            for (node in group) {
                if (node.start) mDFA[k].start = true
                if (node.end) mDFA[k].end = true
                for ((i, tr) in node.transitions.withIndex()) {
                    if (inList(mDFA[k].transitions, tr)) continue
                    mDFA[k].transitions.add(tr)
                    mDFA[k].dfaChildren.add(mDFA[sameId(groups, node.dfaChildren[i])])
                }
            }
        }

        return mDFA
    }

    private fun inList(list: MutableList<Char>, tr: Char): Boolean {
        for (each in list) if (each == tr) return true
        return false
    }

    private fun sameId(groups: MutableList<MutableList<DFA>>, dfaNode: DFA): Int {
        var i = 0
        while (i < groups.size) {
            for (each in groups[i]) if (each == dfaNode) return i
            i++
        }
        return -1
    }

}