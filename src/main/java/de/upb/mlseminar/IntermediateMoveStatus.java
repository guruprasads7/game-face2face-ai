package de.upb.mlseminar;

import java.util.ArrayList;
import java.util.List;

import de.upb.isml.thegamef2f.engine.GameState;
import de.upb.isml.thegamef2f.engine.Placement;
import de.upb.isml.thegamef2f.engine.board.Card;

public class IntermediateMoveStatus {
	
	GameState gameState;
	List<Placement> listOfCardPlacements = new ArrayList<Placement>();
	Card currentTopCardOnOwnAscedingDiscardPile;
	Card currentTopCardOnOwnDescendingDiscardPile;
	
	

	public IntermediateMoveStatus(GameState gameState) {
		super();
		this.gameState = gameState;
	}

	public IntermediateMoveStatus(GameState gameState, List<Placement> listOfCardPlacements,
			Card currentTopCardOnOwnAscedingDiscardPile, Card currentTopCardOnOwnDescendingDiscardPile) {
		super();
		this.gameState = gameState;
		this.listOfCardPlacements = listOfCardPlacements;
		this.currentTopCardOnOwnAscedingDiscardPile = currentTopCardOnOwnAscedingDiscardPile;
		this.currentTopCardOnOwnDescendingDiscardPile = currentTopCardOnOwnDescendingDiscardPile;
	}
	
	public IntermediateMoveStatus(GameState gameState, List<Placement> listOfCardPlacements) {
		super();
		this.gameState = gameState;
		this.listOfCardPlacements = listOfCardPlacements;
	}

	public IntermediateMoveStatus(List<Placement> listOfCardPlacements, Card currentTopCardOnOwnAscedingDiscardPile,
			Card currentTopCardOnOwnDescendingDiscardPile) {
		super();
		this.listOfCardPlacements = listOfCardPlacements;
		this.currentTopCardOnOwnAscedingDiscardPile = currentTopCardOnOwnAscedingDiscardPile;
		this.currentTopCardOnOwnDescendingDiscardPile = currentTopCardOnOwnDescendingDiscardPile;
	}

	public List<Placement> getListOfCardPlacements() {
		return listOfCardPlacements;
	}
	public void setListOfCardPlacements(List<Placement> listOfCardPlacements) {
		this.listOfCardPlacements = listOfCardPlacements;
	}
	public GameState getGameState() {
		return gameState;
	}
	public void setGameState(GameState gameState) {
		this.gameState = gameState;
	}
	public Card getCurrentTopCardOnOwnAscedingDiscardPile() {
		return currentTopCardOnOwnAscedingDiscardPile;
	}
	public void setCurrentTopCardOnOwnAscedingDiscardPile(Card currentTopCardOnOwnAscedingDiscardPile) {
		this.currentTopCardOnOwnAscedingDiscardPile = currentTopCardOnOwnAscedingDiscardPile;
	}
	public Card getCurrentTopCardOnOwnDescendingDiscardPile() {
		return currentTopCardOnOwnDescendingDiscardPile;
	}
	public void setCurrentTopCardOnOwnDescendingDiscardPile(Card currentTopCardOnOwnDescendingDiscardPile) {
		this.currentTopCardOnOwnDescendingDiscardPile = currentTopCardOnOwnDescendingDiscardPile;
	}

	
	
	
}
