package bracketeer.referees;

import java.util.Hashtable;
import bracketeer.*;

public class WomenReferee2009 implements Referee {

	public void playGame(Game game) {
		
		game.summaries[0] = new Hashtable<String, String>();
		game.summaries[1] = new Hashtable<String, String>();
		
		game.summaryStats.add("Game");
		game.summaries[0].put("Game", game.name);
		game.summaries[1].put("Game", game.name);
		
		game.summaryStats.add("Team");		
		
		game.summaryStats.add("BASE PTS");
		game.summaryStats.add("FG BONUS");
		game.summaryStats.add("WIN BONUS");
		game.summaryStats.add("TO PTS");
		game.summaryStats.add("TO PTS LOST");
		game.summaryStats.add("PF PTS");
		game.summaryStats.add("RB PTS");
		game.summaryStats.add("BLK PTS");
		game.summaryStats.add("PTS");
		
		game.scores[0] = calculateScore(game.teams[0], game.teams[1], game.compiledStats, game.summaries[0]);
		game.scores[1] = calculateScore(game.teams[1], game.teams[0], game.compiledStats, game.summaries[1]);
	
		if (game.scores[0] > game.scores[1]) {
			game.winner = game.teams[0];
			game.loser = game.teams[1];
		} else {
			game.winner = game.teams[1];
			game.loser = game.teams[0];
		}
	}
	
	
	private Double calculateScore(Team home, Team away,
			Hashtable<String, CompiledStat> compiledStats,
			Hashtable<String, String> summary) {
		
		// Base score (30% weight)
		Double basePts = 0.4 * (0.4 * home.getStat("PPG") + 0.6 * away.getStat("OPP PPG"));
		Double pts = basePts;
		
		// Shooting bonus
		Double fgBonus = basePts * home.getStat("FG%") * 0.01;
		pts += fgBonus;
		
		// Win bonus (10% of the base points for 100%)
		Double winBonus = 0.1 * basePts * home.getStat("Win %") * 0.01;
		pts += winBonus;
		
		// Turnovers (gain 2 points for each out-turnover and lose 2 the same way)
		Double turnoverLostPoints = -2.0 * (home.getStat("TOPG") + away.getStat("STPG"));
		pts += turnoverLostPoints;
		
		Double turnoverGainedPoints = 2.0 * (home.getStat("STPG") + away.getStat("TOPG"));
		pts += turnoverGainedPoints;
		
		// Personal fouls (gain 2 points per PF at FT%)
		Double pfPts = 2 * home.getStat("FT%") * 0.01 * away.getStat("PFPG"); 
		pts += pfPts;
		
		// Rebounds (gain 2 points for each out-rebound)
		Double reboundPts = 2.0 * (home.getStat("RPG") - away.getStat("RPG"));
		pts += reboundPts;
		
		// Block (gain 2 points for each out-block)
		Double blockPts = 2.0 * (home.getStat("BKPG") - away.getStat("BKPG"));
		pts += blockPts;
		
		// Summarize
		summary.put("BASE PTS", basePts.toString());
		summary.put("FG BONUS", fgBonus.toString());
		summary.put("WIN BONUS", winBonus.toString());
		summary.put("TO PTS", turnoverGainedPoints.toString());
		summary.put("TO PTS LOST", turnoverLostPoints.toString());
		summary.put("PF PTS", pfPts.toString());
		summary.put("RB PTS", reboundPts.toString());
		summary.put("BLK PTS", blockPts.toString());
		summary.put("PTS", pts.toString());
		summary.put("Team", home.toString());
		
		return pts;
		
	}
	
	private double randomAbout(double range) {
		
		double sign = (Math.random()  - 0.5);
		sign = (sign < 0 ? -1 : 1);
		
		return sign * range;
		
	}
	
	private double randomRange(double start, double end) {
		return Math.random() * (end - start) + start;
	}

}

/*
-- CONSTRUCTOR:

game.summaryStats.add("FG %");
game.summaryStats.add("FG PTS");

game.summaryStats.add("3FG %");
game.summaryStats.add("3FG PTS");

game.summaryStats.add("FT %");
game.summaryStats.add("FT PTS");


-- PLAYGAME():

double shootingPossessions = 60.0;

// Turnovers
shootingPossessions -= home.getStat("TOPG");
shootingPossessions += away.getStat("TOPG");

// Steals
shootingPossessions += home.getStat("STPG");
shootingPossessions -= away.getStat("STPG");

// Rebounds
shootingPossessions += 1.0 * home.getStat("RPG");
shootingPossessions -= 1.0 * away.getStat("RPG");

// Blocks
shootingPossessions -= 1.0 * away.getStat("BKPG");

// Assists
shootingPossessions += 0.3 * home.getStat("APG");

// Points
//Double fgRatio = home.getStat("FGA") / (home.getStat("FGA") + home.getStat("3FGA") + home.getStat("FTA"));
//Double fg3Ratio = home.getStat("3FGA") / (home.getStat("FGA") + home.getStat("3FGA") + home.getStat("FTA"));
//Double ftRatio = home.getStat("FTA") / (home.getStat("FGA") + home.getStat("3FGA") + home.getStat("FTA"));

Double fgPercent = 0.4 * home.getStat("FG%") + 0.6 * away.getStat("OPP FG%");
Double fgPoints = 2 * (0.75) * shootingPossessions * fgPercent * 0.01;

Double fg3Percent;
if (home.getStat("3FG%") == null && away.getStat("OPP 3FG%") == null)
	fg3Percent = 30.0;
else if (home.getStat("3FG%") == null)
	fg3Percent = away.getStat("OPP 3FG%");
else if (away.getStat("OPP 3FG%") == null)
	fg3Percent = home.getStat("3FG%");
else
	fg3Percent = 0.5 * home.getStat("3FG%") + 0.5 * away.getStat("OPP 3FG%");

Double fg3Points = 3 * (0.20) * shootingPossessions * fg3Percent * 0.01;
Double ftPoints = 1.5 * home.getStat("FT%") * 0.01 * away.getStat("PFPG");

Double score = ftPoints + fgPoints + fg3Points;
score = (score + 0.8 * home.getStat("PPG") + 1.2 * away.getStat("OPP PPG")) / 3;
score += randomAbout(score * 0.0);

// Save stats
summary.put("Team", home.name);
summary.put("FG %", fgPercent.toString());
summary.put("FG PTS", fgPoints.toString());
summary.put("3FG %", fg3Percent.toString());
summary.put("3FG PTS", fg3Points.toString());
summary.put("FT %", home.getStat("FT%").toString());
summary.put("FT PTS", ftPoints.toString());
summary.put("PTS", score.toString());

return score;
*/