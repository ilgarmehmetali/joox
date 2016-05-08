package net.milgar.joox;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class App {

	public static void main(String[] args) {

		Integer[] intarray = new Integer[args.length];
		int i = 0;
		for (String str : args) {
			try {
				intarray[i] = Integer.parseInt(str);
				i++;
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Not a number: " + str + " at index " + i, e);
			}
		}

		String title = "SimpleAlgo";
		String composer = "joox";

		float crossOverRate = 0.9f;
		float mutationRate = 0.02f;
		int populationSize = 50;
		int iterationCount = 20;

		Integer[][] cantusArrays = { { 5, 7, 6, 5, 8, 7, 9, 8, 7, 6, 5 }/* Dorian */,
				{ 6, 4, 5, 4, 2, 9, 8, 6, 7, 6 }/* Phrygian */, { 7, 8, 9, 7, 5, 6, 7, 11, 9, 7, 8, 7 }/* Lydian */ };
		/*
		 * int currentCantusIndex = 0; List<Integer> cantusFirmus = new
		 * ArrayList<>();
		 * cantusFirmus.addAll(Arrays.asList(cantusArrays[currentCantusIndex]));
		 */
		List<Integer> cantusFirmus = new ArrayList<>();
		cantusFirmus.addAll(Arrays.asList(intarray));

		Random rnd = new Random();

		GeneticAlgorithm[] ga = { null, null, null };
		ga[0] = new GeneticAlgorithm(populationSize, iterationCount, cantusFirmus, Utils.Species.First, crossOverRate,
				mutationRate);
		ga[1] = new GeneticAlgorithm(populationSize, iterationCount, cantusFirmus, Utils.Species.First, crossOverRate,
				mutationRate);
		ga[2] = new GeneticAlgorithm(populationSize, iterationCount, cantusFirmus, Utils.Species.First, crossOverRate,
				mutationRate);

		String currentDateAndTime = Utils.getTime();

		String baseDir = System.getProperty("user.home") + File.separator + "MusicComposer" + File.separator;

		File f = new File(baseDir);
		if (!f.exists()) f.mkdir();

		String resultDir = baseDir + File.separator + currentDateAndTime + File.separator;
		File resultDirHandler = new File(resultDir);
		resultDirHandler.mkdir();

		ga[0].createLilypondFiles(resultDir, currentDateAndTime, composer, title);

		List<List<Double>> bestFitnessOfAllPopulations = new ArrayList<>();
		bestFitnessOfAllPopulations.add(ga[0].getBestFitnessOfEachGeneration());
		bestFitnessOfAllPopulations.add(ga[1].getBestFitnessOfEachGeneration());
		bestFitnessOfAllPopulations.add(ga[2].getBestFitnessOfEachGeneration());

		String chartName = "chart.png";
		String chartPath = resultDir + chartName;

		URL url = Utils.drawChart(bestFitnessOfAllPopulations.get(0), bestFitnessOfAllPopulations.get(1),
				bestFitnessOfAllPopulations.get(2));
		if (url != null) {
			Utils.downloadAndSaveImage(url, chartPath);
		}
		Utils.execSystemCommand(new String[] { "xdg-open", chartName }, resultDir);

	}

}
