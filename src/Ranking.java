import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import ranking.RankedRetrival;

public class Ranking {
	RankedRetrival ranked;
	
	public Ranking(RankedRetrival r){
		this.ranked = r;
	}

	public double getL_d(RandomAccessFile wight, int indexOfName, int totalNumberOfFiles, int docID){
		return ranked.calWeigh(wight, indexOfName, totalNumberOfFiles, docID);
	}

	public double getWq_t(double N, int df_t){
		return ranked.calWq_t(N, df_t);
	}

	
	public double getWd_t(PositionPosting pp, RandomAccessFile wight, RandomAccessFile docLengthA, int indexOfName, int totalNumberOfFiles,int docID){
		return ranked.calWd_t(pp.getPositionSize(), wight, docLengthA,indexOfName,  totalNumberOfFiles , docID);
	}

}
