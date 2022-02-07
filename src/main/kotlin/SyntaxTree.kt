
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.attribute.Style
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.model.Factory
import java.io.File
import guru.nidi.graphviz.model.Node as NodeModel

open class Node() {
    var startNode = NFA()
    var endNode = NFA()
    var c: Char = ' '

    open fun createNFA(
        start: Boolean = true,
        end: Boolean = false,
        nameDigit: MutableList<Int> = mutableListOf(0)
    ): NFA {
        return NFA(start = true, end = false, nameDigit = nameDigit[0])
    }

    open fun clone(): Node {
        return Node(c, startNode, endNode)
    }

    constructor(ch: Char) : this() {
        c = ch
    }

    constructor(ch: Char, startNFA: NFA, endNFA: NFA) : this() {
        c = ch
        startNode = startNFA
        endNode = endNFA
    }
}

open class UnaryOperator(var child: Node?, ch: Char = ' ') : Node(ch) {}

open class BinaryOperator(var left: Node? = null, var right: Node? = null, ch: Char = ' ') : Node(ch) {}

class BracketsPair(val opBr: OpenBracket, var clBr: CloseBracket?) {
    fun setCloseBracket(closeBracket: CloseBracket) {
        clBr = closeBracket
    }
}

class OpenBracket() : Node('(') {}
class CloseBracket() : Node(')') {}

class ANode(ch: Char) : Node(ch) {

    override fun clone(): ANode {
        return ANode(c, startNode, endNode)
    }

    constructor(ch: Char, start: NFA, end: NFA) : this(ch) {
        startNode = start
        endNode = end
    }

    override fun createNFA(start: Boolean, end: Boolean, nameDigit: MutableList<Int>): NFA {
        startNode = NFA(start, end, nameDigit[0])
        nameDigit[0] = nameDigit[0] + 1
        endNode = NFA(end, start, nameDigit[0])
        startNode.transitions.add(c)
        startNode.NFAchildren.add(endNode)
        nameDigit[0] = nameDigit[0] + 1
        return startNode
    }
}

class CaptureGroup(group: Node? = null, var groupNumber: Int = 0) : UnaryOperator(group) {
    override fun clone(): CaptureGroup {
        return CaptureGroup(c, child, startNode, endNode, groupNumber)
    }

    constructor(ch: Char, child_: Node?, start: NFA, end: NFA, groupNumber_: Int) : this(child_) {
        c = ch
        startNode = start
        endNode = end
        groupNumber = groupNumber_
    }
}

class SimpleGroup(group: Node? = null, var groupNumber: Int = 0) : UnaryOperator(group) {
    override fun clone(): SimpleGroup {
        return SimpleGroup(c, child, startNode, endNode, groupNumber)
    }

    constructor(ch: Char, child_: Node?, start: NFA, end: NFA, groupNumber_: Int) : this(child_) {
        c = ch
        startNode = start
        endNode = end
        groupNumber = groupNumber_
    }

    override fun createNFA(start: Boolean, end: Boolean, nameDigit: MutableList<Int>): NFA {
        return child!!.createNFA(start, end, nameDigit)
    }
}

class ENode(ch: Char) : Node(ch) {
    override fun clone(): ENode {
        return ENode(c)
    }

    override fun createNFA(start: Boolean, end: Boolean, nameDigit: MutableList<Int>): NFA {
        return ANode('^').createNFA(start, end, nameDigit)
    }
}

class SyntaxTree(str: String = "") {
    var rootNode: Node = Node()
    private var nodes = mutableListOf<Node>()
    private val groups = mutableListOf<Node>()
    private val alphabet = mutableSetOf<Char>()
    private val nodesNames = mutableSetOf<String>()
    private val regStr = "($str)"
    private var brackets = mutableListOf<BracketsPair>()

    init {
        println(regStr)
        build()
    }

    private fun build() {
        checkCaptures()
        createSyntaxTree()
    }

    private fun nodesScan() {
        var i = 0
        while (i < regStr.length) {
            val ch = regStr[i]
            when (ch) {
                '(' -> nodes.add(OpenBracket())
                ')' -> nodes.add(CloseBracket())
                // escaping a character
                '#' -> {
                    if (i + 1 < regStr.length) {
                        i++
                        alphabet.add(regStr[i])
                        nodes.add(ANode(regStr[i]))
                    } else throw Exception("Syntax error")
                }
                // null substring
                '^' -> nodes.add(ENode('^'))
                // or
                '|' -> {
                    if (i + 1 >= regStr.length) throw Exception("Syntax error")
                    if (regStr[i + 1] != '|') {
                        nodes.add(OrNode())
                    } else throw Exception("Syntax error")
                }
                // concatenation
                '.' -> {
                    if (i + 1 >= regStr.length) throw Exception("Syntax error")
                    if (regStr[i + 1] != '.') nodes.add(CatNode())
                    else throw Exception("Syntax error")
                }
                // positive circuit
                '+' -> nodes.add(PlusNode())

                // repeat expression in diapason {2,} or {2,987}
                '{' -> i = checkRepeatExpression(i)

                // numeric capture group (5:r)
                ':' -> nodes.add(CaptureGroup())
                else -> {
                    nodes.add(ANode(ch))
                    alphabet.add(ch)
                }
            }
            i++
        }
    }

    private fun checkRepeatExpression(k: Int): Int {
        var i = k
        // {2,}
        if ((i + 4 >= regStr.length) or (regStr[i + 1] == '}') or (regStr[k - 1] == '(')) throw Exception("Syntax error")
        var lowNum = 0
        var highNum = 0
        while (regStr[i] != ',') {
            if (regStr[i].isDigit()) {
                if (regStr[i] == '0') throw Exception("Syntax error")
                lowNum *= 10
                lowNum += regStr[i].digitToInt()
                i++
                if (i >= regStr.length) throw Exception("Syntax error")
            } else {
                i++
                if (i >= regStr.length) throw Exception("Syntax error")
            }
        }
        i++
        if (i >= regStr.length) throw Exception("Syntax error")
        // {23,}
        if (regStr[i] == '}') {
            nodes.add(Repeats(lowBorder = lowNum))
        } else {
            // {23,53}
            while (regStr[i] != '}') {
                if (regStr[i].isDigit()) {
                    highNum *= 10
                    highNum += regStr[i].digitToInt()
                    i++
                    if (i >= regStr.length) throw Exception("Syntax error")
                } else throw Exception("Syntax error")
            }
            if (highNum > lowNum) nodes.add(Repeats(lowBorder = lowNum, highBorder = highNum))
            else throw Exception("Syntax error")
        }
        return i
    }

    private fun checkCaptures() {

        val l = regStr.length

        var opBr = 0
        var clBr = 0
        var ind = -1
        while (ind < l) {
            ind = regStr.indexOf('(', ind + 1)
            if (ind != -1) {
                opBr += 1
            } else break
        }
        ind = 0
        while (ind < l) {
            ind = regStr.indexOf(')', ind + 1)
            if (ind != -1) {
                clBr += 1
            } else break
        }

        if (opBr != clBr) throw Exception("Syntax error")
    }

    private fun createSyntaxTree() {
        val captureGroups = mutableListOf<CaptureGroup>()
        nodesScan()
        val size = nodes.size
        var groupsNum = 0
        var i = 0

        // forming brackets groups
        while (i < size) {
            val node = nodes[i]
            if (node is OpenBracket) brackets.add(BracketsPair(node, null))
            else if (node is CloseBracket) {
                var j = brackets.size - 1
                while (j >= 0) {
                    if (brackets[j].clBr == null) {
                        brackets[j].setCloseBracket(node)
                        break
                    }
                    j--
                }
            }
            i++
        }

        while (nodes.size != 1) {
            var f = false
            i = brackets.size - 1

            val start = nodes.indexOf(brackets[i].opBr)
            var end = nodes.getItemPositionByName(brackets[i].clBr)
            var j = start + 1

            //numeric (76:r)
            while (j < end) {
                if (nodes[j] is CaptureGroup) {
                    if ((nodes[j + 1] is CloseBracket) or (nodes[j + 1] is CaptureGroup)) throw Exception("Wrong syntax!")
                    val k = start + 1
                    var capNumber = 0
                    //var i = k
                    for (i in k until j) {
                        if (nodes[i].c.isDigit()) {
                            capNumber *= 10
                            capNumber += nodes[i].c.digitToInt()
                        } else throw Exception("Wrong syntax for capture group!")
                    }
                    (nodes[j] as CaptureGroup).groupNumber = capNumber
                    for (i in k until j) {
                        nodes.removeAt(k)
                        end--
                    }
                    f = true
                    captureGroups.add(nodes[k] as CaptureGroup)
                    nodes.removeAt(k)
                }
                j++
            }
            j = start + 1
            //repeats
            while (j < end) {
                if (nodes[j] is Repeats) {
                    // (gr){1,} = (gr)+
                    if (((nodes[j] as Repeats).lowBorder == 1) and ((nodes[j] as Repeats).highBorder == -1)) {
                        nodes[j] = PlusNode(nodes[j - 1])
                        nodes.removeAt(j - 1)
                    } else if ((nodes[j] as Repeats).highBorder == -1) {
                        // (gr){2,} = (gr)(gr)+     (gr){4,} = (gr)(gr)(gr)(gr)+
                        val tmpListOfFirst = nodes.subList(0, j - 1).toMutableList()
                        val tmpListOfLast = nodes.subList(j + 1, nodes.size).toMutableList()

                        val low = (nodes[j] as Repeats).lowBorder
                        val childNode = nodes[j - 1]

                        var i = 0
                        while (i < low - 1) {
                            tmpListOfFirst.add(childNode.clone())
                            end++
                            i++
                        }
                        tmpListOfFirst.add(PlusNode(childNode.clone()))
                        for (each in tmpListOfLast) tmpListOfFirst.add(each)
                        nodes = tmpListOfFirst
                    } else { // (gr){2,4} = (gr)(gr)(gr)?(gr)?

                        val tmpListOfFirst = nodes.subList(0, j - 1).toMutableList()
                        val tmpListOfLast = nodes.subList(j + 1, nodes.size).toMutableList()

                        val low = (nodes[j] as Repeats).lowBorder
                        val high = (nodes[j] as Repeats).highBorder
                        val childNode = nodes[j - 1]

                        var i = 0
                        while (i < low) {
                            tmpListOfFirst.add(childNode.clone())
                            end++
                            i++
                        }
                        for (i in low until high) {
                            tmpListOfFirst.add(QuestionNode(childNode.clone()))
                            end++
                        }
                        end--
                        for (each in tmpListOfLast) tmpListOfFirst.add(each)
                        nodes = tmpListOfFirst
                    }

                    end--
                }
                j++
            }

            j = start + 1
            //+
            while (j < end) {
                if ((nodes[j] is PlusNode) and (nodes[j - 1] !is OpenBracket)) {
                    if ((nodes[j] as PlusNode).child == null) {
                        (nodes[j] as PlusNode).child = nodes[j - 1]
                        nodes.remove(nodes[j - 1])
                        end--
                    }
                }
                j++
            }

            j = start + 1
            // concatenate a.b
            while (j < end) {
                if (nodes[j] is CatNode) {
                    if ((nodes[j] as CatNode).left != null) break
                    (nodes[j] as CatNode).left = nodes[j - 1]
                    (nodes[j] as CatNode).right = nodes[j + 1]
                    nodes.remove(nodes[j - 1])
                    nodes.remove(nodes[j])
                    end -= 2
                    j--
                }
                j++
            }

            j = start + 1
            // concatenate ab
            while (j < end) {
                if ((nodes[j] !is OrNode) and (nodes[j + 1] !is OrNode) and (nodes[j] !is CloseBracket) and (nodes[j + 1] !is CloseBracket) and (nodes[j] !is CaptureGroup)) {
                    nodes[j] = CatNode(nodes[j], nodes[j + 1])
                    nodes.remove(nodes[j + 1])
                    end--
                    j--
                }
                j++
            }

            // or
            j = start + 1
            while (j < end) {
                if (nodes[j] is OrNode) {
                    (nodes[j] as OrNode).left = nodes[j - 1]
                    (nodes[j] as OrNode).right = nodes[j + 1]
                    nodes.remove(nodes[j - 1])
                    nodes.remove(nodes[j])
                    end -= 2
                    j--
                }
                j++
            }


            val gr = if (f) {
                f = false
                val i = captureGroups.size - 1
                SimpleGroup(nodes[start + 1], captureGroups[i].groupNumber)
            } else SimpleGroup(nodes[start + 1], groupsNum++)
            groups.add(gr)
            nodes[start] = gr
            nodes.removeAt(start + 1) //node
            nodes.removeAt(start + 1) //)
            brackets.removeLast()


        }
        rootNode = nodes[0]
    }

    private fun printUnary(nd: Node?, tab: Int): Node? {
        val space = " ".repeat(tab)
        when (nd) {
            is PlusNode -> {
                println("$space+")
                return nd.child
            }
            is Repeats -> {
                if (nd.highBorder > 0) println("$space{${nd.lowBorder},${nd.highBorder}}")
                else println("$space{${nd.lowBorder},}")
                return nd.child
            }
            is ANode -> {
                println("$space${nd.c}")
                return null
            }
            is ENode -> {
                println("$space${nd.c}")
                return null
            }
            is SimpleGroup -> {
                println("$space(group ${nd.groupNumber})")
                return nd.child
            }
            is QuestionNode -> {
                println("$space(${nd.c})")
                return nd.child
            }
            else -> return null
        }
    }

    private fun printBinary(nd: Node?, tab: Int): Pair<Node?, Node?> {
        val space = " ".repeat(tab)
        return when (nd) {
            is OrNode -> {
                println("$space|")
                Pair(nd.left, nd.right)
            }
            is CatNode -> {
                println("$space.")
                Pair(nd.left, nd.right)
            }
            else -> Pair(null, null)
        }
    }

    fun printTree(root: Node?, space: Int) {

        val tab = space + 1
        var unaryNd: Node? = null
        var binaryNd: Pair<Node?, Node?> = Pair(null, null)
        if ((root !is OrNode) and (root !is CatNode)) unaryNd = printUnary(root, tab)
        else binaryNd = printBinary(root, tab)

        unaryNd?.let { printTree(it, tab) }
        if (binaryNd.first != null) {
            printTree(binaryNd.first, tab)
            printTree(binaryNd.second, tab + 2)
        }
    }

    fun createNFA(): SyntaxTree {
        val l = mutableListOf(0)
        rootNode.createNFA(start = true, end = false, nameDigit = l)
        return this
    }

    fun printNFA(node: Node? = null, nodeNFA: NFA? = null, tab: Int, ndGraph: NodeModel = Factory.node("0")) {
        val newTab = tab + 5
        val space = " ".repeat(newTab)
        var flag = false
        if (node != null) if (node is SimpleGroup) {
            printNFA(node = node.child, tab = newTab)
            return
        }
        if (nodeNFA != null) {
            var i = 0
            println(space + nodeNFA.nodeName)
            print(space + "Transitions: ")
            nodeNFA.transitions.forEach { print("$it ") }
            println()
            println("${space}start: ${nodeNFA.start}, end: ${nodeNFA.end}")
            for (each in nodesNames) if (each == nodeNFA.nodeName) flag = true
            nodesNames.add(nodeNFA.nodeName)
            while (i < nodeNFA.NFAchildren.size) {
                if (!flag) {
                    printNFA(nodeNFA = nodeNFA.NFAchildren[i], tab = newTab)
                    i++
                } else i++
            }
        } else printNFA(nodeNFA = node!!.startNode, tab = newTab)

    }

    fun createNFAGraph(root: Node?) {

        val main: NodeModel = Factory.node(this.rootNode.startNode.nodeName)
        val execute: NodeModel = Factory.node("gowentgo")

        val compare: NodeModel = Factory.node("compare")
        val mkString: NodeModel = Factory.node("mkString")
        val da: NodeModel = Factory.node("da")

        val g = Factory.graph("test").directed().with(
            main.link(
                Factory.to(Factory.node("parse").link(execute)),
                Factory.node("cleanup"),
                Factory.to(da).with(
                    Style.BOLD, Label.of("100 times")
                ),
                Factory.to(main).with(Label.of("dark"))
            ),
            execute.link(
                Factory.graph().with(mkString, da),
                Factory.to(compare).with(Label.of("k"))
            ),
            compare.link(main).with(Label.of("ef"))
        )

        Graphviz.fromGraph(g).width(900).render(Format.PNG).toFile(File("example/ex1.png"))
    }

}

fun <T> List<T>.getItemPositionByName(item: T): Int {
    this.forEachIndexed { index, it ->
        if (it == item)
            return index
    }
    return 0
}