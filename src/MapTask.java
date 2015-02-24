import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Vector;

public class MapTask extends PartialSolution {
	public String docName;
	public int offBegin;
	public int dimFrag;
	public int length;
	byte fragment[];
	private HashMap<String, Integer> currentMap;
	private HashMap<String, Vector<HashMap<String, Integer>>> result;

	public MapTask(String docName, int offBegin, int dimFrag,
			HashMap<String, Vector<HashMap<String, Integer>>> result) {
		this.docName = docName;
		this.offBegin = offBegin;
		this.dimFrag = dimFrag;
		this.length =  dimFrag;
		fragment = new byte[ dimFrag];
		this.result = result;
	}

	@Override
	public void execute() throws IOException {
		currentMap = new HashMap<String, Integer>();
		String word = "";
		RandomAccessFile file = new RandomAccessFile(docName, "r");
		file.seek(offBegin);
		file.read(fragment);

		int i = 0;
		// daca taskul curent este referitor la primul chunk, citesc
		// inclusiv primul cuvant
		if (offBegin == 0) {
			while (i < fragment.length) {
				if (!isDelimitator((char) fragment[i])) {
					word += (char) fragment[i];
				} else {
					addToHash(word);
					word = "";
				}
				i++;
			}
			// verific daca mai am vreun cuvant de citit care se incadreaza
			// in chunkul urmator si deci citesc in avans
			byte b = file.readByte();
			while (!isDelimitator((char) b)) {
				word += (char) b;
				b = file.readByte();
			}
			// adaug si ultimul cuvant
			addToHash(word);
		} else {
			// daca taskul curent nu este primul chunk din fisier
			// sarim peste primul cuvant, care a fost parcurs de taskul
			// precedent si ne pozitionam la inceputul cuvantului urmator
			while (i < fragment.length
					&& !isDelimitator((char) fragment[i])) {
				i++;
			}
			// introducem in map cuvintele din interiorul chunkului
			while (i < dimFrag) {
				if (!isDelimitator((char) fragment[i])) {
					word += (char) fragment[i];
				} else {
					addToHash(word);
					word = "";
				}
				i++;
			}
			// introducem si cuvantul din chunkul urmator, daca exista
			byte b = 0;
			while (offBegin + i < file.length()
					&& !isDelimitator((char) b)) {
				b = file.readByte();
				if (!isDelimitator((char) b))
					word += (char) b;
				i++;
			}
			// daca a fost vreun cuvant nou inceput in chunk-ul curent si
			// care se termina in chunk-ul urmator, il adaug la map
			if (b != 0)
				addToHash(word);
		}

		result.get(docName).add(currentMap);
		file.close();
	}

	// functie ce adauga un cuvant la hash
	public void addToHash(String word) {
		int nrApp = 0;
		word = word.toLowerCase();
		if (word != "") {
			if (currentMap.containsKey(word)) {
				nrApp = currentMap.get(word);
			}
			nrApp++;
			currentMap.put(word, nrApp);
		}
	}

	// functie ce verifica daca un caracter este delimitator sau nu
	public boolean isDelimitator(char ch) {
		if (ch == ';' || ch == ':' || ch == '/' || ch == '?' || ch == '~'
				|| ch == '\\' || ch == '.' || ch == ',' || ch == '>'
				|| ch == '<' || ch == '`' || ch == '[' || ch == ']'
				|| ch == '{' || ch == '}' || ch == '(' || ch == ')'
				|| ch == '!' || ch == '@' || ch == '#' || ch == '$'
				|| ch == '%' || ch == '^' || ch == '&' || ch == '-'
				|| ch == '_' || ch == '+' || ch == '\'' || ch == '='
				|| ch == '*' || ch == '"' || ch == '|' || ch == ' '
				|| ch == '\t' || ch == '\n')
			return true;
		else
			return false;

	}

}
