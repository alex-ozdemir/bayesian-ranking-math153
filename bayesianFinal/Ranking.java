package aozdemir.bayesianFinal;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import jdistlib.Normal;

public class Ranking {
	
	java.util.List<String[]> games;
	Team[] teams;
	public Ranking() {
		
	}
	
	public Ranking(String fileName) throws IOException {
		CSVReader reader = new CSVReader(new FileReader("data/" + fileName + ".csv"));
	    this.games = reader.readAll();
	    reader.close();
	    this.games.remove(0);
	    ArrayList<String> teamsSeen = new ArrayList<String>();
	    for (int i = 0; i < games.size(); i++) {
	    	if (!teamsSeen.contains(games.get(i)[0])) teamsSeen.add(games.get(i)[0]);
	    	if (!teamsSeen.contains(games.get(i)[1])) teamsSeen.add(games.get(i)[1]);
	    }
	    this.teams = new Team[teamsSeen.size()];
	    for (int i = 0; i < teamsSeen.size(); i++) {
	    	teams[i] = new Team(teamsSeen.get(i), teamsSeen.get(i));
	    }
	    
	}
	
	public Ranking(java.util.List<String[]> games, Team[] teams) {
		this.games = games;
		this.teams = teams;
		for (int i = 0; i < teams.length; i++) {
			for (int j = 0; j < i; j++) {
				if(teams[i].name.equals(teams[j].name)) {
					System.err.println("ERROR: Two teams have the same name: " + teams[i].name + " - illegal");
				}
				if(teams[i].abrv.equals(teams[j].abrv)) {
					System.err.println("ERROR: Two teams have the same abbreviation: " + teams[i].abrv + " - illegal");
				}
			}
		}
	}
	
	public Team getTeam(String abrv) {
		for(int i = 0; i < this.teams.length; i++) {
			if (abrv.equals(this.teams[i].abrv)) return this.teams[i];
		}
		System.err.println("There is no team with the abreviation: " + abrv);
		return null;
	}
	
	public void updateWithAllGames() {
		for (int i = 0; i < this.games.size(); i++) {
			this.updateWithGame(this.games.get(i));
			System.out.println("" + (i + 1) + "/" + this.games.size());
		}
	}
	
	public void updateWithManyGames(int number) {
		if (number >= this.games.size()) {
			System.out.println("ERROR: There aren't that "+number+" games to update with!");
			return;
		}
		for (int i = 0; i < number; i++) {
			this.updateWithGame(this.games.get(i));
			System.out.println("" + (i + 1) + "/" + number);
		}
	}
	
	
	public void updateWithGame(String[] game) {
		
		// First, we find the indices of the winning and losing team
		//  and check for errors
		int winnerIndex = -1;
		int loserIndex = -1;
		for (int i = 0; i < teams.length; i++) {
			if (game[0].equals(this.teams[i].abrv)) winnerIndex = i;
			if (game[1].equals(this.teams[i].abrv)) loserIndex = i;			
		}
		if (winnerIndex == -1) System.err.println("ERROR: No Winning Team, " + game[0] + " found!"); 
		else if (loserIndex == -1) System.err.println("ERROR: No Losing Team, " + game[1] + " found!");
		else {
			//Handy variables
			Team winner = this.teams[winnerIndex];
			Team loser = this.teams[loserIndex];


			// Then we create new arrays to hold our posterior
			double[] newWinnerMeanDistY = winner.meanDistY;
			double[] newLoserMeanDistY = loser.meanDistY;
			double[] newWinnerSdDistY = winner.sdDistY;
			double[] newLoserSdDistY = loser.sdDistY;

			//Establish a few differentials
			//d[loser mean] (its identical for winner and loser)
			double dm = (Team.upperMean - Team.lowerMean) / (Team.resolutionMean - 1);
			//d[loser sd] (its identical for winner and loser)
			double ds = (Team.upperSd - Team.lowerSd) / (Team.resolutionSd - 1);


			//Update winner mean
			//System.out.println("Winner Mean Update...");
			double dP;
			for (int mWinI = 0; mWinI < Team.resolutionMean; mWinI++) {
				double pWinnerMean = 0;
				for (int mLoseI = 0; mLoseI < Team.resolutionMean; mLoseI++) {
					for (int sWinI = 0; sWinI < Team.resolutionSd; sWinI++) {
						for (int sLoseI = 0; sLoseI < Team.resolutionSd; sLoseI++) {
							dP = loser.meanDistY[mLoseI] * dm * loser.sdDistY[sLoseI] * ds * winner.sdDistY[sWinI] * ds;
							pWinnerMean += Normal.cumulative(0,
									Team.meanDistX[mLoseI] - Team.meanDistX[mWinI],
									Math.sqrt(Math.pow(Team.sdDistX[sWinI], 2.0)+ Math.pow(Team.sdDistX[sLoseI], 2.0))) * dP;
						}
					}
				}
				newWinnerMeanDistY[mWinI] *= pWinnerMean;
			}

			//Update loser mean
			//System.out.println("Loser Mean Update...");
			for (int mLoseI = 0; mLoseI < Team.resolutionMean; mLoseI++) {
				double pLoserMean = 0;
				for (int mWinI = 0; mWinI < Team.resolutionMean; mWinI++) {
					for (int sWinI = 0; sWinI < Team.resolutionSd; sWinI++) {
						for (int sLoseI = 0; sLoseI < Team.resolutionSd; sLoseI++) {
							dP = winner.meanDistY[mWinI] * dm * loser.sdDistY[sLoseI] * ds * winner.sdDistY[sWinI] * ds;
							pLoserMean += Normal.cumulative(0,
									Team.meanDistX[mLoseI] - Team.meanDistX[mWinI],
									Math.sqrt(Math.pow(Team.sdDistX[sWinI], 2.0)+ Math.pow(Team.sdDistX[sLoseI], 2.0))) * dP;
						}
					}
				}
				newLoserMeanDistY[mLoseI] *= pLoserMean;
			}

			//Push up Mean Updates
			System.arraycopy(newWinnerMeanDistY, 0, winner.meanDistY, 0, newWinnerMeanDistY.length);
			System.arraycopy(newLoserMeanDistY, 0, loser.meanDistY, 0, newLoserMeanDistY.length);

			winner.normalizeMean();
			loser.normalizeMean();


			//Update winner sd
			//System.out.println("Winner SD Update...");
			for (int sWinI = 0; sWinI < Team.resolutionSd; sWinI++) {
				double pWinnerSd = 0;
				for (int mLoseI = 0; mLoseI < Team.resolutionMean; mLoseI++) {
					for (int mWinI = 0; mWinI < Team.resolutionMean; mWinI++) {
						for (int sLoseI = 0; sLoseI < Team.resolutionSd; sLoseI++) {
							dP = loser.meanDistY[mLoseI] * dm * winner.meanDistY[mWinI] * dm * loser.sdDistY[sLoseI] * ds;
							pWinnerSd += Normal.cumulative(0,
									Team.meanDistX[mLoseI] - Team.meanDistX[mWinI],
									Math.sqrt(Math.pow(Team.sdDistX[sWinI], 2.0)+ Math.pow(Team.sdDistX[sLoseI], 2.0))) * dP;
						}
					}
				}
				newWinnerSdDistY[sWinI] *= pWinnerSd;
			}

			//Update loser sd
			//System.out.println("Loser SD Update...");
			for (int sLoseI = 0; sLoseI < Team.resolutionSd; sLoseI++) {
				double pLoserSd = 0;
				for (int mWinI = 0; mWinI < Team.resolutionMean; mWinI++) {
					for (int sWinI = 0; sWinI < Team.resolutionSd; sWinI++) {
						for (int mLoseI = 0; mLoseI < Team.resolutionMean; mLoseI++) {
							dP = loser.meanDistY[mLoseI] * dm * winner.meanDistY[mWinI] * dm * winner.sdDistY[sWinI] * ds;
							pLoserSd += Normal.cumulative(0,
									Team.meanDistX[mLoseI] - Team.meanDistX[mWinI],
									Math.sqrt(Math.pow(Team.sdDistX[sWinI], 2.0)+ Math.pow(Team.sdDistX[sLoseI], 2.0))) * dP;
						}
					}
				}
				newLoserSdDistY[sLoseI] *= pLoserSd;
			}



			//Push up SD Updates
			System.arraycopy(newWinnerSdDistY, 0, winner.sdDistY, 0, newWinnerSdDistY.length);
			System.arraycopy(newLoserSdDistY, 0, loser.sdDistY, 0, newLoserSdDistY.length);

			winner.normalizeSd();
			loser.normalizeSd();


			//System.out.println("Updates done for " + winner.abrv + "'s victory over " + loser.abrv+ ".");

			return;
		}
	}
	
	public void plotMeanSkills(String fileName) {
		ArrayList<double[]> x = new ArrayList<double[]>();
		ArrayList<double[]> y = new ArrayList<double[]>();
		ArrayList<String> seriesNames = new ArrayList<String>();
		
		for (int i = 0; i < this.teams.length; i++) {
			x.add(Team.meanDistX);
			y.add(this.teams[i].meanDistY);
			seriesNames.add(this.teams[i].abrv);
		}
		Plot.plot(x, y, seriesNames, fileName, "The Mean Performance of Teams", "Mean Performance", "Probability", true);
	}
	
	public void plotPerfEsts(String fileName) {
		/*double xMin = 100.0;
		double xMax = -100.0;
		for (int i = 0; i < this.teams.length; i++) {
			double sdEst = this.teams[i].estimateSd();
			double meanEst = this.teams[i].estimateMean();
			if (meanEst - 2 * sdEst < xMin) xMin = meanEst - 2 * sdEst;
			if (meanEst + 2 * sdEst > xMax) xMax = meanEst + 2 * sdEst;
		}
		*/
		System.out.println("Start");
		int res = 500;
		double[][] xs = new double[this.teams.length][res];
		double[][] ys = new double[this.teams.length][res];
		ArrayList<double[]> x = new ArrayList<double[]>();
		ArrayList<double[]> y = new ArrayList<double[]>();
		ArrayList<String> seriesNames = new ArrayList<String>();
		for (int i = 0; i < xs.length; i++) {
			double sdEst = this.teams[i].estimateSd();
			double meanEst = this.teams[i].estimateMean();
			double xMin = meanEst - 3 * sdEst;
			double xMax = meanEst + 3 * sdEst;
			for (int j = 0; j < res; j++) {
				xs[i][j] = xMin + (xMax - xMin) * j / res;
				ys[i][j] = Normal.density(xs[i][j], meanEst, sdEst, false);
			}
			x.add(xs[i]);
			y.add(ys[i]);
			seriesNames.add(this.teams[i].abrv);
		}
		Plot.plot(x, y, seriesNames, fileName, "Teams' Performances", "Performance", "Probability", true);
	}
	
	public void plotSDs(String fileName) {
		ArrayList<double[]> x = new ArrayList<double[]>();
		ArrayList<double[]> y = new ArrayList<double[]>();
		ArrayList<String> seriesNames = new ArrayList<String>();
		
		for (int i = 0; i < this.teams.length; i++) {
			x.add(Team.sdDistX);
			y.add(this.teams[i].sdDistY);
			seriesNames.add(this.teams[i].abrv);
		}
		Plot.plot(x, y, seriesNames, fileName, "The SD of Teams' Performances", "SD of Performance", "Probability", true);
	}
	
	public void writeData(String fileName) throws IOException {
		CSVWriter writer;
		try {
			writer = new CSVWriter(new FileWriter("data/" + fileName + ".csv"), ',');
		} catch (IOException e) {
			System.err.println("Couldn't find write file location: <" + "data/" + fileName + "csv>");
			e.printStackTrace();
			return;
		}
	    String[] nextRow = new String[Math.max(Team.resolutionMean, Team.resolutionSd)];
		for (int i = 0; i < this.teams.length; i++) {
	    	Arrays.fill(nextRow, "");
	    	nextRow[0] = this.teams[i].name;
	    	nextRow[1] = this.teams[i].abrv;
	    	writer.writeNext(nextRow);
	    	Arrays.fill(nextRow, "");
	    	for (int j = 0; j < Team.resolutionMean; j++) {
	    		nextRow[j] = "" + this.teams[i].meanDistY[j];
	    	}
	    	writer.writeNext(nextRow);
	    	Arrays.fill(nextRow, "");
	    	for (int j = 0; j < Team.resolutionMean; j++) {
	    		nextRow[j] = "" + Team.meanDistX[j];
	    	}
	    	writer.writeNext(nextRow);
	    	Arrays.fill(nextRow, "");
	    	for (int j = 0; j < Team.resolutionSd; j++) {
	    		nextRow[j] = "" + this.teams[i].sdDistY[j];
	    	}
	    	writer.writeNext(nextRow);
	    	Arrays.fill(nextRow, "");
	    	for (int j = 0; j < Team.resolutionSd; j++) {
	    		nextRow[j] = "" + Team.sdDistX[j];
	    	}
	    	writer.writeNext(nextRow);
	    }
		writer.close();
	}
	
	public void readData(String fileName) throws IOException {
		System.out.println("Trying to read data....");
		CSVReader reader;
		try {
			reader = new CSVReader(new FileReader("data/" + fileName + ".csv"));
			System.out.println("Found File....");
		} catch (FileNotFoundException e) {
			System.err.println("ERROR: No archive found at: <data/" + fileName + ".csv>");
			e.printStackTrace();
			return;
		}
	    java.util.List<String[]> archive = reader.readAll();
	    //System.out.println("Read data....");
	    //Team.printStrArList(archive);
	    //System.out.println("Feeding into constructor");
	    if (archive.size() % 5 == 0) {
	    	this.teams = new Team[archive.size() / 5];
	    	for (int i = 0; i < this.teams.length; i++) {
	    		//System.out.println("Team "+ i);
	    		this.teams[i] = new Team(archive.subList(i * 5, (i + 1) * 5));
	    	}
	    } else {
	    	System.err.println("ERROR: Number of lines is not a multiple of 5.");
	    }
	    reader.close();
	}
	
	public static void main(String[] args) throws IOException {
		
		//LCS
		
		/*
		Team[] teams = new Team[8];
		teams[0] = new Team("Team SoloMid", "TSM");
		teams[1] = new Team("Counter Logic Gaming", "CLG");
		teams[2] = new Team("Cloud 9", "C9");
		teams[3] = new Team("Curse", "CRS");
		teams[4] = new Team("Dignitas", "DIG");
		teams[5] = new Team("Coast", "CST");
		teams[6] = new Team("Evil Geniuses", "EG");
		teams[7] = new Team("XDG", "XDG");
		
		CSVReader reader = new CSVReader(new FileReader("data/lcs-data.csv"));
	    java.util.List<String[]> games = reader.readAll();
	    games.remove(0);
	    
	    System.out.println("Number of games: " + games.size());
	    
	    Ranking ranks = new Ranking(games, teams);
	    ranks.plotMeanSkills("lcs-prior-mean");
	    ranks.plotSDs("lcs-prior-sd");
	    
	    ranks.updateWithAllGames();
	    
	    ranks.writeData("lcs-archive");
		reader.close();
		
		ranks.plotMeanSkills("lcs-means-120-30");
		ranks.plotSDs("lcs-sds-120-30"); 
		END LCS*/
	    
		
		//EXAMPLE
		/*
		System.out.println("Beginning Demo");
		Team[] teams = new Team[3];
		teams[0] = new Team("Team A Posterior", "A Posterior");
		teams[1] = new Team("Team B Posterior", "B Posterior");
		teams[2] = new Team("Team A/B Prior", "A/B Prior");
		Ranking demo = new Ranking(null, teams);
		String[] demoGame = new String[3];
		demoGame[0] = "A Posterior";
		demoGame[1] = "B Posterior";
		demoGame[2] = "Today";
		demo.updateWithGame(demoGame);
		demo.plotMeanSkills("demo-mean");
		demo.plotSDs("demo-sd");
		System.out.println("Done with Demo");
		*/
		//END EXAMPLE
		
		
		/*
		// PWIN
		Ranking ranks = new Ranking();
		ranks.readData("lcs-archive");
		for (int i = 0; i < ranks.teams.length; i++) {
			System.out.println(ranks.teams[i].abrv + " & " + ranks.getTeam("DIG").pWinVs(ranks.teams[i]));
		}
		String A = "CRS";
		String B = "DIG";
		System.out.println("P(" + A + " beats " + B + ") : " + ranks.getTeam(A).pWinVs(ranks.getTeam(B)));
		A = "CRS";
		B = "C9";
		System.out.println("P(" + A + " beats " + B + ") : " + ranks.getTeam(A).pWinVs(ranks.getTeam(B)));
		A = "C9";
		B = "TSM";
		System.out.println("P(" + A + " beats " + B + ") : " + ranks.getTeam(A).pWinVs(ranks.getTeam(B)));
		A = "TSM";
		B = "CLG";
		System.out.println("P(" + A + " beats " + B + ") : " + ranks.getTeam(A).pWinVs(ranks.getTeam(B)));
		A = "CLG";
		B = "CRS";
		System.out.println("P(" + A + " beats " + B + ") : " + ranks.getTeam(A).pWinVs(ranks.getTeam(B)));
		A = "CLG";
		B = "CST";
		System.out.println("P(" + A + " beats " + B + ") : " + ranks.getTeam(A).pWinVs(ranks.getTeam(B)));
		//END PWIN
		*/
		
		
		//ranks.getTeam("TSM").dump();
		//ranks.plotPerfEsts("lol");
		//for (int i = 0; i < ranks.teams.length; i++) {
		//	ranks.teams[i].printEst();
		//}
		
		//NBA
		/*
		Ranking nba = new Ranking("nba-data");
		nba.plotMeanSkills("nba-test");
		nba.teams[0].printEst();
		nba.updateWithAllGames();
		nba.plotMeanSkills("nba-test-2");
		nba.teams[0].printEst();
		nba.writeData("nba-archive-120-10");*/
		//END NBA
		/*
		Ranking nba2 = new Ranking("nba-data");
		nba2.plotSDs("nba-prior-sd");
		*/
		
		Ranking nba = new Ranking();
		nba.readData("nba-archive-120-10");
		CSVReader reader = new CSVReader(new FileReader("data/nba-testing-data.csv"));
	    java.util.List<String[]> games = reader.readAll();
	    reader.close();
	    Set<String[]> rec = new HashSet<String[]>(games);
	    for (String[] g : rec) {
	    	System.out.println("P(" + g[0] + " beats " + g[1] + ") : " + nba.getTeam(g[0]).pWinVs(nba.getTeam(g[1])));
	    }
		
		/*
		nba.plotSDs("nba-post-sd");
		String A = "San Antonio Spurs";
		String B = "Utah Jazz";
		System.out.println("P(" + A + " beats " + B + ") : " + nba.getTeam(A).pWinVs(nba.getTeam(B)));
		A = "Oklahoma City Thunder";
		B = "Dallas Mavericks";
		System.out.println("P(" + A + " beats " + B + ") : " + nba.getTeam(A).pWinVs(nba.getTeam(B)));
		A = "Los Angeles Lakers";
		B = "Utah Jazz";
		System.out.println("P(" + A + " beats " + B + ") : " + nba.getTeam(A).pWinVs(nba.getTeam(B)));
		A = "San Antonio Spurs";
		B = "Utah Jazz";
		System.out.println("P(" + A + " beats " + B + ") : " + nba.getTeam(A).pWinVs(nba.getTeam(B)));
		A = "San Antonio Spurs";
		B = "Utah Jazz";
		System.out.println("P(" + A + " beats " + B + ") : " + nba.getTeam(A).pWinVs(nba.getTeam(B)));
		A = "San Antonio Spurs";
		B = "Utah Jazz";
		System.out.println("P(" + A + " beats " + B + ") : " + nba.getTeam(A).pWinVs(nba.getTeam(B)));
		A = "San Antonio Spurs";
		B = "Utah Jazz";
		System.out.println("P(" + A + " beats " + B + ") : " + nba.getTeam(A).pWinVs(nba.getTeam(B)));
		A = "San Antonio Spurs";
		B = "Utah Jazz";
		System.out.println("P(" + A + " beats " + B + ") : " + nba.getTeam(A).pWinVs(nba.getTeam(B)));
		*/
		/*
		Team[] teams = new Team[8];
		teams[0] = new Team("Team SoloMid", "TSM");
		teams[1] = new Team("Counter Logic Gaming", "CLG");
		teams[2] = new Team("Team SoloMid EVO", "EVO");
		teams[3] = new Team("Curse", "CRS");
		teams[4] = new Team("Dignitas", "DIG");
		teams[5] = new Team("Team Dynamic", "TD");
		teams[6] = new Team("Monomaniacs Ferus", "MME");
		teams[7] = new Team("Team Legion", "LGN");
		
		CSVReader reader = new CSVReader(new FileReader("data/season2-data.csv"));
	    java.util.List<String[]> games = reader.readAll();
	    games.remove(0);	    
	    System.out.println("Number of games: " + games.size());
	    
	    Ranking ranks = new Ranking(games, teams);
	    
	    //System.out.println(Arrays.toString(ranks.teams[0].meanDistY));
	    //ranks.teams[0].dump();
	    //ranks.teams[2].dump();
	    ranks.updateWithAllGames();
	    //ranks.teams[0].dump();
	    //ranks.teams[2].dump();
	    ranks.plotMeanSkills("s2-mean");
	    ranks.plotSDs("s2-sd");
	    ranks.writeData("s2-archive-120-40");*/
		
		
		/*
		Team[] teams = new Team[2];
		teams[0] = new Team("SoloMid", "TSM", 5);
		teams[1] = new Team("Counter Logic", "CLG", 5);
		
		Ranking ranks = new Ranking(null, teams);
		
		System.out.println(ranks.teams[0].name);
		System.out.println(ranks.teams[0].abrv);
		System.out.println(Arrays.toString(Team.meanDistX));
		System.out.println(Arrays.toString(ranks.teams[0].meanDistY));
		double sum = 0.0;
		for (int i = 0; i < ranks.teams[0].meanDistY.length; i++) {
			sum += ranks.teams[0].meanDistY[i];
		}
		System.out.println("Sum: "+sum);
		String[] game1 = {"TSM","CLG","4/23"};
		
		System.out.println("Going to update with 1 game");
		
		ranks.updateWithGame(game1);
		
		System.out.println("Update Complete. Results: ");
		
		System.out.println(ranks.teams[0].name);
		System.out.println(ranks.teams[0].abrv);
		System.out.println(Arrays.toString(Team.meanDistX));
		System.out.println(Arrays.toString(ranks.teams[0].meanDistY));
		
		System.out.println(ranks.teams[1].name);
		System.out.println(ranks.teams[1].abrv);
		System.out.println(Arrays.toString(Team.meanDistX));
		System.out.println(Arrays.toString(ranks.teams[1].meanDistY));
		
		ranks.teams[0].plotMeanSkill("test");
		ranks.plotMeanSkills("test");
		*/
	}
}
