package de.upb.mlseminar.mymcts.tree;


/**
 * This class is used to create a Tree Structure which will is used in,
 * construct a tree of a MCTS approach
 *
 * @author Guru Prasad Savandaiah
 * Reference : https://github.com/eugenp/tutorials/blob/master/algorithms-searching/src/main/java/com/baeldung/algorithms/mcts/tree/Tree.java
 *
 */
public class MCTSTree {
    MCTSNode root;

    public MCTSTree() {
        root = new MCTSNode();
    }

    public MCTSTree(MCTSNode root) {
        this.root = root;
    }

    public MCTSNode getRoot() {
        return root;
    }

    public void setRoot(MCTSNode root) {
        this.root = root;
    }

    public void addChild(MCTSNode parent, MCTSNode child) {
        parent.getChildArray().add(child);
    }
}
