package wzy.meta;

public class TripletHash {

	
	private int[] triplet;
	
	public boolean equals(Object o)
	{
		TripletHash t=(TripletHash)o;
		for(int i=0;i<3;i++)
		{
			if(triplet[i]!=t.triplet[i])
				return false;
		}
		return true;
	}
	public int hashCode()
	{
		int sum=0;
		for(int i=0;i<3;i++)
		{
			sum*=32767;
			sum+=triplet[i];
		}
		return sum;
	}
	public void setTriplet(int[] triplet) {
		this.triplet = triplet;
	}
	
}
