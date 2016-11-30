import java.util.*;
import ranking.DefaultRanking;
import ranking.OkapiRanking;
import ranking.Tf_idfRanking;
import ranking.Wacky;


public class DiskEngine {


	private static Scanner scan;
	//TODO: Fix the scanner

	public static void main(String[] args) {
		scan = new Scanner(System.in);
		System.out.println("Menu:");
		System.out.println("1) Build");
		System.out.println("2) Query");
		int choice = scan.nextInt();
		switch (choice) {
		case 1:
			//TODO: Make different class for Ranked 
			buildIndex();
			break;
			
		case 2:
			//TODO: Make a different class for Boolean
			Scanner in = new Scanner(System.in);
			System.out.println("Enter the name of an index to read:");
			String indexName = in.nextLine();
			if (indexName.equals("Document")) {
				indexName = "/Users/Abdullah/EclipseWorkSpace/CECSHW5/Document";
			}

			DiskInvertedIndex index = new DiskInvertedIndex(indexName);
			System.out.println("Menu:");
			System.out.println("1) Ranked");
			System.out.println("2) Boolean");
			choice = scan.nextInt();
			switch(choice){
				case 1:
					rankedRetreval(index);
					break;
				case 2:
					BooleanRetrival r = new BooleanRetrival(index, indexName);
					r.start();
					break;
			}
			break;
		}
	}

	public static void rankedRetreval(DiskInvertedIndex index) {
		boolean run = true;
		while(run){
			System.out.println("1) Default");
			System.out.println("2) tf-idf");
			System.out.println("3) Okapi BM25");
			System.out.println("4) Wacky");
			System.out.println("5) Exit Program");
			System.out.println("Choose a selection:");
			int choice = scan.nextInt();
			scan.nextLine();
			Ranking r = null;
			switch(choice){
			case 1:
				r = new Ranking(new DefaultRanking());
				break;
			case 2:
				r = new Ranking(new Tf_idfRanking());
				break;
			case 3:
				r = new Ranking(new OkapiRanking());
				break;
			case 4:
				r = new Ranking(new Wacky());
				break;
			case 5:
				run = false;
				System.out.println("Program Ended");
				continue;
			}
			
			Ranking(index, r);

		}
		
	}
	
	
	
	public static void Ranking(DiskInvertedIndex index, Ranking rank){
		//Getting String
		System.out.println("Enter one or more search terms, separated by spaces:");
		String input = scan.nextLine();
		System.out.println("number of w: " + input.split(" ").length);
		//TODO: Redo Tonight Nov 29
		List<PositionPosting> postingsList = new ArrayList<PositionPosting>();
		List<PositionPosting> postingsListB = new ArrayList<PositionPosting>();
		if (input.split(" ").length <= 1) {
			
			System.out.println("Single");
			postingsList = index.GetPostings(
					PorterStemmer.processToken(input.toLowerCase()), true, rank);
		}else if (input.split(" ").length > 1) {
			String[] inputArray = input.split(" ");
			boolean run = true;
			for (String a : inputArray) {
				System.out.println("Term: " + a);
				List<PositionPosting> postingsListA = index.GetPostings(
						PorterStemmer.processToken(a.toLowerCase()), false, rank);
				
				int i = 0;
				int j = 0;
				while (run) {
					if(postingsListA.size() <= 0){
						run = false;
						continue;
					}else if (postingsList.isEmpty()) {
						System.out.println("Loading...");
						postingsList.addAll(postingsListA);
						run = false;
					} else {
						// Naive Way: O(n^2) --> max(n) = 10 thus Worst
						// Need to change from linear to binary search
						// scenario 100
						
						try{
						if (postingsListA.get(i).getDocument() == postingsListB.get(j).getDocument()) {
							PositionPosting temp = postingsListA.get(i);
							double score = postingsListA.get(i).getScore()
									+ postingsListB.get(j).getScore();
							temp.setScore(score);
							// postingsList.get(j) = temp;
							postingsListB.remove(j);
							postingsListB.add(j, temp);
							if (!(i == postingsListA.size() - 1)) {
								j = 0;
								i++;
							} else {
								postingsList.addAll(postingsListB);
								run = false;
							}

						} else {
							if (!(j == postingsListB.size() - 1)) {
//								System.out.println("incrementing j");
								j++;
							} else {
//								System.out.println("added document "
//										+ index.getFileNames().get(
//												postingsListA.get(i)
//														.getDocument()));
//								postingsList.add(postingsListA.get(i));
								if (!(j == postingsListA.size() - 1)) {
									j = 0;
									i++;
								} else {
									run = false;
								}
							}
						}
						}catch(Exception e){
							run = false;
						}
					}
				}
//				postingsListA.clear();
				i = 0;
				j = 0;
				run = true;
			}

			Heap_PQ j = new Heap_PQ(10);
			j.insert(postingsListB);

			
			for (int count = 0; count < 10; count++) {
				if (count == 10) {
					continue;
				} else {
					postingsList.add(j.extractMax());
				}

			}
		}

		if (postingsList == null) {
			System.out.println("Term not found");
		} else {
			System.out.println("Docs: ");
			int i = 1;
			for (PositionPosting post : postingsList) {
				if(i == 11){
					continue;
				}
				
				System.out.println(i + ") "
						+ index.getFileNames().get(post.getDocument())
						+ " -- Score:" + post.getScore());
				i++;
			}

		}
		postingsList.clear();
		System.out.println();
		System.out.println();
	}

	/**
	 * 
	 */
	public static void buildIndex() {
		scan= new Scanner(System.in);
		System.out.println("Enter the name of a directory to index: ");
		String folder = scan.nextLine();
		
		if (folder.equals("Document")) {
			folder = "/Users/Abdullah/EclipseWorkSpace/CECSHW5/Document";
		}
		
		IndexWriter writer = new IndexWriter(folder);
		writer.buildIndex();
	}
	
}
