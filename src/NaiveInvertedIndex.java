import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Map;
import java.util.List; 
import java.util.Set;
/**
 * 
 * @author Richard
 * A positional inverted index. A hashmap that maps from a string (term) to a positional posting object (position posting).
 * 
 */
public class NaiveInvertedIndex {

	private int hashMapSize, postingsListSize, positionsSize, stringsSize, total, docAvg = 0;
	private double [] frequency = new double[10];
	
	private Map<String, List<PositionPosting>> Index;
	
	/**
	 * Default constructor for an Index
	 */
	public NaiveInvertedIndex () {
		Index = new HashMap<String, List<PositionPosting>>();
	}
	
	/**
	 * Will add a new term or position posting to the index
	 * @param term The term to be worked with (added entirely, or just a position posting if already there)
	 * @param doc Document ID
	 * @param position position in document
	 */
	public void addTerm (String term, int doc, int position) {
		
		if (!(Index.containsKey(term))) {//Index does not contain this term. Create a new list for it
			List <PositionPosting> temp = new ArrayList<PositionPosting>();
			temp.add(new PositionPosting(doc, position));
			Index.put(term, temp);
		}
		
		else if ((Index.get(term).contains(new PositionPosting (doc, 0)))){//index does contain this term with proper doc list
			Index.get(term).get(Index.get(term).size() - 1).addPosting(position);
		}
		
		else {
			Index.get(term).add(new PositionPosting(doc, position));
		}

	}
	
	/**
	 * 
	 */
	public Map<String, List<PositionPosting>> getIndexInfo(){
		return Index;
	}
	/**
	 * 
	 */
	public void addIndexInfo(Map<String, List<PositionPosting>> addedIndex){
		Index.putAll(addedIndex);
	}
	
	/**
	 * Will return a list of position postings for a particular term
	 * @param term Will retrieve all position postings of said term
	 * @return A list of all position postings
	 */
	public List<PositionPosting> getPositionPostings (String term) {
		if (!(Index.containsKey(term))) {
			System.out.println(term + " not in corpus.");
			return null;
		}
		return Index.get(term);
	}
	
	
	/**
	 * 
	 */
	public double getL_d(String term){
		
		ArrayList<Double> docweight = new ArrayList<Double>();
		List<PositionPosting> postion = getPositionPostings(term);
		double l_doc = 0.0;
		for(PositionPosting temp : postion){
			 l_doc += Math.pow(Math.log(temp.getPositionSize()) + 1, 2);
		}
		
		l_doc = Math.sqrt(l_doc);
		
		return l_doc;
	}
	
	/**
	 * 
	 */
//	public double getWdt(int termfeq)
//	{
//		
//		return (Math.log(termfeq) + 1);
//	}
	
	/**
	 * Will return all the documents a term is found in
	 * @param term The term to retrieve documents for
	 * @return A set of documents term is found in
	 */
	public List<Integer> getDocuments (String term) {
		if (!(Index.containsKey(term))) {
			System.out.println(term + " not in corpus.");
			return null;
		}
		List <Integer> documents = new ArrayList<Integer>();
		for (PositionPosting temp: Index.get(term)) {
			documents.add(temp.getDocument());
		}
		return documents;
	}
	
	/**
	 * Will return all the terms in the corpus
	 * @return an array of all terms in the corpus in sorted order
	 */
	public String [] getDictionary () {
		String [] words = Index.keySet().toArray(new String[Index.keySet().size()]);
		Arrays.sort(words);
		return words;
	}
	
	
	/**
	 * Will check if a term2 is within k terms from term1
	 * @param term1 The first term
	 * @param term2 The second term
	 * @param k the distance to check from term1
	 * @return whether or not term2 is found within distance k from term1
	 */
	public List<Integer> near(String term1, String term2, int k) {//can also use list
		List <Integer> docs = new ArrayList<Integer>();
		ListIterator <PositionPosting> positions = Index.get(term1).listIterator();
		ListIterator <PositionPosting> compareList = Index.get(term2).listIterator();
		PositionPosting pointer1 = positions.next();
		PositionPosting pointer2 = compareList.next();
		if (pointer1.near(pointer2, k))
			docs.add(pointer1.getDocument());
		
			while (true) {
				int comparison = pointer1.compareTo(pointer2);//if in lesser doc, will automatically move to next
				if (comparison < 0) {
					if (!(positions.hasNext()))
						break;
					pointer1 = positions.next();
				}
				else if (comparison > 0) {
					if (!(compareList.hasNext())) 
						break;
					pointer2 = compareList.next();
				}
				else if (comparison == 0) {
					if (!(compareList.hasNext()) || !(positions.hasNext()))
						break;
					pointer1 = positions.next();
					pointer2 = compareList.next();
				}

				if (pointer1.near(pointer2, k))
					docs.add(pointer1.getDocument());
			}
		return docs;
	}
	

	/**
	 * Will process a phrase query
	 * @param terms A list of individual strings that formed the phrase query
	 * @return the documents where the phrase query was found in
	 */
	public List<Integer> phraseQuery (String [] terms) {
		
		for(String term : terms){
			System.out.println(term);
		}
		System.out.println("End");
		List <Integer> returnSet = near(terms[0], terms[1], 1);
		List<Integer> temp2;
		for (int i = 2; i < terms.length; i++) {
			temp2 = near(terms[i - 1], terms[i], 1);
			
			returnSet = intersect(returnSet, temp2);
		}
		return returnSet;
 	}
	
	/**
	 * Will give the intersection of two sets of integers
	 * @param set1 The first set to use
	 * @param set2 The second set to use
	 * @return the intersection of set1 and set2
	 */
	private List<Integer> intersect (List<Integer> set1, List<Integer> set2) {
		List <Integer> returnSet = new ArrayList<Integer>();
		for (int a: set1) {
			if (set2.contains(a))
				returnSet.add(a);
		}
		return returnSet;
	}

	/**
	 * A method to be invoked once an index completes indexing. 
	 * Calculates statistics of the index.
	 */
	public void finishIndexing() {
		hashMapSize = 24 + (36 * Index.size());
		for (String a: Index.keySet()) {
			stringsSize += (40 + (2* a.length()));
			postingsListSize += 24 + (8 * Index.get(a).size());
			for (PositionPosting posting: Index.get(a))
				positionsSize += 48 + (4 *posting.getPositions().size());
		}
		total = (stringsSize + hashMapSize + postingsListSize + positionsSize);
		int temp = 0;
		for (String a: Index.keySet())
			temp += Index.get(a).size();
		docAvg = (temp / Index.size());
		frequentTerms();
	}

	/**
	 * Accessor for the total memory requirements
	 * @return total memory requirement (in bytes)
	 */
	public int getMemoryRequirement () {
		return total;
	}
	
	/**
	 * Accessor for size of the hashmap
	 * @return size of the hashmap
	 */
	public int getHashMapSize () {
		return hashMapSize;
	}
	
	/**
	 * 
	 * @return size of all strings (
	 */
	public int getStringsSize () {
		return stringsSize;
	}
	
	/**
	 * 
	 * @return size of all the postings lists 
	 */
	public int getPostingsListSize () {
		return postingsListSize;
	}
	
	/**
	 * 
	 * @return size of all the positions lists
	 */
	public int getPositionsSize () {
		return positionsSize;
	}
	
	/**
	 * 
	 * @return number of terms in the index
	 */
	public int getNumOfTerms () {
		return Index.size();
	}
	
	/**
	 * 
	 * @return average number of documents in the postings list of terms
	 */
	public int getDocAvg () {
		return docAvg;
	}
	
	/**
	 * 
	 * @return Proportion of the documents that the 10 most frequent terms are in
	 */
	public double[] getMostFrequent () {
		return frequency;
	}
	/**
	 * Will calculate the proportion of the 10 most frequent terms
	 */
	private void frequentTerms () {
		ArrayList <Double> frequency1 = new ArrayList<Double>();
		double temp;
		for (String a: Index.keySet()) {
			temp = Index.get(a).size();
			if (temp > docAvg)
			{
			
				frequency1.add(temp);
			}
		}
		Collections.sort(frequency1);
		for (int i = 0; i <10; i++)
			frequency[i] = frequency1.get((frequency1.size() - (11 - i)));
	}
}