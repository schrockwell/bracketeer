package bracketeer;

import java.io.*;
import java.util.*;

import bracketeer.Parser.*;
import bracketeer.referees.Referee;

public class Main {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	
	private static final int TRIALS = 1000;
	
	public static void main(String[] args) throws Exception {
		
		if (args.length < 3 || args[0].isEmpty() || args[1].isEmpty() || args[2].isEmpty()) {
			System.out.println("Too few arguments. Required arguments: [year] [men|women] [referee class name]");
			return;
		}
		
		if (!args[1].equals("men") && !args[1].equals("women")) {
			System.out.println("The second argument must be 'men' or 'women'.");
			return;
		}
		
		String year = args[0];
		String sex = args[1];
		String refereeClassName = args[2];
		
		Referee referee;
		try {
			referee = (Referee) Class.forName("bracketeer.algorithms." + refereeClassName).newInstance();
		} catch (Exception ex) {
			System.out.println("The referee class '" + refereeClassName + "' was not found in the package bracketeer.algorithms. Check your capitalization and be sure to recompile.");
			return;			
		}
		
		Vector<Stat> stats = new Vector<Stat>();
		stats.add(new Stat("Division IWon-Lost Percentage", "Pct", "Win %"));
		stats.add(new Stat("Division IScoring Margin", "PPG", "PPG"));
		stats.add(new Stat("Division IScoring Margin", "OPP PPG", "OPP PPG"));
		stats.add(new Stat("Division IScoring Margin", "SCR MAR", "SCR MAR"));
		stats.add(new Stat("Division IRebound Margin", "RPG", "RPG"));
		stats.add(new Stat("Division IField-Goal Percentage", "FG%", "FG%"));
		stats.add(new Stat("Division IField-Goal Percentage", "FGA", "FGA"));
		stats.add(new Stat("Division IField-Goal Percentage Defense", "OPP FG", "OPP FG"));
		stats.add(new Stat("Division IField-Goal Percentage Defense", "OPP FGA", "OPP FGA"));
		stats.add(new Stat("Division IField-Goal Percentage Defense", "OPP FG%", "OPP FG%"));
		stats.add(new Stat("Division IPersonal Fouls Per Game", "PFPG", "PFPG"));
		stats.add(new Stat("Division IFree-Throw Percentage", "FT%", "FT%"));
		stats.add(new Stat("Division IFree-Throw Percentage", "FTA", "FTA"));
		stats.add(new Stat("Division IBlocked Shots Per Game", "BKPG", "BKPG"));
		stats.add(new Stat("Division ISteals Per Game", "STPG", "STPG"));
		stats.add(new Stat("Division ITurnovers Per Game", "TOPG", "TOPG"));
		stats.add(new Stat("Division ITurnover Margin", "Opp TO", "OPP TOPG"));
		stats.add(new Stat("Division IAssists Per Game", "APG", "APG"));
		stats.add(new Stat("Division IThree Pt FG Defense", "Pct", "OPP 3FG%"));
		stats.add(new Stat("Division IThree-Point Field-Goal Percentage", "3FG%", "3FG%"));
		stats.add(new Stat("Division IThree-Point Field-Goal Percentage", "3FGA", "3FGA"));
		stats.add(new Stat("Division IThree-Point Field-Goal Percentage", "GM", "GM"));
		
		if (args[1].equals("women")) {
			stats.add(new Stat("Division ITurnover Margin", "Margin", "TO RATIO"));
		} else {
			stats.add(new Stat("Division ITurnover Margin", "Ratio", "TO RATIO"));
		}
		
		String rankingsFileName = "seasons/" + year + "/rankings_" + sex + ".csv";
		String firstRoundFileName = "seasons/" + year + "/firstround_" + sex + ".csv";
		String outputFileName = "results/" + refereeClassName + ".csv";
		
		new File("results").mkdir();
		
		Hashtable<String, Team> teams = Parser.parseTeams(new FileReader(rankingsFileName), stats);
		
		Round firstRound = Parser.parseFirstRound(new FileReader(firstRoundFileName), teams);
		
		Vector<Team> firstRoundTeams = new Vector<Team>();
		
		for (Game game : firstRound) {
			firstRoundTeams.add(game.teams[0]);
			firstRoundTeams.add(game.teams[1]);
		}
		
		//Hashtable<String, CompiledStat> compiledStats = Parser.compileStats(stats, firstRoundTeams);
		
		
		
		// To play one game:
		
		Tournament t = new Tournament(firstRound, null, referee);
		t.play();
		System.out.print(t.toPrintableString());
		
		FileWriter writer = new FileWriter(outputFileName);
		writer.write(t.toSummaryString());
		writer.close();
		
		// To play many games:
		
		/*Hashtable<String, Tournament> tourneys = new Hashtable<String, Tournament>();
		
		for (int i = 0 ; i < TRIALS; i++) {
			Tournament t = new Tournament(firstRound, compiledStats, scoreable);
			Team winner = t.play();
			
			winner.wins++;
			
			tourneys.put(new String(t.getHash()), t);
		}
		
		
		Vector<Team> sortedTeams = new Vector<Team>(teams.values());
		Collections.sort(sortedTeams, new TopWins());
		
		System.out.println("\nSample bracket from the winningest team:\n");

		for (Tournament t : tourneys.values()) {
			if (t.getWinner() == sortedTeams.firstElement()) {
				System.out.print(t.toPrintableString());
				break;
			}
		}

		System.out.printf("\n%d tourneys were duplicates\n\n", TRIALS - tourneys.size());		
		System.out.println("Wins out of " + TRIALS + ":\n");
		
		for (Team team : sortedTeams)
			if (team.wins > 0)
				System.out.printf("%s: %d\n", team.toString(), team.wins);
		*/
	}
	
	private static class TopWins implements Comparator<Team> {
		
		public int compare(Team o1, Team o2) {
			if (o1.wins > o2.wins)
				return -1;
			else if (o1.wins < o2.wins)
				return 1;
			else
				return 0;
		}
		
		
	}

}
