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

	private int ownDiscardPileThreshold = 3;
	private int ownDiscardPileIncreamentFactor = 3;
	private final int opponentDiscardPileThreshold = 5;

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

	private Placement cardPlacementUpdator(GameState currentGameState, Card card, CardPosition position) {

		boolean placedOnOppositePiles = false;
		Placement testCardPlacement = new Placement(card, position);
		Placement finalCardPlacement = null;

		if (isPlacementValid(testCardPlacement, currentGameState, !placedOnOppositePiles)) {
			finalCardPlacement = testCardPlacement;
			currentGameState = computeNewGameStateAfterPlacement(currentGameState, testCardPlacement);
		}
		
		if (testCardPlacement.getPosition() == CardPosition.OPPONENTS_ASCENDING_DISCARD_PILE
				|| testCardPlacement.getPosition() == CardPosition.OPPONENTS_DESCENDING_DISCARD_PILE) {
			placedOnOppositePiles = true;
			
			if (isPlacementValid(testCardPlacement, currentGameState, placedOnOppositePiles)) {
				finalCardPlacement = testCardPlacement;
				currentGameState = computeNewGameStateAfterPlacement(currentGameState, testCardPlacement);
			}
			
		}



		return finalCardPlacement;
	}

	private IntermediateMoveStatus backwardsTrickValidator(GameState currentGameState, List<Card> currentHandCards,
			Card currenttopCardOnOwnAscendingDiscardPile, Card currentTopCardOnOwnDescendingDiscardPile) {

		System.out.println("Inside Backwards Trick Validator");
		List<Placement> cardPlacementList = new ArrayList<Placement>();
		// Test for Backwards Trick
		for (Card card : currentHandCards) {

			// System.out.println("Processing for the card : " + card.getNumber());
			// Test if a current card is 10 lesser than topCardOnOwnAscendingDiscardPile
			if (card.is10SmallerThan(currenttopCardOnOwnAscendingDiscardPile)) {

				Placement temp = cardPlacementUpdator(currentGameState, card, CardPosition.OWN_ASCENDING_DISCARD_PILE);

				if (temp != null) {
					cardPlacementList.add(temp);
					currenttopCardOnOwnAscendingDiscardPile = card;
				}

			}

			// Test if a current card is 10 greater than topCardOnOwnDescendingDiscardPile
			if (card.is10LargerThan(currentTopCardOnOwnDescendingDiscardPile)) {

				Placement temp = cardPlacementUpdator(currentGameState, card, CardPosition.OWN_DESCENDING_DISCARD_PILE);

				if (temp != null) {
					cardPlacementList.add(temp);
					currentTopCardOnOwnDescendingDiscardPile = card;
				}

			}

		}
		
		for (Placement currentPlacement : cardPlacementList) {
			if (currentHandCards.contains(currentPlacement.getCard()))
					currentHandCards.remove(currentPlacement.getCard());
		}

		//System.out.println("Placements from backwardTrick : " + cardPlacementList.toString());
		return new IntermediateMoveStatus(currentGameState,currentHandCards, cardPlacementList, currenttopCardOnOwnAscendingDiscardPile,
				currentTopCardOnOwnDescendingDiscardPile);

	}

	private IntermediateMoveStatus opponentCardPlacement(GameState currentGameState, List<Card> currentHandCards,
			Card currentTopCardOnOpponentAscedingDiscardPile, Card currentTopCardOnOpponentDescendingDiscardPile) {

		List<Placement> cardPlacementList = new ArrayList<Placement>();
		Card bestCandidate = null;
		int leastDifference = 99999;
		int ascOrDecFlag = -1; // 1 for ascending, 2 for descending
		int cardIndexInList = -1;

		for (Card card : currentHandCards) {
			// System.out.println("Processing Card =" + card.getNumber());
			int diffBtwOpponentAscendingDiscardPile = currentTopCardOnOpponentAscedingDiscardPile.getNumber()
					- card.getNumber();
			int diffBtwOpponentDescendingDiscardPile = card.getNumber()
					- currentTopCardOnOpponentDescendingDiscardPile.getNumber();

			// int currentLeastDifference = Math.min(diffBtwOpponentAscendingDiscardPile,
			// diffBtwOpponentDescendingDiscardPile);
			int currentMinDifference = 99999;
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
					//localCardIndexInList = card.get
					System.out.println("Inside Opponents Ascending Discard pile");
				}

			}

			// Rule for placement on own Descending Discard pile
			if (diffBtwOpponentDescendingDiscardPile > 0
					&& card.getNumber() > currentTopCardOnOpponentDescendingDiscardPile.getNumber()
					&& card.getNumber() < (currentTopCardOnOpponentDescendingDiscardPile.getNumber()
							+ opponentDiscardPileThreshold)) {

				//System.out.println("Inside Opponents Descending Discard pile");
				
				//System.out.println("diffBtwOpponentDescendingDiscardPile" + diffBtwOpponentDescendingDiscardPile +"currentMinDifference :" + currentMinDifference);
				
				if (diffBtwOpponentDescendingDiscardPile < currentMinDifference) {
					currentMinDifference = diffBtwOpponentDescendingDiscardPile;
					currentBestCandidate = card;
					localAscOrDecFlag = 2;
					//System.out.println("Inside Opponents asadasdasdx");
				}
				
				
				//System.out.println();

			}

			// System.out.println("current Min difference : " + currentMinDifference + " least Difference :" + leastDifference + " localAscOrDecFlag : " + localAscOrDecFlag);
			if (currentBestCandidate != null && currentMinDifference < leastDifference && localAscOrDecFlag > 0) {
				
				// System.out.println("Inside here");
				leastDifference = currentMinDifference;
				bestCandidate = currentBestCandidate;
				ascOrDecFlag = localAscOrDecFlag;
				// System.out.println("current Least difference : " + leastDifference + " bestCandidate :" + bestCandidate.getNumber() + " AscOrDecFlag : " + ascOrDecFlag);
			}

		}
		
		// System.out.println("comes till here");
		
		//System.out.println("current Least difference : " + leastDifference + " bestCandidate :" + bestCandidate.getNumber() + " AscOrDecFlag : " + ascOrDecFlag);
		if (bestCandidate != null && leastDifference > 0 && ascOrDecFlag > 0) {
			// System.out.println("Inside hello here");
			if (ascOrDecFlag == 1) {
				
				System.out.println("Inside Ascending");
				Placement temp = cardPlacementUpdator(currentGameState, bestCandidate,
						CardPosition.OPPONENTS_ASCENDING_DISCARD_PILE);

				if (temp != null) {
					cardPlacementList.add(temp);
					
					//currentTopCardOnOpponentAscedingDiscardPile = (currentHandCards.indexOf(bestCandidate) > currentHandCards.indexOf(currentTopCardOnOpponentAscedingDiscardPile)) ? currentHandCards.get(currentHandCards.indexOf(bestCandidate)): currentHandCards.get(currentHandCards.indexOf(currentTopCardOnOpponentAscedingDiscardPile)); 

				}

			} else if (ascOrDecFlag == 2) {

				System.out.println("Inside Descending");
				Placement temp = cardPlacementUpdator(currentGameState, bestCandidate,
						CardPosition.OPPONENTS_DESCENDING_DISCARD_PILE);

				// System.out.println("Placement : " + temp.toString());

				if (temp != null) {
					cardPlacementList.add(temp);
					

				}
			}

		}
		
		for (Placement currentPlacement : cardPlacementList) {
			if (currentHandCards.contains(currentPlacement.getCard()))
					currentHandCards.remove(currentPlacement.getCard());
		}
		
		// System.out.println("Opponents placement" + cardPlacementList.toString());

		
		return new IntermediateMoveStatus(currentGameState,currentHandCards,cardPlacementList);

	}

	private IntermediateMoveStatus ownDiscardPilesPlacement(GameState currentGameState, List<Card> currentHandCards,
			Card topCardOnOwnAscendingDiscardPile, Card topCardOnOwnDescendingDiscardPile, int ownDiscardPileThreshold) {

		Card initialtopCardOnOwnAscendingDiscardPile = topCardOnOwnAscendingDiscardPile;
		Card intialTopCardOnOwnDescendingDiscardPile = topCardOnOwnDescendingDiscardPile;

		List<Placement> cardPlacementList = new ArrayList<Placement>();

		// Sorting the cards in Ascending order
		List<Card> ascedingOrderListOfCards = new ArrayList<Card>(currentHandCards);
		ascedingOrderListOfCards.sort((Card c1, Card c2) -> c1.getNumber() - c2.getNumber());

		int ownDiscardPilesAverage = Math.abs(ascedingOrderListOfCards.get(0).getNumber()
				+ currentHandCards.get(ascedingOrderListOfCards.size() - 1).getNumber()) / 2;

		// System.out.println("Own discard pile Average =" + ownDiscardPilesAverage);

		// For ascending order rule
		for (Card card : ascedingOrderListOfCards) {

			if (card.getNumber() > ownDiscardPilesAverage) {
				continue;
			}
			// System.out.println("Own discard pile Average =" + ownDiscardPilesAverage);
			// System.out.println("topCardOnOwnAscendingDiscardPile.getNumber() =" + topCardOnOwnAscendingDiscardPile.getNumber());
			
			// System.out.println("Processing for the card : " + card.getNumber());
			// System.out.print("Processing for the card : " + card.getNumber());
			// Rule for placement on own Ascending Discard pile
			if (card.getNumber() > topCardOnOwnAscendingDiscardPile.getNumber()
					&& card.getNumber() < (topCardOnOwnAscendingDiscardPile.getNumber() + ownDiscardPileThreshold)) {

				// System.out.println("Inside Own Ascending Discard pile");
				Placement temp = cardPlacementUpdator(currentGameState, card, CardPosition.OWN_ASCENDING_DISCARD_PILE);

				if (temp != null) {
					cardPlacementList.add(temp);
					topCardOnOwnAscendingDiscardPile = card;
					// System.out.println("Card being removed =" + card.getNumber());
				}
				
				/*
				if (( !initialtopCardOnOwnAscendingDiscardPile.equals(topCardOnOwnAscendingDiscardPile)) && initialtopCardOnOwnAscendingDiscardPile.getNumber() < topCardOnOwnAscendingDiscardPile.getNumber()) {
					
					break;
					System.out.println("Since current card is less than topOfStack check for backwards Trick ");
		// Test for Backwards Trick -- Start
		IntermediateMoveStatus2 backwardTrickResults = backwardsTrickValidator(currentGameState, ascedingOrderListOfCards,
				topCardOnOwnAscendingDiscardPile, topCardOnOwnDescendingDiscardPile);
		
		System.out.println("after the backwards trick ");
		currentGameState = backwardTrickResults.getGameState();
		ascedingOrderListOfCards = backwardTrickResults.getCurrentHandCards();
		topCardOnOwnAscendingDiscardPile = backwardTrickResults.getCurrentTopCardOnOwnAscendingDiscardPile();
		topCardOnOwnDescendingDiscardPile = backwardTrickResults.getCurrentTopCardOnOwnDescendingDiscardPile();
		cardPlacementList.addAll(backwardTrickResults.getListOfCardPlacements());
		System.out.println("after the updated values of backwards trick ");
		System.out.println("New cards list:" + ascedingOrderListOfCards.toString() + "Placements list: " + cardPlacementList.toString());
		
				}
				*/
			}
		}
		// Remove the card from the currentHandCards if it already has a placement
		
		for (Placement currentPlacement : cardPlacementList) {
			if (ascedingOrderListOfCards.contains(currentPlacement.getCard()))
					ascedingOrderListOfCards.remove(currentPlacement.getCard());
		}
		// System.out.println("Placement" + cardPlacementList.toString());
		
		// System.out.print("Modified curretHandCard = " + ascedingOrderListOfCards);
		
		

		// Recursively call the ownDiscardPilesPlacement
		// Test for Placement on Own Discard Pile -- Start
		

		// Sorting the cards in Descending order
				List<Card> descendingOrderListOfCards = ascedingOrderListOfCards;
				descendingOrderListOfCards.sort((Card c1, Card c2) -> c2.getNumber() - c1.getNumber());	
				
				// System.out.print("Descending order list  = " + descendingOrderListOfCards.toString());
		// For Descending order rule
		for (Card card : descendingOrderListOfCards) {

			if (card.getNumber() <= ownDiscardPilesAverage)
				continue;

			// System.out.println("Processing for the card : " + card.getNumber());
			// Rule for placement on own Descending Discard pile
			if (card.getNumber() < topCardOnOwnDescendingDiscardPile.getNumber()
					&& card.getNumber() > (topCardOnOwnDescendingDiscardPile.getNumber() - ownDiscardPileThreshold)) {

				System.out.println("Inside Own Ascending Discard pile");

				Placement temp = cardPlacementUpdator(currentGameState, card, CardPosition.OWN_DESCENDING_DISCARD_PILE);

				if (temp != null) {
					cardPlacementList.add(temp);
					topCardOnOwnDescendingDiscardPile = card;
					//descendingOrderListOfCards.remove(card);
				}
				
				/*
				if (( !intialTopCardOnOwnDescendingDiscardPile.equals(topCardOnOwnDescendingDiscardPile)) && intialTopCardOnOwnDescendingDiscardPile.getNumber() > topCardOnOwnDescendingDiscardPile.getNumber()) {
					
					// Test for Backwards Trick -- Start
					IntermediateMoveStatus2 backwardTrickResults = backwardsTrickValidator(currentGameState, ascedingOrderListOfCards,
							topCardOnOwnAscendingDiscardPile, topCardOnOwnDescendingDiscardPile);
					currentGameState = backwardTrickResults.getGameState();
					ascedingOrderListOfCards = backwardTrickResults.getCurrentHandCards();
					topCardOnOwnAscendingDiscardPile = backwardTrickResults.getCurrentTopCardOnOwnAscendingDiscardPile();
					topCardOnOwnDescendingDiscardPile = backwardTrickResults.getCurrentTopCardOnOwnDescendingDiscardPile();
					cardPlacementList.addAll(backwardTrickResults.getListOfCardPlacements());
				}
				*/

				// System.out.println("topCardOnOwnAscendingDiscardPile after iteration :"
				//		+ topCardOnOwnDescendingDiscardPile.getNumber());
			}

		}
		// System.out.println("Descending Placement" + cardPlacementList.toString());
		
		for (Placement currentPlacement : cardPlacementList) {
			if (descendingOrderListOfCards.contains(currentPlacement.getCard()))
					descendingOrderListOfCards.remove(currentPlacement.getCard());
		}

		// System.out.print("List of placements inside ownDiscardPilesPlacement = ");
		// cardPlacementList.forEach(System.out::println);

		return new IntermediateMoveStatus(currentGameState, descendingOrderListOfCards, cardPlacementList, topCardOnOwnAscendingDiscardPile,
				topCardOnOwnDescendingDiscardPile);

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

		// Test for Placement on Opponents Discard Pile -- Start
		IntermediateMoveStatus opponentDiscardPilePlacement = opponentCardPlacement(gameState, orderedCurrentHandCards,
				topCardOnOpponentAscendingDiscardPile, topCardOnOpponentDescendingDiscardPile);
		gameState = opponentDiscardPilePlacement.getGameState();
		orderedCurrentHandCards = opponentDiscardPilePlacement.getCurrentHandCards();
		placementsOfMove.addAll(opponentDiscardPilePlacement.getListOfCardPlacements());

		// Test for Placement on Opponents Discard Pile -- End
		
		// System.out.println("Hand cards: " + orderedCurrentHandCards.toString());
		
		
		int counter = 0;
		
		while(placementsOfMove.size() <=3) {
			
			// System.out.println("Loop counter:" + counter);
			// Test for Backwards Trick -- Start
			IntermediateMoveStatus backwardTrickResults = backwardsTrickValidator(gameState, orderedCurrentHandCards,
					topCardOnOwnAscendingDiscardPile, topCardOnOwnDescendingDiscardPile);
			gameState = backwardTrickResults.getGameState();
			orderedCurrentHandCards = backwardTrickResults.getCurrentHandCards();
			topCardOnOwnAscendingDiscardPile = backwardTrickResults.getCurrentTopCardOnOwnAscendingDiscardPile();
			topCardOnOwnDescendingDiscardPile = backwardTrickResults.getCurrentTopCardOnOwnDescendingDiscardPile();
			placementsOfMove.addAll(backwardTrickResults.getListOfCardPlacements());

			// Test for Backwards Trick -- End
			
			// System.out.println("After backwards trick validator:");
			
			// Test for Placement on Own Discard Pile -- Start
			IntermediateMoveStatus ownDiscardPilePlacement = ownDiscardPilesPlacement(gameState, orderedCurrentHandCards,
					topCardOnOwnAscendingDiscardPile, topCardOnOwnDescendingDiscardPile, ownDiscardPileThreshold);
			gameState = ownDiscardPilePlacement.getGameState();
			orderedCurrentHandCards = ownDiscardPilePlacement.currentHandCards;
			topCardOnOwnAscendingDiscardPile = ownDiscardPilePlacement.getCurrentTopCardOnOwnAscendingDiscardPile();
			topCardOnOwnDescendingDiscardPile = ownDiscardPilePlacement.getCurrentTopCardOnOwnDescendingDiscardPile();
			placementsOfMove.addAll(ownDiscardPilePlacement.getListOfCardPlacements());
			
			counter ++;
			
			if(placementsOfMove.size() <= 2) {
				ownDiscardPileThreshold = ownDiscardPileThreshold + ownDiscardPileIncreamentFactor;
				System.out.println("DiscardPileThreshold :" +  ownDiscardPileThreshold);
			}
			
			if (counter >= 5) break;
			
		}
		
		

		System.out.print(" Placements= ");
		placementsOfMove.forEach(System.out::print);

		return placementsOfMove;

	}

	@Override
	public Move computeMove(GameState gameState) {
		GameState currentGameState = gameState;
		boolean placedOnOpponentsPiles = false;

		List<Placement> placementsOfMove = new ArrayList<Placement>();

		System.out.println("Player playing the game : " + getName());

		System.out.println("Game state is : " + currentGameState.getHandCards().toString());
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
