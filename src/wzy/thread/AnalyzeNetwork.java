package wzy.thread;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import wzy.meta.*;

public class AnalyzeNetwork implements Callable{

	private ConstructFormulas cf;
	private List<EntityInfo> sortEntity=new ArrayList<EntityInfo>();
	
	
	public void SortEntities()
	{
		int[][][] graph=cf.getTriplet_graph();
		for(int i=0;i<graph.length;i++)
		{
			EntityInfo ei=new EntityInfo();
			ei.setId(i);
			ei.setNeighbour_triplet(graph[i]);
			sortEntity.add(ei);
		}
		Collections.sort(sortEntity,new SortEntityInfoByTripletsNum());
	}
	
	public void PrintRes_EntityOrderByTripeltNum(String filename,int printnum)
	{
		PrintStream ps=null;
		try {
			ps = new PrintStream(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		printnum=printnum<sortEntity.size()?printnum:sortEntity.size();
		for(int i=0;i<printnum;i++)
		{
			//ps.println(sortEntity.get(i).getId()+"\t"+sortEntity.get(i).getNeighbour_triplet().length);
			ps.println(sortEntity.get(i).getNeighbour_triplet().length);
			//ps.println(i);
		}
	}
	
	public void Processing()
	{
		cf.BuildGraph();
		SortEntities();
		
	}
	
	
	
	





	public ConstructFormulas getCf() {
		return cf;
	}
	public void setCf(ConstructFormulas cf) {
		this.cf = cf;
	}



	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}

class SortEntityInfoByTripletsNum implements Comparator
{

	@Override
	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub
		
		EntityInfo e1=(EntityInfo)o1;
		EntityInfo e2=(EntityInfo)o2;
		
		if(e1.getNeighbour_triplet().length>e2.getNeighbour_triplet().length)
			return -1;
		else if(e1.getNeighbour_triplet().length<e2.getNeighbour_triplet().length)
			return 1;	
		else
			return 0;
	}
}

