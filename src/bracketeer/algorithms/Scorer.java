package bracketeer.algorithms;

import bracketeer.*;

public interface Scorer {
	
	// Expected to set game.scores[], game.winner, game.loser, game.summaryStats, game.summaries
	public void playGame(Game game);
	
}
