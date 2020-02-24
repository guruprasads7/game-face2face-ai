package de.upb.mlseminar.informedplayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.core.db.dialect.MySQLDialect;
import de.upb.isml.thegamef2f.engine.GameState;
import de.upb.isml.thegamef2f.engine.Move;
import de.upb.isml.thegamef2f.engine.Placement;
import de.upb.isml.thegamef2f.engine.board.Card;
import de.upb.isml.thegamef2f.engine.board.Game;
import de.upb.isml.thegamef2f.engine.player.Player;
import de.upb.isml.thegamef2f.engine.player.RandomPlayer;
import de.upb.mlseminar.ReadInputConfigs;

import de.upb.mlseminar.mymcts.montecarlo.UCT;
import de.upb.mlseminar.mcts.montecarlo.State;
import de.upb.mlseminar.mcts.tictactoe.Board;
import de.upb.mlseminar.mcts.tree.Node;
import de.upb.mlseminar.mymcts.tree.MCTSNode;
import de.upb.mlseminar.mymcts.tree.MCTSTree;

public class InformedPlayerOrchestrator implements Player {
	
	private Random random;
	private long randomSeed;
	private String name;
	private static final Logger logger = LoggerFactory.getLogger(InformedPlayerOrchestrator.class);
	private final String configFile = "runConfigs.txt";
	private static final int WIN_SCORE = 10;
	
	@Override
	public void initialize(long randomSeed) {
		this.random = new Random(randomSeed);
		this.randomSeed = randomSeed;

	}

	@Override
	public String toString() {
		return "random_player_" + name;
	}

	public InformedPlayerOrchestrator(String name) {
		super();
		this.name = name;
	}

	@Override
	public Move computeMove(GameState gameState) {
		
		logger.debug("Start of the method : computeMove");
		GameState currentGameState = gameState;

		List<Placement> placements = new ArrayList<Placement>();
		List<List<Placement>> listOfPlacements = new ArrayList<List<Placement>>();
		List<Placement> bestMove = new ArrayList<Placement>();
		
		/*
		// Normal Game call
		InformedSingleInstancePlayer instance = new InformedSingleInstancePlayer(name, 3, 5, 10, 3);
		placements = instance.getCardPlacement(currentGameState);
		
		logger.info("List of possible moves from InformedSingleInstancePlayer are =");
		placements.forEach(System.out::println);
		
		
		// Game Call for the new IntermediateGameState
		IntermediateGameState intermediateGameState = constructIntermediateGameState(currentGameState);
		InformedPlayerInstance informedPlayerInstance = new InformedPlayerInstance(name, 3, 5, 10, 3);
		
		intermediateGameState = informedPlayerInstance.getCardPlacement(intermediateGameState);
		logger.info("List of possible moves from InformedPlayerInstance are =");
		intermediateGameState.getListOfCardPlacements().forEach(System.out::println);
		*/
		
		constructRootTree(currentGameState);
		System.exit(-1);
		
		List<List<String>> runConfigs = ReadInputConfigs.readConfigFile(configFile);
		for (List<String> config : runConfigs) {
			int ownDiscardPileThreshold = Integer.parseInt(config.get(0));
			int ownDiscardPileIncreamentFactor = Integer.parseInt(config.get(0));
			int opponentDiscardPileThreshold = Integer.parseInt(config.get(0));
			int minNumOfPlacements = Integer.parseInt(config.get(0));
			
			logger.debug("Player playing the game : " + getName());
			logger.debug("Game state is : " + currentGameState.getHandCards().toString());
			
			InformedSingleInstancePlayer instance1 = new InformedSingleInstancePlayer(name, ownDiscardPileThreshold, ownDiscardPileIncreamentFactor, opponentDiscardPileThreshold, minNumOfPlacements);
			placements = instance1.getCardPlacement(currentGameState);
			
			listOfPlacements.add(placements);
			
		}
		
		logger.info("List of possible moves are =");
		listOfPlacements.forEach(System.out::println);
		
		int randomElementIndex
		  = ThreadLocalRandom.current().nextInt(listOfPlacements.size()) % listOfPlacements.size();

		//logger.info("RandomElementIndex :" + randomElementIndex);
		
		bestMove = listOfPlacements.get(randomElementIndex);
		logger.info("Best Move :" + bestMove.toString());
		
		return new Move(bestMove);
	}
	
	@Override
	public String getName() {
		return toString();
	}
	
	private void constructRootTree(GameState gameState) {
				
		MCTSTree tree = new MCTSTree();
        MCTSNode rootNode = tree.getRoot();
        IntermediateGameState rootstate = constructIntermediateGameState(gameState);
        
        rootNode.getState().setGameState(rootstate);
        rootNode.getState().setVisitCount(0);
        rootNode.getState().setWinScore(0);
        
        // Phase 1 - Selection
        MCTSNode promisingNode = selectPromisingNode(rootNode);
        
        logger.info("Promising Node : " + promisingNode.getState().getGameState().toString());
        // Phase 2 - Expansion
        expandNode(rootNode, rootstate);
        
        // Phase 3 - Simulation
        MCTSNode nodeToExplore = promisingNode;
        if (promisingNode.getChildArray().size() > 0) {
            nodeToExplore = promisingNode.getRandomChildNode();
        }
        logger.info("Node chosen for simulation : " + nodeToExplore.getState().getGameState().toString());
        int playoutResult = simulateRandomPlayout(nodeToExplore);
        
        backPropogation(nodeToExplore, playoutResult);

        MCTSNode winnerNode = rootNode.getChildWithMaxScore();
        System.out.println(" Winner node : " + winnerNode.getState().getGameState().toString());
        
        tree.setRoot(winnerNode);
        
        
	}
	
    private MCTSNode selectPromisingNode(MCTSNode rootNode) {
        MCTSNode node = rootNode;
        while (node.getChildArray().size() != 0) {
            node = UCT.findBestNodeWithUCT(node);
        }
        return node;
    }

    private void expandNode(MCTSNode node,IntermediateGameState rootState) {
      
        System.out.println("Parent Node at top : " + node.getState().getGameState().toString());
        
		List<List<String>> runConfigs = ReadInputConfigs.readConfigFile(configFile);
		
		for (List<String> config : runConfigs) {
			
			IntermediateGameState childState = null;
			int ownDiscardPileThreshold = Integer.parseInt(config.get(0));
			int ownDiscardPileIncreamentFactor = Integer.parseInt(config.get(1));
			int opponentDiscardPileThreshold = Integer.parseInt(config.get(2));
			int minNumOfPlacements = Integer.parseInt(config.get(3));
			
			// Construct a new gamestate for child nodes
	        childState = createChildStates(rootState);
      
	        // Invoke the InformedPlayerInstance for each of the possible states
			InformedPlayerInstance informedPlayerInstance = new InformedPlayerInstance(name, ownDiscardPileThreshold, ownDiscardPileIncreamentFactor, opponentDiscardPileThreshold, minNumOfPlacements);
			IntermediateGameState intermediateGameState = informedPlayerInstance.getCardPlacement(childState);
	        
			// Create a new node and add it to the Tree
			NodeState nodeState = new NodeState(intermediateGameState);
	        MCTSNode newNode = new MCTSNode(nodeState);
	        
	        newNode.setParent(node);
	        node.getChildArray().add(newNode);
	        
	        logger.info("Child Node : " + newNode.getState().getGameState().toString());
        
		}	
        logger.info("Parent Node childrens: " + node.getChildArray().size());
        logger.info("Parent Node : " + node.getState().getGameState().toString());
    }
    
    
    private int simulateRandomPlayout(MCTSNode node) {
        MCTSNode tempNode = node;
        NodeState tempState = tempNode.getState();
        IntermediateGameState tempIntermediateGameState = tempState.getGameState();
        int wincount = 0;

        InformedSingleInstancePlayer playerA = new InformedSingleInstancePlayer("random", 3, 5, 10, 3);
		Player playerB = new RandomPlayer("random");
		Game game = new Game(playerA, playerB, randomSeed);
		Player winner = game.simulate();
		game.getHistory().printHistory();
		wincount += winner == playerA ? 1 :0;
		
		System.out.println("Wincount" + wincount);
        
        return wincount;

    }
    
    private void backPropogation(MCTSNode nodeToExplore, double playoutResult) {
        MCTSNode tempNode = nodeToExplore;
        while (tempNode != null) {
            tempNode.getState().incrementVisit();
            if (playoutResult == 1)
                tempNode.getState().addScore(WIN_SCORE);
            tempNode = tempNode.getParent();
        }
    }
    
    /*
    private void expandNode(Node node) {
        List<State> possibleStates = node.getState().getAllPossibleStates();
        possibleStates.forEach(state -> {
            Node newNode = new Node(state);
            newNode.setParent(node);
            newNode.getState().setPlayerNo(node.getState().getOpponent());
            node.getChildArray().add(newNode);
        });
    }
    */
    
    /*
    private void expandNode(MCTSNode node) {
    	
    	List<List<String>> runConfigs = ReadInputConfigs.readConfigFile(configFile);
    	
		for (List<String> config : runConfigs) {
			int ownDiscardPileThreshold = Integer.parseInt(config.get(0));
			int ownDiscardPileIncreamentFactor = Integer.parseInt(config.get(0));
			int opponentDiscardPileThreshold = Integer.parseInt(config.get(0));
			int minNumOfPlacements = Integer.parseInt(config.get(0));
			
			logger.debug("Player playing the game : " + getName());
			logger.debug("Game state is : " + currentGameState.getHandCards().toString());
			
			InformedPlayer instance1 = new InformedPlayer(name, ownDiscardPileThreshold, ownDiscardPileIncreamentFactor, opponentDiscardPileThreshold, minNumOfPlacements);
			
			
			placements = instance1.getCardPlacement(currentGameState);
			
			listOfPlacements.add(placements);
			
		}
    }
    
    */
    
    
	
	private IntermediateGameState constructIntermediateGameState(GameState gameState) {
		
		IntermediateGameState intermediateGameState = new IntermediateGameState();
		// Copy into a different list as the source list is an immutable list
        intermediateGameState.setCurrentHandCards(new ArrayList<Card>(gameState.getHandCards()));
        intermediateGameState.setListOfCardPlacements(new ArrayList<Placement>());
        intermediateGameState.setCurrentTopCardOnOpponentAscendingDiscardPile(gameState.getTopCardOnOpponentsAscendingDiscardPile());
        intermediateGameState.setCurrentTopCardOnOpponentDescendingDiscardPile(gameState.getTopCardOnOpponentsDescendingDiscardPile());
        intermediateGameState.setCurrentTopCardOnOwnAscendingDiscardPile(gameState.getTopCardOnOwnAscendingDiscardPile());
        intermediateGameState.setCurrentTopCardOnOwnDescendingDiscardPile(gameState.getTopCardOnOwnDescendingDiscardPile());
		
        return intermediateGameState;
	}
	
	private IntermediateGameState createChildStates(IntermediateGameState gameState) {
		
		IntermediateGameState intermediateGameState = new IntermediateGameState();
		// Copy into a different list as the source list is an immutable list
        intermediateGameState.setCurrentHandCards(new ArrayList<Card>(gameState.getCurrentHandCards()));
        intermediateGameState.setListOfCardPlacements(new ArrayList<Placement>());
        intermediateGameState.setCurrentTopCardOnOpponentAscendingDiscardPile(gameState.getCurrentTopCardOnOpponentAscendingDiscardPile());
        intermediateGameState.setCurrentTopCardOnOpponentDescendingDiscardPile(gameState.getCurrentTopCardOnOpponentDescendingDiscardPile());
        intermediateGameState.setCurrentTopCardOnOwnAscendingDiscardPile(gameState.getCurrentTopCardOnOwnAscendingDiscardPile());
        intermediateGameState.setCurrentTopCardOnOwnDescendingDiscardPile(gameState.getCurrentTopCardOnOwnDescendingDiscardPile());
		
        return intermediateGameState;
	}

	
}
