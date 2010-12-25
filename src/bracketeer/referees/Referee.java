package bracketeer.referees;

import bracketeer.*;

public interface Referee {
	
	// Expected to set game.scores[], game.winner, game.loser, game.summaryStats, game.summaries
	public void playGame(Game game);
	
}