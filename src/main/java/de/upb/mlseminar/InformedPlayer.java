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

public class InformedPlayer implements Player {

	private Random random;
	private String name;

	private final int ownDiscardPileThreshold = 10;
	private final int opponentDiscardPileThreshold = 20;

	public InformedPlayer(String name) {
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
	
	private Placement cardPlacementUpdator(GameState currentGameState, Card card,
			CardPosition position) {

		boolean placedOnOppositePiles = false;
		Placement testCardPlacement = new Placement(card, position);
		Placement finalCardPlacement = null;

		if (testCardPlacement.getPosition() == CardPosition.OPPONENTS_ASCENDING_DISCARD_PILE
				|| testCardPlacement.getPosition() == CardPosition.OPPONENTS_DESCENDING_DISCARD_PILE) {
			placedOnOppositePiles = true;
		}

		if (isPlacementValid(testCardPlacement, currentGameState, !placedOnOppositePiles)) {
			finalCardPlacement = testCardPlacement;
			currentGameState = computeNewGameStateAfterPlacement(currentGameState, testCardPlacement);
		}

		return finalCardPlacement;
	}

	private IntermediateMoveStatus backwardsTrickValidator(GameState currentGameState, List<Card> currentHandCards,
			Card currentTopCardOnOwnAscedingDiscardPile, Card currentTopCardOnOwnDescendingDiscardPile) {

		List<Placement> cardPlacementList = new ArrayList<Placement>();
		// Test for Backwards Trick
		for (Card card : currentHandCards) {

			// Test if a current card is 10 lesser than topCardOnOwnAscendingDiscardPile
			if (card.is10SmallerThan(currentTopCardOnOwnAscedingDiscardPile)) {
				
				Placement temp = cardPlacementUpdator(currentGameState, card,
						CardPosition.OWN_ASCENDING_DISCARD_PILE);
				
				if(temp != null) {
				cardPlacementList.add(temp);
				currentTopCardOnOwnAscedingDiscardPile = card;
				}

			}

			// Test if a current card is 10 greater than topCardOnOwnDescendingDiscardPile
			if (card.is10LargerThan(currentTopCardOnOwnDescendingDiscardPile)) {

				Placement temp = cardPlacementUpdator(currentGameState, card,
						CardPosition.OWN_DESCENDING_DISCARD_PILE);
				
				if(temp != null) {
				cardPlacementList.add(temp);
				currentTopCardOnOwnDescendingDiscardPile = card;
				}
				

			}

		}

		return new IntermediateMoveStatus(currentGameState, cardPlacementList,
				currentTopCardOnOwnAscedingDiscardPile, currentTopCardOnOwnDescendingDiscardPile);

	}
	
	private IntermediateMoveStatus opponentCardPlacement(GameState currentGameState, List<Card> currentHandCards,
			Card currentTopCardOnOpponentAscedingDiscardPile, Card currentTopCardOnOpponentDescendingDiscardPile) {
		
		
		List<Placement> cardPlacementList = new ArrayList<Placement>();
		Card bestCandidate = null;
		int  leastDifference = -1;
		int ascOrDecFlag = -1; // 1 for ascending, 2 for descending

		for (Card card : currentHandCards) {

			int diffBtwOpponentAscendingDiscardPile = currentTopCardOnOpponentAscedingDiscardPile.getNumber()
					- card.getNumber();
			int diffBtwOpponentDescendingDiscardPile = card.getNumber()
					- currentTopCardOnOpponentDescendingDiscardPile.getNumber();

			
			//int currentLeastDifference = Math.min(diffBtwOpponentAscendingDiscardPile, diffBtwOpponentDescendingDiscardPile);
			int currentMinDifference = -1;
			Card currentBestCandidate = null;
			int localAscOrDecFlag = -1;
			
			// Rule for placement on Opponents Ascending Discard pile
			if (diffBtwOpponentAscendingDiscardPile > 0
					&& card.getNumber() < currentTopCardOnOpponentAscedingDiscardPile.getNumber()
					&& card.getNumber() > (currentTopCardOnOpponentAscedingDiscardPile.getNumber()
							- opponentDiscardPileThreshold)) {
					
				if (diffBtwOpponentAscendingDiscardPile < currentMinDifference) {
					currentMinDifference = diffBtwOpponentAscendingDiscardPile;
					currentBestCandidate = card;
					localAscOrDecFlag = 1;
				System.out.println("Inside Opponents Ascending Discard pile");
				}

			}

			// Rule for placement on own Descending Discard pile
			if (diffBtwOpponentDescendingDiscardPile > 0
					&& card.getNumber() > currentTopCardOnOpponentDescendingDiscardPile.getNumber()
					&& card.getNumber() < (currentTopCardOnOpponentDescendingDiscardPile.getNumber()
							+ opponentDiscardPileThreshold)) {

					if (diffBtwOpponentDescendingDiscardPile < currentMinDifference) {
						currentMinDifference = diffBtwOpponentDescendingDiscardPile;
						currentBestCandidate = card;
						localAscOrDecFlag = 2;
					}
				System.out.println("Inside Opponents Descending Discard pile");

			}
			
			if (currentBestCandidate != null && currentMinDifference < leastDifference && localAscOrDecFlag > 0) {
				leastDifference = currentMinDifference;
				bestCandidate = currentBestCandidate;
				ascOrDecFlag = localAscOrDecFlag;
			}

		}
		
		if (bestCandidate != null && leastDifference > 0 && ascOrDecFlag > 0 ) {
			if (ascOrDecFlag == 1) {
				
				Placement temp = cardPlacementUpdator(currentGameState, bestCandidate,
						CardPosition.OPPONENTS_ASCENDING_DISCARD_PILE);
				
				if(temp != null) {
				cardPlacementList.add(temp);
				
				}
				
			}else if (ascOrDecFlag == 2){
				
				Placement temp = cardPlacementUpdator(currentGameState, bestCandidate,
						CardPosition.OPPONENTS_DESCENDING_DISCARD_PILE);
				
				if(temp != null) {
				cardPlacementList.add(temp);
				
				}
			}
			
		}	
		return new IntermediateMoveStatus(currentGameState, cardPlacementList);
		
	}

	private IntermediateMoveStatus ownDiscardPilesPlacement(GameState currentGameState, List<Card> currentHandCards,
			Card topCardOnOwnAscedingDiscardPile, Card topCardOnOwnDescendingDiscardPile) {
		
		Card initialTopCardOnOwnAscedingDiscardPile = topCardOnOwnAscedingDiscardPile;
		Card intialTopCardOnOwnDescendingDiscardPile = topCardOnOwnDescendingDiscardPile;
		
		List<Placement> cardPlacementList = new ArrayList<Placement>();
		
		// Sorting the cards in Ascending order
		
		List<Card> ascedingOrderListOfCards = new ArrayList<Card>(currentHandCards);
		ascedingOrderListOfCards.sort((Card c1, Card c2) -> c1.getNumber() - c2.getNumber());	
		
		
		// Sorting the cards in Descending order
		List<Card> descendingOrderListOfCards = new ArrayList<Card>(currentHandCards);;
		descendingOrderListOfCards.sort((Card c1, Card c2) -> c2.getNumber() - c1.getNumber());	
		
		int ownDiscardPilesAverage = Math
				.abs(ascedingOrderListOfCards.get(0).getNumber() + currentHandCards.get(ascedingOrderListOfCards.size() - 1).getNumber()) / 2;
		
		System.out.println("Own discard pile Average =" + ownDiscardPilesAverage);
		
		// For ascending order rule
		for (Card card : ascedingOrderListOfCards) {
			
			
			if (card.getNumber() > ownDiscardPilesAverage) {
				continue;}

			System.out.println("Processing for the card : " + card.getNumber());
			//System.out.print("Processing for the card : " + card.getNumber());
				// Rule for placement on own Ascending Discard pile
				if (card.getNumber() > topCardOnOwnAscedingDiscardPile.getNumber() && card
						.getNumber() < (topCardOnOwnAscedingDiscardPile.getNumber() + ownDiscardPileThreshold)) {

					System.out.println("Inside Own Ascending Discard pile");
					Placement temp = cardPlacementUpdator(currentGameState, card,
							CardPosition.OWN_ASCENDING_DISCARD_PILE);
					
					if(temp != null) {
					cardPlacementList.add(temp);
					topCardOnOwnAscedingDiscardPile = card;
					}
				}	
		}

		// For Descending order rule
		for (Card card : descendingOrderListOfCards) {

			if (card.getNumber() <= ownDiscardPilesAverage)
				continue;

			System.out.println("Processing for the card : " + card.getNumber());
				// Rule for placement on own Descending Discard pile
				if (card.getNumber() < topCardOnOwnDescendingDiscardPile.getNumber() && card
						.getNumber() > (topCardOnOwnDescendingDiscardPile.getNumber() - ownDiscardPileThreshold)) {

					System.out.println("Inside Own Ascending Discard pile");
					
					Placement temp = cardPlacementUpdator(currentGameState, card,
							CardPosition.OWN_DESCENDING_DISCARD_PILE);
					
					if(temp != null) {
						cardPlacementList.add(temp);
						topCardOnOwnDescendingDiscardPile = card;
					}
					
					System.out.println("topCardOnOwnAscendingDiscardPile after iteration :"
							+ topCardOnOwnDescendingDiscardPile.getNumber());
				}

		}
		
		System.out.print("List of placements inside ownDiscardPilesPlacement = ");
		cardPlacementList.forEach(System.out::println);
		
		return new IntermediateMoveStatus(currentGameState, cardPlacementList,
				topCardOnOwnAscedingDiscardPile, topCardOnOwnDescendingDiscardPile);
		
	}
	
	private List<Placement> getCardPlacement(GameState gameState) {

		List<Placement> placementsOfMove = new ArrayList<Placement>();

		// Fetching the details of the current game state
		Card topCardOnOwnAscendingDiscardPile = gameState.getTopCardOnOwnAscendingDiscardPile();
		Card topCardOnOwnDescendingDiscardPile = gameState.getTopCardOnOwnDescendingDiscardPile();

		Card topCardOnOpponentAscendingDiscardPile = gameState.getTopCardOnOpponentsAscendingDiscardPile();
		Card topCardOnOpponentDescendingDiscardPile = gameState.getTopCardOnOpponentsDescendingDiscardPile();


		System.out.println(
				"Own game state : topCardOnOwnAscendingDiscardPile = " + topCardOnOwnAscendingDiscardPile.getNumber()
						+ " , topCardOnOwnDescendingDiscardPile = " + topCardOnOwnDescendingDiscardPile.getNumber());
		System.out.println("Opponents game state : topCardOnOpponentAscendingDiscardPile = "
				+ gameState.getTopCardOnOpponentsAscendingDiscardPile().getNumber()
				+ " , topCardOnOpponentDescendingDiscardPile = "
				+ gameState.getTopCardOnOpponentsDescendingDiscardPile().getNumber());



		// Copy into a different list as the source list is an immutable list
		List<Card> orderedCurrentHandCards = new ArrayList<Card>(gameState.getHandCards());

		// Sorting the cards in Ascending order
		orderedCurrentHandCards.sort((Card c1, Card c2) -> c1.getNumber() - c2.getNumber());
		

		Card smallest = orderedCurrentHandCards.get(0);
		Card largest = orderedCurrentHandCards.get(orderedCurrentHandCards.size() - 1);
		

		//// Real Logic starts here.

		// Test for Backwards Trick -- Start
		IntermediateMoveStatus backwardTrickResults = backwardsTrickValidator(gameState, orderedCurrentHandCards,
				topCardOnOwnAscendingDiscardPile, topCardOnOwnDescendingDiscardPile);
		gameState = backwardTrickResults.getGameState();
		topCardOnOwnAscendingDiscardPile = backwardTrickResults.getCurrentTopCardOnOwnAscedingDiscardPile();
		topCardOnOwnDescendingDiscardPile = backwardTrickResults.getCurrentTopCardOnOwnDescendingDiscardPile();
		placementsOfMove.addAll(backwardTrickResults.getListOfCardPlacements());
		
		for (Placement currentPlacement : placementsOfMove) {
			if (orderedCurrentHandCards.contains(currentPlacement.getCard()))
					orderedCurrentHandCards.remove(currentPlacement.getCard());
		}
		
		// Test for Backwards Trick -- End
		
		// Test for Placement on Opponents Discard Pile	-- Start	
		IntermediateMoveStatus opponentDiscardPilePlacement = opponentCardPlacement(gameState, orderedCurrentHandCards, topCardOnOpponentAscendingDiscardPile, topCardOnOpponentDescendingDiscardPile);
		gameState = opponentDiscardPilePlacement.getGameState();
		placementsOfMove.addAll(opponentDiscardPilePlacement.getListOfCardPlacements());
		
		for (Placement currentPlacement : placementsOfMove) {
			if (orderedCurrentHandCards.contains(currentPlacement.getCard()))
					orderedCurrentHandCards.remove(currentPlacement.getCard());
		}
		
		// Test for Placement on Opponents Discard Pile	-- End
		
		
		// Test for Placement on Own Discard Pile -- Start
		IntermediateMoveStatus ownDiscardPilePlacement = ownDiscardPilesPlacement(gameState, orderedCurrentHandCards,
				topCardOnOwnAscendingDiscardPile, topCardOnOwnDescendingDiscardPile);
		gameState = ownDiscardPilePlacement.getGameState();
		topCardOnOwnAscendingDiscardPile = ownDiscardPilePlacement.getCurrentTopCardOnOwnAscedingDiscardPile();
		topCardOnOwnDescendingDiscardPile = ownDiscardPilePlacement.getCurrentTopCardOnOwnDescendingDiscardPile();
		placementsOfMove.addAll(ownDiscardPilePlacement.getListOfCardPlacements());

		
		System.out.print(" Placements= ");
		placementsOfMove.forEach(System.out::print);

		
		// Test for Placement on Own Discard Pile -- End
		
		
		/*
		// For ascending order rule
		for (Card card : orderedCurrentHandCards) {

			if (card.getNumber() > ownDiscardPilesAverage)
				continue;


			try {

				// Rule for placement on own Ascending Discard pile
				if (card.getNumber() > topCardOnOwnAscendingDiscardPile.getNumber() && card
						.getNumber() < (topCardOnOwnAscendingDiscardPile.getNumber() + ownDiscardPileThreshold)) {


					placementsOfMove = cardPlacementValidatorAndUpdator(gameState, card,
							CardPosition.OWN_ASCENDING_DISCARD_PILE, placementsOfMove);
					topCardOnOwnAscendingDiscardPile = card;

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// For Descending order rule
		for (Card card : descOrderedCurrentHandCards) {

			// List<Placement> validPlacements = new ArrayList<Placement>();

			// Card smallest = orderedCurrentHandCards.get(0);
			// Card largest = orderedCurrentHandCards.get(orderedCurrentHandCards.size() -
			// 1);

			if (card.getNumber() <= ownDiscardPilesAverage)
				continue;


			try {

				// Rule for placement on own Descending Discard pile
				if (card.getNumber() < topCardOnOwnDescendingDiscardPile.getNumber() && card
						.getNumber() > (topCardOnOwnDescendingDiscardPile.getNumber() - ownDiscardPileThreshold)) {

					placementsOfMove = cardPlacementValidatorAndUpdator(gameState, card,
							CardPosition.OWN_DESCENDING_DISCARD_PILE, placementsOfMove);

					topCardOnOwnDescendingDiscardPile = card;
					System.out.println("topCardOnOwnAscendingDiscardPile after iteration :"
							+ topCardOnOwnDescendingDiscardPile.getNumber());
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		*/
		
		
		return placementsOfMove;

	}

	@Override
	public Move computeMove(GameState gameState) {
		GameState currentGameState = gameState;
		boolean placedOnOpponentsPiles = false;

		List<Placement> placementsOfMove = new ArrayList<Placement>();

		System.out.println("Player playing the game : " + getName());

		System.out.println("Gamee state is : " + currentGameState.getHandCards().toString());
		System.out.println("------------------------");

		placementsOfMove = getCardPlacement(currentGameState);

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

		System.out.println("#########################################\n");
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
