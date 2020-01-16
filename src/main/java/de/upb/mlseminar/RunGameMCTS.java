package de.upb.mlseminar;


import de.upb.isml.thegamef2f.engine.board.Game;
import de.upb.isml.thegamef2f.engine.player.Player;
import de.upb.isml.thegamef2f.engine.player.RandomPlayer;

import de.upb.mlseminar.MCTSImplementer;

public class RunGameMCTS {

	public static void main(String[] args) {
		
		Game game = new Game(new MCTSImplementer("a"), new MCTSImplementer("b"));
		Player winner = game.simulate();
		game.getHistory().printHistory();
	}

}