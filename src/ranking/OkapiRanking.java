package ranking;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public class OkapiRanking implements RankedRetrival {
	

	@Override
	public double calWq_t(double N, double df_t) {
		// TODO Auto-generated method stub
		double numerator = N - df_t + 0.5;
		double denominator = df_t + 0.5;
	    double result = Math.log(numerator / denominator);
	    System.out.println("Wq_t before: "+ result);
		return result > 0.1 ? result : 0.1;
	}

	@Override
	public double calWd_t(int tft_d, RandomAccessFile wight,RandomAccessFile docLengthA ,int indexOfName, int totalNumberOfFiles,int docID) {
	
		double nom = (2.2) * tft_d;
		double denom =  getK(wight,docLengthA ,docID) + tft_d ;
//		System.out.println("nom: " + nom + " denom: "+ denom);
		double result = (nom / denom);
//		result = result ;
//		System.out.println("Result: " + result);
		
		return result;
	}
	
	public double getK(RandomAccessFile wight,RandomAccessFile docLengthA ,int docID){
		//docLengthA
	 
		try{
			 byte[] buffer32 = new byte[32];
			docLengthA.seek(0);
			docLengthA.read(buffer32, 0, buffer32.length);
			Double averageToken = ByteBuffer.wrap(buffer32).getDouble();
//			System.out.println("Average: " + averageToken);

		
		
		//docLengthd
		byte[] buffer8 = new byte[8];
			wight.seek(docID * 24);
			wight.read(buffer8, 0, buffer8.length);
			int tokenCount = ByteBuffer.wrap(buffer8).getInt();
//			System.out.println("For document: " + docID +" TokenCount: " + tokenCount);
		
			
		double division = ((double) tokenCount / averageToken);
//		System.out.println("division result: " +  division);
		
		double result = (1.2) * (0.25 + (0.75 * division));
//		System.out.println("K: " +  result);
		return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		return 0.0;
		
	}

	@Override
	public double calWeigh(RandomAccessFile wight, int indexOfName, int totalNumberOfFiles, int docID) {
		return 1.0;
	}

	
	
	

}
