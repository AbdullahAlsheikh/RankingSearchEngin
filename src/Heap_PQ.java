import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Heap_PQ {
	PriorityQueue<PositionPosting> pq;
	
	public Heap_PQ(int Default_Size) {
		pq = new PriorityQueue<PositionPosting>(Default_Size, new Comparator<PositionPosting>() {
			@Override
			public int compare(PositionPosting o1, PositionPosting o2) {
				// TODO Auto-generated method stub
				int retval = Double.compare(o1.getScore(), o2.getScore());
			     if(retval > 0) {

			    	 retval = -1;
			     
			     }
			     else if(retval < 0) {

			        retval = 1;
			     }
			     else {
			    	retval = 0;
			     }
			     
				return retval;
			}
		});
	}
	
	public int Size(){
		return pq.size();
	}

	public void insert(List<PositionPosting> x) {
		for (int i = 0; i < x.size(); i++) {
			pq.offer(x.get(i));
		}
	}

	public PositionPosting extractMax() {
		return pq.poll();
	}
	
	public void print() {
//		System.out.println(pq);
	}
}
