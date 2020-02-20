package de.upb.mlseminar;

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

import de.upb.isml.thegamef2f.engine.GameState;
import de.upb.isml.thegamef2f.engine.Move;
import de.upb.isml.thegamef2f.engine.Placement;
import de.upb.isml.thegamef2f.engine.player.Player;

public class InformedPlayerOrchestrator implements Player {
	
	private Random random;
	private String name;
	private static final Logger logger = LoggerFactory.getLogger(InformedPlayerOrchestrator.class);
	private final String configFile = "runConfigs.txt";
	
	@Override
	public void initialize(long randomSeed) {
		this.random = new Random(randomSeed);

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
		Random rand = new Random();
		
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
		
		logger.info("List of possible moves are =");
		listOfPlacements.forEach(System.out::println);
		
		int randomElementIndex
		  = ThreadLocalRandom.current().nextInt(listOfPlacements.size()) % listOfPlacements.size();

		logger.info("RandomElementIndex :" + randomElementIndex);
		
		bestMove = listOfPlacements.get(randomElementIndex);
		logger.info("Best Move :" + bestMove.toString());
		
		return new Move(bestMove);
	}
	
	@Override
	public String getName() {
		return toString();
	}


	
}
