package de.upb.mlseminar.informedplayer;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.isml.thegamef2f.engine.CardPosition;
import de.upb.isml.thegamef2f.engine.Placement;
import de.upb.isml.thegamef2f.engine.board.Card;
import de.upb.mlseminar.utilities.IntermediateGameState;

/**
 * This class is the base of this game implementation,
 * It is based on the Rule State Engine based on the certain thresholds,
 * which tries to simulate the game play by a human player.
 *
 * @author Guru Prasad Savandaiah
 *
 */
public class InformedPlayerCore {
	
	private String name;
	private int ownDiscardPileThreshold ;
	private int ownDiscardPileIncreamentFactor;
	private int opponentDiscardPileThreshold;
	private int opponentDiscardPileIncreamentFactor;
	private int minNumOfPlacements;
	
	private static final Logger logger = LoggerFactory.getLogger(InformedPlayerCore.class);
	
	public InformedPlayerCore() {
		super();
	}
	
	public InformedPlayerCore(String name, int ownDiscardPileThreshold, int ownDiscardPileIncreamentFactor,
			int opponentDiscardPileThreshold, int opponentDiscardPileIncreamentFactor, int minNumOfPlacements) {
		super();
		this.name = name;
		this.ownDiscardPileThreshold = ownDiscardPileThreshold;
		this.ownDiscardPileIncreamentFactor = ownDiscardPileIncreamentFactor;
		this.opponentDiscardPileThreshold = opponentDiscardPileThreshold;
		this.opponentDiscardPileIncreamentFactor = opponentDiscardPileIncreamentFactor;
		this.minNumOfPlacements = minNumOfPlacements;
	}
	
	public IntermediateGameState getCardPlacement(IntermediateGameState currentGameState) {

		InformedPlayerCore core = new InformedPlayerCore();
		
		IntermediateGameState gameStateAfterPlacement = currentGameState;
		logger.debug("Start of the method : getCardPlacement");
		try {
			
		
		List<Placement> placementsOfMove = currentGameState.getListOfCardPlacements();

		// Fetching the details of the current game state
		Card topCardOnOwnAscendingDiscardPile = currentGameState.getCurrentTopCardOnOwnAscendingDiscardPile();
		Card topCardOnOwnDescendingDiscardPile = currentGameState.getCurrentTopCardOnOwnDescendingDiscardPile();

		Card topCardOnOpponentAscendingDiscardPile = currentGameState.getCurrentTopCardOnOpponentAscendingDiscardPile();
		Card topCardOnOpponentDescendingDiscardPile = currentGameState.getCurrentTopCardOnOpponentDescendingDiscardPile();

		logger.debug(
				"Own game state : topCardOnOwnAscendingDiscardPile = " + topCardOnOwnAscendingDiscardPile.getNumber()
						+ " , topCardOnOwnDescendingDiscardPile = " + topCardOnOwnDescendingDiscardPile.getNumber());
		logger.debug("Opponents game state : topCardOnOpponentAscendingDiscardPile = "
				+ topCardOnOpponentAscendingDiscardPile.getNumber()
				+ " , topCardOnOpponentDescendingDiscardPile = "
				+ topCardOnOpponentDescendingDiscardPile.getNumber());
		logger.debug("Intial Own DiscardPileThreshold : " + ownDiscardPileThreshold
				+ " And Opponent Discard Pile Threshold : " + opponentDiscardPileThreshold);

		// Copy into a different list as the source list is an immutable list
		List<Card> orderedCurrentHandCards = new ArrayList<Card>(currentGameState.getCurrentHandCards());

		// Sorting the cards in Ascending order
		currentGameState.getCurrentHandCards().sort((Card c1, Card c2) -> c1.getNumber() - c2.getNumber());

		//// Real Logic starts here.

		// Test for Placement on Opponents Discard Pile -- Start
		gameStateAfterPlacement = core.opponentCardPlacement(gameStateAfterPlacement, opponentDiscardPileThreshold);

		//gameStateAfterPlacement = opponentCardPlacement(currentGameState, currentHandCards, currentTopCardOnOpponentAscedingDiscardPile, currentTopCardOnOpponentDescendingDiscardPile)
		
		// Test for Placement on Opponents Discard Pile -- End

		int counter = 0;

		while (placementsOfMove.size() <= minNumOfPlacements) {

			// Test for Placement on Opponents Discard Pile -- Start
			// gameStateAfterPlacement = core.opponentCardPlacement(gameStateAfterPlacement, opponentDiscardPileThreshold);
			
			// Test for Backwards Trick -- Start
			gameStateAfterPlacement = core.backwardsTrickValidator(currentGameState);
			// Test for Backwards Trick -- End

			// Test for Placement on Own Discard Pile -- Start
			gameStateAfterPlacement = core.ownDiscardPilesPlacement(currentGameState, ownDiscardPileThreshold);
			counter++;

			if (placementsOfMove.size() <= 2) {
				ownDiscardPileThreshold = ownDiscardPileThreshold + ownDiscardPileIncreamentFactor;
				// opponentDiscardPileThreshold = opponentDiscardPileThreshold + opponentDiscardPileIncreamentFactor;
				logger.debug("Updated Own DiscardPileThreshold :" + ownDiscardPileThreshold);
			}

			if (counter >= 6)
				break;
		}

		gameStateAfterPlacement.setListOfCardPlacements(placementsOfMove);
		
		logger.debug("End of the method : getCardPlacement");
		logger.debug("===================================================================================");

		return gameStateAfterPlacement;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.toString());
			System.out.println(e);
		}
		return gameStateAfterPlacement;
	}

	private Placement cardPlacementUpdator(IntermediateGameState currentGameState, Card card, CardPosition position) {

		boolean placedOnOppositePiles = false;
		Placement testCardPlacement = new Placement(card, position);
		Placement finalCardPlacement = null;

		if (isPlacementValid(testCardPlacement, currentGameState, !placedOnOppositePiles)) {
			finalCardPlacement = testCardPlacement;
			//currentGameState = computeNewGameStateAfterPlacement(currentGameState, testCardPlacement);
		}

		if (testCardPlacement.getPosition() == CardPosition.OPPONENTS_ASCENDING_DISCARD_PILE
				|| testCardPlacement.getPosition() == CardPosition.OPPONENTS_DESCENDING_DISCARD_PILE) {
			placedOnOppositePiles = true;

			if (isPlacementValid(testCardPlacement, currentGameState, placedOnOppositePiles)) {
				finalCardPlacement = testCardPlacement;
				//currentGameState = computeNewGameStateAfterPlacement(currentGameState, testCardPlacement);
			}

		}

		return finalCardPlacement;
	}

	IntermediateGameState backwardsTrickValidator(IntermediateGameState currentGameState) {
		IntermediateGameState gameStateAfterProcedureCall = currentGameState;
		
		logger.debug("Start of the method : backwardsTrickValidator");
		List<Placement> cardPlacementList = currentGameState.getListOfCardPlacements();

		ListIterator<Card> backwardCardIterator = currentGameState.getCurrentHandCards().listIterator();

		try {
			// Test for Backwards Trick
			while (backwardCardIterator.hasNext()) {
				Card card = backwardCardIterator.next();

				logger.debug("Processing for card" + card);

				// Test if a current card is 10 lesser than topCardOnOwnAscendingDiscardPile
				if (card.is10SmallerThan(currentGameState.getCurrentTopCardOnOwnAscendingDiscardPile())) {
					logger.debug("The current card " + card + " is 10 lesser than the current top card "
							+ currentGameState.getCurrentTopCardOnOwnAscendingDiscardPile() + " on the Ascending order file");

					Placement temp = cardPlacementUpdator(currentGameState, card,
							CardPosition.OWN_ASCENDING_DISCARD_PILE);

					if (temp != null) {
						cardPlacementList.add(temp);
						gameStateAfterProcedureCall.setCurrentTopCardOnOwnAscendingDiscardPile(card);
						backwardCardIterator.remove();
					}

				}

				// Test if a current card is 10 greater than topCardOnOwnDescendingDiscardPile
				if (card.is10LargerThan(currentGameState.getCurrentTopCardOnOwnDescendingDiscardPile())) {
					logger.debug("The current card " + card + " is 10 greater than the current top card "
							+ currentGameState.getCurrentTopCardOnOwnDescendingDiscardPile() + " on the Descending order file");
					Placement temp = cardPlacementUpdator(currentGameState, card,
							CardPosition.OWN_DESCENDING_DISCARD_PILE);

					if (temp != null) {
						cardPlacementList.add(temp);
						gameStateAfterProcedureCall.setCurrentTopCardOnOwnDescendingDiscardPile(card);
						backwardCardIterator.remove();
					}

				}

			}
			
			gameStateAfterProcedureCall.setListOfCardPlacements(cardPlacementList);
			logger.debug("CardPlacement list at the end of backwardsTrickValidator" + gameStateAfterProcedureCall.getListOfCardPlacements().toString());
			logger.debug("End of the method : backwardsTrickValidator");
			logger.debug("===================================================================================");
			
			return gameStateAfterProcedureCall;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.toString());
			System.out.println(e);
			
			logger.debug("End of the method : backwardsTrickValidator");
			logger.debug("CardPlacement list at the end of backwardsTrickValidator" + gameStateAfterProcedureCall.getListOfCardPlacements().toString());
			logger.debug("===================================================================================");

			return gameStateAfterProcedureCall;
		}



	}

	IntermediateGameState opponentCardPlacement(IntermediateGameState currentGameState, int opponentDiscardPileThreshold) {

		logger.debug("Start of the method : opponentCardPlacement");

		IntermediateGameState gameStateAfterProcedureCall = currentGameState;
		
		List<Placement> cardPlacementList = currentGameState.getListOfCardPlacements();
		Card bestCandidate = null;
		int leastDifference = 99999;
		int ascOrDecFlag = -1; // 1 for ascending, 2 for descending

		ListIterator<Card> opponentCardIterator = currentGameState.getCurrentHandCards().listIterator();
		
		try {

			while (opponentCardIterator.hasNext()) {
				Card card = opponentCardIterator.next();

				logger.debug("Processing for card" + card);
				int diffBtwOpponentAscendingDiscardPile = currentGameState.getCurrentTopCardOnOpponentAscendingDiscardPile().getNumber()
						- card.getNumber();
				int diffBtwOpponentDescendingDiscardPile = card.getNumber()
						- currentGameState.getCurrentTopCardOnOpponentDescendingDiscardPile().getNumber();

				int currentMinDifference = 99999;
				Card currentBestCandidate = null;
				int localAscOrDecFlag = -1;

				// Rule for placement on Opponents Ascending Discard pile
				logger.debug("Checking the rule for placement on Opponents Ascending Discard pile");

				if (diffBtwOpponentAscendingDiscardPile > 0
						&& card.getNumber() < currentGameState.getCurrentTopCardOnOpponentAscendingDiscardPile().getNumber()
						&& card.getNumber() > (currentGameState.getCurrentTopCardOnOpponentAscendingDiscardPile().getNumber()
								- opponentDiscardPileThreshold)) {

					if (diffBtwOpponentAscendingDiscardPile < currentMinDifference) {
						currentMinDifference = diffBtwOpponentAscendingDiscardPile;
						currentBestCandidate = card;
						localAscOrDecFlag = 1;
						logger.debug(
								"Card " + card + "Can be considered to be placed on Opponents Ascending Discard pile");
					}

				}

				// Rule for placement on own Descending Discard pile
				logger.debug("Checking the rule for placement on Opponents Descending Discard pile");
				if (diffBtwOpponentDescendingDiscardPile > 0
						&& card.getNumber() > currentGameState.getCurrentTopCardOnOpponentDescendingDiscardPile().getNumber()
						&& card.getNumber() < (currentGameState.getCurrentTopCardOnOpponentDescendingDiscardPile().getNumber()
								+ opponentDiscardPileThreshold)) {

					if (diffBtwOpponentDescendingDiscardPile < currentMinDifference) {
						currentMinDifference = diffBtwOpponentDescendingDiscardPile;
						currentBestCandidate = card;
						localAscOrDecFlag = 2;
						logger.debug(
								"Card " + card + "Can be considered to be placed on Opponents Descending Discard pile");
					}

				}

				// Check for the best card which can be placed on opponents discard pile as only
				// one card can be placed at most in one turn
				if (currentBestCandidate != null && currentMinDifference < leastDifference && localAscOrDecFlag > 0) {

					logger.debug(
							"The card" + card + "Is the current best card to be placed on the oppponents discard pile");
					leastDifference = currentMinDifference;
					bestCandidate = currentBestCandidate;
					ascOrDecFlag = localAscOrDecFlag;
				}

			}

			// Determine the best candidate which can be placed on the opponent discard pile
			if (bestCandidate != null && leastDifference > 0 && ascOrDecFlag > 0) {
				if (ascOrDecFlag == 1) {

					logger.debug("The card" + bestCandidate
							+ " Is the best candidate to be placced on Opponent Ascending Order Discard Pile");
					Placement temp = cardPlacementUpdator(currentGameState, bestCandidate,
							CardPosition.OPPONENTS_ASCENDING_DISCARD_PILE);

					if (temp != null) {
						cardPlacementList.add(temp);
						gameStateAfterProcedureCall.setCurrentTopCardOnOpponentAscendingDiscardPile(bestCandidate);
						opponentCardIterator.remove();

					}

				} else if (ascOrDecFlag == 2) {

					logger.debug("The card" + bestCandidate
							+ " Is the best candidate to be placced on Opponent Descending Order Discard Pile");
					Placement temp = cardPlacementUpdator(currentGameState, bestCandidate,
							CardPosition.OPPONENTS_DESCENDING_DISCARD_PILE);

					if (temp != null) {
						cardPlacementList.add(temp);
						gameStateAfterProcedureCall.setCurrentTopCardOnOpponentDescendingDiscardPile(bestCandidate);
						opponentCardIterator.remove();
					}
				}

			}
			gameStateAfterProcedureCall.setListOfCardPlacements(cardPlacementList);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.toString());
			System.out.println(e);
		}

		logger.debug("End of the method : opponentCardPlacement");
		logger.debug("===================================================================================");

		return gameStateAfterProcedureCall;

	}

//
//	IntermediateGameState ownDiscardPilesPlacement(IntermediateGameState currentGameState,
//			int ownDiscardPileThreshold) {
//
//		logger.debug("Start of the method : ownDiscardPilesPlacement");
//		IntermediateGameState gameStateAfterProcedureCall = currentGameState;
//		
//		// Local variables for placement and hand-cards
//		List<Placement> cardPlacementList = currentGameState.getListOfCardPlacements();
//		List<Card> currentHandCards = currentGameState.getCurrentHandCards();
//
//
//		currentGameState.getCurrentTopCardOnOwnAscendingDiscardPile();
//		currentGameState.getCurrentTopCardOnOwnDescendingDiscardPile();
//		
//		// Check if the current hand cards are empty 
//		if (currentHandCards.isEmpty()) {
//			return gameStateAfterProcedureCall;
//		}
//		
//		try {
//
//			// Sorting the cards in Ascending order
//			List<Card> ascedingOrderListOfCards = new ArrayList<Card>(currentGameState.getCurrentHandCards());
//			ascedingOrderListOfCards.sort((Card c1, Card c2) -> c1.getNumber() - c2.getNumber());
//
//			// Logic for checking the discard piles average
//
//			int ownDiscardPilesAverage = Math.abs(
//					currentHandCards.get(0).getNumber() + currentHandCards.get(currentHandCards.size() - 1).getNumber())
//					/ 2;
//
//			/*
//			 * ownDiscardPilesAverage =
//			 * Math.abs(topCardOnOwnAscendingDiscardPile.getNumber() +
//			 * topCardOnOwnDescendingDiscardPile.getNumber()) / 2;
//			 */
//
//			logger.debug("Own discard pile Average =" + ownDiscardPilesAverage);
//
//			// For ascending order rule
//			List<Card> ascendingFilteredList = ascedingOrderListOfCards.stream()
//					.filter(s -> s.getNumber() < ownDiscardPilesAverage).collect(Collectors.toList());
//
//			List<Card> descendingFilteredList = ascedingOrderListOfCards.stream()
//					.filter(s -> s.getNumber() >= ownDiscardPilesAverage).collect(Collectors.toList());
//			descendingFilteredList.sort((Card c1, Card c2) -> c2.getNumber() - c1.getNumber());
//
//			logger.debug("asceding order cards =" + ascendingFilteredList.toString());
//			logger.debug("Descending order cards =" + descendingFilteredList.toString());
//
//			ListIterator<Card> ascendingListIterator = ascendingFilteredList.listIterator();
//
//			// Rule for placement on own Ascending Discard pile
//			while (ascendingListIterator.hasNext()) {
//
//				logger.debug("Checking for placing the card on the Ascending Order Pile");
//
//				Card cardProcessed;
//				cardProcessed = ascendingListIterator.next();
//
//				// Rule for placement on own Ascending Discard pile
//				if (cardProcessed.getNumber() > currentGameState.getCurrentTopCardOnOwnAscendingDiscardPile().getNumber() && cardProcessed
//						.getNumber() < (currentGameState.getCurrentTopCardOnOwnAscendingDiscardPile().getNumber() + ownDiscardPileThreshold)) {
//
//					logger.debug("Inside Own Ascending Discard pile");
//					Placement temp = cardPlacementUpdator(currentGameState, cardProcessed,
//							CardPosition.OWN_ASCENDING_DISCARD_PILE);
//
//					if (temp != null) {
//						cardPlacementList.add(temp);
//						gameStateAfterProcedureCall.setCurrentTopCardOnOwnAscendingDiscardPile(cardProcessed);
//						ascendingListIterator.remove();
//					}
//					
//
//				}
//
//			}
//
//			logger.debug("#####################################################################");
//			// Rule for placement on own Descending Discard pile
//			ListIterator<Card> descendingListIterator = descendingFilteredList.listIterator();
//			while (descendingListIterator.hasNext()) {
//
//				logger.debug("Checking for placing the card on the Descending Order Pile");
//
//				Card cardProcessed;
//				cardProcessed = descendingListIterator.next();
//
//				// Rule for placement on own Descending Discard pile
//				if (cardProcessed.getNumber() < currentGameState.getCurrentTopCardOnOwnDescendingDiscardPile().getNumber() && cardProcessed
//						.getNumber() > (currentGameState.getCurrentTopCardOnOwnDescendingDiscardPile().getNumber() - ownDiscardPileThreshold)) {
//
//					logger.debug("Inside Own Descending Discard pile");
//
//					Placement temp = cardPlacementUpdator(currentGameState, cardProcessed,
//							CardPosition.OWN_DESCENDING_DISCARD_PILE);
//
//					if (temp != null) {
//						cardPlacementList.add(temp);
//						gameStateAfterProcedureCall.setCurrentTopCardOnOwnDescendingDiscardPile(cardProcessed);
//						descendingListIterator.remove();
//						// descendingOrderListOfCards.remove(card);
//					}
//
//				}
//
//			}
//
//			List<Card> remainCardListAfterProcessing = new ArrayList<Card>(ascendingFilteredList);
//			remainCardListAfterProcessing.addAll(descendingFilteredList);
//			
//			gameStateAfterProcedureCall.setCurrentHandCards(remainCardListAfterProcessing);
//			gameStateAfterProcedureCall.setListOfCardPlacements(cardPlacementList);
//
//			logger.debug("End of the method : opponentCardPlacement");
//			logger.debug("===================================================================================");
//
//			return gameStateAfterProcedureCall;
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			logger.error(e.toString());
//			System.out.println(e);
//		}
//
//		return gameStateAfterProcedureCall;
//
//	}
//
	
	IntermediateGameState ownDiscardPilesPlacement(IntermediateGameState currentGameState,
			int ownDiscardPileThreshold) {

		logger.debug("Start of the method : ownDiscardPilesPlacement");
		IntermediateGameState gameStateAfterProcedureCall = currentGameState;
		
		// Local variables for placement and hand-cards
		List<Placement> cardPlacementList = currentGameState.getListOfCardPlacements();
		List<Card> currentHandCards = currentGameState.getCurrentHandCards();
		List<Integer> cardNumbersInPlacementList = new ArrayList<Integer>();

		currentGameState.getCurrentTopCardOnOwnAscendingDiscardPile();
		currentGameState.getCurrentTopCardOnOwnDescendingDiscardPile();
		
		// Check if the current hand cards are empty 
		if (currentHandCards.isEmpty()) {
			return gameStateAfterProcedureCall;
		}
		
		try {

			// Sorting the cards in Ascending order
			List<Card> ascedingOrderListOfCards = new ArrayList<Card>(currentGameState.getCurrentHandCards());
			ascedingOrderListOfCards.sort((Card c1, Card c2) -> c1.getNumber() - c2.getNumber());

			// Logic for checking the discard piles average

			int ownDiscardPilesAverage = Math.abs(
					currentHandCards.get(0).getNumber() + currentHandCards.get(currentHandCards.size() - 1).getNumber())
					/ 2;

			/*
			 * ownDiscardPilesAverage =
			 * Math.abs(topCardOnOwnAscendingDiscardPile.getNumber() +
			 * topCardOnOwnDescendingDiscardPile.getNumber()) / 2;
			 */

			logger.debug("Own discard pile Average =" + ownDiscardPilesAverage);

			// For ascending order rule
			List<Card> ascendingFilteredList = ascedingOrderListOfCards.stream()
					.filter(s -> s.getNumber() < ownDiscardPilesAverage).collect(Collectors.toList());

			List<Card> descendingFilteredList = ascedingOrderListOfCards.stream()
					.filter(s -> s.getNumber() >= ownDiscardPilesAverage).collect(Collectors.toList());
			descendingFilteredList.sort((Card c1, Card c2) -> c2.getNumber() - c1.getNumber());

			logger.debug("asceding order cards =" + ascendingFilteredList.toString());
			logger.debug("Descending order cards =" + descendingFilteredList.toString());

			ListIterator<Card> ascendingListIterator = ascendingFilteredList.listIterator();
			
			for (int i = 0 ; i < ascendingFilteredList.size(); i++) {
				logger.debug("Checking for placing the card on the Ascending Order Pile");
				
				Card cardProcessed = ascendingFilteredList.get(i);
				logger.debug("Card Being processed" + cardProcessed);
				// Rule for placement on own Ascending Discard pile
				if ((cardProcessed.getNumber() > gameStateAfterProcedureCall.getCurrentTopCardOnOwnAscendingDiscardPile().getNumber() && cardProcessed
						.getNumber() < (gameStateAfterProcedureCall.getCurrentTopCardOnOwnAscendingDiscardPile().getNumber() + ownDiscardPileThreshold)) || (cardProcessed.is10SmallerThan(gameStateAfterProcedureCall.getCurrentTopCardOnOwnAscendingDiscardPile()))) {

					
					logger.debug("Inside Own Ascending Discard pile");
					Placement temp = cardPlacementUpdator(currentGameState, cardProcessed,
							CardPosition.OWN_ASCENDING_DISCARD_PILE);

					// If the card is already in the placement list skip
					logger.debug("Card numbers in cardNumbersInPlacementList : " + cardNumbersInPlacementList.toString());
					logger.debug("Does cardNumbersInPlacementList contain the element : " + cardNumbersInPlacementList.contains(cardProcessed.getNumber()));

					if(cardNumbersInPlacementList.contains(cardProcessed.getNumber())) continue;
					
					if (temp != null) {
						cardPlacementList.add(temp);
						cardNumbersInPlacementList.add(cardProcessed.getNumber());
						gameStateAfterProcedureCall.setCurrentTopCardOnOwnAscendingDiscardPile(cardProcessed);
						//ascendingListIterator.remove();
					}
					
					// Test for backwards trick after placing each card
					for(int j=0; j < i; j++) {
						Card temp10 = ascendingFilteredList.get(j);
						if (temp10.is10SmallerThan(cardProcessed) &&  cardNumbersInPlacementList.contains(cardProcessed.getNumber()) && !cardNumbersInPlacementList.contains(temp10.getNumber())) {
							logger.debug("The current card " + temp10 + " is 10 lesser than the current top card "
								+ cardProcessed + " on the Ascending order file");
							i = j - 1 ;
							logger.debug("The loop index for i is updated value" + i);
							
							logger.debug("Ascending order list : " + ascedingOrderListOfCards.toString());
							break;
						}						
					}

				}				
				
			}

			logger.debug("#####################################################################");
			
			// Rule for placement on own Descending Discard pile
			for (int i = 0 ; i < descendingFilteredList.size(); i++) {
				Card cardProcessed = descendingFilteredList.get(i);
				
				logger.debug("getCurrentTopCardOnOwnDescendingDiscardPile()" + gameStateAfterProcedureCall.getCurrentTopCardOnOwnDescendingDiscardPile().getNumber());
				// Rule for placement on own Descending Discard pile
				if ((cardProcessed.getNumber() < gameStateAfterProcedureCall.getCurrentTopCardOnOwnDescendingDiscardPile().getNumber() && cardProcessed
						.getNumber() > (gameStateAfterProcedureCall.getCurrentTopCardOnOwnDescendingDiscardPile().getNumber() - ownDiscardPileThreshold)) || (cardProcessed.is10LargerThan(gameStateAfterProcedureCall.getCurrentTopCardOnOwnDescendingDiscardPile()))) {
					
					logger.debug("Inside Own Descending Discard pile");

					Placement temp = cardPlacementUpdator(currentGameState, cardProcessed,
							CardPosition.OWN_DESCENDING_DISCARD_PILE);

					logger.debug("card processed" + cardProcessed);
					logger.debug("The placement list before element exists test" + cardPlacementList.toString());
					logger.debug("Does the list contain the placement" + cardPlacementList.contains(temp));
					
					// If the card is already in the placement list skip
					logger.debug("Card numbers in cardNumbersInPlacementList : " + cardNumbersInPlacementList.toString());
					logger.debug("Does cardNumbersInPlacementList contain the element : " + cardNumbersInPlacementList.contains(cardProcessed.getNumber()));

					if(cardNumbersInPlacementList.contains(cardProcessed.getNumber())) continue;
					
					
					if (temp != null ) {
						cardPlacementList.add(temp);
						cardNumbersInPlacementList.add(cardProcessed.getNumber());
						gameStateAfterProcedureCall.setCurrentTopCardOnOwnDescendingDiscardPile(cardProcessed);
						// descendingOrderListOfCards.remove(card);
					}
					
					// Test for backwards trick after placing each card
					for(int j=0; j < i; j++) {
						Card temp10 = descendingFilteredList.get(j);
						logger.debug("The card placement before the placement" + cardPlacementList.toString());
						logger.debug("Top Card on descending order pile" + gameStateAfterProcedureCall.getCurrentTopCardOnOwnDescendingDiscardPile().getNumber() );
						if (temp10.is10SmallerThan(cardProcessed) &&  cardNumbersInPlacementList.contains(cardProcessed.getNumber()) && !cardNumbersInPlacementList.contains(temp10.getNumber())) {
							logger.debug("The current card " + temp10 + " is 10 greater than the current top card "
								+ cardProcessed + " on the Descending order file");
							i = j - 1 ;
							logger.debug("The loop index for i is updated value in Descending order List" + i);
							logger.debug("Descending order list : " + descendingFilteredList.toString());
							break;
						}						
					}

				}
				
			}
			// logger.info("Placement list after descending placement" + cardPlacementList.toString());
			

			List<Card> remainCardListAfterProcessing = new ArrayList<Card>(ascendingFilteredList);
			remainCardListAfterProcessing.addAll(descendingFilteredList);
			
			for (Placement currentPlacement : cardPlacementList) {
				if (remainCardListAfterProcessing.contains(currentPlacement.getCard()))
					remainCardListAfterProcessing.remove(currentPlacement.getCard());
			}

			
			gameStateAfterProcedureCall.setCurrentHandCards(remainCardListAfterProcessing);
			gameStateAfterProcedureCall.setListOfCardPlacements(cardPlacementList);

			logger.debug("End of the method : opponentCardPlacement");
			logger.debug("===================================================================================");

			return gameStateAfterProcedureCall;

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.toString());
			System.out.println(e);
		}

		return gameStateAfterProcedureCall;

	}
	
	private boolean isPlacementValid(Placement placement, IntermediateGameState currentGameState, boolean placingOnOpponentPilesAllowed) {
		switch (placement.getPosition()) {
		case OPPONENTS_ASCENDING_DISCARD_PILE:
			return placingOnOpponentPilesAllowed
					? canPlaceCardOnOpponentsAscendingDiscardPile(placement.getCard(), currentGameState)
					: false;
		case OPPONENTS_DESCENDING_DISCARD_PILE:
			return placingOnOpponentPilesAllowed
					? canPlaceCardOnOpponentsDescendingDiscardPile(placement.getCard(), currentGameState)
					: false;
		case OWN_ASCENDING_DISCARD_PILE:
			return canPlaceCardOnOwnAscendingDiscardPile(placement.getCard(), currentGameState);
		case OWN_DESCENDING_DISCARD_PILE:
			return canPlaceCardOnOwnDescendingDiscardPile(placement.getCard(), currentGameState);
		}
		return false;
	}

	private boolean canPlaceCardOnOwnAscendingDiscardPile(Card card, IntermediateGameState currentGameState) {
		return currentGameState.getCurrentTopCardOnOwnAscendingDiscardPile().isSmallerThan(card)
				|| currentGameState.getCurrentTopCardOnOwnAscendingDiscardPile().is10LargerThan(card);
	}

	private boolean canPlaceCardOnOwnDescendingDiscardPile(Card card, IntermediateGameState currentGameState) {
		return card.isSmallerThan(currentGameState.getCurrentTopCardOnOwnDescendingDiscardPile())
				|| card.is10LargerThan(currentGameState.getCurrentTopCardOnOwnDescendingDiscardPile());
	}

	private boolean canPlaceCardOnOpponentsAscendingDiscardPile(Card card, IntermediateGameState currentGameState) {
		return card.isSmallerThan(currentGameState.getCurrentTopCardOnOpponentAscendingDiscardPile());
	}

	private boolean canPlaceCardOnOpponentsDescendingDiscardPile(Card card, IntermediateGameState currentGameState) {
		return currentGameState.getCurrentTopCardOnOpponentDescendingDiscardPile().isSmallerThan(card);
	}

}
