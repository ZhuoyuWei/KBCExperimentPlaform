package wzy.meta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RPath{

	private List<Integer> relationList=new ArrayList<Integer>();
	private double weight=0;
	
	public int Add(Integer relation)
	{
		relationList.add(relation);
		return relationList.size();
	}
	public int length()
	{
		return relationList.size();
	}
	
	public int hashCode()
	{
		int sum=0;
		for(int i=0;i<relationList.size();i++)
		{
			sum*=65535;
			sum+=relationList.get(i);
		}
		return sum;
	}
	
	public boolean equals(Object o)
	{
		RPath r=(RPath)o;
		if(relationList.size()!=r.relationList.size())
			return false;
		for(int i=0;i<relationList.size();i++)
		{
			if(!relationList.get(i).equals(r.relationList.get(i)))
				return false;
		}
		return true;
	}

	public RPath CopySelf()
	{
		RPath rpath=new RPath();
		rpath.relationList.addAll(relationList);
		return rpath;
	}
	
	public void CopyFromStack(int[] stack,int stacksize)
	{
		relationList=new ArrayList<Integer>(stacksize);
		for(int i=0;i<stacksize;i++)
		{
			relationList.add(stack[i]);
		}
	}

	
	public List<Integer> getRelationList() {
		return relationList;
	}
	
	public Integer GetElement(int index)
	{
		return relationList.get(index);
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	
	
}
