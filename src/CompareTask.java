import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;


public class CompareTask extends PartialSolution {
	public String docName1;
	public HashMap<String, Integer> hash1 = new HashMap<String, Integer>();
	public String docName2;
	public HashMap<String, Integer> hash2 = new HashMap<String, Integer>();
	double similaritate;
	public HashMap<String, Double> frequencyWords1 = new HashMap<String, Double>();
	public HashMap<String, Double> frequencyWords2 = new HashMap<String, Double>();
	public Set<String> vocabular = new HashSet<String>();
	// o sa pastrez rezultatul taskului de compare intr-un hash
	public Hashtable<String, Double> compareRes = new Hashtable<String, Double>();

	public CompareTask(String docName1, HashMap<String, Integer> hash1,
			String docName2, HashMap<String, Integer> hash2, Hashtable<String, Double> compareRes) {
		this.docName1 = docName1;
		this.hash1 = hash1;
		this.docName2 = docName2;
		this.hash2 = hash2;
		this.compareRes = compareRes;
	}

	// creez o multime numita vocabular in care am termenii din cele doua documente de comparat
	public void createVocabular(HashMap<String, Integer> h1,
			HashMap<String, Integer> h2) {
		for (Map.Entry<String, Integer> entry : h1.entrySet()) {
			vocabular.add(entry.getKey());
		}
		for (Map.Entry<String, Integer> entry : h2.entrySet()) {
			vocabular.add(entry.getKey());
		}
	}
	
	// returneaza numarul total de cuvinte din document
	public static int getTotal(HashMap<String, Integer> hash) {
		int total = 0;
		for (Map.Entry<String, Integer> entry : hash.entrySet()) {
			total += entry.getValue();
		}
		return total;
	}
	
	// calculeaza frecventa de aparitie a cuvintelor dintr-un document
	public void calcFrequency(HashMap<String, Integer> hash,
			HashMap<String,Double> frequencyWords, int total) {
		for (Object object : vocabular) {
			String word = (String) object;	
			// daca nu exista un cuvant in hash are frecventa 0;
			if (!hash.containsKey(word)) {
					frequencyWords.put(word, 0.0d);
				} else {
					frequencyWords.put(word, (double)((double)hash.get(word)/total));
				}
		}
	
	}

	@Override
	public void execute() throws IOException {
		createVocabular(hash1, hash2);
		calcFrequency(hash1, frequencyWords1, getTotal(hash1));
		calcFrequency(hash2, frequencyWords2, getTotal(hash2));
		// calculeaza similaritatea dintre 2 documente
		for (Object object : vocabular) {
			String word = (String) object;	
			similaritate += frequencyWords1.get(word) * frequencyWords2.get(word);
		}
		similaritate = Math.round(similaritate * 1000000)/10000.0;
		// pun intr-un hash un string cu numele celor 2 documente si similaritatea
		compareRes.put(docName1 + ";" + docName2 + ";", similaritate);
	}
}
