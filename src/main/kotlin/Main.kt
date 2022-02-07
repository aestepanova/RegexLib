fun main(args: Array<String>) {
    println("Enter your regular expression: ")
    val regularString = readLine()!!
    val tree = SyntaxTree(regularString)
    tree.printTree(tree.rootNode, 1)
    tree.createNFA()
    tree.printNFA(node = tree.rootNode, tab = 0)
   // tree.createNFAGraph(tree.rootNode)
}
