package de.upb.mlseminar;


import de.upb.isml.thegamef2f.engine.board.Game;
import de.upb.isml.thegamef2f.engine.player.Player;
import de.upb.isml.thegamef2f.engine.player.RandomPlayer;

public class RunGame {

	public static void main(String[] args) {
		
		Game game = new Game(new RandomPlayer("a"), new RandomPlayer("b"));
		Player winner = game.simulate();
		game.getHistory().printHistory();
	}

}