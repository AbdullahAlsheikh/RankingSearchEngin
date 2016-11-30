package ranking;

import java.io.RandomAccessFile;

public interface RankedRetrival {

	public double calWq_t(double N, double df_t);
	
	public double calWd_t(int tft_d, RandomAccessFile wight,RandomAccessFile docLengthA ,int indexOfName, int totalNumberOfFiles,int docID);
	
	public double calWeigh(RandomAccessFile wight, int indexOfName, int totalNumberOfFiles, int docID);
	
}
