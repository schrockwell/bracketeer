package bracketeer;

import java.util.*;

public class CompiledStat {
	public String name;
	public double mean;
	public double dev;
	public double min;
	public double max;
	
	public CompiledStat(String name, double[] values) {
		this.name = name;
		computeStats(values);
	}
	
	private void computeStats(double[] values) {
		
		double sum = 0.0;
		
		min = values[0];
		max = values[0];
		
		for (double value : values) {
			sum += value;
			min = Math.min(min, value);
			max = Math.max(max, value);
		}
		
		mean = sum / (double)values.length;
		dev = calculateDev(values);
	}

	private double calculateDev(double[] values) {
		return Math.pow(calculateVar(values), 0.5);
	}


	static double calculateVar(double[] values) {
		double total = 0.0;
		double sTotal = 0.0;
		double scalar = 1.0/(double)(values.length - 1);
		
		for (int i = 0; i < values.length; i++) {
			total += values[i];
			sTotal += Math.pow(values[i], 2);
		}
		
		return (scalar * (sTotal - (Math.pow(total, 2) / values.length)));
	}	
	
	public String toString() {
		return name;
	}
}