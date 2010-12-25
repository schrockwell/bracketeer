package bracketeer;

import java.util.*;

import au.com.bytecode.opencsv.*;
import java.io.*;

public class Parser {
	
	public static Hashtable<String, Team> parseTeams(Reader reader, Collection<Stat> stats) {
		
		CSVReader csvReader = new CSVReader(reader);
		return parseStats(csvReader, stats);
		
	}
	
	private static Hashtable<String, Team> parseStats(CSVReader reader, Collection<Stat> stats) {
		
		Hashtable<String, Team> teams = new Hashtable<String, Team>();
		List<String[]> lines; 
		try {
			lines = reader.readAll();
		} catch (IOException e) {
			System.out.println("Error reading the CSV file.");
			return null;
		}
		
		for (int i = 0; i < lines.size(); i++) {
			String[] tokens = lines.get(i);
			
			if (tokens.length == 1 && !tokens[0].trim().equals("")) { 		// If this is a single token
				for (Stat stat : stats) { 				 				// Check all the stats
					if (stat.table.equals(tokens[0])) { 	    // And see if this table has that stat
						
						int j = i + 1;
						
						for (; j < lines.size(); j++)
							if (lines.get(j)[0].equalsIgnoreCase("Rank"))
								break;
						
						String[] columnTitles = lines.get(j);
						int columnIndex = findColumnIndex(columnTitles, stat.column);
						
						j++;
						
						while (true) {
							String[] values = lines.get(j);
							if ((values.length == 1 && values[0].trim().length() == 0) ||
									values[0].equals("Reclassifying"))
								break;
							
							Team team = getTeam(teams, values[1]);
							team.stats.put(stat.name, Double.parseDouble(values[columnIndex]));
							
							j += 1;
						}
					}
				}
			}
		}
				
		return teams;
		
	}
	
	private static Team getTeam(Hashtable<String, Team> teams, String name) {
		Team team = teams.get(name);
		if (team == null) {
			team = new Team(name);
			teams.put(name, team);
		}
		return team;
	}
	
	private static int findColumnIndex(String[] columns, String column) {
		for (int i = 0; i < columns.length; i++)
			if (columns[i].equals(column))
				return i;
		return -1;
	}
	
	public static class Stat {
		
		public String table;
		public String column;
		public String name;
		
		public Stat(String table, String column, String name) {
			this.table = table;
			this.column = column;
			this.name = name;
		}
		
		public String toString() {
			return name;
		}
		
	}

	public static Round parseFirstRound(Reader reader, Hashtable<String, Team> teams) throws Exception {
		Round games = new Round();
		
		CSVReader csvReader = new CSVReader(reader);
		
		List<String[]> lines; 
		try {
			lines = csvReader.readAll();
		} catch (IOException e) {
			System.out.println("Error reading the CSV file.");
			return null;
		}
		
		for (String[] tokens : lines) {
			if (tokens.length == 1 && tokens[0].trim().length() == 0)
				continue;
			
			Team[] gameTeams = new Team[2];
			gameTeams[0] = teams.get(tokens[0]);
			gameTeams[1] = teams.get(tokens[1]);
			
			if (gameTeams[0] == null)
				throw new Exception("Team not found: " + tokens[0]);
			
			if (gameTeams[1] == null)
				throw new Exception("Team not found: " + tokens[1]);
			
			if (tokens.length >= 4) {
				gameTeams[0].seed = Integer.parseInt(tokens[2]);
				gameTeams[1].seed = Integer.parseInt(tokens[3]);
			}
			
			for (int i = 0; i < 2; i++)
				if (gameTeams[i] == null)
					throw new Exception("Team not found: " + tokens[i]);
			
			Game game = new Game(gameTeams);
			game.name = String.format("R0 G%d", games.size());
			games.add(game);		
		}
		
		return games;
	}

	public static Hashtable<String, CompiledStat> compileStats(Vector<Stat> stats, Collection<Team> teams) {
		
		Vector<Team> teamVector = new Vector<Team>(teams);
		Hashtable<String, CompiledStat> compiledStats = new Hashtable<String, CompiledStat>();
		
		for (Stat stat : stats) {
			double[] values = new double[teams.size()];
			for (int i = 0; i < teams.size(); i++) {
				Team team = teamVector.get(i);
				values[i] = team.getStat(stat.name);
			}
			
			compiledStats.put(stat.name, new CompiledStat(stat.name, values));
		}
		
		return compiledStats;
		
	}
	
}
