package test.analy;

class Base
{
	public void run()
	{
		output();
	}
	public void output()
	{
		System.out.println("Base");
	}
}

class A extends Base
{
	@Override
	public void run()
	{
		super.run();
	}	
	
	@Override
	public void output()
	{
		System.out.println("A");
	}
}



public class TestExtentClass {

	public static void main(String[] args)
	{
		A a=new A();
		a.run();
	}
	
}
