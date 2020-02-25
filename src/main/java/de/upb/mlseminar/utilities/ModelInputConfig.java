package de.upb.mlseminar.utilities;

public class ModelInputConfig {

	private int ownDiscardPileThreshold ;
	private int ownDiscardPileIncreamentFactor;
	private int opponentDiscardPileThreshold;
	private int minNumOfPlacements;
	
	public ModelInputConfig() {
		super();
	}
	
	public ModelInputConfig(int ownDiscardPileThreshold, int ownDiscardPileIncreamentFactor,
			int opponentDiscardPileThreshold, int minNumOfPlacements) {
		super();
		this.ownDiscardPileThreshold = ownDiscardPileThreshold;
		this.ownDiscardPileIncreamentFactor = ownDiscardPileIncreamentFactor;
		this.opponentDiscardPileThreshold = opponentDiscardPileThreshold;
		this.minNumOfPlacements = minNumOfPlacements;
	}

	public int getOwnDiscardPileThreshold() {
		return ownDiscardPileThreshold;
	}

	public void setOwnDiscardPileThreshold(int ownDiscardPileThreshold) {
		this.ownDiscardPileThreshold = ownDiscardPileThreshold;
	}

	public int getOwnDiscardPileIncreamentFactor() {
		return ownDiscardPileIncreamentFactor;
	}

	public void setOwnDiscardPileIncreamentFactor(int ownDiscardPileIncreamentFactor) {
		this.ownDiscardPileIncreamentFactor = ownDiscardPileIncreamentFactor;
	}

	public int getOpponentDiscardPileThreshold() {
		return opponentDiscardPileThreshold;
	}

	public void setOpponentDiscardPileThreshold(int opponentDiscardPileThreshold) {
		this.opponentDiscardPileThreshold = opponentDiscardPileThreshold;
	}

	public int getMinNumOfPlacements() {
		return minNumOfPlacements;
	}

	public void setMinNumOfPlacements(int minNumOfPlacements) {
		this.minNumOfPlacements = minNumOfPlacements;
	}

	@Override
	public String toString() {
		return "ModelInputConfig [ownDiscardPileThreshold=" + ownDiscardPileThreshold
				+ ", ownDiscardPileIncreamentFactor=" + ownDiscardPileIncreamentFactor
				+ ", opponentDiscardPileThreshold=" + opponentDiscardPileThreshold + ", minNumOfPlacements="
				+ minNumOfPlacements + "]";
	}

	
	
}
