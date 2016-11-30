package ranking;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public class DefaultRanking implements RankedRetrival {

	@Override
	public double calWq_t(double N, double df_t) {
		
		return (double) Math.log1p(N / df_t);
	}

	@Override
	public double calWd_t(int tft_d, RandomAccessFile wight,RandomAccessFile docLengthA ,int indexOfName, int totalNumberOfFiles,int docID){
		return Math.log(tft_d) + 1;
	}

	@Override
	public double calWeigh(RandomAccessFile wight, int indexOfName, int totalNumberOfFiles, int docID) {
		int positionOfWight = (docID * 24) + 16;
		try {
			wight.seek(positionOfWight);
			byte[] buffer8 = new byte[8];
			wight.read(buffer8, 0, buffer8.length);
			Double orginalwight = ByteBuffer.wrap(buffer8).getDouble();
//			System.out.println("L_d: " + orginalwight);
			return orginalwight;
		
		} catch (IOException e) {
		
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0.0;
		
		}
	}

}
