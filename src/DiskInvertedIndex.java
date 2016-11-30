import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class DiskInvertedIndex {
	private RandomAccessFile mVocabList;
	private RandomAccessFile mPostings;
	private static RandomAccessFile mWights;
	private static RandomAccessFile mDoclengthA;
	private long[] mVocabTable;
	private static List<String> mFileNames;
	private static int wightIndex;

	public DiskInvertedIndex(String path) {
		try {

			mVocabList = new RandomAccessFile(new File(path, "vocab.bin"), "r");
			mPostings = new RandomAccessFile(new File(path, "postings.bin"),
					"r");

			mDoclengthA = new RandomAccessFile(
					new File(path, "docLengthA.bin"), "r");
			mWights = new RandomAccessFile(new File(path, "docWeights.bin"),
					"r");
			mVocabTable = readVocabTable(path);
			mFileNames = readFileNames(path);

		} catch (FileNotFoundException ex) {
			System.out.println(ex.toString());
		}
	}

	private static List<PositionPosting> readPostingsFromFileRanked(
			RandomAccessFile postings, RandomAccessFile wight,
			long postingsPosition, boolean isSingle, Ranking rank) {
		try {
			//
			double l_d = 0;
			//
			List<PositionPosting> DiskIndex = new ArrayList<PositionPosting>();
			// seek to the position in the file where the postings start.
			postings.seek(postingsPosition);

			// read the 4 bytes for the document frequency
			byte[] buffer4 = new byte[4];
			postings.read(buffer4, 0, buffer4.length);

			// use ByteBuffer to convert the 4 bytes into an int.
			int documentFrequency = ByteBuffer.wrap(buffer4).getInt();
			int N = mFileNames.size();
			// Calculating Calculate W-q,t
			double weight_qt = rank.getWq_t(N, documentFrequency);
			System.out.println("Wqt:  " + weight_qt);

			// initialize the array that will hold the postings.
			double[] docscore = new double[documentFrequency];

			int docID = 0;
			// ArrayList<Double> docweight = new ArrayList<Double>();

			for (int i = 0; i < documentFrequency; i++) {

				PositionPosting a = null;
				byte[] readBuffer = new byte[4];
				postings.read(readBuffer, 0, readBuffer.length);

				int gap = ByteBuffer.wrap(readBuffer).getInt();

				// Number Of Positions
				postings.read(readBuffer, 0, readBuffer.length);
				int termFeq = ByteBuffer.wrap(readBuffer).getInt();

				// Document ID
				docID += gap;
				// System.out.println("\n\nDoc:" + mFileNames.get(docID) +
				// " (DocID) " + docID
				// + "\nFrqDoc:" + termFeq
				// + "\nwith positions within Doc:");
				for (int j = 0; j < termFeq; j++) {
					// Positions
					postings.read(readBuffer, 0, readBuffer.length);
					int termPositionPerDoc = ByteBuffer.wrap(readBuffer)
							.getInt();
					// System.out.println(j +") " + termPositionPerDoc);

					if (a == null) {
						a = new PositionPosting(docID, termPositionPerDoc);
					} else {
						a.addPosting(termPositionPerDoc);
					}
				}

				// Increase Ad by wd,t × wq,t.
				// wightOfDoc(a, type, result)
				// int tft_d, RandomAccessFile wight, int docID

				double a_d = rank.getWd_t(a, wight, mDoclengthA, wightIndex,
						mFileNames.size(), docID) * weight_qt;
				l_d = rank.getL_d(wight, wightIndex, mFileNames.size(), docID);
				double score = a_d / l_d;
				// System.out.println("a_d: " + a_d);
				// System.out.println("L_d: "+ l_d);
				// System.out.println("Score: " + score);
				a.setScore(score);
				DiskIndex.add(a);

			}

			// System.out.println("\n-------\n");
			//
			if (!isSingle) {
				Heap_PQ i = new Heap_PQ(docscore.length);
				i.insert(DiskIndex);

				//
				List<PositionPosting> rankedResult = new ArrayList<PositionPosting>();

				// 10 retreated to get 10 retrived document
				for (int count = 9; count >= 0; count--) {
					if (i.Size() <= 0) {
						continue;
					}

					PositionPosting h = i.extractMax();
					rankedResult.add(h);
				}
				return rankedResult;
			}
			
			Collections.sort(DiskIndex, new Comparator<PositionPosting>() {
				@Override
				public int compare(PositionPosting o1, PositionPosting o2) {
					if (o1.getScore() > o2.getScore()) {
						return -1;
					} else if (o1.getScore() < o2.getScore()) {
						return 1;
					} else {
						return 0;
					}
				}
			});

			return DiskIndex;
		} catch (IOException ex) {
			System.out.println(ex.toString());
		}
		return null;
	}

	// public static double wightOfDoc(PositionPosting pp, int type, int docID)
	// throws IOException{
	//
	// byte[] buffer8 = null;
	// switch(type){
	//
	// case 1:
	// return Math.log(pp.getPositionSize()) + 1;
	// case 2:
	//
	// return pp.getPositionSize();
	// case 3:
	//
	// //docLengthA
	// byte[] buffer32 = new byte[32];
	// mDoclengthA.seek(0);
	// mDoclengthA.read(buffer32, 0, buffer32.length);
	// Double averageToken = ByteBuffer.wrap(buffer32).getDouble();
	// System.out.println("Average: " + averageToken);
	//
	// //docLengthd
	// buffer8 = new byte[8];
	// int position = docID * 16;
	// mWights.seek(position);
	// mWights.read(buffer8, 0, buffer8.length);
	// int tokenCount = ByteBuffer.wrap(buffer8).getInt();
	// System.out.println("TokenCount: " + tokenCount);
	//
	// break;
	//
	// case 4:
	// int positionOfWight = (wightIndex * 16) + (mFileNames.size() * 8) ;
	// mWights.seek(positionOfWight);
	// buffer8 = new byte[8];
	// mWights.read(buffer8, 0, buffer8.length);
	// Double orginalwight = ByteBuffer.wrap(buffer8).getDouble();
	// mWights.read(buffer8, 0, buffer8.length);
	// Double avergat = ByteBuffer.wrap(buffer8).getDouble();
	// System.out.println("ave(tft,d): " + orginalwight);
	// System.out.println("Wight: " + avergat);
	//
	//
	// }
	//
	//
	//
	// return 0.0;
	// }

	// public static double readWeight(RandomAccessFile wight, int type)
	// throws IOException {
	// switch(type){
	// case 1:
	// case 2:
	// int positionOfWight = (wightIndex * 16) + (mFileNames.size() * 8) + 8 ;
	// wight.seek(positionOfWight);
	// byte[] buffer8 = new byte[8];
	// wight.read(buffer8, 0, buffer8.length);
	// Double orginalwight = ByteBuffer.wrap(buffer8).getDouble();
	// // System.out.println("L_d: " + orginalwight);
	// return orginalwight;
	//
	// case 3:
	// return 1.0;
	// case 4:
	// return Math.sqrt(32);
	// }
	//
	// return 0.0;
	// }

	// public static double wightOfQuery(int type, int documentFrequency){
	//
	// double N = (double) mFileNames.size();
	// double df_t = documentFrequency;
	// double numerator;
	// double denominator;
	// double result;
	//
	// switch(type){
	// case 1:
	// return Math.log1p(N/df_t);
	//
	// case 2:
	// return Math.log(N/df_t);
	// case 3:
	// numerator = N - df_t + 0.5;
	// denominator = df_t + 0.5;
	// result = Math.log(numerator / denominator);
	// return result > 0.1 ? result : 0.1;
	//
	// case 4:
	// numerator = N - df_t;
	// denominator = df_t;
	// result = Math.log(numerator / denominator);
	// return result > 0 ? result : 0;
	// }
	//
	// return 0.0;
	// }

	/**
	 * 
	 * @param term
	 * @param isSingle
	 * @return
	 */
	public List<PositionPosting> GetPostings(String term, boolean isSingle,
			Ranking rank) {
		long postingsPosition = binarySearchVocabulary(term);
		// System.out.println("Processing: " + term);
		// System.out.println("postingsPostion: " + postingsPosition);
		// System.out.println("WightIndex  " + wightIndex );

		if (postingsPosition >= 0) {

			return readPostingsFromFileRanked(mPostings, mWights,
					postingsPosition, isSingle, rank);

		}
		return null;
	}

	/**
	 * 
	 * @param term
	 * @return
	 */
	public NaiveInvertedIndex getIndex(String term) {
		long postingsPosition = binarySearchVocabulary(term);
		System.out.println("Processing: " + term);
		if (postingsPosition >= 0) {
			return readPostingsFromFileBool(mPostings, postingsPosition, term);
		}
		return null;
	}

	private static NaiveInvertedIndex readPostingsFromFileBool(
			RandomAccessFile postings, long postingsPosition, String term) {
		try {
			//
			NaiveInvertedIndex invertedIndex = new NaiveInvertedIndex();

			//
			// List<PositionPosting> DiskIndex = new
			// ArrayList<PositionPosting>();
			// seek to the position in the file where the postings start.
			postings.seek(postingsPosition);

			// read the 4 bytes for the document frequency
			byte[] buffer4 = new byte[4];
			postings.read(buffer4, 0, buffer4.length);

			// use ByteBuffer to convert the 4 bytes into an int.
			int documentFrequency = ByteBuffer.wrap(buffer4).getInt();
			// System.out.println(documentFrequency);
			int result = 0;

			for (int i = 0; i < documentFrequency; i++) {

				// PositionPosting a = null;
				byte[] readBuffer = new byte[4];
				postings.read(readBuffer, 0, readBuffer.length);

				int gap = ByteBuffer.wrap(readBuffer).getInt();

				// Number Of Positions
				postings.read(readBuffer, 0, readBuffer.length);
				int termFeq = ByteBuffer.wrap(readBuffer).getInt();

				// Document ID
				result += gap;
				// System.out.println("\n\nDoc:" + mFileNames.get(result)
				// + "\nFrqDoc:" + termFeq
				// + "\nwith positions within Doc:");
				for (int j = 0; j < termFeq; j++) {
					// Positions
					postings.read(readBuffer, 0, readBuffer.length);
					int termPositionPerDoc = ByteBuffer.wrap(readBuffer)
							.getInt();
					// System.out.println(j +") " + termPositionPerDoc);
					invertedIndex.addTerm(term, result, termPositionPerDoc);
				}

			}

			return invertedIndex;
		} catch (IOException ex) {
			System.out.println(ex.toString());
		}
		return null;
	}

	private long binarySearchVocabulary(String term) {
		// do a binary search over the vocabulary, using the vocabTable and the
		// file vocabList.
		int i = 0, j = mVocabTable.length / 2 - 1;
		while (i <= j) {
			try {
				int m = (i + j) / 2;
				long vListPosition = mVocabTable[m * 2];
				int termLength;
				if (m == mVocabTable.length / 2 - 1) {
					termLength = (int) (mVocabList.length() - mVocabTable[m * 2]);
				} else {
					termLength = (int) (mVocabTable[(m + 1) * 2] - vListPosition);
				}

				mVocabList.seek(vListPosition);

				byte[] buffer = new byte[termLength];
				mVocabList.read(buffer, 0, termLength);
				String fileTerm = new String(buffer, "ASCII");

				int compareValue = term.compareTo(fileTerm);
				if (compareValue == 0) {
					// found it!
					wightIndex = m;
					return mVocabTable[m * 2 + 1];
				} else if (compareValue < 0) {
					j = m - 1;
				} else {
					i = m + 1;
				}
			} catch (IOException ex) {
				System.out.println(ex.toString());
			}
		}
		return -1;
	}

	private static List<String> readFileNames(String indexName) {
		try {
			final List<String> names = new ArrayList<String>();
			final Path currentWorkingPath = Paths.get(indexName)
					.toAbsolutePath();

			Files.walkFileTree(currentWorkingPath,
					new SimpleFileVisitor<Path>() {

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
								names.add(file.toFile().getName());
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
			return names;
		} catch (IOException ex) {
			System.out.println(ex.toString());
		}
		return null;
	}

	private static long[] readVocabTable(String indexName) {
		try {
			long[] vocabTable;

			RandomAccessFile tableFile = new RandomAccessFile(new File(
					indexName, "vocabTable.bin"), "r");

			byte[] byteBuffer = new byte[4];
			tableFile.read(byteBuffer, 0, byteBuffer.length);

			int tableIndex = 0;
			vocabTable = new long[ByteBuffer.wrap(byteBuffer).getInt() * 2];
			byteBuffer = new byte[8];

			while (tableFile.read(byteBuffer, 0, byteBuffer.length) > 0) { // while
																			// we
																			// keep
																			// reading
																			// 4
																			// bytes
				vocabTable[tableIndex] = ByteBuffer.wrap(byteBuffer).getLong();
				tableIndex++;

			}
			tableFile.close();
			return vocabTable;
		} catch (FileNotFoundException ex) {
			System.out.println(ex.toString());
		} catch (IOException ex) {
			System.out.println(ex.toString());
		}
		return null;
	}

	public List<String> getFileNames() {
		return mFileNames;
	}

	public int getTermCount() {
		return mVocabTable.length / 2;
	}
}
