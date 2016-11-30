package ranking;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public class Wacky implements RankedRetrival {

	@Override
	public double calWq_t(double N, double df_t) {
		System.out.println("wacky");
		double numerator = N - df_t;
		double denominator = df_t;
		double result = Math.log(numerator / denominator);
		System.out.println("Wq_t before: "+ result);
		return result > 0 ? result : 0;
	}

	@Override
	public double calWd_t(int tft_d, RandomAccessFile wight,
			RandomAccessFile docLengthA, int indexOfName,
			int totalNumberOfFiles, int docID) {

		int positionOfWight = (indexOfName * 8) + (totalNumberOfFiles * 24);
		Double avergat = 0.0;
		try {
			byte[] buffer8 = new byte[8];
			wight.seek(positionOfWight);
			buffer8 = new byte[8];
			wight.read(buffer8, 0, buffer8.length);
			avergat = ByteBuffer.wrap(buffer8).getDouble();
//			System.out.println("ave(tft,d): " + avergat);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		double nom = 1 + Math.log(tft_d);
		double denom = 1 + Math.log(avergat);
	

		return (nom/denom);
	}

	@Override
	public double calWeigh(RandomAccessFile wight, int indexOfName,
			int totalNumberOfFiles, int docID) {
		int positionOfWight = (docID * 24) + 8;
		// docLengthd
		byte[] buffer8 = new byte[8];
		try {
			wight.seek(positionOfWight);
			wight.read(buffer8, 0, buffer8.length);
			int byteSize = ByteBuffer.wrap(buffer8).getInt();
//			System.out.println("For document: " + docID + " byteSize: "
//					+ byteSize);
			return Math.sqrt(byteSize);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

}
