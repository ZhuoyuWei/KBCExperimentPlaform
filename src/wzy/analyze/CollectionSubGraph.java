package wzy.analyze;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import wzy.model.TransE;
import wzy.thread.ConstructFormulas;
import wzy.thread.KBCProcess;

public class CollectionSubGraph {

	
	public int[][][] triplet_graph;
	public int entityNum;
	
	public int[][] NodeCenterSubGraph(int node,int depth,int maxsize)
	{
		List<int[]> tripletList=new ArrayList<int[]>();
		List<Integer> nodeList=new ArrayList<Integer>();
		nodeList.add(node);
		Set<Integer> nodeset=new HashSet<Integer>();
		nodeset.add(node);
		for(int i=0;i<depth;i++)
		{
			List<Integer> tmpList=new ArrayList<Integer>();
			for(int j=0;j<nodeList.size();j++)
			{
				int tmpnode=nodeList.get(j);
				
				for(int k=0;k<triplet_graph[tmpnode].length;k++)
				{
					int[] triplet=new int[3];
					triplet[0]=tmpnode;
					triplet[1]=triplet_graph[tmpnode][k][0];
					triplet[2]=triplet_graph[tmpnode][k][1];
					tripletList.add(triplet);
					if(nodeset.contains(triplet_graph[tmpnode][k][1]))
						continue;
					nodeset.add(triplet_graph[tmpnode][k][1]);
					tmpList.add(triplet_graph[tmpnode][k][1]);
				}
			}
			nodeList=tmpList;
			if(tripletList.size()>maxsize)
				break;
		}
		return tripletList.toArray(new int[0][]);
	}
	
	public void PrintSubGraph(int[][] subgraph,PrintStream ps)
	{
		for(int i=0;i<subgraph.length;i++)
		{
			for(int j=0;j<subgraph[i].length;j++)
			{
				ps.print(subgraph[i][j]+"\t");
			}
			ps.println();
		}
		ps.flush();
	}
	
	public static void main(String[] args) throws FileNotFoundException
	{
		//String dir=args[0];
		String dir="C:\\Users\\Administrator\\Documents\\data\\fb15k2\\";
		KBCProcess kbc=new KBCProcess();
		kbc.SetThreeTriplets(dir+"exp_train.txt"
				, dir+"exp_valid.txt"
				, dir+"exp_test.txt"
				, "\t");
		kbc.setEm(new TransE());
		kbc.StatisticTrainingSet();
		
		ConstructFormulas cc=new ConstructFormulas();
		cc.setEntityNum(kbc.getEm().getEntityNum());
		cc.setRelNum(kbc.getEm().getRelationNum());
		cc.setTrain_triplets(kbc.getTrain_triplets());
		cc.Init();
		cc.BuildGraph();
		
		
		CollectionSubGraph csg=new CollectionSubGraph();
		csg.triplet_graph=cc.getTriplet_graph();
		csg.entityNum=csg.triplet_graph.length;
		
		int node=14438;
		int[][] subgraph=csg.NodeCenterSubGraph(node, 4,1000);
		
		PrintStream ps=new PrintStream(dir+node+".subgraph");
		
		csg.PrintSubGraph(subgraph, ps);
		
		/*int[] nodecounts=new int[csg.entityNum];
		for(int i=0;i<csg.entityNum;i++)
		{
			int[][] tmp=csg.NodeCenterSubGraph(i, 4,500);
			nodecounts[i]=tmp.length;
			
		}
		
		PrintStream ps=new PrintStream("log");
		for(int i=0;i<nodecounts.length;i++)
		{
			if(nodecounts[i]<500)
				ps.println(i+"\t"+nodecounts[i]);
		}
		ps.close();*/
		
		
	}
	
	
	
}
