package wzy.main;

import java.util.HashMap;
import java.util.Map;

import wzy.io.FileTools;

public class IndexEntityAndRelation {

	
	public static int[][] IndexEntityAndRelation(String[][] strs)
	{
		Map<String,Integer> entity2id=new HashMap<String,Integer>();
		Map<String,Integer> relation2id=new HashMap<String,Integer>();
		int[][] ints=new int[strs.length][strs[0].length];
		for(int i=0;i<strs.length;i++)
		{
			for(int j=0;j<3;j+=2)
			{
				Integer index=entity2id.get(strs[i][j]);
				if(index==null)
				{
					index=entity2id.size();
					entity2id.put(strs[i][j], index);
				}
				ints[i][j]=index;
			}
			Integer index=relation2id.get(strs[i][1]);
			if(index==null)
			{
				index=relation2id.size();
				relation2id.put(strs[i][1], index);
			}
			ints[i][1]=index;
		}
		
		return ints;
	}
	
	
	public static void main(String[] args)
	{

		FileTools.PrintIntegralTriplets(IndexEntityAndRelation(FileTools.ReadStringTriplets(args[0], "\t")), args[1], "\t");
		FileTools.PrintIntegralTriplets(IndexEntityAndRelation(FileTools.ReadStringTriplets(args[2], "\t")), args[3], "\t");		
		FileTools.PrintIntegralTriplets(IndexEntityAndRelation(FileTools.ReadStringTriplets(args[4], "\t")), args[5], "\t");		
		
		
	}
	
	
}
