class MyRegex (str: String){
    var tree = SyntaxTree()
    var automataDFA = mutableListOf<DFA>()
    var automataNFA = NFA()
    var string = str

    constructor(dfa: MutableList<DFA>) : this("") {
        automataDFA = dfa
    }
    fun makeTree(){
        tree = SyntaxTree(string)
        tree.printTree(tree.rootNode, 4)
    }
    fun makeNFA(): NFA{
        tree.createNFA()
        automataNFA = tree.rootNode.startNode
        return automataNFA
    }

    fun printNFA(){
        tree.printNFA(node = tree.nodes[0], tab = 0)
    }

    fun makeDFA(){
        automataDFA = DFA().createDFA(tree.nodes[0], tree.language, 0)
    }
    fun printDFA() = DFA().printDFA(automataDFA)

    fun makeMDFA(){
        val mdfa = DFA().minimize(automataDFA, tree.language)
        DFA().printDFA(mdfa)
    }

}