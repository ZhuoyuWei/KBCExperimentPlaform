package pre_data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import wzy.meta.RPath;
import wzy.model.RandomWalkModel;

public class PickOnePath {

	public static List<String> relList=new ArrayList<String>();
	
	public static void ReadDict(String filename) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			//String[] sss=ss[0].split("/");
			//relList.add(sss[sss.length-1]);
			relList.add(ss[0]);
		}
		br.close();
	}
	
	public static void PickOneFormula(int r,int f,PrintStream ps)
	{
		RPath rpath=RandomWalkModel.rpathLists[r][f];
		
		ps.print(r+"\t"+relList.get(r)+"\t"+f);
		for(int i=0;i<rpath.length();i++)
		{
			int rel=rpath.GetElement(i);
			String srel="";
			if(rel>=relList.size())
			{
				rel-=relList.size();
				srel="-"+relList.get(rel);
			}
			else
			{
				srel=relList.get(rel);
			}
			ps.print("\t"+srel);
		}
		ps.println();	
	}
	
	public static void PickPathsFromFiles(String inputfile,String outputfile) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(inputfile));
		String buffer=null;
		PrintStream ps=new PrintStream(outputfile);
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			int rel=Integer.parseInt(ss[0]);
			int formula=Integer.parseInt(ss[1]);
			
			PickOneFormula(rel,formula,ps);
		}
		ps.close();
		br.close();
	}
	
	public static void main(String[] args) throws IOException
	{
		String dir="C:\\Users\\Administrator\\Documents\\data\\fb15k_doubledirect\\";
		RandomWalkModel.relationNum=2690;
		RandomWalkModel.ReadFormulasNoBuildTree(dir+"paths.txt.100");
		
		ReadDict(dir+"relation2id.txt");
		PickPathsFromFiles("C:\\Users\\liushulin\\Desktop\\emnlp paper write\\figure2\\goal.input",
				"C:\\Users\\liushulin\\Desktop\\emnlp paper write\\figure2\\goal.output2");
		
		
	}
}
