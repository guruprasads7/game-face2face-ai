# game-face2face-ai

An AI engine implemented in Java for playing a 2 person Card game [The Game: Face to Face](https://boardgamegeek.com/boardgame/236461/game-face-face)

## Approach

The game uses UCT Based Monte Carlo Tree search (MCTS) approach to decide on the best moves (card placements) for each iteration.

Stages of MCTS approach:
* **Selection** : From the root node, the child with best UCT value is chosen for exploration.
* **Expansion** : Once the promising node is selected in the previous stage, it is expanded based on the custom rule based engine, which generates an list of possible child nodes corresponding to different possible moves.
* **Simulation** : For each of the possible children generated in the previous stage, a game is simulated between the Rule engine based player vs Random player. And the result simulated game is noted. 
* **Backpropagation** : Based on the result obtained from the previous stage, the winscore and visit count is updated for each of nodes propagating until the parent node.

## Running the game

Please clone the repository and build it using maven clean package. Add the jar game-face2face-ai.jar and the-game-f2f-engine.jar to the class paths and the Run the code using the below snippet as reference.

import de.upb.isml.thegamef2f.engine.board.Game;

import de.upb.isml.thegamef2f.engine.player.Player;

import de.upb.isml.thegamef2f.engine.player.RandomPlayer;

import de.upb.mlseminar.mymcts.montecarlo.*;

1. Initialize the player A

    `Player playerA = new InformedMonteCarloPlayer("playerA");`

2. Initialize the player B (In this case we are using Random Player as player B, but can be any of the other player)

    `Player playerB = new RandomPlayer("playerB");`

3. Start the game simulation

		Game game = new Game(playerA, playerB,i);
		Player winner = game.simulate();
		game.getHistory().printHistory();
    
