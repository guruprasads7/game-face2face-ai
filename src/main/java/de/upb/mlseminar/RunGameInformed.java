package de.upb.mlseminar;


import de.upb.isml.thegamef2f.engine.board.Game;
import de.upb.isml.thegamef2f.engine.player.Player;
import de.upb.isml.thegamef2f.engine.player.RandomPlayer;
import de.upb.mlseminar.informedplayer.InformedSingleInstancePlayer;
import de.upb.mlseminar.mymcts.montecarlo.InformedMonteCarloPlayer;

public class RunGameInformed {

	public static void main(String[] args) {
		
		int numPlayerAWins=0;
		int numPlayerBWins=0;
		
		Player playerA1 = new InformedMonteCarloPlayer("a");
		Player playerB1 = new RandomPlayer("b");
		Game game1 = new Game(playerA1, playerB1,1121231213);
		Player winner1 = game1.simulate();
		game1.getHistory().printHistory();
		
		/*
		for (int i=10; i < 1000; i = i + 1000) {
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
		*/
	}

}