package de.upb.mlseminar.mymcts.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.upb.mlseminar.utilities.NodeState;

/**
 * This class is used to create a Node Structure which will is used in,
 * construct a Node of a MCTS tree
 *
 * @author Guru Prasad Savandaiah
 * Reference : https://github.com/eugenp/tutorials/blob/master/algorithms-searching/src/main/java/com/baeldung/algorithms/mcts/tree/Node.java
 *
 */
public class MCTSNode {

	NodeState state;
	MCTSNode parent;
    List<MCTSNode> childArray;
    
	public MCTSNode() {
        this.state = new NodeState();
        childArray = new ArrayList<>();
    }

    public MCTSNode(NodeState state) {
        this.state = state;
        childArray = new ArrayList<>();
    }

    public MCTSNode(NodeState state, MCTSNode parent, List<MCTSNode> childArray) {
        this.state = state;
        this.parent = parent;
        this.childArray = childArray;
    }

	public NodeState getState() {
		return state;
	}

	public void setState(NodeState state) {
		this.state = state;
	}

	public MCTSNode getParent() {
		return parent;
	}

	public void setParent(MCTSNode parent) {
		this.parent = parent;
	}

	public List<MCTSNode> getChildArray() {
		return childArray;
	}

	public void setChildArray(List<MCTSNode> childArray) {
		this.childArray = childArray;
	}

    public MCTSNode getRandomChildNode() {
        int noOfPossibleMoves = this.childArray.size();
        int selectRandom = (int) (Math.random() * noOfPossibleMoves);
        return this.childArray.get(selectRandom);
    }

    public MCTSNode getChildWithMaxScore() {
        return Collections.max(this.childArray, Comparator.comparing(c -> {
            return c.getState().getVisitCount();
        }));
    }

	@Override
	public String toString() {
		return "MCTSNode [state=" + state + ", parent=" + parent + ", childArray=" + childArray + "]";
	}
    
    
    
}
