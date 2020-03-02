package de.upb.mlseminar;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.isml.thegamef2f.engine.board.Game;
import de.upb.isml.thegamef2f.engine.player.Player;
import de.upb.isml.thegamef2f.engine.player.RandomPlayer;
import de.upb.mlseminar.informedplayer.InformedSingleInstancePlayer;
import de.upb.mlseminar.mymcts.montecarlo.InformedMonteCarloPlayer;

public class RunGameInformed {

	private static final Logger logger = LoggerFactory.getLogger(RunGameInformed.class);
	public static void main(String[] args) {
		
		
		/*
		Player playerA1 = new InformedMonteCarloPlayer("a");
		Player playerB1 = new RandomPlayer("b");
		Game game1 = new Game(playerA1, playerB1, 4343);
		Player winner1 = game1.simulate();
		game1.getHistory().printHistory();
		*/
		
		/*
		int numPlayerAWins2=0;
		int numPlayerBWins2=0;
		
		for (int i = 0; i < 1000; i = i +10) {
			Player playerA12 = new InformedMonteCarloPlayer("a");
			Player playerB12 = new InformedSingleInstancePlayer("b",5,5,10,3);
			Game game12 = new Game(playerA12, playerB12, i);
			Player winner12 = game12.simulate();
			game12.getHistory().printHistory();
			numPlayerAWins2 += winner12 == playerA12 ? 1 : 0;
			numPlayerBWins2 += winner12 == playerB12 ? 1 : 0;

		}
		System.out.println("Scores for MonteCarlo Search approach");
		System.out.println("Player A win count = "+ numPlayerAWins2 );
		System.out.println("Player B win count = "+ numPlayerBWins2 );
		*/
		
		
		int numPlayerAWins1=0;
		int numPlayerBWins1=0;
		
		for (int i = 0; i < 1000; i = i +10) {
			Player playerA1 = new InformedMonteCarloPlayer("a");
			Player playerB1 = new RandomPlayer("b");
			Game game1 = new Game(playerA1, playerB1, i);
			Player winner1 = game1.simulate();
			//game1.getHistory().printHistory();
			numPlayerAWins1 += winner1 == playerA1 ? 1 : 0;
			numPlayerBWins1 += winner1 == playerB1 ? 1 : 0;

		}
		System.out.println("Scores for MonteCarlo Search approach");
		System.out.println("Player A win count = "+ numPlayerAWins1 );
		System.out.println("Player B win count = "+ numPlayerBWins1 );
		
		
		
		int numPlayerAWins=0;
		int numPlayerBWins=0;
		for (int i=0; i < 1000; i=i+10) {
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