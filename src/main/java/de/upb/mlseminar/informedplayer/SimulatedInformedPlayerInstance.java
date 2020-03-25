package de.upb.mlseminar.informedplayer;

import java.util.ArrayList;
import java.util.Collections;
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
import de.upb.mlseminar.mymcts.montecarlo.InformedMonteCarloPlayer;
import de.upb.mlseminar.utilities.IntermediateGameState;

/**
 * This class is instance of Informed Player which is used, during the
 * simulation phase of the MCTS approach based on the configs passed from
 * MonteCarloPlayer
 *
 * @author Guru Prasad Savandaiah
 *
 */
public class SimulatedInformedPlayerInstance implements Player {

	private Random random;
	private String name;

	private IntermediateGameState intermediateGameState;
	private int ownDiscardPileThreshold;
	private int ownDiscardPileIncreamentFactor;
	private int opponentDiscardPileThreshold;
	private int opponentDiscardPileIncreamentFactor;
	private int minNumOfPlacements;

	private int classCallCount = 0;
	private int methodCallCount = 0;

	public void rest() {

		classCallCount = 0;
		methodCallCount = 0;

	}

	private static final Logger logger = LoggerFactory.getLogger(InformedSingleInstancePlayer.class);

	public SimulatedInformedPlayerInstance() {
		super();
	}

	public SimulatedInformedPlayerInstance(String name, IntermediateGameState intermediateGameState,
			int ownDiscardPileThreshold, int ownDiscardPileIncreamentFactor, int opponentDiscardPileThreshold,
			int minNumOfPlacements) {
		super();
		this.name = name;
		this.intermediateGameState = intermediateGameState;
		this.ownDiscardPileThreshold = ownDiscardPileThreshold;
		this.ownDiscardPileIncreamentFactor = ownDiscardPileIncreamentFactor;
		this.opponentDiscardPileThreshold = opponentDiscardPileThreshold;
		this.opponentDiscardPileIncreamentFactor = opponentDiscardPileIncreamentFactor;
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

	@Override
	public Move computeMove(GameState gameState) {

		// Increment the method call count
		methodCallCount = methodCallCount + 1;
		List<Placement> placements = new ArrayList<Placement>();
		InformedPlayerCore playerCore = new InformedPlayerCore(name, ownDiscardPileThreshold,
				ownDiscardPileIncreamentFactor, opponentDiscardPileThreshold, opponentDiscardPileIncreamentFactor,
				minNumOfPlacements);

//		logger.debug("Class call count : " + classCallCount);
//		logger.debug("Method call count : " + methodCallCount);

		// int counter = TestInformedPlayerInstance.classCallCount;

		try {

			IntermediateGameState childState = createChildStates(intermediateGameState);

			if (childState.getCurrentHandCards().isEmpty()) {
				placements = childState.getListOfCardPlacements();
				// this.intermediateGameState = constructIntermediateGameState(gameState);
				return new Move(placements);
			}

//			logger.debug("Hand Card Before the call" + childState.getCurrentHandCards().toString());
//			logger.debug("Card Placements Before the call" + childState.getListOfCardPlacements().toString());

			if (methodCallCount == 1 && childState.getListOfCardPlacements().size() >= 2) {

//				logger.debug("Intermediate states have placement list");
				placements = childState.getListOfCardPlacements();

				// this.intermediateGameState = constructIntermediateGameState(gameState);
				return new Move(placements);
			}

			// System.out.println("hello inside increamentor");
			IntermediateGameState currentGameState = constructIntermediateGameState(gameState);
			IntermediateGameState resultState = playerCore.getCardPlacement(currentGameState);
//			logger.debug("Hand Card" + resultState.getCurrentHandCards().toString());
//			logger.debug("after the call" + resultState.toString());
			placements = resultState.getListOfCardPlacements();
//			logger.debug("List of possible moves from InformedPlayerInstance are =");
			// placements.forEach(System.out::println);

			// this.intermediateGameState = constructIntermediateGameState(gameState);
			Move move = new Move(placements);
//			logger.debug("Move is " + move.getPlacements());

			return move;

		} catch (Exception e) {
			e.printStackTrace();
//			logger.error(e.toString());
//			System.out.println(e);
			return new Move(placements);
		}

	}

	private IntermediateGameState constructIntermediateGameState(GameState gameState) {

		IntermediateGameState intermediateGameState = new IntermediateGameState();
		// Copy into a different list as the source list is an immutable list
		intermediateGameState
				.setCurrentHandCards(Collections.synchronizedList(new ArrayList<Card>(gameState.getHandCards())));
		intermediateGameState.setListOfCardPlacements(Collections.synchronizedList(new ArrayList<Placement>()));
		intermediateGameState
				.setCurrentTopCardOnOpponentAscendingDiscardPile(gameState.getTopCardOnOpponentsAscendingDiscardPile());
		intermediateGameState.setCurrentTopCardOnOpponentDescendingDiscardPile(
				gameState.getTopCardOnOpponentsDescendingDiscardPile());
		intermediateGameState
				.setCurrentTopCardOnOwnAscendingDiscardPile(gameState.getTopCardOnOwnAscendingDiscardPile());
		intermediateGameState
				.setCurrentTopCardOnOwnDescendingDiscardPile(gameState.getTopCardOnOwnDescendingDiscardPile());

		return intermediateGameState;
	}

	private IntermediateGameState createChildStates(IntermediateGameState gameState) {

		IntermediateGameState intermediateGameState = new IntermediateGameState();
		// Copy into a different list as the source list is an immutable list
		intermediateGameState.setCurrentHandCards(
				Collections.synchronizedList(new ArrayList<Card>(gameState.getCurrentHandCards())));
		intermediateGameState.setListOfCardPlacements(
				Collections.synchronizedList(new ArrayList<Placement>(gameState.getListOfCardPlacements())));
		intermediateGameState.setCurrentTopCardOnOpponentAscendingDiscardPile(
				gameState.getCurrentTopCardOnOpponentAscendingDiscardPile());
		intermediateGameState.setCurrentTopCardOnOpponentDescendingDiscardPile(
				gameState.getCurrentTopCardOnOpponentDescendingDiscardPile());
		intermediateGameState
				.setCurrentTopCardOnOwnAscendingDiscardPile(gameState.getCurrentTopCardOnOwnAscendingDiscardPile());
		intermediateGameState
				.setCurrentTopCardOnOwnDescendingDiscardPile(gameState.getCurrentTopCardOnOwnDescendingDiscardPile());

		return intermediateGameState;
	}

}
