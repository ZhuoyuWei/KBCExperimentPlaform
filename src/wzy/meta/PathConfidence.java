package wzy.meta;

import java.util.Comparator;

public class PathConfidence implements Comparator{

	public RPath getPath() {
		return path;
	}
	public void setPath(RPath path) {
		this.path = path;
	}

	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}



	private RPath path;
	private double score;
	@Override
	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub
		PathSupport pc1=(PathSupport)o1;
		PathSupport pc2=(PathSupport)o2;		
		if(Math.abs(pc1.getScore()-pc2.getScore())<1e-6)
			return 0;
		else if(pc1.getScore()>pc2.getScore())
			return -1;
		else
			return 1;
	}
}
