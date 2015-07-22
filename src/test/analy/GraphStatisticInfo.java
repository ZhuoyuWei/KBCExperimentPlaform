package test.analy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class GraphStatisticInfo {

	public Map<String,Map<String,List<String>>> graph=new HashMap<String,Map<String,List<String>>>();
	
	public void ReadOriganFile(String filename) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("[\\s]+");
			Map<String,List<String>> relMap=graph.get(ss[0]);
			if(relMap==null)
			{
				relMap=new HashMap<String,List<String>>();
				graph.put(ss[0], relMap);
			}
			List<String> tList=relMap.get(ss[1]);
			
		}
	}
	
	
	public static void main(String[] args)
	{
		
	}
}
