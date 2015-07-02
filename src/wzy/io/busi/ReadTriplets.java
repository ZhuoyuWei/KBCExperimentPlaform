package wzy.io.busi;

import wzy.io.FileTools;

public class ReadTriplets {

	/**
	 * Read triplets from a file.
	 * @param filename
	 * @param separator
	 * @return
	 */
	public static int[][] ReadTripletsFromFile(String filename,String separator)
	{
		return FileTools.ReadIntegralTriplets(filename, separator);
	}
	/**
	 * copy one data set from other data set in memory.
	 * @param triplets
	 * @return
	 */
	public static int[][] ReadTripletsFromTriplets(int[][] triplets)
	{
		int[][] res=new int[triplets.length][triplets[0].length];
		for(int i=0;i<triplets.length;i++)
		{
			for(int j=0;j<triplets[i].length;j++)
			{
				res[i][j]=triplets[i][j];
			}
		}
		return res;
	}
	
	public static int[] StaticTrainSet(int[][] train_triplets)
	{
		int maxEntityId=-1;
		int maxRelationId=-1;
		for(int i=0;i<train_triplets.length;i++)
		{
			if(train_triplets[i][0]>maxEntityId)
				maxEntityId=train_triplets[i][0];
			if(train_triplets[i][1]>maxRelationId)
				maxRelationId=train_triplets[i][1];
			if(train_triplets[i][2]>maxEntityId)
				maxEntityId=train_triplets[i][2];
		}
		int[] countpair=new int[2];
		countpair[0]=maxEntityId+1;
		countpair[1]=maxRelationId+1;
		return countpair;
	}
	
}
