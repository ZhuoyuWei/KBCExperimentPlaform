package wzy.thread.cons4rel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;

import wzy.meta.PathConfidence;
import wzy.meta.PathSupport;
import wzy.meta.RPath;

public class ConstrForRel implements Callable{

	protected int relation;
	protected Map<RPath,Integer> path2Count=new HashMap<RPath,Integer>();
	protected Map<RPath,int[]> path2Conf=new HashMap<RPath,int[]>();	
	private Random rand=new Random();
	protected Map<Integer,Set<Integer>> checkmap;
	protected Integer[] candfalselist;	
	protected int[][] train_triplets;
	
	protected static int[][][] triplet_graph;
	protected static int maxPathNum=100;
	protected static int maxLength;
	protected static int minLength;
	protected static int false_triplet=1000;	
	protected static boolean queit=false;
	protected static int support_threthold=10;
	protected static double confidence_threthold=0.8;
	
	
	protected static boolean filter=false;
	
	private List<PathSupport>[] rpathLists=null;
	
	/**
	 * Need to be overwritten.
	 * @Overwrite
	 */
	protected void Mining(int[] triplet)
	{}
	protected void Mining(int[] triplet,int flag)
	{}
	private List<PathSupport> Filter(List<PathSupport> pathList)
	{
		int reslength=pathList.size()<maxPathNum?pathList.size():maxPathNum;
		List<PathSupport> resList=pathList.subList(0, reslength);
		return resList;
	}
	public List<PathSupport> Processing()
	{
		//travel all, you can also sampling several ones.
		for(int i=0;i<train_triplets.length;i++)
		{
			Mining(train_triplets[i]);
		}
		
		List<PathSupport> pathList=new ArrayList<PathSupport>();
		Iterator it=path2Count.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			RPath rpath=(RPath)entry.getKey();
			Integer count=(Integer)entry.getValue();
			
			//you can filter relation path by support threshold here.
			//if count < threshold
			//	continue
			
			PathSupport ps=new PathSupport();
			ps.setPath(rpath);
			ps.setCount(count);
			pathList.add(ps);
		}
		
		Collections.sort(pathList,new PathSupport());
		if(filter)
			pathList=Filter(pathList);
		
		
		if(rpathLists!=null)
		{
			rpathLists[relation]=pathList;
		}
		
		return pathList;
	}
	private	int[] produce_false_triplet(int[] triplet)
	{
		int[] ftriplet=new int[3];
		for(int i=0;i<3;i++)
			ftriplet[i]=triplet[i];
		int index;
		while(checkmap.get(ftriplet[0]).contains(ftriplet[2]))
		{
			index=Math.abs(rand.nextInt())%candfalselist.length;
			while(index<0)
				index=Math.abs(rand.nextInt())%candfalselist.length;
			ftriplet[2]=candfalselist[index];
		}
		return ftriplet;
	}
	private void BuildRelGraph()
	{
		checkmap=new HashMap<Integer,Set<Integer>>();
		for(int i=0;i<train_triplets.length;i++)
		{
			Set<Integer> set=checkmap.get(train_triplets[i][0]);
			if(set==null)
			{
				set=new HashSet<Integer>();
				checkmap.put(train_triplets[i][0], set);
			}
			set.add(train_triplets[i][2]);
		}
		Set<Integer> bigset=new HashSet<Integer>();
		Iterator it=checkmap.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			Set<Integer> set=(Set<Integer>)entry.getValue();
			bigset.addAll(set);
		}
		candfalselist=bigset.toArray(new Integer[0]);
	}
	public List<PathSupport> Processing_Conf()
	{
		//if(relation!=16)
			//return null;
		if(false_triplet<=0)
			Processing();
		//build a little graph for relation
		BuildRelGraph();
		//travel all, you can also sampling several ones.
		for(int i=0;i<train_triplets.length;i++)
		{
			//true
			Mining(train_triplets[i],1);
			//false
			for(int j=0;j<false_triplet;j++)
			{
				int[] false_instance=produce_false_triplet(train_triplets[i]);
				Mining(false_instance,0);		
			}
		}
		
		List<PathSupport> pathList=new ArrayList<PathSupport>();
		Iterator it=path2Conf.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			RPath rpath=(RPath)entry.getKey();
			int[] counts=(int[])entry.getValue();
			
			//if the rpath = relation, continue
			if(rpath.length()==1&&rpath.GetElement(0)==relation)
				continue;
			
			//you can filter relation path by support threshold here.
			if(counts[1] < support_threthold)
				continue;
			
			
			PathSupport pc=new PathSupport();
			pc.setPath(rpath);
			pc.setScore((double)counts[1]/(double)(counts[0]+counts[1]));
			if(pc.getScore()<confidence_threthold)
				continue;
			pathList.add(pc);
		}
		
		Collections.sort(pathList,new PathConfidence());
		if(filter)
			pathList=Filter(pathList);
		
		
		if(rpathLists!=null)
		{
			rpathLists[relation]=pathList;
		}
		
		return pathList;
	}	
	
	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub
		long start=System.currentTimeMillis();
		List<PathSupport> psList=null;
		try{
			psList=Processing_Conf();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		long end=System.currentTimeMillis();
		if(!queit)
		{
			System.out.println("Relation "+relation+" is over and takes "+(end-start)/1000
					+"s, and get "+rpathLists[relation].size()+"/"+path2Count.size()+" paths.");
		}
		return psList;
	}

	public int getRelation() {
		return relation;
	}

	public void setRelation(int relation) {
		this.relation = relation;
	}
	public static int getMaxPathNum() {
		return maxPathNum;
	}
	public static void setMaxPathNum(int maxPathNum) {
		ConstrForRel.maxPathNum = maxPathNum;
	}
	public Map<RPath, Integer> getPath2Count() {
		return path2Count;
	}
	public void setPath2Count(Map<RPath, Integer> path2Count) {
		this.path2Count = path2Count;
	}
	public int[][] getTrain_triplets() {
		return train_triplets;
	}
	public void setTrain_triplets(int[][] train_triplets) {
		this.train_triplets = train_triplets;
	}
	public static int getMaxLength() {
		return maxLength;
	}
	public static void setMaxLength(int maxLength) {
		ConstrForRel.maxLength = maxLength;
	}
	public static int getMinLength() {
		return minLength;
	}
	public static void setMinLength(int minLength) {
		ConstrForRel.minLength = minLength;
	}
	public List<PathSupport>[] getRpathLists() {
		return rpathLists;
	}
	public void setRpathLists(List<PathSupport>[] rpathLists) {
		this.rpathLists = rpathLists;
	}
	public static int[][][] getTriplet_graph() {
		return triplet_graph;
	}
	public static void setTriplet_graph(int[][][] triplet_graph) {
		ConstrForRel.triplet_graph = triplet_graph;
	}
	public static boolean isQueit() {
		return queit;
	}
	public static void setQueit(boolean queit) {
		ConstrForRel.queit = queit;
	}
	public static int getSupport_threthold() {
		return support_threthold;
	}
	public static void setSupport_threthold(int support_threthold) {
		ConstrForRel.support_threthold = support_threthold;
	}
	public static double getConfidence_threthold() {
		return confidence_threthold;
	}
	public static void setConfidence_threthold(double confidence_threthold) {
		ConstrForRel.confidence_threthold = confidence_threthold;
	}
	public static int getFalse_triplet() {
		return false_triplet;
	}
	public static void setFalse_triplet(int false_triplet) {
		ConstrForRel.false_triplet = false_triplet;
	}
	
	
	
}
