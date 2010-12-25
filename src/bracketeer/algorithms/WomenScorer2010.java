package bracketeer.algorithms;

import bracketeer.Game;
import bracketeer.Team;

import java.util.Random;

public class WomenScorer2010 implements Scorer {

	Random _rand = new Random();
	
	private final static int AVG_POSS_LENGTH = 15;
	private final static int SEC_PER_GAME = 40 * 60;
	private final static int POSS_PER_GAME = SEC_PER_GAME / AVG_POSS_LENGTH;
	private final static int HALFTIME = SEC_PER_GAME / 2;
	
	private class TeamStatus {
		public int points = 0;
		public int fouls = 0;
		public int halfFouls = 0;
		public int timeOuts = 5;
		public double momentum = 0.0;
	}
	
	public void playGame(Game game) {

		// Potential possession outcomes:
		// Made 2FG
		// Made 3FG
		// Missed 2FG
		// Missed 3FG
		// Offensive foul
		// Defensive foul
		// Turnover
		
		TeamStatus[] status = new TeamStatus[2];
		status[0] = new TeamStatus();
		status[1] = new TeamStatus();
		
		int timeLeft = SEC_PER_GAME; // 40 minutes
		int offenseTeamIndex = _rand.nextInt(1); // Tip-off
		
		while (true) {
			if (timeLeft < 0) break;
			Team offense = game.teams[offenseTeamIndex];
			Team defense = game.teams[(offenseTeamIndex + 1) % 2];
			TeamStatus offenseStatus = status[offenseTeamIndex];
			TeamStatus defenseStatus = status[(offenseTeamIndex + 1) % 2];
			
			int possessionLength = (int)constrain(gaussian(20, 5), 5, 35);
			boolean shotMade = false;
			
			// Call a timeout if opponent has too much momentum
			if (defenseStatus.momentum > 3 && offenseStatus.timeOuts > 0) {
				offenseStatus.timeOuts--;
				offenseStatus.momentum = 1;
				defenseStatus.momentum = 1;
			}
			
			double turnoverPercent = (offense.getStat("TOPG") + defense.getStat("STPG")) / 2.0 / POSS_PER_GAME;
			if (happens(turnoverPercent)) {
				// Turnover; no shot made
				possessionLength /= 2;
				
			} else {
				// Attempt a shot
				// First determine type of shot (FG or 3FG)
				int shotTypeSum, shotTypeValue;
				if (offense.getStat("3FGA") == null) {
					// Force FG
					shotTypeValue = 0;
					shotTypeSum = 1;
				} else {
					shotTypeSum = offense.getStat("FGA").intValue() + offense.getStat("3FGA").intValue();
					shotTypeValue = _rand.nextInt(shotTypeSum);
				}
				
				if (shotTypeValue < offense.getStat("FGA")) {
					// FG attempt
					for (int i = 0; i < offenseStatus.momentum / 2; i++) {
						double shotPercent = (offense.getStat("FG%") + defense.getStat("OPP FG%")) / 2.0 / 100.0;
						if (happens(shotPercent)) {
							// FG made
							offenseStatus.points += 2;
							offenseStatus.momentum += 1;
							defenseStatus.momentum = Math.max(1, defenseStatus.momentum - 1);
							shotMade = true;
							break;
						}
					}
				} else {
					// 3FG attempt
					for (int i = 0; i < offenseStatus.momentum / 2; i++) {
						double shotPercent = (offense.getStat("3FG%") + defense.getStat("OPP 3FG%")) / 2.0 / 100.0;
						if (happens(shotPercent)) {
							// 3FG made
							offenseStatus.points += 3;
							offenseStatus.momentum += 2;
							defenseStatus.momentum = Math.max(1, defenseStatus.momentum - 1);
							shotMade = true;
							break;
						}
					}
				}
			}
			
			if (!shotMade) {
				// Decrement momentum 
				offenseStatus.momentum = Math.max(1, offenseStatus.momentum - 1);
			}
			
			timeLeft -= possessionLength;
			
			if (timeLeft <= HALFTIME && timeLeft + possessionLength >= HALFTIME) {
				// Halftime!
				offenseStatus.timeOuts = 5;
				offenseStatus.momentum = 1;
				offenseStatus.halfFouls = 0;
				defenseStatus.timeOuts = 5;
				defenseStatus.momentum = 1;
				defenseStatus.halfFouls = 0;
			}
			
			offenseTeamIndex = (offenseTeamIndex + 1) % 2;
		}
		
		game.scores[0] = status[0].points;
		game.scores[1] = status[1].points;
			
		//playPossession(timeLeft, status, startingTeam);
		
		if (game.scores[0] > game.scores[1]) {
			game.winner = game.teams[0];
			game.loser = game.teams[1];
		} else {
			game.winner = game.teams[1];
			game.loser = game.teams[0];
		}
	}
	
//	private void playPossession(int timeLeft, TeamStatus[] status, int teamWithPossession) {
//		if (timeLeft < 0)
//			return;
//	}
	
	private double gaussian(double mean, double stdDev) {
		return _rand.nextGaussian() * stdDev + mean;
	}
	
	private double constrain(double value, double min, double max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}
	
	private boolean happens(double percent) {
		// percent ranges from 0 to 1
		return _rand.nextDouble() < percent;
	}

}
