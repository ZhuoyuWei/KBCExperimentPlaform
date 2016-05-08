package test;

import java.util.HashMap;
import java.util.Map;

public class TestMap {

	
	public static void main(String[] args)
	{
		Map<String,Integer> map=new HashMap<String,Integer>();
		map.put("a", 1);
		for(int i=0;i<10;i++)
		{
			int count=map.get("a");
			count++;
			//map.put("a", count);
		}
		System.out.println(map.get("a"));
	}
}
