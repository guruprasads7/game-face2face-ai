package de.upb.mlseminar.informedplayer;

import java.util.ArrayList;
import java.util.Comparator;
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

/**
 * This class is simulates Rule State Engine based on the certain thresholds, to
 * generate optimal placements and tries to simulate the game play by a human
 * player.
 *
 * @author Guru Prasad Savandaiah
 *
 */
public class InformedSingleInstancePlayer implements Player {

	private Random random;
	private String name;

	private int ownDiscardPileThreshold;
	private int ownDiscardPileIncreamentFactor;
	private int opponentDiscardPileThreshold;
	private int minNumOfPlacements;

	private static final Logger logger = LoggerFactory.getLogger(InformedSingleInstancePlayer.class);

	public InformedSingleInstancePlayer(String name, int ownDiscardPileThreshold, int ownDiscardPileIncreamentFactor,
			int opponentDiscardPileThreshold, int minNumOfPlacements) {
		super();
		this.name = name;
		this.ownDiscardPileThreshold = ownDiscardPileThreshold;
		this.ownDiscardPileIncreamentFactor = ownDiscardPileIncreamentFactor;
		this.opponentDiscardPileThreshold = opponentDiscardPileThreshold;
		this.minNumOfPlacements = minNumOfPlacements;
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

		logger.debug("Start of the method : backwardsTrickValidator");
		List<Placement> cardPlacementList = new ArrayList<Placement>();

		ListIterator<Card> backwardCardIterator = currentHandCards.listIterator();

		try {
			// Test for Backwards Trick
			while (backwardCardIterator.hasNext()) {
				Card card = backwardCardIterator.next();

				logger.debug("Processing for card" + card);

				// Test if a current card is 10 lesser than topCardOnOwnAscendingDiscardPile
				if (card.is10SmallerThan(currenttopCardOnOwnAscendingDiscardPile)) {
					logger.debug("The current card " + card + " is 10 lesser than the current top card "
							+ currenttopCardOnOwnAscendingDiscardPile + " on the Ascending order file");

					Placement temp = cardPlacementUpdator(currentGameState, card,
							CardPosition.OWN_ASCENDING_DISCARD_PILE);

					if (temp != null) {
						cardPlacementList.add(temp);
						currenttopCardOnOwnAscendingDiscardPile = card;
						backwardCardIterator.remove();
					}

				}

				// Test if a current card is 10 greater than topCardOnOwnDescendingDiscardPile
				if (card.is10LargerThan(currentTopCardOnOwnDescendingDiscardPile)) {
					logger.debug("The current card " + card + "is 10 greater than the current top card "
							+ currentTopCardOnOwnDescendingDiscardPile + " on the Descending order file");
					Placement temp = cardPlacementUpdator(currentGameState, card,
							CardPosition.OWN_DESCENDING_DISCARD_PILE);

					if (temp != null) {
						cardPlacementList.add(temp);
						currentTopCardOnOwnDescendingDiscardPile = card;
						backwardCardIterator.remove();
					}

				}

			}

			logger.debug("CardPlacement list at the end of backwardsTrickValidator" + cardPlacementList.toString());
			logger.debug("End of the method : backwardsTrickValidator");
			logger.debug("===================================================================================");
			
			return new IntermediateMoveStatus(currentGameState, currentHandCards, cardPlacementList,
					currenttopCardOnOwnAscendingDiscardPile, currentTopCardOnOwnDescendingDiscardPile);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.toString());
			System.out.println(e);
			
			logger.debug("CardPlacement list at the end of backwardsTrickValidator" + cardPlacementList.toString());
			logger.debug("End of the method : backwardsTrickValidator");
			logger.debug("===================================================================================");
			
			return new IntermediateMoveStatus(currentGameState, currentHandCards, cardPlacementList,
					currenttopCardOnOwnAscendingDiscardPile, currentTopCardOnOwnDescendingDiscardPile);

		}


	}

	private IntermediateMoveStatus opponentCardPlacement(GameState currentGameState, List<Card> currentHandCards,
			Card currentTopCardOnOpponentAscedingDiscardPile, Card currentTopCardOnOpponentDescendingDiscardPile) {

		logger.debug("Start of the method : opponentCardPlacement");

		List<Placement> cardPlacementList = new ArrayList<Placement>();
		Card bestCandidate = null;
		int leastDifference = 99999;
		int ascOrDecFlag = -1; // 1 for ascending, 2 for descending

		ListIterator<Card> opponentCardIterator = currentHandCards.listIterator();

		try {

			while (opponentCardIterator.hasNext()) {
				Card card = opponentCardIterator.next();

				logger.debug("Processing for card" + card);
				int diffBtwOpponentAscendingDiscardPile = currentTopCardOnOpponentAscedingDiscardPile.getNumber()
						- card.getNumber();
				int diffBtwOpponentDescendingDiscardPile = card.getNumber()
						- currentTopCardOnOpponentDescendingDiscardPile.getNumber();

				int currentMinDifference = 99999;
				Card currentBestCandidate = null;
				int localAscOrDecFlag = -1;

				// Rule for placement on Opponents Ascending Discard pile
				logger.debug("Checking the rule for placement on Opponents Ascending Discard pile");

				if (diffBtwOpponentAscendingDiscardPile > 0
						&& card.getNumber() < currentTopCardOnOpponentAscedingDiscardPile.getNumber()
						&& card.getNumber() > (currentTopCardOnOpponentAscedingDiscardPile.getNumber()
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
						&& card.getNumber() > currentTopCardOnOpponentDescendingDiscardPile.getNumber()
						&& card.getNumber() < (currentTopCardOnOpponentDescendingDiscardPile.getNumber()
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
						currentTopCardOnOpponentAscedingDiscardPile = bestCandidate;
						currentHandCards.remove(bestCandidate);

					}

				} else if (ascOrDecFlag == 2) {

					logger.debug("The card" + bestCandidate
							+ " Is the best candidate to be placced on Opponent Descending Order Discard Pile");
					Placement temp = cardPlacementUpdator(currentGameState, bestCandidate,
							CardPosition.OPPONENTS_DESCENDING_DISCARD_PILE);

					if (temp != null) {
						cardPlacementList.add(temp);
						currentTopCardOnOpponentDescendingDiscardPile = bestCandidate;
						currentHandCards.remove(bestCandidate);
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.toString());
			System.out.println(e);
		}

		logger.debug("End of the method : opponentCardPlacement");
		logger.debug("===================================================================================");

		return new IntermediateMoveStatus(currentGameState, currentHandCards, cardPlacementList);

	}

	private IntermediateMoveStatus ownDiscardPilesPlacement(GameState currentGameState, List<Card> currentHandCards,
			Card topCardOnOwnAscendingDiscardPile, Card topCardOnOwnDescendingDiscardPile,
			int ownDiscardPileThreshold) {

		logger.debug("Start of the method : ownDiscardPilesPlacement");
		List<Placement> cardPlacementList = new ArrayList<Placement>();
		List<Integer> cardNumbersInPlacementList = new ArrayList<Integer>();

		if (currentHandCards.isEmpty()) {
			return new IntermediateMoveStatus(currentGameState, currentHandCards, cardPlacementList,
					topCardOnOwnAscendingDiscardPile, topCardOnOwnDescendingDiscardPile);
		}
		try {

			// Sorting the cards in Ascending order
			List<Card> ascedingOrderListOfCards = new ArrayList<Card>(currentHandCards);
			ascedingOrderListOfCards.sort((Card c1, Card c2) -> c1.getNumber() - c2.getNumber());

			// Logic for checking the discard piles average

			int ownDiscardPilesAverage = Math.abs(
					currentHandCards.get(0).getNumber() + currentHandCards.get(currentHandCards.size() - 1).getNumber())
					/ 2;
			
			logger.debug("Own discard pile Average =" + ownDiscardPilesAverage);

			// For ascending order rule
			List<Card> ascendingFilteredList = ascedingOrderListOfCards.stream()
					.filter(s -> s.getNumber() < ownDiscardPilesAverage).collect(Collectors.toList());

			List<Card> descendingFilteredList = ascedingOrderListOfCards.stream()
					.filter(s -> s.getNumber() >= ownDiscardPilesAverage).collect(Collectors.toList());
			descendingFilteredList.sort((Card c1, Card c2) -> c2.getNumber() - c1.getNumber());

			logger.debug("asceding order cards =" + ascendingFilteredList.toString());
			logger.debug("Descending order cards =" + descendingFilteredList.toString());

			for (int i = 0; i < ascendingFilteredList.size(); i++) {
				logger.debug("Checking for placing the card on the Ascending Order Pile");

				Card cardProcessed = ascendingFilteredList.get(i);
				logger.debug("Card Being processed : " + cardProcessed);
				// Rule for placement on own Ascending Discard pile
				if ((cardProcessed.getNumber() > topCardOnOwnAscendingDiscardPile.getNumber() && cardProcessed
						.getNumber() < (topCardOnOwnAscendingDiscardPile.getNumber() + ownDiscardPileThreshold)) || (cardProcessed.is10SmallerThan(topCardOnOwnAscendingDiscardPile))) {

					logger.debug("Inside Own Ascending Discard pile");
					Placement temp = cardPlacementUpdator(currentGameState, cardProcessed,
							CardPosition.OWN_ASCENDING_DISCARD_PILE);

					logger.debug("Card numbers in cardNumbersInPlacementList : " + cardNumbersInPlacementList.toString());
					logger.debug("Does cardNumbersInPlacementList contain the element : " + cardNumbersInPlacementList.contains(cardProcessed.getNumber()));

					if(cardNumbersInPlacementList.contains(cardProcessed.getNumber())) continue;

					if (temp != null) {
						cardPlacementList.add(temp);
						cardNumbersInPlacementList.add(cardProcessed.getNumber());
						topCardOnOwnAscendingDiscardPile = cardProcessed;
						// ascendingListIterator.remove();
					}

					logger.debug("Placement after the card placement : " + cardPlacementList.toString());
					
					// Test for backwards trick after placing each card
					for (int j = 0; j < i; j++) {
						Card temp10 = ascendingFilteredList.get(j);
						if (temp10.is10SmallerThan(cardProcessed) &&  cardNumbersInPlacementList.contains(cardProcessed.getNumber()) && !cardNumbersInPlacementList.contains(temp10.getNumber())) {
							logger.debug("The current card " + temp10 + " is 10 lesser than the current top card "
									+ cardProcessed + " on the Ascending order file");
							i = j - 1;
							logger.debug("The loop index for i is updated value" + i);

							logger.debug("Ascending order list : " + ascedingOrderListOfCards.toString());
							break;
						}
					}
					

				}

			}

			logger.debug("#####################################################################");

			
			// Rule for placement on own Descending Discard pile
			for (int i = 0; i < descendingFilteredList.size(); i++) {
				logger.debug("Checking for placing the card on the Descending Order Pile");
				Card cardProcessed = descendingFilteredList.get(i);

				logger.debug("Card Being processed" + cardProcessed);
				logger.debug("Card Placement list " + cardPlacementList.toString() );
				logger.debug("Top of the card before if check is " + topCardOnOwnDescendingDiscardPile);
				// Rule for placement on own Descending Discard pile
				if ((cardProcessed.getNumber() < topCardOnOwnDescendingDiscardPile.getNumber() && cardProcessed
						.getNumber() > (topCardOnOwnDescendingDiscardPile.getNumber() - ownDiscardPileThreshold)) || (cardProcessed.is10LargerThan(topCardOnOwnAscendingDiscardPile))) {

					logger.debug("Inside Own Descending Discard pile");

				Placement temp = cardPlacementUpdator(currentGameState, cardProcessed,
						CardPosition.OWN_DESCENDING_DISCARD_PILE);

				logger.debug("Does card placement list contain the element : " + cardPlacementList.contains(temp));
				logger.debug("Card numbers in cardNumbersInPlacementList : " + cardNumbersInPlacementList.toString());
				logger.debug("Does cardNumbersInPlacementList contain the element : " + cardNumbersInPlacementList.contains(cardProcessed.getNumber()));
				
				if(cardNumbersInPlacementList.contains(cardProcessed.getNumber())) continue;
				
				logger.debug("The top card on descending order pile" + topCardOnOwnDescendingDiscardPile );
				if (temp != null) {
					logger.debug("Placement being made : " + temp.toString());
					cardPlacementList.add(temp);
					topCardOnOwnDescendingDiscardPile = cardProcessed;
					logger.debug("Top card on the topCardOnOwnDescendingDiscardPile is updated to a value of " + topCardOnOwnDescendingDiscardPile);
					// descendingOrderListOfCards.remove(card);
				}
				logger.debug("Placement after the card placement" + cardPlacementList.toString());
				// Test for backwards trick after placing each card
				
				
				for (int j = 0; j < i; j++) {
					Card temp10 = descendingFilteredList.get(j);
					//cardPlacementList.forEach(temptest -> temptest.getCard().equals(temp10));
//					Placement testPlacement = cardPlacementList.stream()
//								.filter(var -> temp10.equals(var.getCard()))
//								.findAny()
//								.orElse(null);	
					//logger.debug("Placement test" + testPlacement.toString());
					if (temp10.is10LargerThan(cardProcessed) &&  cardNumbersInPlacementList.contains(cardProcessed.getNumber()) && !cardNumbersInPlacementList.contains(temp10.getNumber()) ) {
						logger.debug("The current card " + temp10 + "is 10 greater than the current top card "
								+ cardProcessed + " on the Descending order file");
						i = j - 1;
						logger.debug("The loop index for i is updated value in Descending order List " + i);
						logger.debug("Descending order list : " + descendingFilteredList.toString());
						break;
					}
				}
				
				}

			}

			// logger.info("Placement list after descending placement" +
			// cardPlacementList.toString());

			List<Card> remainCardListAfterProcessing = new ArrayList<Card>(ascendingFilteredList);
			remainCardListAfterProcessing.addAll(descendingFilteredList);
			
			
			for (Placement currentPlacement : cardPlacementList) {
				if (remainCardListAfterProcessing.contains(currentPlacement.getCard()))
					remainCardListAfterProcessing.remove(currentPlacement.getCard());
			}

			logger.debug("End of the method : ownDiscardPilesPlacement");
			logger.debug("===================================================================================");

			return new IntermediateMoveStatus(currentGameState, remainCardListAfterProcessing, cardPlacementList,
					topCardOnOwnAscendingDiscardPile, topCardOnOwnDescendingDiscardPile);

		}catch (Exception e) {
			e.printStackTrace();
			logger.error(e.toString());
			System.out.println(e);
		}

		return new IntermediateMoveStatus(currentGameState, currentHandCards, cardPlacementList,
				topCardOnOwnAscendingDiscardPile, topCardOnOwnDescendingDiscardPile);

	}

	public List<Placement> getCardPlacement(GameState gameState) {

		logger.debug("Start of the method : getCardPlacement");
		List<Placement> placementsOfMove = new ArrayList<Placement>();

		// Fetching the details of the current game state
		Card topCardOnOwnAscendingDiscardPile = gameState.getTopCardOnOwnAscendingDiscardPile();
		Card topCardOnOwnDescendingDiscardPile = gameState.getTopCardOnOwnDescendingDiscardPile();

		Card topCardOnOpponentAscendingDiscardPile = gameState.getTopCardOnOpponentsAscendingDiscardPile();
		Card topCardOnOpponentDescendingDiscardPile = gameState.getTopCardOnOpponentsDescendingDiscardPile();

		logger.debug(
				"Own game state : topCardOnOwnAscendingDiscardPile = " + topCardOnOwnAscendingDiscardPile.getNumber()
						+ " , topCardOnOwnDescendingDiscardPile = " + topCardOnOwnDescendingDiscardPile.getNumber());
		logger.debug("Opponents game state : topCardOnOpponentAscendingDiscardPile = "
				+ gameState.getTopCardOnOpponentsAscendingDiscardPile().getNumber()
				+ " , topCardOnOpponentDescendingDiscardPile = "
				+ gameState.getTopCardOnOpponentsDescendingDiscardPile().getNumber());
		logger.debug("Intial Own DiscardPileThreshold : " + ownDiscardPileThreshold
				+ " And Opponent Discard Pile Threshold : " + opponentDiscardPileThreshold);

		// Copy into a different list as the source list is an immutable list
		List<Card> orderedCurrentHandCards = new ArrayList<Card>(gameState.getHandCards());

		// Sorting the cards in Ascending order
		orderedCurrentHandCards.sort((Card c1, Card c2) -> c1.getNumber() - c2.getNumber());

		//// Real Logic starts here.

		// Test for Placement on Opponents Discard Pile -- Start
		IntermediateMoveStatus opponentDiscardPilePlacement = opponentCardPlacement(gameState, orderedCurrentHandCards,
				topCardOnOpponentAscendingDiscardPile, topCardOnOpponentDescendingDiscardPile);
		gameState = opponentDiscardPilePlacement.getGameState();
		orderedCurrentHandCards = opponentDiscardPilePlacement.getCurrentHandCards();
		placementsOfMove.addAll(opponentDiscardPilePlacement.getListOfCardPlacements());

		// Test for Placement on Opponents Discard Pile -- End

		int counter = 0;

		while (placementsOfMove.size() <= minNumOfPlacements) {

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
			IntermediateMoveStatus ownDiscardPilePlacement = ownDiscardPilesPlacement(gameState,
					orderedCurrentHandCards, topCardOnOwnAscendingDiscardPile, topCardOnOwnDescendingDiscardPile,
					ownDiscardPileThreshold);
			gameState = ownDiscardPilePlacement.getGameState();
			orderedCurrentHandCards = ownDiscardPilePlacement.currentHandCards;
			topCardOnOwnAscendingDiscardPile = ownDiscardPilePlacement.getCurrentTopCardOnOwnAscendingDiscardPile();
			topCardOnOwnDescendingDiscardPile = ownDiscardPilePlacement.getCurrentTopCardOnOwnDescendingDiscardPile();
			placementsOfMove.addAll(ownDiscardPilePlacement.getListOfCardPlacements());

			counter++;

			if (placementsOfMove.size() <= 2) {
				ownDiscardPileThreshold = ownDiscardPileThreshold + ownDiscardPileIncreamentFactor;
				logger.debug("Updated Own DiscardPileThreshold :" + ownDiscardPileThreshold);
			}

			if (counter >= 6)
				break;

		}

		logger.debug("List of placements at the end of get placement method is " + placementsOfMove.toString());
		logger.debug("End of the method : getCardPlacement");
		logger.debug("===================================================================================");

		return placementsOfMove;

	}

	private GameState computeNewGameStateAfterPlacement(GameState gameStatePriorToPlacement, Placement placement) {
		List<Card> handCards = new ArrayList<>(gameStatePriorToPlacement.getHandCards());
		handCards.remove(placement.getCard());

		List<Card> cardsOnOwnAscendingDiscardPile = new ArrayList<>(
				gameStatePriorToPlacement.getCardsOnOwnAscendingDiscardPile());
		if (placement.getPosition() == CardPosition.OWN_ASCENDING_DISCARD_PILE) {
			cardsOnOwnAscendingDiscardPile.add(placement.getCard());
		}

		List<Card> cardsOnOwnDescendingDiscardPile = new ArrayList<>(
				gameStatePriorToPlacement.getCardsOnOwnDescendingDiscardPile());
		if (placement.getPosition() == CardPosition.OWN_DESCENDING_DISCARD_PILE) {
			cardsOnOwnDescendingDiscardPile.add(placement.getCard());
		}

		List<Card> cardsOnOpponentsAscendingDiscardPile = new ArrayList<>(
				gameStatePriorToPlacement.getCardsOnOpponentsAscendingDiscardPile());
		if (placement.getPosition() == CardPosition.OPPONENTS_ASCENDING_DISCARD_PILE) {
			cardsOnOpponentsAscendingDiscardPile.add(placement.getCard());
		}

		List<Card> cardsOnOpponentsDescendingDiscardPile = new ArrayList<>(
				gameStatePriorToPlacement.getCardsOnOpponentsDescendingDiscardPile());
		if (placement.getPosition() == CardPosition.OPPONENTS_ASCENDING_DISCARD_PILE) {
			cardsOnOpponentsDescendingDiscardPile.add(placement.getCard());
		}

		return new GameState(handCards, cardsOnOwnAscendingDiscardPile, cardsOnOwnDescendingDiscardPile,
				cardsOnOpponentsAscendingDiscardPile, cardsOnOpponentsDescendingDiscardPile);
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
	public Move computeMove(GameState gameState) {

		logger.debug("Start of the method : computeMove");
		GameState currentGameState = gameState;

		List<Placement> placements = new ArrayList<Placement>();

		InformedSingleInstancePlayer instance = new InformedSingleInstancePlayer(name, 3, 5, 10, 3);
		placements = instance.getCardPlacement(currentGameState);

		return new Move(placements);

	}

}
