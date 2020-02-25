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
		
		int numPlayerAWins1=0;
		int numPlayerBWins1=0;
		
		for (int i = 0; i < 100; i++) {
			Player playerA1 = new InformedMonteCarloPlayer("a");
			Player playerB1 = new RandomPlayer("b");
			Game game1 = new Game(playerA1, playerB1, i);
			Player winner1 = game1.simulate();
			// game1.getHistory().printHistory();
			numPlayerAWins1 += winner1 == playerA1 ? 1 : 0;
			numPlayerBWins1 += winner1 == playerB1 ? 1 : 0;

		}
		System.out.println("Scores for MonteCarlo Search approach");
		System.out.println("Player A win count = "+ numPlayerAWins1 );
		System.out.println("Player B win count = "+ numPlayerBWins1 );
		
		
		for (int i=0; i < 100; i++) {
			Player playerA = new InformedSingleInstancePlayer("a",5,5,10,3);
			Player playerB = new RandomPlayer("b");
			Game game = new Game(playerA, playerB,i);
			Player winner = game.simulate();
			//game.getHistory().printHistory();
			numPlayerAWins += winner == playerA ? 1 :0;
			numPlayerBWins += winner == playerB ? 1 :0;
			
		}
	
		System.out.println("Scores for InformedSingleInstancePlayer  approach");
		System.out.println("Player A win count = "+ numPlayerAWins );
		System.out.println("Player B win count = "+ numPlayerBWins );
		
	}

}