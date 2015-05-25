package test;

public class TestingErrAndOut {

	
	public static void main(String[] args)
	{
		//System.out.println("write to out.");
		//System.out.flush();
		//System.err.println("write to err.");
		
		A b=new B();
		A c=new C();
		
		b.Func2();
		c.Func2();
		
	}
	
}

class A
{
	public void Func()
	{
		System.out.println("A called.");
	}
	
	public void Func2()
	{
		Func();
	}
}

class B extends A
{
	public void Func()
	{
		System.out.println("B called.");
	}
}

class C extends A
{
	public void Func()
	{
		System.out.println("C called.");
	}
}
