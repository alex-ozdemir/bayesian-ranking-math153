package aozdemir.bayesianFinal;


import java.util.Arrays;
import java.util.List;

import jdistlib.Normal;

public class Team {
	String name;
	String abrv;
	double[] meanDistY;
	double[] sdDistY;
	
	static int resolutionMean = 120 + 1;//Remember to add 1!
	static double lowerMean = 0.0;
	static double upperMean = 20.0;
	static double priorSd = 6.0;
	
	static int resolutionSd = 40 + 1;//Remember to add 1!
	static double lowerSd = 0.0;
	static double upperSd = 5.0;
	
	static double[] meanDistX;
	static double[] sdDistX;
	static boolean DistXSet = false;
	
	Team(String name, String abrv) {
		this.name = name;
		this.abrv = abrv;
		
		this.meanDistY = new double[resolutionMean];
		
		//If not done already, fill out the skills, using a Normal Distribution
		if (!DistXSet) {
			//Set the possibilities for the mean
			meanDistX = new double[resolutionMean];
			for (int i = 0; i < resolutionMean; i++) {
				meanDistX[i] = lowerMean + (((upperMean - lowerMean) * i) / ((double) (resolutionMean - 1)));
			}
			
			//Set the possibilities for the sd
			sdDistX = new double[resolutionSd];
			for (int i = 0; i < resolutionSd; i++) {
				sdDistX[i] = lowerSd + (((upperSd - lowerSd) * i) / ((double) (resolutionSd - 1)));
			}
			
			//Record that you've set the X parts of the distributions;
			DistXSet = true;
		}
		
		//Fill out the skill distribution
		for (int i = 0; i < resolutionMean; i++) {
			this.meanDistY[i] = Normal.density(meanDistX[i], (upperMean + lowerMean) / 2.0, priorSd, false);
		}
		
		//Normalize it:
		this.normalizeMean();

		//Fill out the sd distribution
		this.sdDistY = new double[resolutionSd];
		double density = 1.0 / (upperSd - lowerSd);
		Arrays.fill(this.sdDistY, density);

		//Normalize it
		this.normalizeSd();
		
	}
	
	Team(List<String[]> data) {
		//length is 5
		//printStrArList(data);
		this.name = data.get(0)[0];
		//System.out.println(this.name);
		this.abrv = data.get(0)[1];
		//System.out.println("Has the Resolution/X been set? " + DistXSet);
		if (!DistXSet) {
			resolutionMean = -1;
			resolutionSd = -1;
			for (int i = 0; i < data.get(1).length; i ++) {
				//System.out.println("    "+ i + ". " + data.get(1)[i]);
				if (data.get(1)[i].equals("")) {
					//System.out.println("Found end of mean: " + i);
					resolutionMean = i;
					break;
				}
			}
			//System.out.println();
			for (int i = 0; i < data.get(3).length; i ++) {
				//System.out.println("    " + i + ". " + data.get(3)[i]);
				if (data.get(3)[i].equals("")) {
					//System.out.println("Found end of sd: " + i);
					resolutionSd = i;
					break;
				}
			} 
			if(resolutionMean == -1) resolutionMean = data.get(1).length;
			if(resolutionSd == -1) resolutionSd = data.get(3).length;

			//System.out.println("Mean Res: " + Team.resolutionMean);
			//System.out.println("SD Res: " + Team.resolutionSd);
			meanDistX = new double[resolutionMean];
			//System.out.println("Parsing Mean X:");
			for (int i = 0; i < resolutionMean; i++) {
				meanDistX[i] = Double.parseDouble(data.get(2)[i]);
				//System.out.println("  " + data.get(2)[i] + " -> " + Double.parseDouble(data.get(2)[i]));
			}
			lowerMean = meanDistX[0];
			upperMean = meanDistX[resolutionMean-1];
			sdDistX = new double[resolutionSd];
			//System.out.println("Parsing Sd X:");
			for (int i = 0; i < resolutionSd; i++) {
				sdDistX[i] = Double.parseDouble(data.get(4)[i]);
				//System.out.println("  " + data.get(2)[i] + " -> " + Double.parseDouble(data.get(2)[i]));
			}
			lowerSd = sdDistX[0];
			upperSd = sdDistX[resolutionSd-1];
			DistXSet = true;
		}
		this.meanDistY = new double[resolutionMean];
		//System.out.println("Parsing Mean Y:");
		for (int i = 0; i < resolutionMean; i++) {
			this.meanDistY[i] = Double.parseDouble(data.get(1)[i]);
			//System.out.println("  " + data.get(1)[i] + " -> " + Double.parseDouble(data.get(1)[i]));
		}
		this.sdDistY = new double[resolutionSd];
		//System.out.println("Parsing SD Y:");
		for (int i = 0; i < resolutionSd; i++) {
			this.sdDistY[i] = Double.parseDouble(data.get(3)[i]);
			//System.out.println("  " + data.get(3)[i] + " -> " + Double.parseDouble(data.get(3)[i]));
		}
	}
	
	public void normalizeMean() {
		//System.out.println("Normalize has been called!");
		//System.out.println("   " + Arrays.toString(this.meanDistY));
		double sum = 0.0;
		for (int i = 0; i < resolutionMean; i++) {
			if (i == 0) {
				sum += this.meanDistY[i] * (Team.meanDistX[1] - Team.meanDistX[0]);				
			} else {
				sum += this.meanDistY[i] * (Team.meanDistX[i] - Team.meanDistX[i - 1]);				
				
			}

		}
		//System.out.println("   Sum: "+ sum);
		//Correct it to one
		for (int i = 0; i < resolutionMean; i++) {
			this.meanDistY[i] /= sum;
		}
		/*
		System.out.println("   " + Arrays.toString(this.meanDistY));
		
		sum = 0.0;
		for (int i = 0; i < resolutionMean; i++) {
			if (i == 0) {
				sum += this.meanDistY[i] * (Team.meanDistX[1] - Team.meanDistX[0]);				
			} else {
				sum += this.meanDistY[i] * (Team.meanDistX[i] - Team.meanDistX[i - 1]);				
				
			}

		}
		System.out.println("   New Sum: "+ sum);*/
		
	}
	
	public void normalizeSd() {
		double sum = 0.0;
		for (int i = 0; i < resolutionSd; i++) {
			if (i == 0) {
				sum += this.sdDistY[i] * (Team.sdDistX[1] - Team.sdDistX[0]);				
			} else {
				sum += this.sdDistY[i] * (Team.sdDistX[i] - Team.sdDistX[i - 1]);				
				
			}

		}
		//System.out.println("Normalize initial sum: " + sum);
		//System.out.println("Normalize initial array: " + Arrays.toString(this.sdDistY));
		//System.out.println("   Sum: "+ sum);
		//Correct it to one
		for (int i = 0; i < resolutionSd; i++) {
			this.sdDistY[i] /= sum;
		}
		/*
		sum = 0.0;
		for (int i = 0; i < resolutionSd; i++) {
			if (i == 0) {
				sum += this.sdDistY[i] * (Team.sdDistX[1] - Team.sdDistX[0]);				
			} else {
				sum += this.sdDistY[i] * (Team.sdDistX[i] - Team.sdDistX[i - 1]);				
				
			}

		}
		System.out.println("Normalize final sum: " + sum);
		System.out.println("Normalize final array: " + Arrays.toString(this.sdDistY));*/
	}
	
	public double pWinVs(Team other) {
		//d[loser mean] (its identical for winner and loser)
		double dm = (Team.upperMean - Team.lowerMean) / (Team.resolutionMean - 1);
		//d[loser sd] (its identical for winner and loser)
		double ds = (Team.upperSd - Team.lowerSd) / (Team.resolutionSd - 1);
		
		double dP;//The combined "probability distribution
		
		double pWin = 0;
		for (int sLoseI = 0; sLoseI < Team.resolutionSd; sLoseI++) {
			for (int mWinI = 0; mWinI < Team.resolutionMean; mWinI++) {
				for (int sWinI = 0; sWinI < Team.resolutionSd; sWinI++) {
					for (int mLoseI = 0; mLoseI < Team.resolutionMean; mLoseI++) {
						dP = other.meanDistY[mLoseI] * dm * this.meanDistY[mWinI] * dm * this.sdDistY[sWinI] * ds
								* other.sdDistY[sLoseI] * ds;
						pWin += Normal.cumulative(0,
								Team.meanDistX[mLoseI] - Team.meanDistX[mWinI],
								Math.sqrt(Math.pow(Team.sdDistX[sWinI], 2.0)+ Math.pow(Team.sdDistX[sLoseI], 2.0))) * dP;
					}
				}
			}
		}
		return pWin;
	}

	public double estimateMean() {
		double estimate = 0.0;

		double dm = (Team.upperMean - Team.lowerMean) / (Team.resolutionMean - 1);
		//d[loser sd] (its identical for winner and loser)
		
		for (int mI = 0; mI < Team.resolutionMean; mI++) {
			estimate += this.meanDistY[mI] * Team.meanDistX[mI] * dm;
		}
		
		return estimate;
	}
	
	public double estimateSd() {
		double estimate = 0.0;

		double ds = (Team.upperSd - Team.lowerSd) / (Team.resolutionSd - 1);
		//d[loser sd] (its identical for winner and loser)
		
		for (int mI = 0; mI < Team.resolutionSd; mI++) {
			estimate += this.sdDistY[mI] * Team.sdDistX[mI] * ds;
		}
		
		return estimate;
	}
	
	public void plotMean(String fileName) {
		Plot.plot(Team.meanDistX, this.meanDistY, this.abrv + "Mean", fileName, this.name + " Mean Performance", "Mean", "Probability", false);
	}
	public void plotSD(String fileName) {
		Plot.plot(Team.sdDistX, this.sdDistY, this.abrv + "SD", fileName, this.name + " SD of Performance", "SD", "Probability", false);
	}
	
	public static void printStrArList(List<String[]> list) {
		System.out.println("List:");
		for(int i = 0; i < list.size(); i++) {
			System.out.println("  " + list.get(1).toString());
		}
		System.out.println("/List");
	}
	
	
	public void dump() {
		System.out.println();
		System.out.println("Team : " + this.name + " (" + this.abrv + ")");
		System.out.println("Mean Distribution:");
		System.out.println("  Y: " + Arrays.toString(this.meanDistY));
		System.out.println("  X: " + Arrays.toString(Team.meanDistX));
		System.out.println("SD Distribution:");
		System.out.println("  Y: " + Arrays.toString(this.sdDistY));
		System.out.println("  X: " + Arrays.toString(Team.sdDistX));
		System.out.println();
	}

	public void printEst() {
		System.out.println(this.abrv + ": Mean = " + this.estimateMean() + ", SD = " + this.estimateSd());		
	}
}

