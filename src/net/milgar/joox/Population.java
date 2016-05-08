package net.milgar.joox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Population {
	private Utils.Species species;
	private List<Chromosome> members;
	private Random rnd;
	private int maxSize;
	private float crossOverRate;
	private float mutationRate;
	private List<Integer> cantusFirmus;

	private boolean isSorted;

	public Population(int maxSize, Utils.Species species, List<Integer> cantusFirmus, float crossOverRate,
			float mutationRate) {
		this.maxSize = maxSize;
		this.species = species;
		this.crossOverRate = crossOverRate;
		this.mutationRate = mutationRate;
		this.cantusFirmus = new ArrayList<>(cantusFirmus);
		this.members = new ArrayList<>();
		this.rnd = new Random();
		for (int i = 0; i < maxSize; i++) {
			this.members.add(new Chromosome(cantusFirmus));
		}

	}

	public Population createNextGeneration() {
		Population newPop =
				new Population(this.maxSize, this.species, this.cantusFirmus, this.crossOverRate, this.mutationRate);
		while (newPop.members.size() < ((float) maxSize) * this.crossOverRate) {
			Chromosome[] parrentChoromosmes = Utils.tournementSelection(newPop.members, Utils.Species.First);

			Chromosome childFirst = new Chromosome(parrentChoromosmes[0]);
			Chromosome childSecond = new Chromosome(parrentChoromosmes[1]);
			Chromosome.crossOver(childFirst, childSecond);
			newPop.members.add(childFirst);
			newPop.members.add(childSecond);
		}
		newPop.members.add(new Chromosome(this.getBestChromosome()));
		for (int k = newPop.members.size(); k < maxSize; k++) {
			int index = rnd.nextInt(maxSize);
			Chromosome chromosome = new Chromosome(this.members.get(index));
			newPop.members.add(chromosome);
		}
		for (int i = 0; i < ((float) maxSize) * this.mutationRate; i++) {
			int index = rnd.nextInt(maxSize);
			newPop.members.get(index).mutate();
		}
		return newPop;
	}

	public Chromosome getBestChromosome() {
		if (!this.isSorted) {
			this.sort();
		}
		return this.members.get(this.members.size() - 1);
	}

	public void sort() {
		if (!this.isSorted) {
			Collections.sort(this.members, new Comparator<Chromosome>() {
				@Override
				public int compare(Chromosome o1, Chromosome o2) {
					return o1.compareTo(o2, species);
				}
			});
			this.isSorted = true;
		}
	}
}
