package de.upb.mlseminar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import de.upb.isml.thegamef2f.engine.CardPosition;
import de.upb.isml.thegamef2f.engine.GameState;
import de.upb.isml.thegamef2f.engine.Move;
import de.upb.isml.thegamef2f.engine.Placement;
import de.upb.isml.thegamef2f.engine.board.Card;
import de.upb.isml.thegamef2f.engine.player.Player;

public class MCTSImplementer implements Player {

	private Random random;
	private String name;

	private final int ownDiscardPileThreshold = 5;
	private final int opponentDiscardPileThreshold = 5;

	public MCTSImplementer(String name) {
		this.name = name;
	}

	@Override
	public void initialize(long randomSeed) {
		this.random = new Random(randomSeed);

	}

	@Override
	public String toString() {
		return "random_player_" + name;
	}

	private List<Placement> cardPlacementValidatorAndUpdator(GameState currentGameState, Card card, CardPosition position, List<Placement> listOfCardPlacements) {

		boolean placedOnOppositePiles = false;
		Placement cardPlacement = new Placement(card, position);

		if (cardPlacement.getPosition() == CardPosition.OPPONENTS_ASCENDING_DISCARD_PILE
				|| cardPlacement.getPosition() == CardPosition.OPPONENTS_DESCENDING_DISCARD_PILE) {
			placedOnOppositePiles = true;
		}

		if (isPlacementValid(cardPlacement, currentGameState, !placedOnOppositePiles)) {
			listOfCardPlacements.add(cardPlacement);
			currentGameState = computeNewGameStateAfterPlacement(currentGameState, cardPlacement);
		}

		return listOfCardPlacements;
	}

	private List<Placement> getCardPlacement(GameState gameState) {

		List<Placement> placementsOfMove = new ArrayList<Placement>();

		// Fetching the details of the current game state
		Card topCardOnOwnAscendingDiscardPile = gameState.getTopCardOnOwnAscendingDiscardPile();
		Card topCardOnOwnDescendingDiscardPile = gameState.getTopCardOnOwnDescendingDiscardPile();

		Card topCardOnOpponentAscendingDiscardPile = gameState.getTopCardOnOpponentsAscendingDiscardPile();
		Card topCardOnOpponentDescendingDiscardPile = gameState.getTopCardOnOpponentsDescendingDiscardPile();

		int ownDiscardPilesRange = Math
				.abs(gameState.getTopCardOnOwnAscendingDiscardPile().getNumber() + gameState.getTopCardOnOwnDescendingDiscardPile().getNumber()) / 2;

		System.out.println(
				"Own game state : topCardOnOwnAscendingDiscardPile = " + gameState.getTopCardOnOwnAscendingDiscardPile().getNumber()
						+ " , topCardOnOwnDescendingDiscardPile = " + gameState.getTopCardOnOwnDescendingDiscardPile().getNumber());
		System.out.println("Opponents game state : topCardOnOpponentAscendingDiscardPile = "
				+ gameState.getTopCardOnOpponentsAscendingDiscardPile().getNumber() + " , topCardOnOpponentDescendingDiscardPile = "
				+ gameState.getTopCardOnOpponentsDescendingDiscardPile().getNumber());

		// Copy into a different list as the source list is an immutable list
		List<Card> orderedCurrentHandCards = new ArrayList<Card>(gameState.getHandCards());

		// Sorting the cards in Ascending order
		orderedCurrentHandCards.sort((Card c1, Card c2) -> c1.getNumber() - c2.getNumber());

		
		// Test for Backwards Trick
		for (Card card : orderedCurrentHandCards) {

			// Test if a current card is 10 lesser than topCardOnOwnAscendingDiscardPile
			if (card.is10SmallerThan(gameState.getTopCardOnOwnAscendingDiscardPile())) {

				placementsOfMove = cardPlacementValidatorAndUpdator(gameState, card,
						CardPosition.OWN_ASCENDING_DISCARD_PILE, placementsOfMove);
			}

			// Test if a current card is 10 greater than topCardOnOwnDescendingDiscardPile
			if (card.is10LargerThan(gameState.getTopCardOnOwnDescendingDiscardPile())) {

				placementsOfMove = cardPlacementValidatorAndUpdator(gameState, card,
						CardPosition.OWN_DESCENDING_DISCARD_PILE, placementsOfMove);
			}

		}
	
		// For ascending order rule
		for (Card card : orderedCurrentHandCards) {
			//List<Placement> validPlacements = new ArrayList<Placement>();
			
			//Card smallest = orderedCurrentHandCards.get(0);
			//Card largest = orderedCurrentHandCards.get(orderedCurrentHandCards.size() - 1);

			try {
				
				// Rule for placement on own Ascending Discard pile
				if (card.getNumber() > gameState.getTopCardOnOwnAscendingDiscardPile().getNumber() && card
						.getNumber() < (gameState.getTopCardOnOwnAscendingDiscardPile().getNumber() + ownDiscardPileThreshold)) {
					
					System.out.println("Inside own Ascending Discard pile");
					placementsOfMove = cardPlacementValidatorAndUpdator(gameState, card,
							CardPosition.OWN_ASCENDING_DISCARD_PILE, placementsOfMove);
					

				}

				// Rule for placement on own Descending Discard pile
				if (card.getNumber() < gameState.getTopCardOnOwnDescendingDiscardPile().getNumber() && card
						.getNumber() > (gameState.getTopCardOnOwnDescendingDiscardPile().getNumber() - ownDiscardPileThreshold)) {
					
					System.out.println("Inside own Descending Discard pile");
					placementsOfMove = cardPlacementValidatorAndUpdator(gameState, card,
							CardPosition.OWN_DESCENDING_DISCARD_PILE, placementsOfMove);

				}

				// Rule for placement on Opponents Ascending Discard pile
				if (card.getNumber() < gameState.getTopCardOnOpponentsAscendingDiscardPile().getNumber()
						&& card.getNumber() > (gameState.getTopCardOnOpponentsAscendingDiscardPile().getNumber()
								- opponentDiscardPileThreshold)) {

					System.out.println("Inside Opponents Ascending Discard pile");
					placementsOfMove = cardPlacementValidatorAndUpdator(gameState, card,
							CardPosition.OPPONENTS_ASCENDING_DISCARD_PILE, placementsOfMove);
					
				}

				// Rule for placement on own Descending Discard pile
				if (card.getNumber() > gameState.getTopCardOnOpponentsDescendingDiscardPile().getNumber()
						&& card.getNumber() < (gameState.getTopCardOnOpponentsDescendingDiscardPile().getNumber()
								+ opponentDiscardPileThreshold)) {
					
					System.out.println("Inside Opponents Descending Discard pile");
					placementsOfMove = cardPlacementValidatorAndUpdator(gameState, card,
							CardPosition.OPPONENTS_DESCENDING_DISCARD_PILE, placementsOfMove);


				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		

		
		System.out.println("New Placements = ");
		placementsOfMove.forEach(System.out::println);
		return placementsOfMove;

	}

	@Override
	public Move computeMove(GameState gameState) {
		GameState currentGameState = gameState;
		boolean placedOnOpponentsPiles = false;

		List<Placement> placementsOfMove = new ArrayList<Placement>();

		placementsOfMove = getCardPlacement(currentGameState);

		System.out.println("Gane state is : " + currentGameState.getHandCards().toString());
		/*
		 * List<Card> orderedCurrentHandCards = new
		 * ArrayList<Card>(currentGameState.getHandCards());
		 * 
		 * // Sorting the cards in Ascending order orderedCurrentHandCards.sort((Card
		 * c1,Card c2) -> c1.getNumber()- c2.getNumber());
		 * 
		 * 
		 * 
		 * while (!orderedCurrentHandCards.isEmpty()) { List<Placement> validPlacements
		 * = new ArrayList<Placement>();
		 * 
		 * 
		 * // compute all valid placements for (Card card : orderedCurrentHandCards) {
		 * for (CardPosition position : CardPosition.values()) { Placement placement =
		 * new Placement(card, position); if (isPlacementValid(placement,
		 * currentGameState, !placedOnOpponentsPiles)) { validPlacements.add(placement);
		 * } } }
		 * 
		 * 
		 * 
		 * System.out.println( "Current game hand "+ orderedCurrentHandCards);
		 * 
		 * System.out.println( "valid Placements are "+ validPlacements.toString());
		 * 
		 * // if we cannot find a valid placement anymore, we can stop here and return
		 * the // ones we have so far if (validPlacements.isEmpty()) { return new
		 * Move(placementsOfMove); }
		 * 
		 * // pick a random placement out of the valid ones int randomInteger =
		 * random.nextInt(validPlacements.size()); Placement randomPlacement =
		 * validPlacements.get(randomInteger);
		 * 
		 * System.out.println( "Random Placement is "+ randomPlacement.toString());
		 * 
		 * System.out.println( "------------ " );
		 * 
		 * 
		 * if (randomPlacement.getPosition() ==
		 * CardPosition.OPPONENTS_ASCENDING_DISCARD_PILE ||
		 * randomPlacement.getPosition() ==
		 * CardPosition.OPPONENTS_DESCENDING_DISCARD_PILE) { placedOnOpponentsPiles =
		 * true; } // add this random placement to the placements for our move
		 * placementsOfMove.add(randomPlacement);
		 * 
		 * // update the view we have on the game to make sure that the next set of
		 * valid // placements is indeed valid currentGameState =
		 * computeNewGameStateAfterPlacement(currentGameState, randomPlacement); }
		 */

		return new Move(placementsOfMove);
	}

	private GameState computeNewGameStateAfterPlacement(GameState gameStatePriorToPlacement, Placement placement) {
		List<Card> handCards = new ArrayList<>(gameStatePriorToPlacement.getHandCards());
		handCards.remove(placement.getCard());

		Card topCardOnOwnAscendingDiscardPile = placement.getPosition() == CardPosition.OWN_ASCENDING_DISCARD_PILE
				? placement.getCard()
				: gameStatePriorToPlacement.getTopCardOnOwnAscendingDiscardPile();
		Card topCardOnOwnDescendingDiscardPile = placement.getPosition() == CardPosition.OWN_DESCENDING_DISCARD_PILE
				? placement.getCard()
				: gameStatePriorToPlacement.getTopCardOnOwnDescendingDiscardPile();

		Card topCardOnOpponentsAscendingDiscardPile = placement
				.getPosition() == CardPosition.OPPONENTS_ASCENDING_DISCARD_PILE ? placement.getCard()
						: gameStatePriorToPlacement.getTopCardOnOpponentsAscendingDiscardPile();
		Card topCardOnOpponentsDescendingDiscardPile = placement
				.getPosition() == CardPosition.OPPONENTS_DESCENDING_DISCARD_PILE ? placement.getCard()
						: gameStatePriorToPlacement.getTopCardOnOpponentsDescendingDiscardPile();

		return new GameState(handCards, topCardOnOwnAscendingDiscardPile, topCardOnOwnDescendingDiscardPile,
				topCardOnOpponentsAscendingDiscardPile, topCardOnOpponentsDescendingDiscardPile);
	}

	private boolean isPlacementValid(Placement placement, GameState gameState, boolean placingOnOpponentPilesAllowed) {
		switch (placement.getPosition()) {
		case OPPONENTS_ASCENDING_DISCARD_PILE:
			return placingOnOpponentPilesAllowed
					? canPlaceCardOnOpponentsAscendingDiscardPile(placement.getCard(), gameState)
					: false;
		case OPPONENTS_DESCENDING_DISCARD_PILE:
			return placingOnOpponentPilesAllowed
					? canPlaceCardOnOpponentsDescendingDiscardPile(placement.getCard(), gameState)
					: false;
		case OWN_ASCENDING_DISCARD_PILE:
			return canPlaceCardOnOwnAscendingDiscardPile(placement.getCard(), gameState);
		case OWN_DESCENDING_DISCARD_PILE:
			return canPlaceCardOnOwnDescendingDiscardPile(placement.getCard(), gameState);
		}
		return false;
	}

	private boolean canPlaceCardOnOwnAscendingDiscardPile(Card card, GameState gameState) {
		return gameState.getTopCardOnOwnAscendingDiscardPile().isSmallerThan(card)
				|| gameState.getTopCardOnOwnAscendingDiscardPile().is10LargerThan(card);
	}

	private boolean canPlaceCardOnOwnDescendingDiscardPile(Card card, GameState gameState) {
		return card.isSmallerThan(gameState.getTopCardOnOwnDescendingDiscardPile())
				|| card.is10LargerThan(gameState.getTopCardOnOwnDescendingDiscardPile());
	}

	private boolean canPlaceCardOnOpponentsAscendingDiscardPile(Card card, GameState gameState) {
		return card.isSmallerThan(gameState.getTopCardOnOpponentsAscendingDiscardPile());
	}

	private boolean canPlaceCardOnOpponentsDescendingDiscardPile(Card card, GameState gameState) {
		return gameState.getTopCardOnOpponentsDescendingDiscardPile().isSmallerThan(card);
	}

	@Override
	public String getName() {
		return toString();
	}

}
