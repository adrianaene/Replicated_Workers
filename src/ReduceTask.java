import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class ReduceTask extends PartialSolution {
	public String docName;
	// o sa pastrez rezultatul taskului de reduce intr-un hashMap
	public HashMap<String, HashMap<String, Integer>> hashVector = new HashMap<String, HashMap<String, Integer>>();
	public Vector<HashMap<String, Integer>> list = new Vector<HashMap<String, Integer>>();
	public HashMap<String, Integer> hash = new HashMap<String, Integer>();

	public ReduceTask(String docName, Vector<HashMap<String, Integer>> list,
			HashMap<String, HashMap<String, Integer>> hashVector) {
		this.docName = docName;
		this.list = list;
		this.hashVector = hashVector;
	}

	@Override
	public void execute() throws IOException {
		// creez un hash mare care sa contina toate hashurile mai mici ale
		// documentului
		for (HashMap<String, Integer> per : list) {
			for (Map.Entry<String, Integer> entry : per.entrySet()) {
				if (!hash.containsKey(entry.getKey())) {
					hash.put(entry.getKey(), entry.getValue());
				} else {
					hash.put(entry.getKey(),
							hash.get(entry.getKey()) + entry.getValue());
				}
			}
		}
		hashVector.put(docName, hash);
	}

}
