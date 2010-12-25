package bracketeer;

import java.io.IOException;
import java.io.StringWriter;
import java.security.*;
import java.util.*;
import au.com.bytecode.opencsv.*;

import bracketeer.algorithms.Scorer;

public class Tournament {

	private static MessageDigest md5 = null;
	
	public int matches = 0;
	
	private Vector<Round> rounds = new Vector<Round>(); 
	private Hashtable<String, CompiledStat> compiledStats;
	private Scorer scoreable;
	
	public Tournament(Round firstRound, Hashtable<String, CompiledStat> compiledStats, Scorer scoreable) {
		
		if (md5 == null) {
			try {
				md5 = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
		
		this.compiledStats = compiledStats;
		this.scoreable = scoreable;
		
		Round duplicate = new Round();
		
		for (Game game : firstRound) {
			Game dupeGame = new Game(game.teams, scoreable);
			dupeGame.name = game.name;
			duplicate.add(dupeGame);
		}
		
		rounds.add(duplicate);
		appendNextRound(duplicate);
	}
	
	public void appendNextRound(Round round) {
		Round nextRound = new Round();
		rounds.add(nextRound);
		
		int roundNumber = rounds.indexOf(nextRound);
		int gameCount = 0;
		
		for (int i = 0; i < round.size(); i += 2) {
			Game newGame = new Game();
			newGame.scorer = scoreable;
			
			Game[] previousGames = new Game[] { round.get(i), round.get(i + 1) };
			for (Game previousGame : previousGames)
				previousGame.nextGame = newGame;
			
			newGame.previousGames = previousGames;
			newGame.name = String.format("R%d G%d", roundNumber, gameCount);
			nextRound.add(newGame);
			gameCount++;
		}
		
		
		if (nextRound.size() == 1)
			return;
		
		appendNextRound(nextRound);
	}
	
	public Team play() {
		return play(rounds.get(0));
	}
	
	private Team play(Round round) {
		for (Game game : round) {
			game.compiledStats = compiledStats;
			Team winner = game.play();
			
			if (round.size() == 1)
				return winner;
			
			game.nextGame.addTeam(winner);
			
		}
	
		return play(rounds.get(rounds.indexOf(round) + 1));
	}

	//private final int PRINT_HEIGHT = 128;  // Four lines per first-round
	private final int PRINT_HEIGHT = 96; // Three lines per first-round
	//private final int PRINT_HEIGHT = 64; // Two lines per first-round
	private final int COLUMN_PADDING = 1;
	private final int COLUMN_PREFIX_MARGIN = 2; // length of " /" or " \"
	private final int COLUMN_SUFFIX_MARGIN = 8; // length of " - xx <<"
	private final int EMPTY_ROW_PULL = 4;
	
	public String toPrintableString() {
		StringBuilder builder = new StringBuilder();
		
		int maxNameLength = 0;
		for (Game game : rounds.get(0)) 
			maxNameLength = Math.max(Math.max(game.teams[0].toString().length(), game.teams[1].toString().length()), maxNameLength);
		
		int columnWidth = maxNameLength + COLUMN_PADDING +
			COLUMN_PREFIX_MARGIN + COLUMN_SUFFIX_MARGIN;
		
		for (int line = 0; line < PRINT_HEIGHT; line++) {
			for (int round = 0; round < rounds.size(); round++) {
				if (round == 0)
					builder.append(teamAt(line, round, columnWidth - COLUMN_PREFIX_MARGIN));
				else
					builder.append(teamAt(line, round, columnWidth));
			}
			builder.append("\n");
		}
			
		return builder.toString();
	}
	
	private String teamAt(int line, int roundNumber, int columnWidth) {
		Round round = rounds.get(roundNumber);
		Team team = null;
		String teamName = "";
		int teamScore = 0;
		
		int linesPerGame = PRINT_HEIGHT / round.size();
		int lineGame = (int)Math.floor((double)line / (double)linesPerGame);
		
		int startLine = lineGame * linesPerGame;
		int bottomGameLine = startLine + linesPerGame / 2;
		int topGameLine = bottomGameLine - 1;
		
		Game game = round.get(lineGame);
		if (line == topGameLine) {
			team = game.teams[0];
			if (roundNumber > 0)
				teamName += "\\ ";
			teamScore = (int)game.scores[0];
		} else if (line == bottomGameLine) {
			team = round.get(lineGame).teams[1];
			if (roundNumber > 0)
				teamName += "/ ";
			teamScore = (int)game.scores[1];
		}
		
		if (team != null) {
			teamName += team.toString();
			teamName += String.format(" - %d", teamScore);
			if (team == game.winner) {
				teamName += " <";
				if (game.winner.seed > game.loser.seed)
					teamName += "<<";
			}
		}
		
		int end = teamName.length() == 0 ? columnWidth - EMPTY_ROW_PULL * roundNumber : columnWidth; 
		for (int i = teamName.length(); i < end; i++)
			teamName += " ";
		
		return teamName;
	}
	
	public String toSummaryString() {

		StringWriter stringWriter = new StringWriter();
		CSVWriter writer = new CSVWriter(stringWriter);
		
		String[] columns = new String[0];
		
		for (Round round : rounds) {
			for (Game game : round) {
				for (int i = 0; i < 2; i++) {
					if (columns.length == 0) {
						if (game.summaryStats.size() == 0)
							return "";
						
						columns = game.summaryStats.toArray(columns);
						writer.writeNext(columns);
					}
					
					String[] values = new String[columns.length];
					
					for (int j = 0; j < values.length; j++)
						values[j] = game.summaries[i].get(columns[j]).toString();
					
					writer.writeNext(values);
				}
			}
		}

		
		return stringWriter.toString();
				
	}
	
	public byte[] getHash() {		
		String id = this.rounds.toString();
		return md5.digest(id.getBytes());
	}
	
	public Team getWinner() {
		return rounds.lastElement().get(0).winner;
	}
}
