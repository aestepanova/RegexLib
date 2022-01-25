open class Node() {

    var c: Char = '^'

    constructor(ch: Char) : this() {
        c = ch
    }

    open fun print() = print("Symbols is ${this.c}")
}

open class UnaryOperator(unaryChild: Node?) : Node() {

    var child = unaryChild
    open fun setUnaryChild(child: Node) {
        this.child = child
    }
}

open class BinaryOperator(leftChild: Node? = null, rightChild: Node? = null) : Node() {

    var left = leftChild
    var right = rightChild

    open fun setLeftChild(leftChild: Node) {
        left = leftChild
    }

    open fun setRightChild(rightChild: Node) {
        right = rightChild
    }
}

class ANode(ch: Char) : Node(ch) {}

class BracketsPair(val opBr: OpenBracket, var clBr: CloseBracket?) {

    open fun setCloseBracket(closeBracket: CloseBracket) {
        clBr = closeBracket
    }

}

class OpenBracket() : Node() {}
class CloseBracket() : Node() {}

class CatNode(leftSymbols: Node? = null, rightSymbols: Node? = null) : BinaryOperator(leftSymbols, rightSymbols) {}

class OrNode(leftSymbols: Node? = null, rightSymbols: Node? = null) : BinaryOperator(leftSymbols, rightSymbols) {}

class PlusNode(symbols: Node? = null) : UnaryOperator(symbols) {}

class Repeats(symbols: Node? = null, low: Int, high: Int = -1) : UnaryOperator(symbols) {
    val lowBorder = low
    val highBorder = high
}

class CaptureGroup(group: Node? = null, num: Int = 0) : UnaryOperator(group) {
    var groupNumber = num

    fun setCaptureGroupNumber(number: Int) {
        groupNumber = number
    }
}

class SimpleGroup(group: Node? = null, num: Int = 0) : UnaryOperator(group) {
    var groupNumber = num

    fun setSimpleGroupNumber(number: Int) {
        groupNumber = number
    }
}

class ENode() : Node() {}

class SyntaxTree(str: String = "") {
    var rootNode: Node = Node()
    private val Nodes = mutableListOf<Node>()
    private val groups = mutableListOf<Node>()
//    private val aNodes = mutableListOf<ANode>()
//
//    //private val starNodes = mutableListOf<StarNode>()
//    private val catNodes = mutableListOf<CatNode>()
//    private val orNodes = mutableListOf<OrNode>()

    private val alphabet = mutableSetOf<Char>()

    private val regStr = "($str)"
    private var brackets = mutableListOf<BracketsPair>()
    //private var closingBrackets = mutableListOf<CloseBracket>()

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
            when {
                ch == '(' -> Nodes.add(OpenBracket())
                ch == ')' -> Nodes.add(CloseBracket())
                // escaping a character
                ch == '#' -> {
                    if (i + 1 < regStr.length) {
                        i++
                        alphabet.add(regStr[i])
                        Nodes.add(ANode(regStr[i]))
                    } else throw Exception("Syntax error")
                }

                // null substring
                ch == '^' -> Nodes.add(ENode())

                // or
                ch == '|' -> {
                    if (i + 1 >= regStr.length) throw Exception("Syntax error")
                    if (regStr[i + 1] != '|') {
                        Nodes.add(OrNode())
                    } else throw Exception("Syntax error")
                }

                // concatenation
                ch == '.' -> {
                    if (i + 1 >= regStr.length) throw Exception("Syntax error")
                    if (regStr[i + 1] != '.') Nodes.add(CatNode())
                    else throw Exception("Syntax error")
                }

                // positive circuit
                ch == '+' -> Nodes.add(PlusNode())

                // repeat expression in diapason {2,} or {2,987}
                ch == '{' -> i = checkRepeatExpression(i)

                // numeric capture group (5:r)
                // smth6:b
//                ch.isDigit() -> {
//                    var capNumber = 0
//                    while (regStr[i] != ':') {
//                        if (regStr[i].isDigit()) {
//                            capNumber *= 10
//                            capNumber += regStr[i].digitToInt()
//                            i++
//                            if (i >= regStr.length) throw Exception("Syntax error")
//                        } else {
//                            i++
//                            if (i >= regStr.length) throw Exception("Syntax error")
//                        }
//                    }
//                    i++
//                }
                ch == ':' -> Nodes.add(CaptureGroup())
                else -> {
                    Nodes.add(ANode(ch))
                    alphabet.add(ch)
                }
            }
            i++
        }
    }

    private fun checkRepeatExpression(k: Int): Int {
        var i = k
        if ((i + 4 >= regStr.length) or (regStr[i + 1] == '}')) throw Exception("Syntax error")
        var lowNum = 0
        var highNum = 0
        while (regStr[i] != ',') {
            if (regStr[i].isDigit()) {
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
            Nodes.add(Repeats(low = lowNum))
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
            if (highNum > lowNum) Nodes.add(Repeats(low = lowNum, high = highNum))
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
                //openBrackets.add(ind)
            } else break
        }
        ind = 0
        while (ind < l) {
            ind = regStr.indexOf(')', ind + 1)
            if (ind != -1) {
                clBr += 1
                //closingBrackets.add(ind)
            } else break
        }

        if (opBr != clBr) throw Exception("Syntax error")
    }

    private fun createSyntaxTree() {
        nodesScan()
        val size = Nodes.size
        var groupsNum = 0
        var i = 0

        // forming brackets groups
        while (i < size) {
            val node = Nodes[i]
            if (node is OpenBracket) brackets.add(BracketsPair(node, null))
            else if (node is CloseBracket) {
                var j = size - i
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

        while (Nodes.size != 1) {
            var f = false
            i = brackets.size - 1

            var start = Nodes.indexOf(brackets[i].opBr)
            var end = Nodes.getItemPositionByName(brackets[i].clBr)

            if (Nodes[start + 1] is ANode) {
                groups.add(SimpleGroup(num = groupsNum))
                groupsNum++
                f = true
            }

            var j = start + 1

            //repeats
            while (j < end) {
                if (Nodes[j] is Repeats) {
                    (Nodes[j] as Repeats).child = Nodes[j - 1]
                    Nodes.remove(Nodes[j - 1])
                    end--
                }
                j++
            }

            j = start + 1
            //+
            while (j < end) {
                if (Nodes[j] is PlusNode) {
                    (Nodes[j] as PlusNode).child = Nodes[j - 1]
                    Nodes.remove(Nodes[j - 1])
                    end--
                }
                j++
            }

            j = start + 1
            // concatenate a.b
            while (j < end) {
                if (Nodes[j] is CatNode) {
                    (Nodes[j] as CatNode).left = Nodes[j - 1]
                    (Nodes[j] as CatNode).right = Nodes[j + 1]
                    Nodes.remove(Nodes[j - 1])
                    Nodes.remove(Nodes[j + 1])
                    end -= 2
                    j--
                }
                j++
            }

            j = start + 1
            // concatenate ab
            while (j < end) {
                if ((Nodes[j] !is OrNode) and (Nodes[j + 1] !is OrNode) and (Nodes[j + 1] !is CloseBracket)) {
                    (Nodes[j] as CatNode).left = Nodes[j]
                    (Nodes[j] as CatNode).right = Nodes[j + 1]
                    Nodes.remove(Nodes[j + 1])
                    end--
                    j--
                }
                j++
            }

            // or
            j = start + 1
            while (j < end) {
                if (Nodes[j] is OrNode) {
                    (Nodes[j] as OrNode).left = Nodes[j]
                    (Nodes[j] as OrNode).right = Nodes[j + 1]
                    Nodes.remove(Nodes[j - 1])
                    Nodes.remove(Nodes[j + 1])
                    end -= 2
                    j--
                }
                j++
            }
        }
    }
}

fun <T> List<T>.getItemPositionByName(item: T): Int {
    this.forEachIndexed { index, it ->
        if (it == item)
            return index
    }
    return 0
}