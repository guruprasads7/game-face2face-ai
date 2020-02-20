package de.upb.mlseminar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
		GameState currentGameState = gameState;

		List<Placement> placementsOfMove = new ArrayList<Placement>();

		logger.debug("Player playing the game : " + getName());
		logger.debug("Game state is : " + currentGameState.getHandCards().toString());

		InformedPlayer instance1 = new InformedPlayer(name, 3, 3, 10,3);
		placementsOfMove = instance1.getCardPlacement(currentGameState);

		System.out.println("#########################################\n");
		return new Move(placementsOfMove);
	}
	
	@Override
	public String getName() {
		return toString();
	}
	
}
