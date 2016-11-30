import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class PositionPosting implements Comparable<PositionPosting> {

	private int document;
	private double score;
	private List<Integer> positions = new ArrayList<Integer>();

	/**
	 * Constructor for a new positionposting object. Must pass an ID and a
	 * position to initialize
	 * 
	 * @param doc
	 *            The integer representing a documents ID
	 * @param position
	 *            the integer position representing a location in the corpus
	 */
	public PositionPosting(int doc, int position) {
		document = doc;
		positions.add(position);
	}
	
	
	/**
	 * 
	 * @param score
	 */
	public void setScore(double score){
		this.score = score;
	}
	
	/**
	 * 
	 */
	public double getScore(){
		return score;
	}

	/**
	 * Will add a position to the list of positions
	 * 
	 * @param position
	 *            The integer representing the position in the document
	 */
	public void addPosting(int position) {
		positions.add(position);
	}
	/**
	 * 
	 * @return 
	 */
	public int getPositionSize(){
//		System.out.println("Doc Weight "+(Math.log(positions.size()) + 1));
		
		return positions.size();
	}
	
	
	/**
	 * Will return the document int
	 * 
	 * @return document
	 */
	public int getDocument() {
		return document;
	}

	/**
	 * Will retrieve all the positions from a position posting
	 * 
	 * @return
	 */
	public List<Integer> getPositions() {
		return positions;
	}
	
	/**
	 * Will compare if two position postings are equal (same doc ID).
	 */
	public boolean equals(Object other) {
		if (!(other instanceof PositionPosting))
			return false;
		PositionPosting p = (PositionPosting) other;
		if (this.document != p.getDocument())
			return false;
		// At this point, they're of the same document
		else
			return true;
	}

	/**
	 * Will compare position postings by document
	 */
	@Override
	public int compareTo(PositionPosting other) {
		// TODO Auto-generated method stub
		return this.document - other.getDocument();
	}
	/**
	 * 
	 */

	/**
	 * Will check if a position posting is within a certain distance ahead
	 * 
	 * @param other
	 *            The position posting to compare to
	 * @param k
	 *            distance to be traversed ahead
	 * @return true or false
	 */
	public boolean near(PositionPosting other, int k) {
		if (document != other.getDocument())
			return false;
		ListIterator<Integer> thisList = positions.listIterator();
		ListIterator<Integer> compareList = other.getPositions().listIterator();
		int pointer1 = thisList.next();
		int pointer2 = compareList.next();
		int comparison = pointer2 - pointer1;

		while (true) {
			if (comparison >= 0 && comparison <= k)
				return true;
			if (comparison > 0) {
				// pointer2 (the compare list) was greater. Increment smaller
				// one
				if (!thisList.hasNext())
					return false;
				pointer1 = thisList.next();
			} else {
				// pointer1 (the this list element) was greater. Increment
				// smaller one
				if (!compareList.hasNext())
					return false;
				pointer2 = compareList.next();
			}
			// if comparison == 0 it would have returned already
			comparison = pointer2 - pointer1;
		}
	}

	public String toString() {
		return document + "|" + score;
	}


	

}