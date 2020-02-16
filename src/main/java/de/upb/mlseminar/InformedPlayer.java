package de.upb.mlseminar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.stream.Collectors;

import de.upb.isml.thegamef2f.engine.CardPosition;
import de.upb.isml.thegamef2f.engine.GameState;
import de.upb.isml.thegamef2f.engine.Move;
import de.upb.isml.thegamef2f.engine.Placement;
import de.upb.isml.thegamef2f.engine.board.AscendingDiscardPile;
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

		// System.out.println("Placements from backwardTrick : " +
		// cardPlacementList.toString());
		return new IntermediateMoveStatus(currentGameState, currentHandCards, cardPlacementList,
				currenttopCardOnOwnAscendingDiscardPile, currentTopCardOnOwnDescendingDiscardPile);

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
					// localCardIndexInList = card.get
					System.out.println("Inside Opponents Ascending Discard pile");
				}

			}

			// Rule for placement on own Descending Discard pile
			if (diffBtwOpponentDescendingDiscardPile > 0
					&& card.getNumber() > currentTopCardOnOpponentDescendingDiscardPile.getNumber()
					&& card.getNumber() < (currentTopCardOnOpponentDescendingDiscardPile.getNumber()
							+ opponentDiscardPileThreshold)) {

				// System.out.println("Inside Opponents Descending Discard pile");

				// System.out.println("diffBtwOpponentDescendingDiscardPile" +
				// diffBtwOpponentDescendingDiscardPile +"currentMinDifference :" +
				// currentMinDifference);

				if (diffBtwOpponentDescendingDiscardPile < currentMinDifference) {
					currentMinDifference = diffBtwOpponentDescendingDiscardPile;
					currentBestCandidate = card;
					localAscOrDecFlag = 2;
					// System.out.println("Inside Opponents asadasdasdx");
				}

				// System.out.println();

			}

			// System.out.println("current Min difference : " + currentMinDifference + "
			// least Difference :" + leastDifference + " localAscOrDecFlag : " +
			// localAscOrDecFlag);
			if (currentBestCandidate != null && currentMinDifference < leastDifference && localAscOrDecFlag > 0) {

				// System.out.println("Inside here");
				leastDifference = currentMinDifference;
				bestCandidate = currentBestCandidate;
				ascOrDecFlag = localAscOrDecFlag;
				// System.out.println("current Least difference : " + leastDifference + "
				// bestCandidate :" + bestCandidate.getNumber() + " AscOrDecFlag : " +
				// ascOrDecFlag);
			}

		}

		// System.out.println("comes till here");

		// System.out.println("current Least difference : " + leastDifference + "
		// bestCandidate :" + bestCandidate.getNumber() + " AscOrDecFlag : " +
		// ascOrDecFlag);
		if (bestCandidate != null && leastDifference > 0 && ascOrDecFlag > 0) {
			// System.out.println("Inside hello here");
			if (ascOrDecFlag == 1) {

				System.out.println("Inside Ascending");
				Placement temp = cardPlacementUpdator(currentGameState, bestCandidate,
						CardPosition.OPPONENTS_ASCENDING_DISCARD_PILE);

				if (temp != null) {
					cardPlacementList.add(temp);

					// currentTopCardOnOpponentAscedingDiscardPile =
					// (currentHandCards.indexOf(bestCandidate) >
					// currentHandCards.indexOf(currentTopCardOnOpponentAscedingDiscardPile)) ?
					// currentHandCards.get(currentHandCards.indexOf(bestCandidate)):
					// currentHandCards.get(currentHandCards.indexOf(currentTopCardOnOpponentAscedingDiscardPile));

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

		return new IntermediateMoveStatus(currentGameState, currentHandCards, cardPlacementList);

	}

	private IntermediateMoveStatus ownDiscardPilesPlacement(GameState currentGameState, List<Card> currentHandCards,
			Card topCardOnOwnAscendingDiscardPile, Card topCardOnOwnDescendingDiscardPile,
			int ownDiscardPileThreshold) {

		Card initialtopCardOnOwnAscendingDiscardPile = topCardOnOwnAscendingDiscardPile;
		Card intialTopCardOnOwnDescendingDiscardPile = topCardOnOwnDescendingDiscardPile;

		List<Placement> cardPlacementList = new ArrayList<Placement>();

		// Sorting the cards in Ascending order
		List<Card> ascedingOrderListOfCards = new ArrayList<Card>(currentHandCards);
		ascedingOrderListOfCards.sort((Card c1, Card c2) -> c1.getNumber() - c2.getNumber());

		int ownDiscardPilesAverage = Math.abs(ascedingOrderListOfCards.get(0).getNumber()
				+ currentHandCards.get(ascedingOrderListOfCards.size() - 1).getNumber()) / 2;

		/*
		 * int ownDiscardPilesAverage =
		 * Math.abs(topCardOnOwnAscendingDiscardPile.getNumber() +
		 * topCardOnOwnDescendingDiscardPile.getNumber()) / 2;
		 */

		System.out.println("Own discard pile Average =" + ownDiscardPilesAverage);

		// For ascending order rule
		List<Card> ascendingFilteredList = ascedingOrderListOfCards.stream()
				.filter(s -> s.getNumber() < ownDiscardPilesAverage).collect(Collectors.toList());

		List<Card> descendingFilteredList = ascedingOrderListOfCards.stream()
				.filter(s -> s.getNumber() >= ownDiscardPilesAverage).collect(Collectors.toList());
		descendingFilteredList.sort((Card c1, Card c2) -> c2.getNumber() - c1.getNumber());

		System.out.println("asceding order cards =" + ascendingFilteredList.toString());
		System.out.println("Descending order cards =" + descendingFilteredList.toString());

		System.out.println("================================================================");

		ListIterator<Card> ascendingListIterator = ascendingFilteredList.listIterator();

		// Rule for placement on own Ascending Discard pile
		while (ascendingListIterator.hasNext()) {

			Card previousCard;
			Card cardProcessed;

			if (ascendingListIterator.hasPrevious()) {
				// System.out.println("previous check");
				previousCard = ascendingListIterator.previous();
				ascendingListIterator.next();
				cardProcessed = ascendingListIterator.next();
				// System.out.println("Current card = " + cardProcessed + " Previous card = "+
				// previousCard);

			} else {
				cardProcessed = ascendingListIterator.next();
				previousCard = cardProcessed;
				// System.out.println("Current card = " + cardProcessed );
			}

			// Rule for placement on own Ascending Discard pile
			if (cardProcessed.getNumber() > topCardOnOwnAscendingDiscardPile.getNumber() && cardProcessed
					.getNumber() < (topCardOnOwnAscendingDiscardPile.getNumber() + ownDiscardPileThreshold)) {

				System.out.println("Inside Own Ascending Discard pile");
				Placement temp = cardPlacementUpdator(currentGameState, cardProcessed,
						CardPosition.OWN_ASCENDING_DISCARD_PILE);

				if (temp != null) {
					cardPlacementList.add(temp);
					topCardOnOwnAscendingDiscardPile = cardProcessed;
					ascendingListIterator.remove();
				}
				System.out.println("Check for backward trick");

				if (previousCard.is10SmallerThan(cardProcessed)) {
					System.out.println("sadasdasdasdasdasdasdasdasd");
					IntermediateMoveStatus backwardTrickResults = backwardsTrickValidator(currentGameState,
							ascendingFilteredList, topCardOnOwnAscendingDiscardPile, topCardOnOwnDescendingDiscardPile);
					currentGameState = backwardTrickResults.getGameState();
					ascendingFilteredList = backwardTrickResults.getCurrentHandCards();
					topCardOnOwnAscendingDiscardPile = backwardTrickResults
							.getCurrentTopCardOnOwnAscendingDiscardPile();
					topCardOnOwnDescendingDiscardPile = backwardTrickResults
							.getCurrentTopCardOnOwnDescendingDiscardPile();
					cardPlacementList.addAll(backwardTrickResults.getListOfCardPlacements());

				} else {
					// ascendingFilteredList.remove(cardProcessed);
				}

			}

		}

		System.out.println("#####################################################################");

		// Rule for placement on own Descending Discard pile
		ListIterator<Card> descendingListIterator = descendingFilteredList.listIterator();
		while (descendingListIterator.hasNext()) {

			Card previousCard;
			Card cardProcessed;

			if (descendingListIterator.hasPrevious()) {
				// System.out.println("previous check");
				previousCard = descendingListIterator.previous();
				descendingListIterator.next();
				cardProcessed = descendingListIterator.next();
				// System.out.println("Current card = " + cardProcessed + " Previous card = "+
				// previousCard);

			} else {
				cardProcessed = descendingListIterator.next();
				previousCard = cardProcessed;
				// System.out.println("Current card = " + cardProcessed );
			}

			// Rule for placement on own Descending Discard pile
			if (cardProcessed.getNumber() < topCardOnOwnDescendingDiscardPile.getNumber() && cardProcessed
					.getNumber() > (topCardOnOwnDescendingDiscardPile.getNumber() - ownDiscardPileThreshold)) {

				System.out.println("Inside Own Descending Discard pile");

				Placement temp = cardPlacementUpdator(currentGameState, cardProcessed,
						CardPosition.OWN_DESCENDING_DISCARD_PILE);

				if (temp != null) {
					cardPlacementList.add(temp);
					topCardOnOwnDescendingDiscardPile = cardProcessed;
					descendingListIterator.remove();
					// descendingOrderListOfCards.remove(card);
				}
				System.out.println("Check for backward trick");

				if (previousCard.is10LargerThan(cardProcessed)) {
					System.out.println("sadasdasdasdasdasdasdasdasd");
					IntermediateMoveStatus backwardTrickResults = backwardsTrickValidator(currentGameState,
							descendingFilteredList, topCardOnOwnAscendingDiscardPile,
							topCardOnOwnDescendingDiscardPile);
					currentGameState = backwardTrickResults.getGameState();
					descendingFilteredList = backwardTrickResults.getCurrentHandCards();
					topCardOnOwnAscendingDiscardPile = backwardTrickResults
							.getCurrentTopCardOnOwnAscendingDiscardPile();
					topCardOnOwnDescendingDiscardPile = backwardTrickResults
							.getCurrentTopCardOnOwnDescendingDiscardPile();
					cardPlacementList.addAll(backwardTrickResults.getListOfCardPlacements());
					descendingFilteredList.remove(cardProcessed);

				} else {
					// descendingFilteredList.remove(cardProcessed);
				}

			}

		}

		System.out.println("#####################################################################");

		List<Card> remainCardListAfterProcessing = new ArrayList<Card>(ascendingFilteredList);
		remainCardListAfterProcessing.addAll(descendingFilteredList);

		return new IntermediateMoveStatus(currentGameState, remainCardListAfterProcessing, cardPlacementList,
				topCardOnOwnAscendingDiscardPile, topCardOnOwnDescendingDiscardPile);

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

		while (placementsOfMove.size() <= 3) {

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
				System.out.println("DiscardPileThreshold :" + ownDiscardPileThreshold);
			}

			if (counter >= 7)
				break;

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
		System.out.println("------------------------------------------------------");

		placementsOfMove = getCardPlacement(currentGameState);

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
