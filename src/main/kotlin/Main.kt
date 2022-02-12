fun main(args: Array<String>) {
    println("Enter your regular expression: ")
    val regularString = readLine()!!
    println("------tree---------")
    val tree = SyntaxTree(regularString)
    tree.printTree(tree.nodes[0], 1)
    println("\n------NFA----------")
    tree.createNFA()
    tree.printNFA(node = tree.nodes[0], tab = 0)
    println("\n-------DFA---------")
   // tree.createNFAGraph(tree.rootNode)
    val dfa = DFA().createDFA(tree.nodes[0], tree.language, 0)
    DFA().printDFA(dfa)
    DFA().minimize(dfa, tree.language)

//    val a = MyRegex("ok")
//    a.makeTree()
//    a.makeNFA()
//    a.printNFA()
//    a.makeDFA()
//    a.printDFA()
//
//    a.makeMDFA()


}
