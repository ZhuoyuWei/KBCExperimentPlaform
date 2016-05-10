package wzy.main;

import wzy.thread.RandomWalkProcess;

public class RandomWalkMain {

	public static void main(String[] args)
	{
		RandomWalkProcess pp=new RandomWalkProcess();
		pp.dir=args[0];
		//pp.dir="C:\\Users\\Administrator\\Documents\\data\\wn18\\";
		pp.Processing();
	}
	
}
