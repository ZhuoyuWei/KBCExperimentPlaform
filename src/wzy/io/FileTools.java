package wzy.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import wzy.meta.GroundPath;
import wzy.meta.PathSupport;
import wzy.meta.RPath;

public class FileTools {

	
	/**
	 * Read lines as the format: 'h r t'
	 * @param filename
	 * @param separator is a regular expression for separate one line
	 * @return
	 */
	public static int[][] ReadIntegralTriplets(String filename,String separator)
	{
		List<int[]> triplets=new ArrayList<int[]>();
		try {
			BufferedReader br=new BufferedReader(new FileReader(filename));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split(separator);
				if(ss.length!=3)
				{
					System.err.println("Input Data Format is wrong");
					System.err.println(buffer);
					System.exit(-1);
				}
				int[] triplet=new int[3];
				for(int i=0;i<3;i++)
				{
					triplet[i]=Integer.parseInt(ss[i]);
				}
				triplets.add(triplet);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return triplets.toArray(new int[0][0]);
	}
	
	/**
	 * Read lines as the format: 'h r t'
	 * h,r,t are all string
	 * @param filename
	 * @param separator is a regular expression for separate one line
	 * @return
	 */
	public static String[][] ReadStringTriplets(String filename,String separator)
	{
		List<String[]> triplets=new ArrayList<String[]>();
		try {
			BufferedReader br=new BufferedReader(new FileReader(filename));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split(separator);
				if(ss.length!=3)
				{
					System.err.println("Input Data Format is wrong");
					System.err.println(buffer);
					System.exit(-1);
				}
				triplets.add(ss);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return triplets.toArray(new String[0][0]);
	}	
	
	/**
	 * Print triplets as the specific format.
	 * @param triplets
	 * @param filename
	 * @param separator
	 */
	public static void PrintIntegralTriplets(int[][] triplets,String filename,String separator)
	{
		try {
			PrintStream ps=new PrintStream(filename);
			for(int i=0;i<triplets.length;i++)
			{
				ps.println(triplets[i][0]+separator+triplets[i][1]+separator+triplets[i][2]);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void PrintEmbeddingList(String filename,List<Object> embeddingList)
	{
		PrintStream ps=null;
		try {
			ps=new PrintStream(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return; //if fail to create file, don't print model and continue process.
		}
		for(int i=0;i<embeddingList.size();i++)
		{
			if(embeddingList.get(i) instanceof double[][][])
			{
				PrintEmbedding((double[][][])embeddingList.get(i),ps);
			}
			else if(embeddingList.get(i) instanceof double[][])
			{
				PrintEmbedding((double[][])embeddingList.get(i),ps);	
			}
			else if(embeddingList.get(i) instanceof double[])
			{
				PrintEmbedding((double[])embeddingList.get(i),ps);			
			}
		}
	}

	public static void PrintEmbedding(double[][][] embedding,PrintStream ps)
	{
		for(int i=0;i<embedding.length;i++)
		{
			PrintEmbedding(embedding[i],ps);
		}
	}
	public static void PrintEmbedding(double[][] embedding,PrintStream ps)
	{
		for(int i=0;i<embedding.length;i++)
		{
			PrintEmbedding(embedding[i],ps);
		}		
	}
	public static void PrintEmbedding(double[] embedding,PrintStream ps)
	{
		if(embedding==null||embedding.length<=0)
			return;
		for(int i=0;i<embedding.length-1;i++)
		{
			ps.print(embedding[i]+"\t");
		}
		ps.println(embedding[embedding.length-1]);
	}
	public static void ReDirectOutputStreamToFile(String filename)
	{
		try {
			System.setOut(new PrintStream(filename));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void makeDir(String dirString) {
		File dir=new File(dirString);
        if(! dir.getParentFile().exists()) {  
            makeDir(dir.getParent());  
        }  
        dir.mkdir();  
    }  
	
	public static boolean ReadEmbeddingsFromFile(String filename,List<Object> embeddingList)
	{
		boolean flag=true;
		try {
			BufferedReader br=new BufferedReader(new FileReader(filename));
			
			for(int i=0;i<embeddingList.size();i++)
			{
				
				if(embeddingList.get(i) instanceof double[][][])
				{
					flag=ReadEmbedding(br,(double[][][])embeddingList.get(i));
				}
				else if(embeddingList.get(i) instanceof double[][])
				{
					flag=ReadEmbedding(br,(double[][])embeddingList.get(i));
				}
				else if(embeddingList.get(i) instanceof double[])
				{
					flag=ReadEmbedding(br,(double[])embeddingList.get(i));
				}
				if(!flag)
					break;
			}
			
			//if(br.readLine()==null)
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
	}
	public static boolean ReadEmbedding(BufferedReader br,double[] embedding)
	{
		String buffer=null;
		while(true)
		{
			try {
				buffer=br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(buffer==null)
				return false;
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("\t");
			if(ss.length!=embedding.length)
			{
				System.err.println("There is an error occurs in reading embedding: "
						+ss.length+" "+embedding.length);
				System.exit(-1);
			}
			for(int i=0;i<ss.length;i++)
			{
				embedding[i]=Double.parseDouble(ss[i]);
			}
			break;
		}
		return true;
	}
	public static boolean ReadEmbedding(BufferedReader br,double[][] embedding)
	{
		for(int i=0;i<embedding.length;i++)
		{
			boolean flag=ReadEmbedding(br,embedding[i]);
			if(!flag)
				return false;
		}
		return true;
	}
	public static boolean ReadEmbedding(BufferedReader br,double[][][] embedding)
	{
		for(int i=0;i<embedding.length;i++)
		{
			boolean flag=ReadEmbedding(br,embedding[i]);
			if(!flag)
				return false;
		}
		return true;
	}
	
	public static void PrintFinalResult(String filename,long time,int raw_hit10l,int raw_meanl,int filter_hit10l,
			int filter_meanl,int raw_hit10r,int raw_meanr,int filter_hit10r,int filter_meanr,int test_triplets_length)
	{
		try {
			PrintStream ps=new PrintStream(filename);
			ps.println("Testing is end at "+time+"s. Final testing result:");
			ps.println("Left:\t"+(double)raw_hit10l/test_triplets_length
					+"\t"+(double)raw_meanl/test_triplets_length
					+"\t"+(double)filter_hit10l/test_triplets_length
					+"\t"+(double)filter_meanl/test_triplets_length);
			ps.println("Right:\t"+(double)raw_hit10r/test_triplets_length
					+"\t"+(double)raw_meanr/test_triplets_length
					+"\t"+(double)filter_hit10r/test_triplets_length
					+"\t"+(double)filter_meanr/test_triplets_length);		
			ps.println("Final:\t"+(double)(raw_hit10l+raw_hit10r)/test_triplets_length/2.
					+"\t"+(double)(raw_meanl+raw_meanr)/test_triplets_length/2.
					+"\t"+(double)(filter_hit10l+filter_hit10r)/test_triplets_length/2.
					+"\t"+(double)(filter_meanl+filter_meanr)/test_triplets_length/2.);
			ps.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void PrintPaths(List<PathSupport> pathList,PrintStream ps)
	{
		for(int i=0;i<pathList.size();i++)
		{
			List<Integer> relationList=pathList.get(i).getPath().getRelationList();
			for(int j=0;j<relationList.size();j++)
			{
				ps.print(relationList.get(j)+"\t");
			}
			ps.println();
		}
	}
	public static void PrintPathsAndCount(List<PathSupport> pathList,PrintStream ps)
	{
		for(int i=0;i<pathList.size();i++)
		{
			PathSupport psupport=pathList.get(i);
			List<Integer> relationList=pathList.get(i).getPath().getRelationList();
			for(int j=0;j<relationList.size();j++)
			{
				//ps.print(relationList.get(j)+"_"+psupport.getCount()+"\t");
				ps.print(relationList.get(j)+"_"+psupport.getScore()+"\t");
			}
			ps.println();
		}
	}	
	
	public static void PrintAllFormula(List<PathSupport>[] pathLL,String filename,boolean printcount)
	{
		try {
			PrintStream ps=new PrintStream(filename);
			for(int i=0;i<pathLL.length;i++)
			{
				ps.println(i+"\t"+pathLL[i].size());
				if(printcount)
					PrintPathsAndCount(pathLL[i],ps);
				else
					PrintPaths(pathLL[i],ps);
			}
			ps.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static RPath[][] ReadFormulasForRelations(String filename,int relNum)
	{
		RPath[][] rpathLists=null;
		try {
			rpathLists=new RPath[relNum][];
			BufferedReader br=new  BufferedReader(new FileReader(filename));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split("\t");
				int relation=Integer.parseInt(ss[0]);
				int pcount=Integer.parseInt(ss[1]);
				rpathLists[relation]=new RPath[pcount];
				for(int i=0;i<pcount;i++)
				{
					rpathLists[relation][i]=new RPath();
					
					String line=br.readLine();
					String[] rels=line.split("\t");
					for(int j=0;j<rels.length;j++)
					{
						if(rels[j].contains("_"))
						{
							String[] relsjs=rels[j].split("_");
							rels[j]=relsjs[0];
							rpathLists[relation][i].setWeight(Double.parseDouble(relsjs[1]));
						}
						rpathLists[relation][i].Add(Integer.parseInt(rels[j]));
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rpathLists;
	}
	
	//read formula collections
	public static int[][] ReadFormulas_RelationMatrix(String filename,int relNum)
	{
		int[][] relationmatrix=null;
		try {
			relationmatrix=new int[relNum][relNum];
			BufferedReader br=new  BufferedReader(new FileReader(filename));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split("\t");
				int relation=Integer.parseInt(ss[0]);
				int pcount=Integer.parseInt(ss[1]);
				
				for(int i=0;i<pcount;i++)
				{
					
					
					String line=br.readLine();
					String[] rels=line.split("\t");
					if(rels.length<2)
						continue;
					for(int j=0;j<rels.length;j++)
					{
						if(rels[j].contains("_"))
						{
							String[] relsjs=rels[j].split("_");
							rels[j]=relsjs[0];
						}

					}
					
					for(int j=0;j<rels.length-1;j++)
					{
						relationmatrix[Integer.parseInt(rels[j])][Integer.parseInt(rels[j+1])]=relation;
					}
					
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return relationmatrix;
	}
	
	public static void PrintPaths(List<GroundPath>[] gpList,String dir) throws FileNotFoundException
	{
		for(int i=1;i<=10;i++)
		{
			PrintStream ps=new PrintStream(dir+i);
			for(int j=0;j<gpList[i].size();j++)
			{
				ps.print(gpList[i].get(j).entity[0]+"\t");
				
				for(int k=0;k<i-1;k++)
				{
					ps.print(gpList[i].get(j).path.getRelationList().get(k)+"-");
				}
				ps.print(gpList[i].get(j).path.getRelationList().get(i-1)+"\t");
				
				ps.println(gpList[i].get(j).entity[1]);
			}
			ps.flush();
			ps.close();
		}
	}
	
	public static List<GroundPath>[] ReadGroundPath(String dir) throws IOException
	{
		List<GroundPath>[] resList=new List[11];
		
		for(int i=1;i<=10;i++)
		{
			try {
				resList[i]=new ArrayList<GroundPath>();
				BufferedReader br=new BufferedReader(new FileReader(dir+"/"+i));
				String buffer=null;
				while((buffer=br.readLine())!=null)
				{
					if(buffer.length()<2)
						continue;
					String[] ss=buffer.split("\t");
					GroundPath gp=new GroundPath();
					gp.entity[0]=Integer.parseInt(ss[0]);
					gp.entity[1]=Integer.parseInt(ss[2]);
					String[] token=ss[1].split("-");
					for(int j=0;j<token.length;j++)
					{
						gp.path.getRelationList().add(Integer.parseInt(token[j]));
					}
					resList[i].add(gp);
				}
				br.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return resList;
	}
	
	public static int[][][] ReadTestAllCandidates(String filename,int test_triplet_size,int candidate_size,int state)
	{
		int[][][] res=new int[test_triplet_size][state][candidate_size];
		try {
			BufferedReader br=new BufferedReader(new FileReader(filename));
			String buffer=null;
			for(int i=0;i<test_triplet_size;i++)
			{
				for(int j=0;j<state;j++)
				{
					buffer=br.readLine();
					String[] ss=buffer.split("\t");
					if(ss.length!=candidate_size)
					{
						System.err.println("read test candidate error "+ss.length+" "+candidate_size);
						System.exit(-1);
					}
					for(int k=0;k<candidate_size;k++)
					{
						res[i][j][k]=Integer.parseInt(ss[k]);
					}
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	
	
	
}
