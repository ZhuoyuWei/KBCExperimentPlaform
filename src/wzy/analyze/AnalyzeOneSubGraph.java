package wzy.analyze;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnalyzeOneSubGraph {

	public void ReadEdges(String filename)
	{}
	
	public void ReadNodes(String filename) throws NumberFormatException, IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		int line=0;
		Set<Integer> nodeSet=new HashSet<Integer>();
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split(",");
			if(line==0)
			{
				line++;
				continue;
			}
			nodeSet.add(Integer.parseInt(ss[0]));
		}
		System.out.println("Node set: "+nodeSet.size());
	}
	
	List<String> nodeLabels=new ArrayList<String>();
	List<String> edgeLabels=new ArrayList<String>();
	
	public void ReadNodeLabels(String filename) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("\t");
			nodeLabels.add(ss[0]);
		}
		br.close();
		System.out.println("Node Size: "+nodeLabels.size());
	}
	
	public void ReadEdgeLabels(String filename) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("\t");
			String[] sss=ss[0].split("/");
			edgeLabels.add(sss[sss.length-1]);
		}
		br.close();
		System.out.println("Edge Size: "+edgeLabels.size());		
	}
	
	public void LabelNodeFile(String inputfile,String outputfile) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(inputfile));
		String buffer=null;
		PrintStream ps=new PrintStream(outputfile);
		int line=0;
		
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			if(line==0)
			{
				line++;
				ps.println(buffer);
				continue;
			}
			String[] ss=buffer.split(",");
			ss[1]=nodeLabels.get(Integer.parseInt(ss[1]));
			ps.println(MergeStrings(ss,","));
		}
		br.close();
		ps.close();
	}	

	public void LabelEdgeFile(String inputfile,String outputfile) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(inputfile));
		String buffer=null;
		PrintStream ps=new PrintStream(outputfile);
		int line=0;
		
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			if(line==0)
			{
				line++;
				ps.println(buffer);
				continue;
			}
			String[] ss=buffer.split(",");
			ss[2]=edgeLabels.get(Integer.parseInt(ss[2]));
			ps.println(MergeStrings(ss,","));
		}
		br.close();
		ps.close();
	}	
	
	public String MergeStrings(String[] ss,String regex)
	{
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<ss.length-1;i++)
		{
			sb.append(ss[i]);
			sb.append(regex);
		}
		sb.append(ss[ss.length-1]);
		return sb.toString();
	}
	
	
	public void LabelNodeColor(String[] filenames,String inputfile,String outputfile) throws IOException
	{
		Set<String>[] colors=new Set[filenames.length];
		for(int i=0;i<filenames.length;i++)
		{
			colors[i]=new HashSet<String>();
			BufferedReader br=new BufferedReader(new FileReader(filenames[i]));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				colors[i].add(buffer);
			}
			br.close();
		}
		
		BufferedReader br=new BufferedReader(new FileReader(inputfile));
		String buffer=null;
		PrintStream ps=new PrintStream(outputfile);
		int line=0;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			if(line==0)
			{
				line++;
				ps.println(buffer+",Color");
				continue;
			}
			String[] ss=buffer.split(",");
			int i=0;
			for(;i<colors.length;i++)
			{
				if(colors[i].contains(ss[1]))
					break;
			}
			ps.println(buffer+","+i);
		}
		ps.close();
		br.close();
	}
	
	
	
	public static void main(String[] args) throws NumberFormatException, IOException
	{
		AnalyzeOneSubGraph aos=new AnalyzeOneSubGraph();
		//aos.ReadNodes("C:\\Users\\liushulin\\Desktop\\14438.subgraph.node");
		
		/*aos.ReadNodeLabels("C:\\Users\\Administrator\\Documents\\data\\fb15k2\\entity2id.txt");
		aos.ReadEdgeLabels("C:\\Users\\Administrator\\Documents\\data\\fb15k2\\relation2id.txt");
		
		aos.LabelEdgeFile("C:\\Users\\liushulin\\Desktop\\emnlp paper write\\showfigures\\14438.subgraph.edge"
				, "C:\\Users\\liushulin\\Desktop\\emnlp paper write\\showfigures\\14438.edge.label");
		aos.LabelNodeFile("C:\\Users\\liushulin\\Desktop\\emnlp paper write\\showfigures\\14438.subgraph.node"
				, "C:\\Users\\liushulin\\Desktop\\emnlp paper write\\showfigures\\14438.node.label");	*/
		
		String dir="C:\\Users\\liushulin\\Desktop\\emnlp paper write\\showfigures\\";
		String[] colorfiles=new String[2];
		colorfiles[0]=dir+"red.txt";
		colorfiles[1]=dir+"blue.txt";
		aos.LabelNodeColor(colorfiles, dir+"14438.node.label", dir+"14438.node.label.color");
		
		
	}
	
	
}
