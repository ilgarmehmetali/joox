package net.milgar.joox;

import java.util.List;

public class FirstSpecies {

	// Some sane defaults.
	int DEFAULT_POPULATION_SIZE = 1000;
	int DEFAULT_MAX_GENERATION = 100;
	int DEFAULT_MUTATION_RANGE = 7;
	float DEFAULT_MUTATION_RATE = 0.4f;

	// Intervals between notes that are allowed in first sepcies counterpoint.
	int[] VALID_INTERVALS = { 2, 4, 5, 7, 9, 11 };

	// Various rewards and punishments used with different aspects of the
	// solution.

	// Reward / punishment to ensure the solution starts correctly (5th or 8ve).
	static int REWARD_FIRST = 1;
	static float PUNISH_FIRST = 0.1f;
	// Reward / punishment to ensure the solution finishes correctly (at an
	// 8ve).
	static int REWARD_LAST = 1;
	static float PUNISH_LAST = 0.1f;
	// Reward / punishment to ensure the penultimate note is step wise onto the
	// final note.
	static int REWARD_LAST_STEP = 1;
	static float PUNISH_LAST_STEP = 0.7f;
	// Reward / punish contrary motion onto the final note.
	static int REWARD_LAST_MOTION = 1;
	static float PUNISH_LAST_MOTION = 0.1f;
	// Punishment if the penultimate note is a repeated note.
	static float PUNISH_REPEATED_PENULTIMATE = 0.1f;
	// Make sure the movement to the penultimate note isn't from too
	// far away (not greater than a third).
	static int REWARD_PENULTIMATE_PREPARATION = 1;
	static float PUNISH_PENULTIMATE_PREPARATION = 0.7f;
	// Punish parallel fifths or octaves.
	static float PUNISH_PARALLEL_FIFTHS_OCTAVES = 0.5f;
	// Punishment for too many repeated notes.
	static float PUNISH_REPEATS = 0.1f;
	// Punishment for too many parallel thirds
	static float PUNISH_THIRDS = 0.1f;
	// Punishment for too many parallel sixths.
	static float PUNISH_SIXTHS = 0.1f;
	// Punishment for too many parallel/similar movements.
	static float PUNISH_PARALLEL = 0.1f;
	// Punishment for too many large leaps in the melody.
	static float PUNISH_LEAPS = 0.1f;

	// The highest score a candidate solution may achieve. (Hack!)
	static int MAX_REWARD = (REWARD_FIRST + REWARD_LAST + REWARD_LAST_STEP + REWARD_LAST_MOTION
			+ REWARD_PENULTIMATE_PREPARATION);

	public static float calculateFitness(Chromosome chromosome, List<Integer> cantusFirmus){
		/*
        Given a candidate solution will return its fitness score assuming
        the cantusFirmus in this closure. Caches the fitness score in the
        genome.
        */
	    float repeat_threshold = (float) ((float) (cantusFirmus.size()) * 0.5);
	    float jump_threshold = (float) ((float) (cantusFirmus.size()) * 0.3);

	    // The fitness score to be returned.
	    float fitnessScore = 0;
	    
        // Counts the number of repeated notes in the contrapunctus.
        int repeats = 0;
        // Counts consecutive parallel thirds.
        int thirds = 0;
        // Counts consecutive parallel sixths.
        int sixths = 0;
        // Counts the amount of parallel motion.
        int parallel_motion = 0;
        // Counts the number of jumps in the melodic contour.
        int jump_contour = 0;

        List<Integer> contrapunctus = chromosome.getGenes();

        // Make sure the solution starts correctly (at a 5th or octave).
        int first_interval = contrapunctus.get(0) - cantusFirmus.get(0);
        if (first_interval == 7 || first_interval == 4)
            fitnessScore += REWARD_FIRST;
        else
            fitnessScore -= PUNISH_FIRST;

        // Make sure the solution finishes correctly (at an octave).
        if (contrapunctus.get(contrapunctus.size()-1) - cantusFirmus.get(cantusFirmus.size()-1) == 7)
            fitnessScore += REWARD_LAST;
        else
            fitnessScore -= PUNISH_LAST;

        // Ensure the penultimate note is step wise onto the final note.
        if (Math.abs(contrapunctus.get(contrapunctus.size()-1) - contrapunctus.get(contrapunctus.size()-2)) == 1)
            fitnessScore += REWARD_LAST_STEP;
        else
            fitnessScore -= PUNISH_LAST_STEP;

        // Reward contrary motion onto the final note.
        int cantusFirmus_motion = cantusFirmus.get(cantusFirmus.size()-1) - cantusFirmus.get(cantusFirmus.size()-2);
        int contrapunctus_motion = contrapunctus.get(contrapunctus.size()-1) - contrapunctus.get(contrapunctus.size()-2);

        if ((cantusFirmus_motion < 0 && contrapunctus_motion > 0) ||
            (cantusFirmus_motion > 0 && contrapunctus_motion < 0)){
            fitnessScore += REWARD_LAST_MOTION;
        } else {
            fitnessScore -= PUNISH_LAST_MOTION;
        }

        // Make sure the penultimate note isn't a repeated note.
        int penultimate_preparation = Math.abs(contrapunctus.get(contrapunctus.size()-2) - contrapunctus.get(contrapunctus.size()-3));
        if (penultimate_preparation == 0){
            fitnessScore -= PUNISH_REPEATED_PENULTIMATE;
        }else{
            // Make sure the movement to the penultimate note isn't from too
            // far away (not greater than a third).
            if (penultimate_preparation < 2){
                fitnessScore += REWARD_PENULTIMATE_PREPARATION;
            }else{
                fitnessScore -= PUNISH_PENULTIMATE_PREPARATION;
            }
        }

        // Check the fitness of the body of the solution.
        int[] last_notes = {contrapunctus.get(contrapunctus.size()-1), cantusFirmus.get(cantusFirmus.size()-1)};
        int last_interval = last_notes[0] - last_notes[1];
        //for contrapunctus_note, cantusFirmus_note in solution[1:]{
        for(int i=0; i<contrapunctus.size()-1; i++){
            int[] current_notes = {contrapunctus.get(i), cantusFirmus.get(i)};
            int current_interval = contrapunctus.get(i) - cantusFirmus.get(i);

            // Punish parallel fifths or octaves.
            if (((current_interval == 4 || current_interval == 7) &&
                (last_interval == 4 || last_interval == 7)))
                fitnessScore -= PUNISH_PARALLEL_FIFTHS_OCTAVES;

            // Check if the melody is a repeating note.
            if (contrapunctus.get(i) == last_notes[0])
                repeats += 1;

            // Check for parallel thirds.
            if (current_interval == 2 && last_interval == 2)
                thirds += 1;

            // Check for parallel sixths.
            if (current_interval == 4 && last_interval == 4)
                sixths += 1;

            // Check for parallel motion.
            if (Utils.isParallel(last_notes, current_notes))
                parallel_motion += 1;

            // Check the melodic contour.
            int contour_leap = Math.abs(current_notes[0] - last_notes[0]);
            if (contour_leap > 2)
                jump_contour += contour_leap - 2;

            last_notes = current_notes;
            last_interval = current_interval;
        }

        // Punish too many (> 1/3) repeated notes.
        if (repeats > repeat_threshold)
            fitnessScore -= PUNISH_REPEATS;

        // Punish too many (> 1/3) parallel thirds
        if (thirds > repeat_threshold)
            fitnessScore -= PUNISH_THIRDS;

        // Punish too many (> 1/3) parallel sixths.
        if (sixths > repeat_threshold)
            fitnessScore -= PUNISH_SIXTHS;

        // Punish too many (> 1/3) parallel movements.
        if (parallel_motion > repeat_threshold)
            fitnessScore -= PUNISH_PARALLEL;

        // Punish too many large leaps in the melody.
        if (jump_contour > jump_threshold)
            fitnessScore -= PUNISH_LEAPS;

        return fitnessScore;
	}

}
