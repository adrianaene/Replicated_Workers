import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

/**
 * Clasa ce reprezinta un thread worker.
 */
/**
 * Clasa ce reprezinta o solutie partiala pentru problema de rezolvat. Aceste
 * solutii partiale constituie task-uri care sunt introduse in workpool.
 */

class PartialSolution {
	public void execute() throws IOException {
	}
}

class Worker extends Thread {
	WorkPool wp;

	public Worker(WorkPool workpool) {
		this.wp = workpool;
	}

	void processPartialSolution(PartialSolution ps) throws IOException {
		ps.execute();
	}

	public void run() {
		while (true) {
			PartialSolution ps = wp.getWork();
			if (ps == null)
				break;
			try {
				processPartialSolution(ps);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

public class ReplicatedWorkers {
	static int D, ND;
	static float X;

	// fiecare worker de map va face multe map-uri mici pe care va trebui sa le
	// puna undeva de unde sa poata fi luate de catre workerii de reduce, deci
	// am folosit un vector de map-uri pentru un fisier:
	// Vector<HashMap<String, Integer>> chunkMaps;

	static HashMap<String, Vector<HashMap<String, Integer>>> chunkMaps;

	// fiecare worker de reduce va face mai multe hash-uri de cuvinte
	// pentru toate documentele
	static HashMap<String, HashMap<String, Integer>> fileMaps;

	// fiecare worker de compare va face mai multe hash-uri avand ca si cheie
	// numele celor 2 documente de comparat si similaritatea dintre ele
	static Hashtable<String, Double> compareRes;

	// intoarcea lungimea unui fisier
	public static long getFileSize(String name) {
		File file = new File(name);
		return file.length();
	}

	public static void main(String args[]) throws IOException,
			InterruptedException {
		// iau argumentele din linia de comanda
		int NT = Integer.parseInt(args[0]);
		String input = args[1], output = args[2];
		String line;
		String[] files = new String[100];
		FileInputStream stream = new FileInputStream(input);
		BufferedReader bf = new BufferedReader(new InputStreamReader(stream));
		// citirea dimensiunii fragmentelor
		line = bf.readLine();
		D = Integer.parseInt(line);

		// citirea pragului de similaritate
		line = bf.readLine();
		X = Float.parseFloat(line);

		// citirea numarului de fisiere de comparat
		line = bf.readLine();
		ND = Integer.parseInt(line);

		// citirea numelor fisierelor
		for (int i = 0; i < ND; i++) {
			files[i] = bf.readLine();
		}

		bf.close();

		// creez un workpool pentru taskurile de map
		WorkPool mapPool = new WorkPool(NT);

		chunkMaps = new HashMap<String, Vector<HashMap<String, Integer>>>();
		// impart fisierele in fragmente pe care le vor prelucra in paralel
		// fiecare worker
		for (int i = 0; i < ND; i++) {
			chunkMaps.put(files[i], new Vector<HashMap<String, Integer>>());
			int lenDoc = (int) getFileSize(files[i]);
			int offBegin = 0;
			while (lenDoc > 0) {
				// ultimul fragment poate avea dimensiune mai mica
				if (lenDoc < D) {
					MapTask mapTask = new MapTask(files[i], offBegin, lenDoc,
							chunkMaps);
					mapPool.putWork(mapTask);
					lenDoc = 0;
				} else {
					MapTask mapTask = new MapTask(files[i], offBegin, D,
							chunkMaps);
					mapPool.putWork(mapTask);
					lenDoc -= D;
					offBegin += D;
				}
			}
		}

		Worker[] workers = new Worker[NT];

		// pornesc workerii
		for (int i = 0; i < NT; i++) {
			workers[i] = new Worker(mapPool);
			workers[i].start();
		}
		// astept ca toti workerii sa termine ce au de prelucrat
		for (int i = 0; i < NT; i++) {
			workers[i].join();
		}

		// creez un workpool pentru taskurile de reduce
		WorkPool reducePool = new WorkPool(NT);
		fileMaps = new HashMap<String, HashMap<String, Integer>>();

		// pentru fiecare fisier se va returna hashul de cuvinte
		for (int i = 0; i < ND; i++) {
			ReduceTask reduceTask = new ReduceTask(files[i],
					chunkMaps.get(files[i]), fileMaps);
			reducePool.putWork(reduceTask);

		}

		// pornesc workerii
		for (int i = 0; i < NT; i++) {
			workers[i] = new Worker(reducePool);
			workers[i].start();
		}

		// astept ca toti workerii sa termine ce au de prelucrat
		for (int i = 0; i < NT; i++) {
			workers[i].join();
		}

		PrintWriter out = new PrintWriter(output);

		// creez un workpool pentru taskurile de compare
		WorkPool comparePool = new WorkPool(NT);

		compareRes = new Hashtable<String, Double>();

		// execut taskurile pentru fisierele pe care trebuie sa le compar
		// se va returna un hashtable cu numele celor 2 fisiere de comparat
		// si similaritatea dintre ele
		for (int i = 0; i < ND - 1; i++) {
			for (int j = 1; j < ND; j++) {
				if (i != j && i < j) {
					CompareTask c = new CompareTask(files[i],
							fileMaps.get(files[i]), files[j],
							fileMaps.get(files[j]), compareRes);
					comparePool.putWork(c);
				}
			}
		}

		// pornesc workerii
		for (int i = 0; i < NT; i++) {
			workers[i] = new Worker(comparePool);
			workers[i].start();
		}

		// astept ca toti workerii sa termine ce au de prelucrat
		for (int i = 0; i < NT; i++) {
			workers[i].join();
		}
		ArrayList<Map.Entry<String, Double>> l = new ArrayList<Entry<String, Double>>(
				compareRes.entrySet());
		// creez un comparator pentru a sorta descrescator rezultatele finale
		Collections.sort(l, new Comparator<Map.Entry<String, Double>>() {

			public int compare(Map.Entry<String, Double> o1,
					Map.Entry<String, Double> o2) {
				return -o1.getValue().compareTo(o2.getValue());
			}
		});

		// scriu in fisier numai fisierele care au gradul de similaritate mai
		// mare decat un prag
		for (int i = 0; i < compareRes.size(); i++) {
			if (l.get(i).getValue() > X) {
				BigDecimal similPrint = new BigDecimal(l.get(i).getValue());
				similPrint = similPrint.setScale(4, RoundingMode.HALF_DOWN);
				out.println(l.get(i).getKey() + similPrint);
			}
		}
		out.close();
	}
}
