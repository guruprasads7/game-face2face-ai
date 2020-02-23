package de.upb.mlseminar;


import de.upb.isml.thegamef2f.engine.board.Game;
import de.upb.isml.thegamef2f.engine.player.Player;
import de.upb.isml.thegamef2f.engine.player.RandomPlayer;
import de.upb.mlseminar.informedplayer.InformedSingleInstancePlayer;
import de.upb.mlseminar.informedplayer.InformedPlayerOrchestrator;

public class RunGameInformed {

	public static void main(String[] args) {
		
		int numPlayerAWins=0;
		int numPlayerBWins=0;
		for (int i=0; i < 100; i = i + 100) {
			Player playerA = new InformedPlayerOrchestrator("a");
			Player playerB = new RandomPlayer("b");
			Game game = new Game(playerA, playerB,i);
			Player winner = game.simulate();
			game.getHistory().printHistory();
			numPlayerAWins += winner == playerA ? 1 :0;
			numPlayerBWins += winner == playerB ? 1 :0;
			
		}
		System.out.println("Player A win count = "+ numPlayerAWins );
		System.out.println("Player B win count = "+ numPlayerBWins );
	}

}