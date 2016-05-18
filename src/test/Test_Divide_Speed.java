package test;

public class Test_Divide_Speed {

	
	
	
	
	public static void main(String[] args)
	{
		long start=System.currentTimeMillis();
		
		double a=0.123312334;
		double b=234.3245;
		
		for(int i=0;i<Integer.MAX_VALUE;i++)
		{
			b/=a;
			//b*=(1/a);
		}
		
		long end=System.currentTimeMillis();
		
		System.out.println((end-start));
	}
}
