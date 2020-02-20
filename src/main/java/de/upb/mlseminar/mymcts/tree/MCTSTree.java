package de.upb.mlseminar.mymcts.tree;


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
