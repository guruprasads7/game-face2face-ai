package de.upb.mlseminar.mymcts.montecarlo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.isml.thegamef2f.engine.GameState;
import de.upb.isml.thegamef2f.engine.Move;
import de.upb.isml.thegamef2f.engine.Placement;
import de.upb.isml.thegamef2f.engine.board.Card;
import de.upb.isml.thegamef2f.engine.board.Game;
import de.upb.isml.thegamef2f.engine.player.Player;
import de.upb.isml.thegamef2f.engine.player.RandomPlayer;

import de.upb.mlseminar.informedplayer.InformedPlayerCore;
import de.upb.mlseminar.informedplayer.SimulatedInformedPlayerInstance;
import de.upb.mlseminar.mymcts.tree.MCTSNode;
import de.upb.mlseminar.mymcts.tree.MCTSTree;
import de.upb.mlseminar.utilities.IntermediateGameState;
import de.upb.mlseminar.utilities.ModelInputConfig;
import de.upb.mlseminar.utilities.NodeState;
import de.upb.mlseminar.utilities.ReadInputConfigs;

/**
 * This class is Monte Carlo Tree Search based AI player.
 * At each turn, This MCTS based player generates best list of valid placements,
 * for each iteration of hand cards. The optimal move was generated based on Custom Rule Based Informed Player,
 * in conjunction with MCTS approach. The resulting list of placements is
 * finally converted into a move.
 *
 * @author Guru Prasad Savandaiah
 * Reference : https://github.com/eugenp/tutorials/blob/master/algorithms-searching/src/main/java/com/baeldung/algorithms/mcts/montecarlo/MonteCarloTreeSearch.java
 *
 */
public class InformedMonteCarloPlayer implements Player {
	
	private Random random;
	private long randomSeed;
	private String name;
	private static final Logger logger = LoggerFactory.getLogger(InformedMonteCarloPlayer.class);
	private final String configFile = "runConfigs.txt";
	private long maxTimeInterationInMilliSec = 2500;
	private int maxIterations = 2;
	
	public InformedMonteCarloPlayer(String name) {
		super();
		this.name = name;
	}
	
	@Override
	public void initialize(long randomSeed) {
		this.random = new Random(randomSeed);
		this.randomSeed = randomSeed;

	}

	@Override
	public String toString() {
		return "informed_montecarlo_player_" + name;
	}
	
	@Override
	public String getName() {
		return toString();
	}
	
	
	@Override
	public Move computeMove(GameState gameState) {
		
		List<Placement> placements = new ArrayList<Placement>();
		
		logger.debug("Start of the method : computeMove");
        long start = System.currentTimeMillis();
        long end = (start + maxTimeInterationInMilliSec);
		
        // Construct the root Tree
		MCTSTree tree = new MCTSTree();
        MCTSNode rootNode = tree.getRoot();
        IntermediateGameState rootstate = constructIntermediateGameState(gameState);
        
        rootNode.getState().setGameState(rootstate);
        rootNode.getState().setVisitCount(0);
        rootNode.getState().setWinScore(0);
		
        int numberOfIterations = 0;
        
        int noOfChildren = 0; 
        while(numberOfIterations < 5) {
        		
        	// Phase 1 - Selection
            MCTSNode promisingNode = selectPromisingNode(rootNode);
            
            logger.debug("Promising Node : " + promisingNode.getState().getGameState().toString());
            // Phase 2 - Expansion
            expandNode(rootNode, rootstate);
            
            noOfChildren = rootNode.getChildArray().size();
            
            if(noOfChildren == 0 ) {
            	logger.debug("Parent Node has: " + rootNode.getChildArray().size() + " children, hence terminating the game");
            	List<Placement> zeroPlacements = new ArrayList<Placement>();
            	return (new Move(zeroPlacements));
            }
            
            
            // Phase 3 - Simulation
            MCTSNode nodeToExplore = promisingNode;
            if (promisingNode.getChildArray().size() > 0) {
                nodeToExplore = promisingNode.getRandomChildNode();
            }
            
            
            logger.debug("Node chosen for simulation : " + nodeToExplore.getState().getGameState().toString());
            int playoutResult = simulateRandomPlayout(nodeToExplore);
            
            // Phase 4 - Back Propagation
            backPropogation(nodeToExplore, playoutResult);
            
            numberOfIterations++;
            
        }
        
        MCTSNode winnerNode = rootNode.getChildWithMaxScore();
        logger.debug("\n");
        logger.debug("Winner Node : " + winnerNode.getState().getGameState().toString() + "RunTime config" + winnerNode.getState().getModelInputConfig().toString());
        placements = winnerNode.getState().getGameState().getListOfCardPlacements();
        logger.debug("Best Placement" + placements.toString());
        
        logger.debug("--------------------------------------------------------------------------------");
        tree.setRoot(winnerNode);
		
        
		return (new Move(placements));
	}

    private MCTSNode selectPromisingNode(MCTSNode rootNode) {
        MCTSNode node = rootNode;
        while (node.getChildArray().size() != 0) {
            node = UCT.findBestNodeWithUCT(node);
        }
        return node;
    }

    private void expandNode(MCTSNode node,IntermediateGameState rootState) {
              
		List<ModelInputConfig> runConfigsList = ReadInputConfigs.readConfigFile(configFile);
		List<Placement> placements = Collections.synchronizedList(new ArrayList<Placement>());
		
		for (ModelInputConfig inputConfig : runConfigsList) {
			
			IntermediateGameState childState = null;

			// Construct a new gamestate for child nodes
	        childState = createChildStates(rootState);
      
	        // Invoke the InformedPlayerInstance for each of the possible states
			InformedPlayerCore informedPlayerInstance = new InformedPlayerCore(name, inputConfig.getOwnDiscardPileThreshold(), inputConfig.getOwnDiscardPileIncreamentFactor(), inputConfig.getOpponentDiscardPileThreshold(), inputConfig.getOpponentDiscardPileIncreamentFactor(), inputConfig.getMinNumOfPlacements());
			IntermediateGameState intermediateGameState = informedPlayerInstance.getCardPlacement(childState);
	        placements = intermediateGameState.getListOfCardPlacements();
			
	        // Skip adding the child as the number of placements, do not match the required valid moves
	        if(placements.size() < 2) continue;
			// Create a new node and add it to the Tree
			NodeState nodeState = new NodeState(intermediateGameState,inputConfig);
	        MCTSNode newNode = new MCTSNode(nodeState);
	        
	        newNode.setParent(node);
	        node.getChildArray().add(newNode);
	        
	        logger.debug("Child Node : " + newNode.getState().getGameState().toString() + "RunTime config" + newNode.getState().getModelInputConfig().toString());
        
		}	
        logger.debug("Parent Node childrens: " + node.getChildArray().size());
        //logger.info("Parent Node : " + node.getState().getGameState().toString());
    }
    
    
    private int simulateRandomPlayout(MCTSNode node) {
    	
    	int wincount = 0;
    	try {
    	
        MCTSNode tempNode = node;
        NodeState tempState = tempNode.getState();
        IntermediateGameState tempIntermediateGameState = tempState.getGameState();
        logger.debug("Random Playout Intermediate Status :" + tempIntermediateGameState.toString());
        logger.debug("Random Playout Run Configuration Status :" + tempState.getModelInputConfig());
        ModelInputConfig tempModelInputConfig = tempState.getModelInputConfig();
        
        

        SimulatedInformedPlayerInstance playerA = new SimulatedInformedPlayerInstance(name,tempIntermediateGameState,tempModelInputConfig.getOwnDiscardPileThreshold(),tempModelInputConfig.getOwnDiscardPileIncreamentFactor(),tempModelInputConfig.getOpponentDiscardPileThreshold(),tempModelInputConfig.getMinNumOfPlacements());
        //InformedSingleInstancePlayer playerA = new InformedSingleInstancePlayer(name, tempModelInputConfig.getOwnDiscardPileThreshold(),tempModelInputConfig.getOwnDiscardPileIncreamentFactor(),tempModelInputConfig.getOpponentDiscardPileThreshold(),tempModelInputConfig.getMinNumOfPlacements());
        Player playerB = new RandomPlayer("random");
		Game game = new Game(playerA, playerB, randomSeed);
		Player winner = game.simulate();
		playerA.rest();
		//game.getHistory().printHistory();
		wincount += winner == playerA ? 1 :0;
		
		logger.debug("Wincount" + wincount);
        
        return wincount;
    	}catch (Exception e) {
    		e.printStackTrace();
    		logger.error(e.toString());
    		System.out.println(e);
    		return 0;
    	}
    	
    }
    
    private void backPropogation(MCTSNode nodeToExplore, int playoutResult) {
        MCTSNode tempNode = nodeToExplore;
        while (tempNode != null) {
            tempNode.getState().incrementVisit();
            if (playoutResult == 1)
                tempNode.getState().addScore(playoutResult);
            tempNode = tempNode.getParent();
        }
    }
    
	private IntermediateGameState constructIntermediateGameState(GameState gameState) {
		
		IntermediateGameState intermediateGameState = new IntermediateGameState();
		// Copy into a different list as the source list is an immutable list
        intermediateGameState.setCurrentHandCards(Collections.synchronizedList(new ArrayList<Card>(gameState.getHandCards())));
        intermediateGameState.setListOfCardPlacements(Collections.synchronizedList(new ArrayList<Placement>()));
        intermediateGameState.setCurrentTopCardOnOpponentAscendingDiscardPile(gameState.getTopCardOnOpponentsAscendingDiscardPile());
        intermediateGameState.setCurrentTopCardOnOpponentDescendingDiscardPile(gameState.getTopCardOnOpponentsDescendingDiscardPile());
        intermediateGameState.setCurrentTopCardOnOwnAscendingDiscardPile(gameState.getTopCardOnOwnAscendingDiscardPile());
        intermediateGameState.setCurrentTopCardOnOwnDescendingDiscardPile(gameState.getTopCardOnOwnDescendingDiscardPile());
		
        return intermediateGameState;
	}
	
	private IntermediateGameState createChildStates(IntermediateGameState gameState) {
		
		IntermediateGameState intermediateGameState = new IntermediateGameState();
		// Copy into a different list as the source list is an immutable list
        intermediateGameState.setCurrentHandCards(Collections.synchronizedList(new ArrayList<Card>(gameState.getCurrentHandCards())));
        intermediateGameState.setListOfCardPlacements(Collections.synchronizedList(new ArrayList<Placement>()));
        intermediateGameState.setCurrentTopCardOnOpponentAscendingDiscardPile(gameState.getCurrentTopCardOnOpponentAscendingDiscardPile());
        intermediateGameState.setCurrentTopCardOnOpponentDescendingDiscardPile(gameState.getCurrentTopCardOnOpponentDescendingDiscardPile());
        intermediateGameState.setCurrentTopCardOnOwnAscendingDiscardPile(gameState.getCurrentTopCardOnOwnAscendingDiscardPile());
        intermediateGameState.setCurrentTopCardOnOwnDescendingDiscardPile(gameState.getCurrentTopCardOnOwnDescendingDiscardPile());
		
        return intermediateGameState;
	}

	
}
