package de.upb.mlseminar.informedplayer;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.isml.thegamef2f.engine.CardPosition;
import de.upb.isml.thegamef2f.engine.GameState;
import de.upb.isml.thegamef2f.engine.Move;
import de.upb.isml.thegamef2f.engine.Placement;
import de.upb.isml.thegamef2f.engine.board.Card;
import de.upb.isml.thegamef2f.engine.player.Player;
import de.upb.mlseminar.utilities.IntermediateGameState;

public class InformedPlayerInstance {

	private Random random;
	private String name;

	private int ownDiscardPileThreshold ;
	private int ownDiscardPileIncreamentFactor;
	private int opponentDiscardPileThreshold;
	private int minNumOfPlacements;

	private static final Logger logger = LoggerFactory.getLogger(InformedSingleInstancePlayer.class);

	public InformedPlayerInstance(String name, int ownDiscardPileThreshold, int ownDiscardPileIncreamentFactor,
			int opponentDiscardPileThreshold, int minNumOfPlacements) {
		super();
		this.name = name;
		this.ownDiscardPileThreshold = ownDiscardPileThreshold;
		this.ownDiscardPileIncreamentFactor = ownDiscardPileIncreamentFactor;
		this.opponentDiscardPileThreshold = opponentDiscardPileThreshold;
		this.minNumOfPlacements = minNumOfPlacements;
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

	private IntermediateGameState backwardsTrickValidator(IntermediateGameState currentGameState) {
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
					logger.debug("The current card " + card + "is 10 greater than the current top card "
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
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.toString());
			System.out.println(e);
		}

		logger.debug("End of the method : backwardsTrickValidator");
		logger.debug("===================================================================================");

		return gameStateAfterProcedureCall;

	}

	private IntermediateGameState opponentCardPlacement(IntermediateGameState currentGameState) {

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
				logger.debug("Checking the rule for placement on own Descending Discard pile");
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

//	private IntermediateGameState ownDiscardPilesPlacement(IntermediateGameState currentGameState,
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
	
	private IntermediateGameState ownDiscardPilesPlacement(IntermediateGameState currentGameState,
			int ownDiscardPileThreshold) {

		logger.debug("Start of the method : ownDiscardPilesPlacement");
		IntermediateGameState gameStateAfterProcedureCall = currentGameState;
		
		// Local variables for placement and hand-cards
		List<Placement> cardPlacementList = currentGameState.getListOfCardPlacements();
		List<Card> currentHandCards = currentGameState.getCurrentHandCards();


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

			// Rule for placement on own Ascending Discard pile
			while (ascendingListIterator.hasNext()) {

				logger.debug("Checking for placing the card on the Ascending Order Pile");
				Card previousCard;
				Card cardProcessed;

				if (ascendingListIterator.hasPrevious()) {
					// System.out.println("previous check");
					previousCard = ascendingListIterator.previous();
					if (ascendingListIterator.hasNext()) {
						ascendingListIterator.next();
						cardProcessed = ascendingListIterator.next();
					} else {
						cardProcessed = previousCard;
					}

				} else {
					cardProcessed = ascendingListIterator.next();
					previousCard = cardProcessed;
				}

				logger.debug("Current card = " + cardProcessed + " Previous card = " + previousCard);

				// Rule for placement on own Ascending Discard pile
				if (cardProcessed.getNumber() > currentGameState.getCurrentTopCardOnOwnAscendingDiscardPile().getNumber() && cardProcessed
						.getNumber() < (currentGameState.getCurrentTopCardOnOwnAscendingDiscardPile().getNumber() + ownDiscardPileThreshold)) {

					logger.debug("Inside Own Ascending Discard pile");
					Placement temp = cardPlacementUpdator(currentGameState, cardProcessed,
							CardPosition.OWN_ASCENDING_DISCARD_PILE);

					if (temp != null) {
						cardPlacementList.add(temp);
						gameStateAfterProcedureCall.setCurrentTopCardOnOwnAscendingDiscardPile(cardProcessed);
						ascendingListIterator.remove();
					}
					if (previousCard.is10SmallerThan(currentGameState.getCurrentTopCardOnOwnAscendingDiscardPile())) {
						logger.debug("The current card " + previousCard + " is 10 lesser than the current top card "
								+ currentGameState.getCurrentTopCardOnOwnAscendingDiscardPile() + " on the Ascending order file");

						temp = cardPlacementUpdator(currentGameState, previousCard,
								CardPosition.OWN_ASCENDING_DISCARD_PILE);

						if (temp != null) {
							cardPlacementList.add(temp);
							gameStateAfterProcedureCall.setCurrentTopCardOnOwnAscendingDiscardPile(previousCard);
							ascendingFilteredList.remove(previousCard);
						}

					}

				}

			}

			logger.debug("#####################################################################");
			// Rule for placement on own Descending Discard pile
			ListIterator<Card> descendingListIterator = descendingFilteredList.listIterator();
			while (descendingListIterator.hasNext()) {

				logger.debug("Checking for placing the card on the Descending Order Pile");

				Card previousCard;
				Card cardProcessed;

				if (descendingListIterator.hasPrevious()) {
					previousCard = descendingListIterator.previous();
					if (descendingListIterator.hasNext()) {
						descendingListIterator.next();
						cardProcessed = descendingListIterator.next();
					} else {
						cardProcessed = previousCard;
					}

				} else {
					cardProcessed = descendingListIterator.next();
					previousCard = cardProcessed;
				}
				logger.debug("Current card = " + cardProcessed + " Previous card = " + previousCard);

				// Rule for placement on own Descending Discard pile
				if (cardProcessed.getNumber() < currentGameState.getCurrentTopCardOnOwnDescendingDiscardPile().getNumber() && cardProcessed
						.getNumber() > (currentGameState.getCurrentTopCardOnOwnDescendingDiscardPile().getNumber() - ownDiscardPileThreshold)) {

					logger.debug("Inside Own Descending Discard pile");

					Placement temp = cardPlacementUpdator(currentGameState, cardProcessed,
							CardPosition.OWN_DESCENDING_DISCARD_PILE);

					if (temp != null) {
						cardPlacementList.add(temp);
						gameStateAfterProcedureCall.setCurrentTopCardOnOwnDescendingDiscardPile(cardProcessed);
						descendingListIterator.remove();
						// descendingOrderListOfCards.remove(card);
					}
					if (previousCard.is10LargerThan(currentGameState.getCurrentTopCardOnOwnDescendingDiscardPile())) {
						logger.debug("The current card " + previousCard + "is 10 greater than the current top card "
								+ currentGameState.getCurrentTopCardOnOwnDescendingDiscardPile() + " on the Descending order file");
						temp = cardPlacementUpdator(currentGameState, previousCard,
								CardPosition.OWN_DESCENDING_DISCARD_PILE);

						if (temp != null) {
							cardPlacementList.add(temp);
							gameStateAfterProcedureCall.setCurrentTopCardOnOwnDescendingDiscardPile(previousCard);
							descendingFilteredList.remove(previousCard);
						}

					}

				}

			}

			List<Card> remainCardListAfterProcessing = new ArrayList<Card>(ascendingFilteredList);
			remainCardListAfterProcessing.addAll(descendingFilteredList);
			
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

	public IntermediateGameState getCardPlacement(IntermediateGameState currentGameState) {

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
		gameStateAfterPlacement = opponentCardPlacement(gameStateAfterPlacement);

		//gameStateAfterPlacement = opponentCardPlacement(currentGameState, currentHandCards, currentTopCardOnOpponentAscedingDiscardPile, currentTopCardOnOpponentDescendingDiscardPile)
		
		// Test for Placement on Opponents Discard Pile -- End

		int counter = 0;

		while (placementsOfMove.size() <= minNumOfPlacements) {

			// Test for Backwards Trick -- Start
			gameStateAfterPlacement = backwardsTrickValidator(currentGameState);
			// Test for Backwards Trick -- End

			// Test for Placement on Own Discard Pile -- Start
			gameStateAfterPlacement = ownDiscardPilesPlacement(currentGameState, ownDiscardPileThreshold);
			counter++;

			if (placementsOfMove.size() <= 2) {
				ownDiscardPileThreshold = ownDiscardPileThreshold + ownDiscardPileIncreamentFactor;
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

	/*
	private GameState computeNewGameStateAfterPlacement(IntermediateGameState currentGameState, Placement placement) {
		List<Card> handCards = new ArrayList<>(currentGameState.getCurrentHandCards());
		handCards.remove(placement.getCard());

		List<Card> cardsOnOwnAscendingDiscardPile = new ArrayList<>(
				currentGameState.getCardsOnOwnAscendingDiscardPile());
		if (placement.getPosition() == CardPosition.OWN_ASCENDING_DISCARD_PILE) {
			cardsOnOwnAscendingDiscardPile.add(placement.getCard());
		}

		List<Card> cardsOnOwnDescendingDiscardPile = new ArrayList<>(
				currentGameState.getCardsOnOwnDescendingDiscardPile());
		if (placement.getPosition() == CardPosition.OWN_DESCENDING_DISCARD_PILE) {
			cardsOnOwnDescendingDiscardPile.add(placement.getCard());
		}

		List<Card> cardsOnOpponentsAscendingDiscardPile = new ArrayList<>(
				currentGameState.getCardsOnOpponentsAscendingDiscardPile());
		if (placement.getPosition() == CardPosition.OPPONENTS_ASCENDING_DISCARD_PILE) {
			cardsOnOpponentsAscendingDiscardPile.add(placement.getCard());
		}

		List<Card> cardsOnOpponentsDescendingDiscardPile = new ArrayList<>(
				currentGameState.getCardsOnOpponentsDescendingDiscardPile());
		if (placement.getPosition() == CardPosition.OPPONENTS_ASCENDING_DISCARD_PILE) {
			cardsOnOpponentsDescendingDiscardPile.add(placement.getCard());
		}

		return new GameState(handCards, cardsOnOwnAscendingDiscardPile, cardsOnOwnDescendingDiscardPile,
				cardsOnOpponentsAscendingDiscardPile, cardsOnOpponentsDescendingDiscardPile);
	}
	
	*/

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

