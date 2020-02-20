package de.upb.mlseminar.informedplayer;

public class NodeState {

	private IntermediateGameState gameState;
    private int visitCount;
    private double winScore;
    
	public NodeState(IntermediateGameState gameState) {
		super();
		this.gameState = gameState;
	}
	
	public NodeState() {
		super();
	}

	public NodeState(IntermediateGameState gameState, int visitCount, double winScore) {
		super();
		this.gameState = gameState;
		this.visitCount = visitCount;
		this.winScore = winScore;
	}
    
	public IntermediateGameState getGameState() {
		return gameState;
	}


	public void setGameState(IntermediateGameState gameState) {
		this.gameState = gameState;
	}


	public int getVisitCount() {
		return visitCount;
	}


	public void setVisitCount(int visitCount) {
		this.visitCount = visitCount;
	}


	public double getWinScore() {
		return winScore;
	}


	public void setWinScore(double winScore) {
		this.winScore = winScore;
	}
	
    void incrementVisit() {
        this.visitCount++;
    }

    void addScore(double score) {
        if (this.winScore != Integer.MIN_VALUE)
            this.winScore += score;
    }
    
	
	
}
