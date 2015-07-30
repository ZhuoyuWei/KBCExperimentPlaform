package wzy.thread.cons4rel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import wzy.meta.PathSupport;
import wzy.meta.RPath;

public class ConstrForRel implements Callable{

	protected int relation;
	protected Map<RPath,Integer> path2Count=new HashMap<RPath,Integer>();
	
	protected int[][] train_triplets;
	
	protected static int[][][] triplet_graph;
	protected static int maxPathNum=100;
	protected static int maxLength;
	protected static int minLength;
	protected static boolean queit=false;
	
	private List<PathSupport>[] rpathLists=null;
	
	/**
	 * Need to be overwritten.
	 * @Overwrite
	 */
	protected void Mining(int[] triplet)
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
			psList=Processing();
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
	
	
	
}
