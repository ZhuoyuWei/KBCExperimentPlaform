package test;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class test {
	private String name;
	private int population;
	public test(String name, int population)
	{
		this.name = name;
	    this.population = population;
	}
	public String getName()
	{
	     return this.name;
	}

	public int getPopulation()
	{
	     return this.population;
	}
	public String toString()
	{
	     return getName() + " - " + getPopulation();
	}
	public static void main(String args[])
	{
		Comparator<test> OrderIsdn =  new Comparator<test>(){
			public int compare(test o1, test o2) {
				// TODO Auto-generated method stub
				int numbera = o1.getPopulation();
				int numberb = o2.getPopulation();
				if(numbera < numberb)
				{
					return 1;
				}
				else if(numbera > numberb)
				{
					return -1;
				}
				else
				{
					return 0;
				}
			
			}

			
			
		};
		Queue<test> priorityQueue =  new PriorityQueue<test>(11,OrderIsdn);
		
			    
			
		test t1 = new test("t1",1);
		test t3 = new test("t3",3);
		test t2 = new test("t2",2);
		test t4 = new test("t4",0);
		priorityQueue.add(t1);
		priorityQueue.add(t3);
		priorityQueue.add(t2);
		priorityQueue.add(t4);
		System.out.println(priorityQueue.poll().toString());
	}
}
