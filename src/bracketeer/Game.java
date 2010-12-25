package bracketeer;

import java.util.*;

import bracketeer.referees.Referee;

public class Game {

	public Team[] teams = new Team[2];
	public Game[] previousGames = new Game[2];
	public Hashtable<String, String>[] summaries = new Hashtable[2];
	public Vector<String> summaryStats = new Vector<String>();
	public Game nextGame;
	public String name;
	public Team winner, loser;
	public double[] scores = new double[2];
	public Hashtable<String, CompiledStat> compiledStats;
	public Referee scorer;
	
	public Team play() {
		scorer.playGame(this);
		return winner;
	}
	
	public Game() {};
	
	public Game(Team[] teams) {
		this.teams = teams;
	}
	
	public Game(Team[] teams, Referee scoreable) {
		this(teams);
		this.scorer = scoreable;
	}
	
	public void addTeam(Team team) {
		for (int i = 0; i < teams.length; i++) {
			if (teams[i] == null) {
				teams[i] = team;
				return;
			}
		}
	}
	
	public String toString() {
		return String.format("%s (%s vs. %s)", name, teams[0].toString(), teams[1].toString());
	}
	
}
