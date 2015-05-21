package test;

public class TestNan {

	
	public static void main(String[] args)
	{
		double a=Double.NaN;
		double b=Double.NaN;
		System.out.println(a==b);
		
		int[] t={2,3,4};
		TestPrivateClass t1=new TestPrivateClass();
		TestPrivateClass t2=new TestPrivateClass();
		t2.SetValues(t);
		System.out.println(t1.equals(t2));
		t1.Print(t2);
		t2.Print(t2);
		
		int aa=10;
		System.out.println((double)aa/3);
		//(TestPrivateClass)((Object)t1).
	}
	
}
