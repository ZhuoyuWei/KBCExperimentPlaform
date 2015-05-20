package test;

public class TestPrivateClass {

	private int[] value={1,2,3};
	
	public void SetValues(int[] t)
	{
		for(int i=0;i<3;i++)
		{
			value[i]=t[i];
		}
	}
	
	public void Print(TestPrivateClass t)
	{
		//TestPrivateClass t=(TestPrivateClass)o;
		System.out.println(t.value[0]+"\t"+t.value[1]+"\t"+t.value[2]);
	}
	
	public boolean equals(Object o)
	{
		TestPrivateClass t=(TestPrivateClass)o;
		for(int i=0;i<3;i++)
		{
			if(value[i]!=t.value[i])
				return false;
		}
		return true;
	}
	
}
