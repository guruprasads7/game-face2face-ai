package de.upb.mlseminar.informedplayer;

import java.util.ArrayList;
import java.util.List;

import de.upb.isml.thegamef2f.engine.GameState;
import de.upb.isml.thegamef2f.engine.Placement;
import de.upb.isml.thegamef2f.engine.board.Card;

/**
 * This class is an Intermediate object used by InformedSingleInstancePlayer
 *
 * @author Guru Prasad Savandaiah
 *
 */
public class IntermediateMoveStatus {

	GameState gameState;
	List<Card> currentHandCards;
	List<Placement> listOfCardPlacements = new ArrayList<Placement>();
	Card currentTopCardOnOwnAscendingDiscardPile;
	Card currentTopCardOnOwnDescendingDiscardPile;

	public IntermediateMoveStatus(GameState gameState) {
		super();
		this.gameState = gameState;
	}

	public IntermediateMoveStatus(GameState gameState, List<Card> currentHandCards,
			List<Placement> listOfCardPlacements, Card currentTopCardOnOwnAscedingDiscardPile,
			Card currentTopCardOnOwnDescendingDiscardPile) {
		super();
		this.gameState = gameState;
		this.currentHandCards = currentHandCards;
		this.listOfCardPlacements = listOfCardPlacements;
		this.currentTopCardOnOwnAscendingDiscardPile = currentTopCardOnOwnAscedingDiscardPile;
		this.currentTopCardOnOwnDescendingDiscardPile = currentTopCardOnOwnDescendingDiscardPile;
	}

	public IntermediateMoveStatus(GameState gameState, List<Card> currentHandCards,
			List<Placement> listOfCardPlacements) {
		super();
		this.gameState = gameState;
		this.currentHandCards = currentHandCards;
		this.listOfCardPlacements = listOfCardPlacements;
	}

	public IntermediateMoveStatus(List<Placement> listOfCardPlacements, Card currentTopCardOnOwnAscedingDiscardPile,
			Card currentTopCardOnOwnDescendingDiscardPile) {
		super();
		this.listOfCardPlacements = listOfCardPlacements;
		this.currentTopCardOnOwnAscendingDiscardPile = currentTopCardOnOwnAscedingDiscardPile;
		this.currentTopCardOnOwnDescendingDiscardPile = currentTopCardOnOwnDescendingDiscardPile;
	}

	public List<Card> getCurrentHandCards() {
		return currentHandCards;
	}

	public void setCurrentHandCards(List<Card> currentHandCards) {
		this.currentHandCards = currentHandCards;
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

	public Card getCurrentTopCardOnOwnAscendingDiscardPile() {
		return currentTopCardOnOwnAscendingDiscardPile;
	}

	public void setCurrentTopCardOnOwnAscendingDiscardPile(Card currentTopCardOnOwnAscedingDiscardPile) {
		this.currentTopCardOnOwnAscendingDiscardPile = currentTopCardOnOwnAscedingDiscardPile;
	}

	public Card getCurrentTopCardOnOwnDescendingDiscardPile() {
		return currentTopCardOnOwnDescendingDiscardPile;
	}

	public void setCurrentTopCardOnOwnDescendingDiscardPile(Card currentTopCardOnOwnDescendingDiscardPile) {
		this.currentTopCardOnOwnDescendingDiscardPile = currentTopCardOnOwnDescendingDiscardPile;
	}

}
