import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class BooleanRetrival {
		// commonFiles is the combination of all the results
		private static List<Integer> commonFiles = new ArrayList<Integer>();
		// basePath is the file path to read from
		private static String basePath = null;
		// index stores all of the positions and the DocumentIDs
		private static NaiveInvertedIndex indexI = new NaiveInvertedIndex();
		// fileNames is all of the files within the specified path
		private final static List<String> fileNames = new ArrayList<String>();
		// numberOfDocuments is the number of files within the document
		private static int numberOfDocuments = 0;
		// pqSet is global variable for the set returned for Phrase Query
		private static List<Integer> pqSet = null;
		// pqbool is global variable to check if a phrase is used
		private static boolean pqbool = false;
		//
		private static Scanner scan;
		//
		private static DiskInvertedIndex indexD = null;


	public BooleanRetrival(DiskInvertedIndex index,String filePath){
		scan = new Scanner(System.in);
		fileNames.addAll(index.getFileNames());
		basePath = filePath;
		indexD = index;
	}
	
	public static void start(){
		boolean run = true;
		
		while (run) {
			System.out.print("Search: ");
			String input = scan.nextLine();
			if (!(input.equals(" ") || input.equals(""))) {
				try {
					quiriesFilter(PorterStemmer.processToken(input.toLowerCase()));
					if (!(commonFiles.isEmpty())) {
						printCommonFiles(fileNames);
						commonFiles.clear();
					}
				} catch (Exception e) {

					System.out.println("Error (Main) - > " + e);
					e.printStackTrace();

				}

			} else {
				System.out.println();
			}
		}	
	}
	
	
	/**
	 * Main Menu for commands
	 */
	public static void quiriesFilter(String userinput) {
		
//		DiskInvertedIndex index = new DiskInvertedIndex(indexName);
		// Split the user input with +
		String[] termArray = userinput.split("\\s*\\+\\s*");
		// Convert it to an ArrayList
		ArrayList<String> term = new ArrayList<String>(Arrays.asList(termArray));
		// System.out.println("Searching " + termArray[0].trim());
		switch (termArray[0].trim()) {
		case ":q":
			// Shutting Down
			System.out.println("Shutting Down");
			// Writing the path in a txt file
			System.exit(0);
			break;
		case ":stem":
			// Stemming a term
			try {
				System.out.println("Enter a word to Stem");
				System.out.print(":");
				String stemming = scan.nextLine();
				stemming = PorterStemmer.processToken(stemming);
				System.out.println("Out Of the stemmer is -> " + stemming);

			} catch (InputMismatchException ie) {
				System.out
						.println("Cant stem a Number or a better answer a number is itself stemmed !! Blow, Your, Mind Boom");
			} catch (Exception e) {
				System.out.println("No Word To stem?");
			}
			break;

		case ":index":
			// Index a new File
			System.out.println("Indexing: ");
//			fileNames.clear();
//			index = new NaiveInvertedIndex();
//			buildIndex();
			break;

		case ":vocab":
			// Print all of the terms within the corpus
			System.out.println("Printing all vocab in the corpus");
			printResults(fileNames);
			break;

		case ":system":
			// System Requirements
			System.out.println("Number of terms: " + indexI.getNumOfTerms());
			System.out.println("Avrg number of documents in positings list: "
					+ indexI.getDocAvg());
			System.out.println("10 most frequent terms:");
			double[] frq = indexI.getMostFrequent();
			for (int i = 9; i >= 0; i--) {
				System.out.print((frq[i] / 100.0) + " ");
			}

			System.out.println("\nTotal Memory Requirment: "
					+ indexI.getMemoryRequirement() + "bytes");
			break;

		default:
			if (!(termArray[0].startsWith(":"))) {
				for (int i = 0; i < termArray.length; i++) {
					try {
						// Query
						String query = term.get(i);
						//-----
						if (query.contains("\"")) {
							// PhraseQuery
							NaiveInvertedIndex index = new NaiveInvertedIndex();
							NaiveInvertedIndex tempIndex = null;
							String temppq = "";
							// Split by "
							temppq = query.split("\"")[1];
							// return the query without the phrase
							query = query.replaceAll("\"" + temppq + "\"", "");
							
							// Start Stemming the PhraseQuery
							String[] phraseQuery = temppq.split(" ");
							for (int j = 0; j < phraseQuery.length; j++) {
								phraseQuery[j] = PorterStemmer
										.processToken(phraseQuery[j]);
//								System.out.println(phraseQuery[j]);
								tempIndex = indexD.getIndex(phraseQuery[j]);
								index.addIndexInfo(tempIndex.getIndexInfo());
								
							}
							// Get the result of the query
							List<Integer> result = index.phraseQuery(phraseQuery);

							Iterator<Integer> a = result.iterator();

							if (query.isEmpty()) {
//								System.out.println("result:");
								// An OR operation
								while (a.hasNext()) {
									Integer b = a.next();
//									System.out.println(b);
									if (!(commonFiles.contains(b))) {
										commonFiles.add(b);
									}
								}
							} else {
								// AND operation
								pqSet = result;
								pqbool = true;
							}
						}
						
						//-----
						if (query.contains(" ") || pqbool == true) {
							// Does AND operation
							operationAND(query);

						} else {
							//-----
							if (!(query.isEmpty())) {
								// Does OR operation
								operationOR(query);
							}

						}

					} catch (IndexOutOfBoundsException e) {
//						System.out.println("an error (quiriesFilter) ->" + e);
					} catch (NullPointerException nul) {
						// System.out.println("an error (quiriesFilter) ->" +
						// e);
					}
				}
			}
			break;
		}
	}
	/**
	 * Using the (term) what ever term that does not have space is part of the OR operation
	 * @param term
	 */
	private static void operationOR(String term) {
		try {
			
			
			//Main Iterator for OR Operation
			List<Integer> a = null;
			//Checking if NEAR == true
			String[] nearTerm = term.split("near/");

			
			if (!(nearTerm.length > 1)) {
				NaiveInvertedIndex index = indexD.getIndex(term);
				term = PorterStemmer.processToken(term);
				a = index.getDocuments(term);
				
			} else {
				// Near Operation
//				System.out.println("NEAR");
				List<Integer> setTemp = generateNEAR(term);
				//index.near(term1, term2, diffBetween);
				a = setTemp;
			}

			// Doing OR Operation
			for(int doc: a){
				
				Integer b = doc;
				if (!(commonFiles.contains(b))) {
					// If not in the final result add it
					commonFiles.add(b);
				}
			}

		} catch (NullPointerException nul) {
			System.out.println("Not Found in Courpus");
		} catch (Exception e) {
			System.out.println("an error (generateORList)  - > " + e);
		}
	}
	
	/**
	 * Using the (terms) that contain a space between them, start operation 
	 * @param terms
	 */
	private static void operationAND(String terms) {
		try {
			// While loop boolean
			boolean check = true;
			// place holder String
			String psTerm = null;
			// All of the AND operation result stored at andDocuments
			List<Integer> andDocuments = new ArrayList<Integer>();
			// Splitting the terms with space
			String[] termArray = terms.split("\\s*\\ \\s*");
			// All of the termlist - documentlist stored at document
			List<List<Integer>> documents = new ArrayList<List<Integer>>();
			// Place holder Set
			List<Integer> tempSet = null;

			// Making sure the document is met for a AND operation
			if (termArray.length < 2 && pqbool == false) {
				operationOR(terms);
				return;
			}
			NaiveInvertedIndex index = null;
			// Getting all of the required Document ID throgh these filters
			for (int x = 0; x < termArray.length; x++) {
				psTerm = PorterStemmer.processToken(termArray[x]);
//				System.out.println(termArray[x]);
				index = indexD.getIndex(psTerm);
				try {
					String[] nearTerm = termArray[x].split("NEAR/");

					if (!(nearTerm.length > 1)) {
						// Normal AND Operation to be done
						psTerm = PorterStemmer.processToken(termArray[x]);
						tempSet = index.getDocuments(psTerm);

					} else {
						// NEAR operation
						// Get the set
						tempSet = generateNEAR(termArray[x]);

					}

					// Converting a Set to list
					Integer[] tempArray = tempSet.toArray(new Integer[tempSet
							.size()]);
					List<Integer> temp = Arrays.asList(tempArray);
					Collections.sort(temp);
					System.out.println("added term " + temp);
					documents.add(temp);

				} catch (NullPointerException e) {

					System.out.println(termArray[x] + "= Null");

				}// catch
			}// For loop

			// if there is AND Operation with phraseQuery
			if (pqbool == true) {
				andDocuments.addAll(pqSet);
				Collections.sort(andDocuments);
				pqbool = false;
			}

			for (int i = 0; i < documents.size(); i++) {

				if (andDocuments.isEmpty()) {

					andDocuments.addAll(documents.get(i));

				} else {

					int j = 0;
					int x = 0;
					while (x < andDocuments.size() && check == true) {
						// Postings list merges used
						try {
							if (andDocuments.get(x).compareTo(
									documents.get(i).get(j)) == 0) {
								// if both docID are equal add
								// andDocuments(a) == DocumentID of the other
								// term(b)
								if (!(commonFiles.contains(andDocuments.get(x)))) {
									commonFiles.add(andDocuments.get(x));
								}
								x++;
								j++;
							} else if (andDocuments.get(x).compareTo(
									documents.get(i).get(j)) < 0) {
								// a < b
								x++;
							} else {
								// a > b
								j++;
							}// End of main if term comperson
						} catch (IndexOutOfBoundsException e) {
//							System.out
//									.println("an error (generateANDList) #1 - > "
//											+ e);
							check = false;
						}
					}

				}
			}

		} catch (Exception e) {
//			System.out.println("an error (generateANDList) #2  - > " + e);
			// e.printStackTrace();
		}
	}
	/**
	 * 
	 */
	public static List<Integer> generateNEAR(String nearTerm){
		
		String[] nearSplit = nearTerm.split("near/");
		String term1 = null;
		String term2 = null;
		NaiveInvertedIndex index = new NaiveInvertedIndex();
		NaiveInvertedIndex tempIndex = null;  
		int diffBetween = 0;

		for (int j = 0; j < nearSplit.length; j++) {
			if (nearSplit[j].split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)").length > 1) {
				// For splitting a Number from a word
				// NEAR/(Number)(word)
				String[] numberSplit = nearSplit[j]
						.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
				term2 = numberSplit[1];
				
				diffBetween = Integer.parseInt(numberSplit[0]);
			
			} else {
				// The first word (word)NEAR
				term1 = nearSplit[j];
			}
		}
		// Porter Stem both terms
		term1 = PorterStemmer.processToken(term1);
		tempIndex = indexD.getIndex(term1);
		index.addIndexInfo(tempIndex.getIndexInfo());
		
		term2 = PorterStemmer.processToken(term2);
		tempIndex = indexD.getIndex(term2);
		index.addIndexInfo(tempIndex.getIndexInfo());
		
		
		
		return index.near(term1, term2, diffBetween);
	}

	/**
	 * Printing all the vocabulary in the corpus
	 * 
	 * @param fileNames
	 */
	private static void printResults(List<String> fileNames) {
		String[] allterm = indexI.getDictionary();
		int count = 0;
		for (int i = 0; i < allterm.length; i++) {
			count++;
			System.out.print(allterm[i] + ": ");
			List<Integer> documents = indexI.getDocuments(allterm[i]);

			Iterator<Integer> iterator = documents.iterator();
			while (iterator.hasNext()) {
				System.out.print(fileNames.get(iterator.next()) + " ");
			}
			System.out.println("");
		}
		System.out.println("\n------\nCount: " + count);
	}
	
	
	/**
	 * Printing the final result of the search
	 * 
	 * @param fileNames
	 */
	private static void printCommonFiles(List<String> fileNames) {
		System.out.println("");
		int count = 0;
		List<String> print = new ArrayList<String>();
		// adding all of the commonfiles (final result)
		for (int x = 0; x < commonFiles.size(); x++) {

			print.add(fileNames.get(commonFiles.get(x)));
			System.out.println(commonFiles.get(x));
		}

		// Sorting all of the files
		Collections.sort(print, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return extractInt(o1) - extractInt(o2);
			}

			int extractInt(String s) {
				String num = s.replaceAll("\\D", "");
				// return 0 if no digits found
				return num.isEmpty() ? 0 : Integer.parseInt(num);
			}
		});

		for (int i = 0; i < print.size(); i++) {
		
			System.out.println(i + " ) " + print.get(i));
			count++;
		}

		System.out.println("Document Count" + count);
		System.out.println("\n");
		if (!(print.isEmpty()))
			printDocumentsMenu(print);
	}

	/**
	 * Printing a menu for user chose
	 * 
	 * @param print
	 */
	private static void printDocumentsMenu(List<String> print) {
		try {
			System.out.println("Print ?(1 for yes | 0 for No)");
			System.out.print(":");
			int answer = scan.nextInt();
			if (answer == 1) {
				System.out.println("Choose a number from 0 to "
						+ (print.size() - 1));
				System.out.print(":");
				int chose = scan.nextInt();
				System.out.println("Printing " + print.get(chose) +  " ...\n\n");
				File read = new File(basePath + "/" + print.get(chose));
				printDocuments(read);
			}
		} catch (InputMismatchException e) {
			System.out.println("You should input a NUMBER!! Like -> (123)");
			printDocumentsMenu(print);

		} catch (IndexOutOfBoundsException e) {
			System.out
					.println("Weird the array haven't found you document (Try again)");
			printDocumentsMenu(print);
		} catch (Exception e) {
			System.out.println("an error (printDocumentsMenu)  - > " + e);
		}
	}

	/**
	 * 
	 * @param file
	 */
	private static void printDocuments(File file) {

		try (InputStream inputStream = new FileInputStream(file)) {
			// Using Gson parsing for the body
			Gson gson = new GsonBuilder().create();
			Reader reading = new InputStreamReader(inputStream);
			GsonData data = gson.fromJson(reading,
					GsonData.class);
			try {

				SimpleTokenStream s = new SimpleTokenStream(data.body);

				while (s.hasNextToken()) {

					System.out.print(s.nextLineToken());

				}

			} catch (Exception e) {
				System.out.println("Error (printDocuments#1) ->  " + e);
				e.printStackTrace();
			}

		} catch (Exception e) {
			System.out.println("Error (printDocuments#2) - > " + e);
			e.printStackTrace();
		}
		System.out.println("\n\n");
	}


	
}
