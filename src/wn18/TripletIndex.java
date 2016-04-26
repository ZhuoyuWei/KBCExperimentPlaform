package wn18;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TripletIndex {

	public Map<String,Integer> ename2id=new HashMap<String,Integer>();
	public List<String> eid2name=new ArrayList<String>();
	
	public Map<String,Integer> rname2id=new HashMap<String,Integer>();
	public List<String> rid2name=new ArrayList<String>();	
	
	
	
	
	public List<int[]> ReadDataSetFromFile(String filename) throws IOException
	{
		List<int[]> resList=new ArrayList<int[]>();
		
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),"utf8"));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("[\\s]+");
			if(ss.length!=3)
			{
				System.err.println("Read Err:\t"+ss.length+"\n"+buffer);
			}
			
			int[] res=new int[3];
			
			for(int i=0;i<ss.length;i+=2)
			{
				Integer index=ename2id.get(ss[i]);
				if(index==null)
				{
					index=ename2id.size();
					ename2id.put(ss[i], index);
					eid2name.add(ss[i]);
				}
				res[i]=index;
			}
			
			Integer index=rname2id.get(ss[1]);
			if(index==null)
			{
				index=rname2id.size();
				rname2id.put(ss[1], index);
				rid2name.add(ss[1]);
			}
			res[1]=index;
			
			resList.add(res);
		}
		
		br.close();
		return resList;
	}
	
	public void PrintDict(List<String> list,String filename) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter pw=new PrintWriter(filename,"utf8");
		for(int i=0;i<list.size();i++)
		{
			pw.println(list.get(i)+"\t"+i);
		}
		pw.flush();
		pw.close();
	}
	
	public void PrintIndex(List<int[]> tripletList,String filename) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter pw=new PrintWriter(filename,"utf8");
		for(int i=0;i<tripletList.size();i++)
		{
			pw.println(tripletList.get(i)[0]+"\t"+tripletList.get(i)[1]+"\t"+tripletList.get(i)[2]);
		}
		pw.flush();
		pw.close();
	}
	
	
	public static void main(String[] args) throws IOException
	{
		TripletIndex ti=new TripletIndex();
		ti.PrintIndex(ti.ReadDataSetFromFile("C:\\Users\\liushulin\\Desktop\\emnlp\\kbdata\\antoine的原始数据\\" +
				"wordnet-mlj12\\wordnet-mlj12\\wordnet-mlj12-train.txt"),"C:\\Users\\liushulin\\Desktop\\emnlp\\kbdata\\antoine的原始数据\\" +
				"wordnet-mlj12\\wordnet-mlj12\\int_wordnet-mlj12-train.txt");
		ti.PrintIndex(ti.ReadDataSetFromFile("C:\\Users\\liushulin\\Desktop\\emnlp\\kbdata\\antoine的原始数据\\" +
				"wordnet-mlj12\\wordnet-mlj12\\wordnet-mlj12-valid.txt"),"C:\\Users\\liushulin\\Desktop\\emnlp\\kbdata\\antoine的原始数据\\" +
				"wordnet-mlj12\\wordnet-mlj12\\int_wordnet-mlj12-valid.txt");
		ti.PrintIndex(ti.ReadDataSetFromFile("C:\\Users\\liushulin\\Desktop\\emnlp\\kbdata\\antoine的原始数据\\" +
				"wordnet-mlj12\\wordnet-mlj12\\wordnet-mlj12-test.txt"),"C:\\Users\\liushulin\\Desktop\\emnlp\\kbdata\\antoine的原始数据\\" +
				"wordnet-mlj12\\wordnet-mlj12\\int_wordnet-mlj12-test.txt");
		ti.PrintDict(ti.eid2name, "C:\\Users\\liushulin\\Desktop\\emnlp\\kbdata\\antoine的原始数据\\" +
				"wordnet-mlj12\\wordnet-mlj12\\entity2id.txt");		
		ti.PrintDict(ti.rid2name, "C:\\Users\\liushulin\\Desktop\\emnlp\\kbdata\\antoine的原始数据\\" +
				"wordnet-mlj12\\wordnet-mlj12\\relation2id.txt");
	}
	
	
	
}
