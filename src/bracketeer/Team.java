package bracketeer;

import java.util.*;

public class Team {
	
	public String name;
	public Hashtable<String, Double> stats = new Hashtable<String, Double>();
	public int seed = 0;
	public int wins = 0;
	
	public Team(String name) {
		this.name = name;
	}
	
	public Team(String name, int seed) {
		this.name = name;
		this.seed = seed;
	}
	
	public String toString() {
		if (seed >= 10)
			return String.format("%d %s", seed, name);
		else if (seed > 0)
			return String.format(" %d %s", seed, name);
		else
			return name;
	}
	
	public Double getStat(String stat) {
		return this.stats.get(stat);
	}
	
}
