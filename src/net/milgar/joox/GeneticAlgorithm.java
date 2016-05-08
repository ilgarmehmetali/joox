package net.milgar.joox;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithm {
	private int populationSize;
	private int iterationCount;
	private List<Integer> cantusFirmus;
	private Utils.Species species;
	private float crossOverRate;
	private float mutationRate;
	private Random rnd;
	private List<Population> populations;

	public GeneticAlgorithm(int populationSize, int iterationCount, List<Integer> cantusFirmus, Utils.Species species,
			float crossOverRate, float mutationRate) {
		this.populationSize = populationSize;
		this.iterationCount = iterationCount;
		this.cantusFirmus = new ArrayList<>(cantusFirmus);
		this.species = species;
		this.rnd = new Random();
		this.crossOverRate = crossOverRate;
		this.mutationRate = mutationRate;

		this.createGenerations();
	}

	private void createGenerations() {

		this.populations = new ArrayList<>();

		this.populations.add(new Population(this.populationSize, this.species, this.cantusFirmus, this.crossOverRate,
				this.mutationRate));

		for (int currentPopulation = 1; currentPopulation < this.iterationCount; currentPopulation++) {
			Population nextGen = this.populations.get(currentPopulation - 1).createNextGeneration();
			this.populations.add(nextGen);
		}

		for (int k = 0; k < this.populations.size(); k++) {
			this.populations.get(k).sort();
		}
	}

	public List<Double> getBestFitnessOfEachGeneration() {
		List<Double> bestFitnessOfPopulations = new ArrayList<>();
		for (int k = 0; k < iterationCount; k++) {
			double fitness = populations.get(k).getBestChromosome().getFitness(Utils.Species.First);
			bestFitnessOfPopulations.add(fitness);
		}
		return bestFitnessOfPopulations;
	}

	public void createLilypondFiles(String directory, String currentDateAndTime, String composer, String title) {
		Utils.createLilypondFiles(populations.get(0).getBestChromosome(), "Best_Of_First_Generation.ly", directory,
				currentDateAndTime, composer, title);
		Utils.createLilypondFiles(populations.get(iterationCount - 1).getBestChromosome(), "Best_Of_Last_Generation.ly",
				directory, currentDateAndTime, composer, title);

	}

}
