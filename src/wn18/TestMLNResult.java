package wn18;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wzy.meta.EntityAndScoreForSort;
import wzy.meta.TripletHash;
import wzy.thread.ConstructFormulas;
import wzy.thread.KBCProcess;

public class TestMLNResult {

	
	public int[][] triplets;
	public int[][][] graph;
	public double hitat10raw=0;
	public double gmeanraw=0;
	public double hitat10filt=0;
	public double gmeanfilt=0;	
	public int count=0;
	
	public Set<TripletHash> triset=new HashSet<TripletHash>();
	public void BuildTrainHash(int[][] triplets)
	{
		for(int i=0;i<triplets.length;i++)
		{
			TripletHash th=new TripletHash();
			th.setTriplet(triplets[i]);
			triset.add(th);
		}
	}
	
	
	public void AllFiles(String dirfile,String datadir,int lorr) throws IOException
	{

		
		File[] dirs=new File(dirfile).listFiles();
		for(int i=0;i<dirs.length;i++)
		{
			List<Double> scoreline=new ArrayList<Double>();
			List<Integer> indexline=new ArrayList<Integer>();
			List<String> tripletline=new ArrayList<String>();
			
			BufferedReader br=null;
			try{
				br=new BufferedReader(new FileReader(dirs[i].getPath()+"/res/probability.result"));
			}catch(Exception e)
			{
				continue;
			}
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split("[\\s]+");
				scoreline.add(Double.parseDouble(ss[0]));
			}
			br.close();
			try{
			br=new BufferedReader(new FileReader(datadir+"/"+dirs[i].getName()+"/test.query.map"));
			}catch(Exception e)
			{
				continue;
			}
			buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split("[\\s]+");
				tripletline.add(ss[0]);
				indexline.add(Integer.parseInt(ss[2]));
			}
			br.close();	
			
			if(scoreline.size()!=indexline.size())
			{
				System.err.println("Error:\t"+dirs[i].getPath()+"/res/probability.result");
				System.err.println("\t\t"+datadir+"/"+dirs[i].getName()+"/test.query.map");
				continue;
			}
			
			
			int index=-1;
			int r=-1;
			int h=-1;
			int t=-1;
			TripletHash th=new TripletHash();
			List<EntityAndScoreForSort> reverselist=new ArrayList<EntityAndScoreForSort>();
			
			for(int j=0;j<scoreline.size();j++)
			{
				if(indexline.get(j)!=index)
				{
					if(index>=0)
					{
						Collections.sort(reverselist,new EntityAndScoreForSort());
						int meanraw=1;
						int meanfilt=1;
						for(int k=reverselist.size()-1;k>=0;k--)
						{
							if(lorr==0&&reverselist.get(k).entity==t)
							{
								break;
							}
							else if(lorr!=0&&reverselist.get(k).entity==h)
							{
								break;
							}
							else
							{
								meanraw++;
								if(lorr==0)
								{
									th.getTriplet()[2]=reverselist.get(k).entity;
								}
								else
								{
									th.getTriplet()[0]=reverselist.get(k).entity;
								}
								if(!triset.contains(th))
								{
									meanfilt++;
								}
							}
						}
						if(meanraw<=10)
							hitat10raw++;
						if(meanfilt<=10)
							hitat10filt++;
						gmeanraw+=meanraw;
						gmeanfilt+=meanfilt;
						count++;
							
					}
					//build answer
					index=indexline.get(j);
					int[] answer=triplets[index];
					
					h=answer[0];
					r=answer[1];
					t=answer[2];
					th.setTriplet(answer);
					
					reverselist=new ArrayList<EntityAndScoreForSort>();
				}
				EntityAndScoreForSort e=new EntityAndScoreForSort();
				String[] triplet=tripletline.get(j).split("[(),]+");
				if(lorr==0)
					e.entity=Integer.parseInt(triplet[2]);
				else
					e.entity=Integer.parseInt(triplet[1]);
				e.score=scoreline.get(j);
				
				reverselist.add(e);
			}
			Collections.sort(reverselist,new EntityAndScoreForSort());
			int meanraw=1;
			int meanfilt=1;
			for(int k=reverselist.size()-1;k>=0;k--)
			{
				if(lorr==0&&reverselist.get(k).entity==t)
				{
					break;
				}
				else if(lorr!=0&&reverselist.get(k).entity==h)
				{
					break;
				}
				else
				{
					meanraw++;
					//System.out.println(th.getTriplet()[0]+"\t"+th.getTriplet()[1]+"\t"+th.getTriplet()[2]);
					if(lorr==0)
					{
						th.getTriplet()[2]=reverselist.get(k).entity;
					}
					else
					{
						th.getTriplet()[0]=reverselist.get(k).entity;
					}
					if(!triset.contains(th))
					{
						meanfilt++;
					}
					else
					{
						System.out.println("have");
					}
				}
			}
			if(meanraw<=10)
				hitat10raw++;
			if(meanfilt<=10)
				hitat10filt++;
			gmeanraw+=meanraw;
			gmeanfilt+=meanfilt;
			count++;
			
			
		}
		
		
		
		
		
		System.out.println((gmeanraw/count)+"\t"
				+(hitat10raw/count)+"\t"
				+(gmeanfilt/count)+"\t"
				+(hitat10filt/count));
		
	}
	
	
	public void PrintSet() throws FileNotFoundException
	{
		Iterator it=this.triset.iterator();
		PrintStream ps=new PrintStream("F:\\datainpaper\\SME-master_for_read\\SME-master\\FB15k\\mln_exp_can1000\\result\\log"); 
		while(it.hasNext())
		{
			TripletHash tri=(TripletHash)it.next();
			ps.println(tri.getTriplet()[0]+"\t"+tri.getTriplet()[1]+"\t"+tri.getTriplet()[2]);
		}
		ps.close();
	}
	
	public static void main(String[] args) throws IOException
	{
		String dir=args[0];
		
		//Read Three DB Files
		KBCProcess kbc_raw_tester=new KBCProcess();
		
		/*kbc_raw_tester.SetThreeTriplets(dir+"liuzhiyuan.train.txt"
				, dir+"liuzhiyuan.valid.txt"
				, dir+"liuzhiyuan.test.txt"
				, "\t");*/
		
		kbc_raw_tester.SetThreeTriplets(dir+"exp_train.txt"
				, dir+"exp_valid.txt"
				, dir+"exp_test.txt"
				, "\t");
		
		TestMLNResult tmr=new TestMLNResult();
		tmr.triplets=kbc_raw_tester.getTest_triplets();
		
		/*
		ConstructFormulas cf=new ConstructFormulas();
		cf.setTrain_triplets(kbc_raw_tester.getTrain_triplets());
		cf.setEntityNum(kbc_raw_tester.getEm().getEntityNum());
		cf.setRelNum(kbc_raw_tester.getEm().getRelationNum());
		cf.Init();
		cf.BuildGraph();
		tmr.graph=cf.getTriplet_graph();
		*/
		tmr.BuildTrainHash(kbc_raw_tester.getTrain_triplets());
		//tmr.AllFiles("F:/Workspace/KBCworkspace/wm18forMLN/wm18MLNresult/right_other"
				//, "F:/Workspace/KBCworkspace/wm18forMLN/wm18candidateright", 1);
		tmr.AllFiles(args[1]
				, args[2], Integer.parseInt(args[3]));
		
		tmr.PrintSet();
	}
	
}
