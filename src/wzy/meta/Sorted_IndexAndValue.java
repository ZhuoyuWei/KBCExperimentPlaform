package wzy.meta;

import java.util.Comparator;

public class Sorted_IndexAndValue implements Comparator{

	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	
	private int index;
	private double value;
	
	@Override
	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub
		
		Sorted_IndexAndValue s1=(Sorted_IndexAndValue)o1;
		Sorted_IndexAndValue s2=(Sorted_IndexAndValue)o2;
		
		if(s1.value==s2.value||Math.abs(s1.value-s2.value)<1e-10)
			return 0;
		else if(s1.value==Double.NaN)
			return 1;
		else if(s2.value==Double.NaN)
			return -1;
		else if(s1.value<s2.value)
			return -1;
		else
			return 1;
	}

}
