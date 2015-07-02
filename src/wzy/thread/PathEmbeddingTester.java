package wzy.thread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import wzy.meta.PathSupport;
import wzy.meta.RPath;
import wzy.model.EmbeddingModel;
import wzy.model.TransEAndPathModel;
import wzy.tool.MatrixTool;

public class PathEmbeddingTester {
	
	private EmbeddingModel em;
	private ConstructFormulas cf;
	
	
	private RPath[][] rpathLists;
	private double[][][] pathEmbedding;
	private int[][][] triplet_graph;
	private double[][] entityEmbedding;
	
	
	private String pathtester_log_file;
	
	public void Init()
	{
		TransEAndPathModel tem=(TransEAndPathModel)em;
		
		rpathLists=tem.getRpathLists();
		pathEmbedding=tem.getPathEmbedding();
		triplet_graph=cf.getTriplet_graph();
		entityEmbedding=tem.getEntityEmbedding();
		
		
		SortTripletGraph();
		
	}
	
	
	private int[][][] SplitRelationTrainingData(int[][] triplets)
	{
		int relNum=em.getRelationNum();
		int[][][] relationTriplets=new int[relNum][][];
		
		
		List<int[]>[] tempTriplets=new List[relNum];
		for(int i=0;i<relNum;i++)
		{
			tempTriplets[i]=new ArrayList<int[]>();
		}
		for(int i=0;i<triplets.length;i++)
		{
			tempTriplets[triplets[i][1]].add(triplets[i]);
		}
		for(int i=0;i<relNum;i++)
		{
			relationTriplets[i]=tempTriplets[i].toArray(new int[0][]);
		}
		return relationTriplets;
	}
	
	public void Testing(int[][] test_triplet)
	{
		int[][][] relationTriplets=SplitRelationTrainingData(test_triplet);
		
		for(int r=0;r<relationTriplets.length;r++)
		{
			for(int j=0;j<rpathLists[r].length;j++)
			{	
				double[] simi=new double[relationTriplets[r].length];
				boolean[] flags=new boolean[relationTriplets[r].length];
				
				for(int i=0;i<relationTriplets[r].length;i++)
				{
					List<Integer> stack=new ArrayList();
					stack.add(relationTriplets[r][i][0]);
					int count=DFScount(relationTriplets[r][i][2],stack,rpathLists[r][j]);
					flags[i]=count>0;
					simi[i]=CalculateSimilarity(entityEmbedding[relationTriplets[r][i][0]],
							pathEmbedding[r][j],entityEmbedding[relationTriplets[r][i][2]]);
				}
			}
		}
	}
	
	private double CalculateSimilarity(double[] hE,double[] pE,double[] tE)
	{
		double[] res=new double[hE.length];
		for(int i=0;i<hE.length;i++)
		{
			res[i]=hE[i]+pE[i]-tE[i];
		}
		return MatrixTool.VectorNorm1(res);
	}
	
	private boolean CheckVisited(int entity,List<Integer> stack)
	{
		for(int i=0;i<stack.size();i++)
		{
			if(stack.get(i).equals(entity))
				return true;
		}
		return false;
	}
	
	private int DFScount(int t,List<Integer> stack,RPath rpath)
	{
		int count=0;
		
		int l=stack.size()-1;
		int s=stack.get(l);
		
		int[][] edges=triplet_graph[s];
		int findr=0;
		for(;findr<edges.length;findr++)
		{
			if(edges[findr][0]==rpath.GetElement(l))
			{
				for(int i=findr;i<edges.length;i++)
				{
					if(edges[findr][0]>rpath.GetElement(l))
						break;
					stack.add(edges[findr][1]);
					if(stack.size()>rpath.length())
					{
						if(t==edges[findr][1])
						{
							count++;
						}
					}
					else
					{
						count+=DFScount(t,stack,rpath);
					}
					stack.remove(stack.size()-1);
				}
			}
			else if(edges[findr][0]>rpath.GetElement(l))
				break;
		}
		
		return count;
	}
	

	
	public void SortTripletGraph()
	{
		for(int i=0;i<triplet_graph.length;i++)
		{
			List<int[]> tupleList=Arrays.asList(triplet_graph[i]);
			Collections.sort(tupleList,new IntListSorter());
			triplet_graph[i]=tupleList.toArray(new int[0][]);
		}
	}

	
}

class IntListSorter implements Comparator
{

	@Override
	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub
		
		int[] t1=(int[])o1;
		int[] t2=(int[])o2;
		
		if(t1[0]<t2[0])
			return -1;
		else if(t1[0]>t2[0])
			return 1;
		else
		{
			if(t1[1]<t2[1])
				return -1;
			else if(t1[1]>t2[1])
				return 1;
			else
				return 0;
		}

	}
	
}