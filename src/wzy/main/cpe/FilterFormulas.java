package wzy.main.cpe;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class FilterFormulas {

	
	public static void Filter(String inputfile,String outputfile,int sub) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(inputfile));
		PrintStream ps=new PrintStream( outputfile);
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("\t");
			int id=Integer.parseInt(ss[0]);
			int count=Integer.parseInt(ss[1]);

			ps.println(id+"\t"+(count<sub?count:sub));
			
			for(int i=0;i<count;i++)
			{
				buffer=br.readLine();
				if(i<sub)
				{
					ps.println(buffer);
				}
			}
		}
	}
	
	public static void main(String[] args) throws IOException
	{
		String dir="F:\\Workspace\\KBCworkspace\\groundformulas\\";
		Filter(dir+"fb15k_3.for",dir+"fb15k_3_10.for",10);
		Filter(dir+"fb15k_4.for",dir+"fb15k_4_10.for",10);	
		Filter(dir+"fb15k_3.for",dir+"fb15k_3_20.for",20);
		Filter(dir+"fb15k_4.for",dir+"fb15k_4_20.for",20);			
		Filter(dir+"fb15k_3.for",dir+"fb15k_3_50.for",50);
		Filter(dir+"fb15k_4.for",dir+"fb15k_4_50.for",50);			
		
		Filter(dir+"fb15k_3.for",dir+"fb15k_3_3.for",3);
		Filter(dir+"fb15k_4.for",dir+"fb15k_4_3.for",3);				
	}
}
