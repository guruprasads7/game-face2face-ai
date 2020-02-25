package de.upb.mlseminar.utilities;

public class NodeState {

	private IntermediateGameState gameState;
	private ModelInputConfig modelInputConfig;
    private int visitCount;
    private double winScore;
    
	public NodeState(IntermediateGameState gameState) {
		super();
		this.gameState = gameState;
		modelInputConfig = new ModelInputConfig();
	}
	
	public NodeState() {
		super();
	}

	public NodeState(IntermediateGameState gameState, ModelInputConfig modelInputConfig) {
		super();
		this.gameState = gameState;
		this.modelInputConfig = modelInputConfig;
	}

	public NodeState(IntermediateGameState gameState, ModelInputConfig modelInputConfig, int visitCount,
			double winScore) {
		super();
		this.gameState = gameState;
		this.modelInputConfig = modelInputConfig;
		this.visitCount = visitCount;
		this.winScore = winScore;
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

	public ModelInputConfig getModelInputConfig() {
		return modelInputConfig;
	}

	public void setModelInputConfig(ModelInputConfig modelInputConfig) {
		this.modelInputConfig = modelInputConfig;
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
	
    public void incrementVisit() {
        this.visitCount++;
    }

    public void addScore(double score) {
        if (this.winScore != Integer.MIN_VALUE)
            this.winScore += score;
    }
    
	
	
}
