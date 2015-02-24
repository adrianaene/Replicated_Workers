Ene Adriana
331CC

	Am urmat scheletul de laborator pentru rezolvarea temei.	
	Am creat 3 clase separate pentru cele 3 taskuri: map, reduce si compare ce extind clasa PartialSolution.
	Clasa workPool am lasata neschimbata, ca in scheletul de laborator.
	In clasa replicatedWorkers creez taskurile de map, reduce si compare si pornesc workerii.
	Am folosit 3 structuri pentru a tine rezultatul dupa fiecare etapa. 
	1.chunkMaps: HashMap<String, Vector<HashMap<String, Integer>>>
		se vor retine pentru fiecare document hash-urile mici de cuvinte
	2.fileMaps: HashMap<String, HashMap<String, Integer>>;
		va contine pentru fiecare document intregul hash de cuvinte
	3.compareRes: Hashtable<String, Double> 
		va contine numele celor 2 documente de comparat si similaritate dintre ele.

	Paralelizarea problemei este facuta de modelul thread-pool
	