package net.milgar.joox;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.googlecode.charts4j.AxisLabelsFactory;
import com.googlecode.charts4j.Color;
import com.googlecode.charts4j.DataUtil;
import com.googlecode.charts4j.Fills;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.Line;
import com.googlecode.charts4j.LineChart;
import com.googlecode.charts4j.LineStyle;
import com.googlecode.charts4j.Plot;
import com.googlecode.charts4j.Plots;

public class Utils {

	private static int[] SPECIES_DURATION = { 1, 2, 4 };

	public enum Species {
		First, Second
	};

	// A dictionary that maps numbers to lilypond notes.
	private static String[] NOTES =
			{ "g", "a", "b", "c'", "d'", "e'", "f'", "g'", "a'", "b'", "c''", "d''", "e''", "f''", "g''", "a''", "r" };

	private static String translateNoteToLilypond(int note) {
		// Ensure the notes are in range
		if (note <= 0 || note >= 18) {
			throw new IndexOutOfBoundsException("");
		}
		return Utils.NOTES[note - 1];
	}

	public static String cantusFirmusToString(List<Integer> notes) {
		/*
		 * Given a list of notes as integers, will return the lilypond notes for
		 * the cantus firmus.
		 */
		String result = "";
		List<String> lilyPondNotes = new ArrayList<String>();
		for (Integer note : notes) {
			lilyPondNotes.add(translateNoteToLilypond(note));
		}

		// Set the duration against the first note.
		result = lilyPondNotes.get(0) + " 1 ";
		lilyPondNotes.remove(0);
		// Translate all the others.
		result += String.join(" ", lilyPondNotes);
		// End with a double bar.
		result += " \\bar \"|.\"";
		// Tidy up double spaces.
		result = result.replaceAll("  ", " ");
		return result;
	}

	public static String getSimpleContrapunctus(List<Integer> notes, int duration) {
		/*
		 * Given a list of notes as integers and the duration to use, will
		 * return the lilypond notes for the contrapunctus. Durations:
		 * 1-semibreve, 2-minim, 4-crotchet
		 */
		String result = "";

		List<String> lilyPondNotes = new ArrayList<String>();
		for (Integer note : notes) {
			lilyPondNotes.add(translateNoteToLilypond(note));
		}
		// Set the duration against the first note.
		result = lilyPondNotes.get(0) + " " + duration + " ";
		// Translate all the others except the final two.
		lilyPondNotes.remove(lilyPondNotes.size() - 1);
		lilyPondNotes.remove(lilyPondNotes.size() - 1);
		lilyPondNotes.remove(0);
		result += String.join(" ", lilyPondNotes);

		int[] a = { 1, 2, 3 };
		ArrayList<Integer> b = new ArrayList<>();
		b.add(4);
		b.add(7);
		b.add(11);
		b.add(14);

		// Ensure the penultimate note is a semitone away IFF moving up to the
		// final
		// note. (Kinda hacky - would be easier in Lisp)
		int final_note = notes.get(notes.size() - 1);
		int penultimate_note = notes.get(notes.size() - 2);
		String next_note = translateNoteToLilypond(penultimate_note);
		if (final_note == penultimate_note + 1) {
			// Check if the note isn't a C or an F
			if (b.indexOf(final_note) == -1) {
				// insert 'is' to sharpen the pitch of the note by a semitone.
				result += " " + next_note.charAt(0) + "is";
				for (int i = 1; i < next_note.length(); i++) {
					result += next_note.charAt(i) + " ";
				}
			}
		} else {
			result += " " + next_note;
		}

		// Ensure the final note is a semibreve.
		result += " " + translateNoteToLilypond(final_note);
		if (duration != 1) result += " 1";

		// Tidy up double spaces.
		result = result.replace("  ", " ");
		return result;
	}

	public static String render(int species, Chromosome chromosome, String date, String composer, String title) {
		/*
		 * Given an indication of the species (1-3), a list of notes for the
		 * cantus_firmus and contrapunctus returns a string containing lilypond
		 * code to render the musical information as PDF and MIDI files.
		 */

		String contrapunctus_notes = "";
		if (species < 4) {
			int duration = SPECIES_DURATION[species - 1];
			contrapunctus_notes = getSimpleContrapunctus(chromosome.getGenes(), duration);
		} else if (species == 4) {
			// contrapunctus_notes = get_fourth_species(contrapunctus);
		}

		String temp = "";

		InputStream is = Chromosome.class.getResourceAsStream("LilypondTemplate");
		BufferedReader r = new BufferedReader(new InputStreamReader(is));
		String line = "";
		try {
			while ((line = r.readLine()) != null) {
				temp += line + "\n";
			}
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
		// System.out.println(contrapunctus_notes);

		temp = temp.replace("$title", title);
		temp = temp.replace("$created_on", date);
		temp = temp.replace("$composer", composer);
		temp = temp.replace("$contrapunctus", contrapunctus_notes);
		temp = temp.replace("$cantus_firmus", cantusFirmusToString(chromosome.getCantusFirmus()));

		return temp;
	}

	/**
	 * @return True if the motion between last and current notes is parallel.
	 */
	public static boolean isParallel(int[] last, int[] current) {
		boolean parallel = false;
		if (last[0] - current[0] < 0 && last[1] - current[1] < 0) {
			parallel = true;
		} else if (last[0] - current[0] > 0 && last[1] - current[1] > 0) {
			parallel = true;
		}
		return parallel;
	}

	public static Chromosome[] tournementSelection(List<Chromosome> population, Utils.Species species) {
		int[] parrentIndex = { 0, 0 };
		Chromosome[] parrentChoromosmes = { null, null };
		Random rnd = new Random();

		do {
			for (int j = 0; j < 2; j++) {
				int tournementCandidateFirst = 0, tournementCandidateSecond = 0;
				do {
					tournementCandidateFirst = rnd.nextInt(population.size());
					tournementCandidateSecond = rnd.nextInt(population.size());
				} while (tournementCandidateFirst == tournementCandidateSecond);
				Chromosome chromosomeFirst = population.get(tournementCandidateFirst);
				Chromosome chromosomeSecond = population.get(tournementCandidateSecond);
				float fitnessFirst = chromosomeFirst.getFitness(species);
				float fitnessSecond = chromosomeSecond.getFitness(species);
				parrentIndex[j] = fitnessFirst > fitnessSecond ? tournementCandidateFirst : tournementCandidateSecond;
				parrentChoromosmes[j] = fitnessFirst > fitnessSecond ? chromosomeFirst : chromosomeSecond;
			}
		} while (parrentIndex[0] == parrentIndex[1]);

		return parrentChoromosmes;
	}

	public static String getTime() {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss SSS");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(c.getTime());
	}

	public static boolean saveStringAsFile(String string, String path) {
		PrintWriter out;
		try {
			out = new PrintWriter(path);
			out.println(string);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean execSystemCommand(String[] commands, String executionPath) {

		try {
			File handler = new File(executionPath);

			Runtime rt = Runtime.getRuntime();
			Process p1 = rt.exec(commands, null, handler);

			p1.waitFor();

		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static URL drawChart(List<Double>... fitness) {
		// Defining Line
		double minFitness = Double.MAX_VALUE;
		double maxFitness = Double.MIN_VALUE;
		List<Color> colors = new ArrayList<>();
		colors.addAll(Arrays.asList(new Color[] { Color.RED, Color.BLUE, Color.ORANGE, Color.CYAN }));
		for (List<Double> fit : fitness) {
			if (minFitness > Collections.min(fit)) minFitness = Collections.min(fit);
			if (maxFitness < Collections.max(fit)) maxFitness = Collections.max(fit);
		}
		List<Plot> plots = new ArrayList<>();
		int i = 1;
		for (List<Double> fit : fitness) {
			Line line = Plots.newLine(DataUtil.scaleWithinRange(minFitness - 0.5, maxFitness + 0.5, fit));
			line.setColor(colors.get(0));
			line.setLegend(i++ + ". Sonuç");
			line.setLineStyle(LineStyle.newLineStyle(3, 1, 0));
			colors.remove(0);
			plots.add(line);
		}
		// Defining chart.
		final LineChart chart = GCharts.newLineChart(plots);
		chart.setSize(400, 400);
		chart.setTitle("Genetic Algorihm on Music Composition");
		chart.addXAxisLabels(AxisLabelsFactory.newNumericRangeAxisLabels(0, fitness[0].size(), fitness[0].size() / 4));
		chart.addYAxisLabels(AxisLabelsFactory.newNumericRangeAxisLabels(minFitness - 0.5, maxFitness + 0.5));
		chart.setGrid(25, 25, 5, 5);

		// Defining background and chart fills.
		chart.setBackgroundFill(Fills.newSolidFill(Color.LIGHTGREY));

		String urlString = chart.toURLString();
		System.out.println(urlString);
		try {
			URL url = new URL(urlString);
			return url;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static boolean downloadAndSaveImage(URL imageUrl, String path) {
		try {
			InputStream in;
			in = new BufferedInputStream(imageUrl.openStream());
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int n = 0;
			while (-1 != (n = in.read(buf))) {
				out.write(buf, 0, n);
			}
			out.close();
			in.close();
			byte[] response = out.toByteArray();

			FileOutputStream fos;
			fos = new FileOutputStream(path);
			fos.write(response);
			fos.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
		return true;
	}

	public static void createLilypondFiles(Chromosome chromosome, String lilypondFileName, String directory,
			String currentDateAndTime, String composer, String title) {

		String lilyFile = Utils.render(1, chromosome, currentDateAndTime, composer, title);
		Utils.saveStringAsFile(lilyFile, directory + lilypondFileName);

		Utils.execSystemCommand(new String[] { "lilypond", lilypondFileName }, directory);
	}

}
