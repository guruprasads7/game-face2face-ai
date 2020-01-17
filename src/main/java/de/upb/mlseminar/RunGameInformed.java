package de.upb.mlseminar;


import de.upb.isml.thegamef2f.engine.board.Game;
import de.upb.isml.thegamef2f.engine.player.Player;
import de.upb.isml.thegamef2f.engine.player.RandomPlayer;

import de.upb.mlseminar.InformedPlayer;

public class RunGameInformed {

	public static void main(String[] args) {
		
		Game game = new Game(new InformedPlayer("a"), new InformedPlayer("b"),10);
		Player winner = game.simulate();
		game.getHistory().printHistory();
	}

}