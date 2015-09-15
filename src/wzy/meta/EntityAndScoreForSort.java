package wzy.meta;

import java.util.Comparator;

public class EntityAndScoreForSort implements Comparator{

	public int entity;
	public double score;
	@Override
	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub
		
		EntityAndScoreForSort e1=(EntityAndScoreForSort)o1;
		EntityAndScoreForSort e2=(EntityAndScoreForSort)o2;
		
		if(Math.abs(e1.score-e2.score)<1e-10)
			return 0;
		else if(e1.score<e2.score)
			return -1;
		else
			return 1;
	}
	
}
