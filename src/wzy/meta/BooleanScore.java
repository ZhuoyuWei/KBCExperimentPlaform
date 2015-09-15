package wzy.meta;

import java.util.Comparator;

public class BooleanScore implements Comparator{

	public boolean flag=false;
	public double score;
	@Override
	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub
		
		BooleanScore b1=(BooleanScore)o1;
		BooleanScore b2=(BooleanScore)o2;
		
		if(Math.abs(b1.score-b2.score)<1e-10)
			return 0;
		else if(b1.score<b2.score)
			return -1;
		else
			return 1;
	}
	
}
