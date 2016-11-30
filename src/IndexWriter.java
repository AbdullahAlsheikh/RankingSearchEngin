import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Writes an inverted indexing of a directory to disk.
 */
public class IndexWriter {

	private String mFolderPath;
	private static int totalTermCountForAllDoc = 0;
	private static List<Integer> tokenDoc = new ArrayList<Integer>();
	private static List<String> fileNames = new ArrayList<String>();
	private static List<Double> documentL_d = new ArrayList<Double>();


	/**
	 * Constructs an IndexWriter object which is prepared to index the given
	 * folder.
	 */
	public IndexWriter(String folderPath) {
		mFolderPath = folderPath;
	}

	/**
	 * Builds and writes an inverted index to disk. Creates three files:
	 * vocab.bin, containing the vocabulary of the corpus; postings.bin,
	 * containing the postings list of document IDs; vocabTable.bin, containing
	 * a table that maps vocab terms to postings locations
	 */
	public void buildIndex() {
		buildIndexForDirectory(mFolderPath);
	}

	/**
	 * Builds the normal NaiveInvertedIndex for the folder.
	 */
	private static void buildIndexForDirectory(String folder) {
		NaiveInvertedIndex index = new NaiveInvertedIndex();

		// Index the directory using a naive index
		System.out.println(":Loading");
		indexFiles(folder, index);
		// the array of terms
		String[] dictionary = index.getDictionary();
		// an array of positions in the vocabulary file
		long[] vocabPositions = new long[dictionary.length];
		buildVocabFile(folder, dictionary, vocabPositions);
		buildPostingsFile(folder, index, dictionary, vocabPositions);
		System.out.println(":Done");
		
	}

	/**
	 * Builds the postings.bin file for the indexed directory, using the given
	 * NaiveInvertedIndex of that directory.
	 */
	private static void buildPostingsFile(String folder,
			NaiveInvertedIndex index, String[] dictionary, long[] vocabPositions) {
		FileOutputStream postingsFile = null;
		FileOutputStream docWeights = null;
		FileOutputStream docLengthA = null;
		int totalTokencount = 0;
		try {
			//
			//
			postingsFile = new FileOutputStream(
					new File(folder, "postings.bin"));
			
			//
			//
			docLengthA = new FileOutputStream(
					new File(folder, "docLengthA.bin"));

			//
			//
			docWeights = new FileOutputStream(
					new File(folder, "docWeights.bin"));

			// simultaneously build the vocabulary table on disk, mapping a term
			// index to a
			// file location in the postings file.
			FileOutputStream vocabTable = new FileOutputStream(new File(folder,
					"vocabTable.bin"));

			// the first thing we must write to the vocabTable file is the
			// number of vocab terms.
			byte[] tSize = ByteBuffer.allocate(4).putInt(dictionary.length)
					.array();
			vocabTable.write(tSize, 0, tSize.length);
			
			
			

			int vocabI = 0;
			
			//Writing Document docLength_d
			for(int docIDs = 0; docIDs < tokenDoc.size() ; docIDs++){
//				System.out.println("Document ID: "+ docIDs + " num of token" + tokenDoc.get(docIDs));
				totalTokencount +=  tokenDoc.get(docIDs);
//				System.out.println("Doc total tokens " + totalTokencount);
				
				//docLength_d
				byte[] docLengthd = ByteBuffer.allocate(8)
						.putInt(tokenDoc.get(docIDs)).array();
				docWeights.write(docLengthd, 0, docLengthd.length);
				
				//byteSize_d
				byte[] bytesize = ByteBuffer.allocate(8)
						.putInt(tokenDoc.get(docIDs) * 2 ).array();
//				System.out.println("Document ID: "+ docIDs + " has a bytesize of " + (tokenDoc.get(docIDs) * 2) + " bytes");
				docWeights.write(bytesize, 0, bytesize.length);
				
				
				//Writing L_d
				byte[] l_d = ByteBuffer.allocate(8)
						.putDouble(documentL_d.get(docIDs)).array();
//				System.out.println("Document ID: "+ docIDs + " has a bytesize of " + (tokenDoc.get(docIDs) * 2) + " bytes");
				docWeights.write(l_d, 0, l_d.length);
				
//				System.out.println("Document of index " + docIDs 
//						+ ":\ndocLengthd : " + tokenDoc.get(docIDs)
//						+ ":\nbyteSize_d : " + (tokenDoc.get(docIDs) * 2)
//						+ ":\nL_d : " + documentL_d.get(docIDs));
			}
			
			
			for (String s : dictionary) {

				// for each String in dictionary, retrieve its postings.
				List<Integer> postings = index.getDocuments(s);
				List<PositionPosting> positions = index.getPositionPostings(s);

				// write the vocab table entry for this term: the byte
				// location of the term in the vocab list file,
				// and the byte location of the postings for the term in the
				// postings file.
				byte[] vPositionBytes = ByteBuffer.allocate(8)
						.putLong(vocabPositions[vocabI]).array();
				vocabTable.write(vPositionBytes, 0, vPositionBytes.length);

				byte[] pPositionBytes = ByteBuffer.allocate(8)
						.putLong(postingsFile.getChannel().position()).array();
				vocabTable.write(pPositionBytes, 0, pPositionBytes.length);

				// write the postings file for this term. first, the
				// document frequency for the term, then
				// the document IDs, encoded as gaps.
				byte[] docFreqBytes = ByteBuffer.allocate(4)
						.putInt(postings.size()).array();
				postingsFile.write(docFreqBytes, 0, docFreqBytes.length);
				//
				//
				
				
				
				int i = 0;
				int countPositions = 0;
				int lastDocId = 0;
//				int count = vocabI * 8;
//				System.out.print(count + ") ");
//				System.out.println("term: " + s );
				for (int docId : postings) {
					// DocID
					byte[] docIdBytes = ByteBuffer.allocate(4)
							.putInt(docId - lastDocId).array(); 
					// encode a gap, not  a doc ID
					postingsFile.write(docIdBytes, 0, docIdBytes.length);

					// Write tft,d: the number of times term occurs in doc
					byte[] termFrqPerID = ByteBuffer.allocate(4)
							.putInt(positions.get(i).getPositions().size())
							.array();
					postingsFile.write(termFrqPerID, 0, termFrqPerID.length);

					// then a forloop with ith position of t in d
					List<Integer> termDocPositions = positions.get(i)
							.getPositions();
					for (int j = 0; j < termDocPositions.size(); j++) {
						// System.out.print(termDocPositions.get(j) +
						// " -- ");
						byte[] termPositionPerDoc = ByteBuffer.allocate(4)
								.putInt(termDocPositions.get(j)).array();
						postingsFile.write(termPositionPerDoc, 0,
								termPositionPerDoc.length);
						countPositions += termDocPositions.get(j);
					}
					// 
					lastDocId = docId;
					i++;
				}
				
				//ave(tft,d)
//				System.out.println("Average Tftd : " + getAverageTftd(i));
				byte[] averageTftd = ByteBuffer.allocate(8)
						.putDouble(getAverageTftd(i)).array();
				docWeights.write(averageTftd, 0, averageTftd.length);
				
//				//docWeights_d (Old Way)
//				byte[] docwight = ByteBuffer.allocate(8)
//						.putDouble(index.getL_d(s)).array();
//				System.out.println("L_d for term: " + s + " is -->"+ index.getL_d(s));
//				docWeights.write(docwight, 0, docwight.length);
//				System.out.println();
				
				
				vocabI++;
			}
			
			//docLengthA
			byte[] doclengthA  = ByteBuffer.allocate(32)
					.putDouble(getAverageToken(totalTokencount)).array();
//			System.out.println("Writting average --> "+ getAverageToken(totalTokencount));
			docLengthA.write(doclengthA, 0, doclengthA.length);
			
			docLengthA.close();
			vocabTable.close();
			postingsFile.close();
			docWeights.close();

		} catch (FileNotFoundException ex) {
		} catch (IOException ex) {
		} finally {
			try {
				postingsFile.close();
			} catch (IOException ex) {
			}
		}
	}
	
	//average tft,d
	private static double getAverageTftd(int totalTokensTerm){
//		System.out.println("Total Number of terms " + totalTokenCountForAllDoc);
//		System.out.println("sum of tf values for term " + totalTokensTerm);
		return (double) totalTokensTerm / totalTermCountForAllDoc;
	}
	
	
	//docLengthA
	private static double getAverageToken(int totalTokencount){
		//Number of characters of the entire corpus divided with the number of files.
		return (double) totalTokencount / fileNames.size() ;
	}

	private static void buildVocabFile(String folder, String[] dictionary,
			long[] vocabPositions) {
		OutputStreamWriter vocabList = null;
		try {
			// first build the vocabulary list: a file of each vocab word
			// concatenated together.
			// also build an array associating each term with its byte location
			// in this file.
			int vocabI = 0;
			vocabList = new OutputStreamWriter(new FileOutputStream(new File(
					folder, "vocab.bin")), "ASCII");

			int vocabPos = 0;
			for (String vocabWord : dictionary) {
				// for each String in dictionary, save the byte position where
				// that term will start in the vocab file.
				vocabPositions[vocabI] = vocabPos;
				vocabList.write(vocabWord); // then write the String
				vocabI++;
				vocabPos += vocabWord.length();
			}
		} catch (FileNotFoundException ex) {
			System.out.println(ex.toString());
		} catch (UnsupportedEncodingException ex) {
			System.out.println(ex.toString());
		} catch (IOException ex) {
			System.out.println(ex.toString());
		} finally {
			try {
				vocabList.close();
			} catch (IOException ex) {
				System.out.println(ex.toString());
			}
		}
	}

	private static void indexFiles(String folder, final NaiveInvertedIndex index) {
		final Path currentWorkingPath = Paths.get(folder).toAbsolutePath();

		try {
			Files.walkFileTree(currentWorkingPath,
					new SimpleFileVisitor<Path>() {
						int mDocumentID = 0;

						public FileVisitResult preVisitDirectory(Path dir,
								BasicFileAttributes attrs) {
							// make sure we only process the current working
							// directory
							if (currentWorkingPath.equals(dir)) {
								return FileVisitResult.CONTINUE;
							}
							return FileVisitResult.SKIP_SUBTREE;
						}

						public FileVisitResult visitFile(Path file,
								BasicFileAttributes attrs) {
							// only process .txt files
							if (file.toString().endsWith(".json")) {
								// we have found a .txt file;
								// add its name to the fileName list,
								// then index the file and increase the document
								// ID counter.
//								System.out.println("Indexing file "
//										+ file.getFileName());
								fileNames.add(file.getFileName() + "");

								indexFile(file.toFile(), index, mDocumentID);
								mDocumentID++;
							}
							return FileVisitResult.CONTINUE;
						}

						// don't throw exceptions if files are locked/other
						// errors occur
						public FileVisitResult visitFileFailed(Path file,
								IOException e) {

							return FileVisitResult.CONTINUE;
						}

					});
		} catch (IOException ex) {
			Logger.getLogger(IndexWriter.class.getName()).log(Level.SEVERE,
					null, ex);
		}
	}

	private static void indexFile(File fileName, NaiveInvertedIndex index,
			int documentID) {
		String stemmed = "";
		Map<String, Integer> termCount = new HashMap<String, Integer>();

		try (InputStream inputStream = new FileInputStream(fileName)) {
			System.out.println(":::File Name: "   + fileName.toString());
			try{
			Gson gson = new GsonBuilder().create();
//			System.out.println(":::File Name" + fileName.getName() +  " passed Gson");
			Reader reading = new InputStreamReader(inputStream);
//			System.out.println(":::File Name" + fileName.getName() +  " passed Reader");
			// Getting the parsed Data
//			System.out.println(":::File Name" + fileName.getName());
			GsonData data = gson.fromJson(reading, GsonData.class);
			// try {
			SimpleTokenStream stream = new SimpleTokenStream(data.body);
			int i = 0;
			int characterCount = 0;
			String term = "";
			while (stream.hasNextToken()) {
				try{
					term = stream.nextToken().trim();
					System.out.println("   " + term);
				
				}catch(Exception e){
					System.out.println("stemmer exception");
				}
				
					if(!(term == null) && term.trim().length() > 0 && term.matches(".*\\w.1*")){
						try{
						stemmed = PorterStemmer.processToken(term);
						}catch(NullPointerException np){
							System.out.print("PorterStemmer ");
							System.out.println(fileName.getName() +" "+ np.toString() + " "+stemmed);

						}
//						System.out.println(stemmed);
						if(!termCount.containsKey(stemmed)){
							termCount.put(stemmed, 1);
						
						}else {
							int countTerm = (int)termCount.get(stemmed);
							countTerm ++;
							termCount.put(stemmed, countTerm);
						}
						characterCount += term.length();
						if (stemmed != null && stemmed.trim().length() > 0) {
							if (!(stemmed.contains("-") || stemmed.contains("—") ||stemmed.contains("-"))) {
								// does not contain hyphen
								index.addTerm(stemmed, documentID, i);
							} else {
								// Contains a hyphen
								//TODO: Make sure the Hyohen Case works with the TermCount
								hyphenCase(index, stemmed, documentID, i);
							}

						}
						i++;
					}
				
				
			}
			
		
			totalTermCountForAllDoc += i; 
			tokenDoc.add(characterCount);
			
			double A_d = 0.0;
			for (String key : termCount.keySet()) {
			
			    int count = (Integer) termCount.get(key);
			    double cal = Math.pow((1 + Math.log(count)), 2);
			    A_d += cal;
			}
			
			A_d = Math.sqrt(A_d);
			documentL_d.add(A_d);
			}catch(NullPointerException np){
				System.out.print("Gson ");
				System.out.println(fileName.getName() +" "+ np.toString());
				np.printStackTrace();

			}
		} catch (Exception ex) {
			System.out.print("error ");
			System.out.println(fileName.getName() +" "+ ex.toString() + " "+stemmed);
		}
	}

	/**
	 * Will work with hyphenated words
	 * 
	 * @param term
	 *            the term with the hyphen (hyphen must still be in the term!)
	 */
	public static void hyphenCase(NaiveInvertedIndex index, String term,
			int doc, int position) {
		// System.out.println("Has hyphen " + term);
		String newTerm = "";
		if (term.contains("-")) {
			newTerm = term.replace("-", "");
		}

		if (term.contains("—")) {
			newTerm = term.replace("—", "");
		}
		
		if (term.contains("―")) {
			newTerm = term.replace("―", "");
		}

		if (newTerm.trim().length() > 0) {

			// System.out.println("after w/out hyphen " + newTerm);
			// Without the hyphen
			index.addTerm(PorterStemmer.processToken(newTerm), doc, position);

			// First term from the hyphen
			if (term.contains("-")) {
				index.addTerm(
						PorterStemmer.processToken(term.substring(0,
								term.indexOf("-"))), doc, position);
				// Second term from the hyphen
				index.addTerm(
						PorterStemmer.processToken(term.substring(
								term.indexOf("-") + 1, term.length())), doc,
						position);
			}

			
			
			if (term.contains("—")) {
				index.addTerm(
						PorterStemmer.processToken(term.substring(0,
								term.indexOf("—"))), doc, position);
				// Second term from the hyphen
				index.addTerm(
						PorterStemmer.processToken(term.substring(
								term.indexOf("—") + 1, term.length())), doc,
						position);
			}
			
			
			
			if (term.contains("―")) {
				index.addTerm(
						PorterStemmer.processToken(term.substring(0,
								term.indexOf("―"))), doc, position);
				// Second term from the hyphen
				index.addTerm(
						PorterStemmer.processToken(term.substring(
								term.indexOf("―") + 1, term.length())), doc,
						position);
			}

		}
	}
}

class GsonData {
	// Gson required variables
	public String body;

}
