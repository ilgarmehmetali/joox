/*
 *  MIT Licensed
 *  
 *  python source: https://github.com/ntoll/foox/
 */
package net.milgar.joox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.milgar.joox.Utils.Species;

public class Chromosome {
	private List<Integer> genes;
	private List<Integer> cantusFirmus;
	private boolean isDirty;
	private HashMap<Utils.Species, Float> fitness;

	public Chromosome(List<Integer> cantusFirmus) {
		this.cantusFirmus = new ArrayList<>(cantusFirmus);
		this.genes = new ArrayList<>();
		Random rnd = new Random();
		for (int i = 0; i < cantusFirmus.size(); i++) {
			this.genes.add(rnd.nextInt(17) + 1);
		}
		init();
	}

	public Chromosome(List<Integer> genes, List<Integer> cantusFirmus) {
		this.genes = new ArrayList<>(genes);
		this.cantusFirmus = new ArrayList<>(cantusFirmus);
		init();
	}

	public Chromosome(Chromosome that) {
		this(that.genes, that.cantusFirmus);
	}

	private void init() {
		fitness = new HashMap<>();
		this.setDirty(true);
	}

	public int getGene(int index) {
		return this.genes.get(index);
	}

	public List<Integer> getGenes() {
		return new ArrayList<Integer>(this.genes);
	}

	/**
	 * @return Copy of chromosome's cantus firmus.
	 */
	public List<Integer> getCantusFirmus() {
		return new ArrayList<>(this.cantusFirmus);
	}

	public void setCantusFirmus(List<Integer> cantusFirmus) {
		this.cantusFirmus = new ArrayList<>(cantusFirmus);
		this.setDirty(true);
	}

	public void setDirty(boolean dirty) {
		this.isDirty = dirty;
	}

	public boolean isDirty() {
		return this.isDirty;
	}

	public float getFitness(Utils.Species species) {
		if (this.isDirty) {
			this.fitness.clear();
			this.fitness.put(Species.First, FirstSpecies.calculateFitness(this, this.cantusFirmus));
			this.fitness.put(Species.Second, FirstSpecies.calculateFitness(this, this.cantusFirmus));
			this.setDirty(false);
		}
		return this.fitness.get(species);
	}

	public static void crossOver(Chromosome c1, Chromosome c2) {
		Random rnd = new Random();
		int crossOverPointFirst = rnd.nextInt(c1.genes.size());
		int crossOverPointSecond = rnd.nextInt(c1.genes.size() - crossOverPointFirst) + crossOverPointFirst;
		for (int i = crossOverPointFirst; i < crossOverPointSecond; i++) {
			int tmpGene = c1.genes.get(i);
			c1.genes.set(i, c2.genes.get(i));
			c2.genes.set(i, tmpGene);
		}
		c1.setDirty(true);
		c2.setDirty(true);
	}

	public void mutate() {
		int indexFirst, indexSecond;
		int newGeneFirst, newGeneSecond;
		Random rnd = new Random();
		do {
			indexFirst = rnd.nextInt(this.genes.size());
			indexSecond = rnd.nextInt(this.genes.size());
		} while (indexFirst == indexSecond);
		do {
			newGeneFirst = rnd.nextInt(17) + 1;
			newGeneSecond = rnd.nextInt(17) + 1;
		} while (newGeneFirst == newGeneSecond);
		this.genes.set(indexFirst, newGeneFirst);
		this.genes.set(indexSecond, newGeneSecond);
		this.setDirty(true);
	}

	public int compareTo(Chromosome that, Utils.Species species) {
		float fitnessFirst = getFitness(species);
		float fitnessSecond = that.getFitness(species);
		if (fitnessFirst < fitnessSecond)
			return -1;
		else if (fitnessFirst > fitnessSecond)
			return 1;
		else
			return 0;

	}

}
