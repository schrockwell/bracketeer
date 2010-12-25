package bracketeer.algorithms;

import java.util.*;

import bracketeer.CompiledStat;
import bracketeer.Game;
import bracketeer.Team;

public class MenScorer2009 implements Scorer {

	private static final double VARIABILITY = 0.15;
	
	public void playGame(Game game) {
		game.scores[0] = calculateScore(game.teams[0], game.teams[1], game.compiledStats);
		game.scores[1] = calculateScore(game.teams[1], game.teams[0], game.compiledStats);
	
		if (game.scores[0] > game.scores[1]) {
			game.winner = game.teams[0];
			game.loser = game.teams[1];
		} else {
			game.winner = game.teams[1];
			game.loser = game.teams[0];
		}
	}
	
	private double calculateScore(Team home, Team away, Hashtable<String, CompiledStat> compiledStats) {
		// Base expected score
		double baseScore = (0.5) * (1.0 * home.getStat("PPG") + 1.0 * away.getStat("OPP PPG")) / 2.0;
		double score = baseScore + randomAbout(baseScore * VARIABILITY);
				
		// Give a bonus for having wins
		double winBonus = (0.4) * (home.getStat("Win %"));
		score += winBonus + randomAbout(winBonus * VARIABILITY);
		
		// Decrease the score by the opponent's scoring margin
		double scoringMarginMargin = away.getStat("SCR MAR") - home.getStat("SCR MAR"); 
		double scoringMarginPoints = 0.7 * scoringMarginMargin + randomAbout(scoringMarginMargin * VARIABILITY); 
		score -= scoringMarginPoints;
		
		// Turnovers
		double turnoverPoints = 0.4 * (home.getStat("TOPG") + away.getStat("STPG"));
		score -= turnoverPoints * randomAbout(turnoverPoints * VARIABILITY);
		
		// Personal fouls
		double pfPoints = 1.5 * home.getStat("FT%") * 0.01 * away.getStat("PFPG"); 
		score += pfPoints + randomAbout(pfPoints * VARIABILITY);
		
		double reboundDiff = home.getStat("RPG") - away.getStat("RPG");
		score += 1 * reboundDiff + randomAbout(reboundDiff * VARIABILITY);
		
		double opponentBlocks = 1 * away.getStat("BKPG");
		score -= opponentBlocks + randomAbout(opponentBlocks * VARIABILITY);
		
		return score;
	}
	
	private double randomAbout(double range) {
		
		double sign = (Math.random()  - 0.5);
		sign = (sign < 0 ? -1 : 1);
		
		return sign * range;
		
	}
		
}
