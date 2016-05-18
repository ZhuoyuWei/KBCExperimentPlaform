package wzy.thread;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import wzy.io.FileTools;
import wzy.meta.GroundPath;
import wzy.meta.PathSupport;
import wzy.thread.cons4rel.BFS2Direct;
import wzy.thread.cons4rel.BFSearch;
import wzy.thread.cons4rel.ConstrForRel;
import wzy.thread.cons4rel.DFSNoFalseSearch;
import wzy.thread.cons4rel.DFSearch;

public class ConstructFormulas implements Callable{

	private int search_method=0;
	private int minLength=1;
	private int maxLength=2;
	
	private int threNum=1;
	private int relNum;
	private int entityNum;
	private List<PathSupport>[] rpathLists;
	private int[][][] relationTriplets;
	
	private int[][] train_triplets;
	private boolean reverseEdgeFlag=false;
	private int[][][] triplet_graph;
	private boolean queit=false;
	
	private String formulaPrintFile=null;
	private boolean printsupport=false;

	
	public void Init()
	{
		rpathLists=new List[relNum];
		/*for(int i=0;i<relNum;i++)
		{
			rpathLists[i]=new ArrayList<PathSupport>();
		}*/
		relationTriplets=new int[relNum][][];
		triplet_graph=new int[entityNum][][];
	}
	
	/**
	 * Build entity to entity graph, and with the edge type (relation)
	 * reverseEdgeFlag can control whether include reversed edges.
	 * h--->r,t
	 */
	public void BuildGraph()
	{
		List<int[]>[] graph=new List[entityNum];
		for(int i=0;i<entityNum;i++)
		{
			graph[i]=new ArrayList<int[]>();
		}
		for(int i=0;i<train_triplets.length;i++)
		{
			int[] tuple=new int[2];
			tuple[0]=train_triplets[i][1];
			tuple[1]=train_triplets[i][2];			
			graph[train_triplets[i][0]].add(tuple);
			if(reverseEdgeFlag)
			{
				int[] restuple=new int[2];
				restuple[0]=train_triplets[i][1];
				restuple[1]=train_triplets[i][0];
				graph[train_triplets[i][2]].add(tuple);
			}
		}
		for(int i=0;i<entityNum;i++)
		{
			triplet_graph[i]=graph[i].toArray(new int[0][]);
		}
	}
	public void SplitRelationTrainingData()
	{
		List<int[]>[] tempTriplets=new List[relNum];
		for(int i=0;i<relNum;i++)
		{
			tempTriplets[i]=new ArrayList<int[]>();
		}
		for(int i=0;i<train_triplets.length;i++)
		{
			tempTriplets[train_triplets[i][1]].add(train_triplets[i]);
		}
		for(int i=0;i<relNum;i++)
		{
			relationTriplets[i]=tempTriplets[i].toArray(new int[0][]);
		}
	}
	
	public void StatisticMiningResult()
	{
		int emptyrelcount=0;
		int pathsum=0;
		for(int i=0;i<relNum;i++)
		{
			if(rpathLists[i].size()==0)
				emptyrelcount++;
			pathsum+=rpathLists[i].size();
		}
		if(!queit)
		{
			System.out.println("Empty relation: "+emptyrelcount+"/"+relNum);
			System.out.println("Path rate: "+(double)pathsum/relNum);
		}
	}
	
	public void Processing()
	{
		Init();
		SplitRelationTrainingData();
		BuildGraph();
		ExecutorService exec = Executors.newFixedThreadPool(threNum); 
		List<Callable<Integer>> alThreads=new ArrayList<Callable<Integer>>();
		
		ConstrForRel.setMaxLength(maxLength);
		ConstrForRel.setMinLength(minLength);
		ConstrForRel.setTriplet_graph(triplet_graph);
		ConstrForRel.setQueit(queit);
		
		for(int i=0;i<relNum;i++)
		{
			ConstrForRel cfr=null;
			switch(search_method)
			{
			case 0:
			{
				cfr=new BFSearch();
				break;
			}
			case 1:
			{
				cfr=new DFSearch();
				break;
			}	
			case 2:
			{
				cfr=new DFSNoFalseSearch();
				ConstrForRel.BuildTripletHashSet(train_triplets);
				break;
			}
			}
			cfr.setRelation(i);	
			cfr.setTrain_triplets(relationTriplets[i]);
			cfr.setRpathLists(rpathLists);
			
			alThreads.add(cfr);
		}
		System.err.println("All theads are ready.");
		
		try {
			//Thread.sleep(10000);
			exec.invokeAll(alThreads);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		exec.shutdown();
		StatisticMiningResult();
		if(formulaPrintFile!=null)
		{
			FileTools.PrintAllFormula(rpathLists, formulaPrintFile,printsupport);
		}
		
	}
	
	
	
	
	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub
		try{
		Processing();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public int getThreNum() {
		return threNum;
	}

	public void setThreNum(int threNum) {
		this.threNum = threNum;
	}

	public int getRelNum() {
		return relNum;
	}

	public void setRelNum(int relNum) {
		this.relNum = relNum;
	}

	public List<PathSupport>[] getRpathLists() {
		return rpathLists;
	}

	public void setRpathLists(List<PathSupport>[] rpathLists) {
		this.rpathLists = rpathLists;
	}

	public int[][] getTrain_triplets() {
		return train_triplets;
	}

	public void setTrain_triplets(int[][] train_triplets) {
		this.train_triplets = train_triplets;
	}

	public int getSearch_method() {
		return search_method;
	}

	public void setSearch_method(int search_method) {
		this.search_method = search_method;
	}

	public int getEntityNum() {
		return entityNum;
	}

	public void setEntityNum(int entityNum) {
		this.entityNum = entityNum;
	}
	public int[][][] getTriplet_graph() {
		return triplet_graph;
	}
	public String getFormulaPrintFile() {
		return formulaPrintFile;
	}
	public void setFormulaPrintFile(String formulaPrintFile) {
		this.formulaPrintFile = formulaPrintFile;
	}
	public int getMinLength() {
		return minLength;
	}
	public void setMinLength(int minLength) {
		this.minLength = minLength;
	}
	public int getMaxLength() {
		return maxLength;
	}
	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}
	public boolean isQueit() {
		return queit;
	}
	public void setQueit(boolean queit) {
		this.queit = queit;
	}
	public boolean isPrintsupport() {
		return printsupport;
	}
	public void setPrintsupport(boolean printsupport) {
		this.printsupport = printsupport;
	}
	public int[][][] getRelationTriplets() {
		return relationTriplets;
	}


	////////////////////Check Path Existence Experiments
	public Random rand=new Random();
	public List<GroundPath>[] DFSMiningPaths()
	{
		List<GroundPath>[] pathList=new List[11];
		for(int i=1;i<=10;i++)
		{
			pathList[i]=new ArrayList<GroundPath>();
		}
		for(int i=0;i<2000;i++)
		{
			int snode=Math.abs(rand.nextInt())%entityNum;
			int rsnode=snode;
			List<Integer> relList=new ArrayList<Integer>();
			Set<Integer> entitySet=new HashSet<Integer>();
			entitySet.add(snode);
			for(int j=0;j<10;j++)
			{
				int count=0;
				while(count++<10)
				{
					if(triplet_graph[snode].length==0)
						break;
					int index=Math.abs(rand.nextInt())%triplet_graph[snode].length;
					int nextnode=triplet_graph[snode][index][1];
					if(!entitySet.contains(nextnode))
					{
						relList.add(triplet_graph[snode][index][0]);
						GroundPath gp=new GroundPath();
						gp.entity[0]=rsnode;
						gp.entity[1]=nextnode;
						gp.path.getRelationList().addAll(relList);
						snode=nextnode;
						entitySet.add(nextnode);
						pathList[relList.size()].add(gp);
						break;
					}
				}
			}
		}
		return pathList;
	}
	
	
	public void DFSforCPE(List<GroundPath>[] pathList)
	{
		for(int i=1;i<=10;i++)
		{
			long start=System.currentTimeMillis();
			for(int j=0;j<pathList[i].size();j++)
			{
				//count=0;
				boolean flag=DFSOnePath(pathList[i].get(j),pathList[i].get(j).entity[0],0);
				if(!flag)
				{
					System.out.println("err");
				}
				//System.out.println(count);
				
				//count=0;
				pathList[i].get(j).entity[1]=Math.abs(rand.nextInt())%entityNum;
				DFSOnePath(pathList[i].get(j),pathList[i].get(j).entity[0],0);
				
			}
			long end=System.currentTimeMillis();
			System.out.println(i+"\t"+(end-start)*0.1+"\t"+Math.log((end-start)*0.1));
		}
	}
	
	//public int count=0;
	public boolean DFSOnePath(GroundPath gp,int node,int l)
	{
		if(l>=gp.path.length())
			return false;
		int[][] nextnodes=triplet_graph[node];
		//int count=0;
		for(int i=0;i<nextnodes.length;i++)
		{
			if(gp.path.GetElement(l).equals(nextnodes[i][0]))
			{
				//count++;
				if(l+1==gp.path.length()&&nextnodes[i][1]==gp.entity[1])
					return true;
				else if(l+1==gp.path.length())
					continue;
				else
				{
					boolean flag=DFSOnePath(gp,nextnodes[i][1],l+1);
					if(flag)
						return true;
				}
			}
		}
		//System.out.println(count);
		return false;
	}
	
}
