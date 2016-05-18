package wzy.main;

import wzy.model.RandomWalkModel;
import wzy.thread.RandomWalkProcess;

public class RandomWalkMain {

	public static void main(String[] args)
	{
		RandomWalkProcess pp=new RandomWalkProcess();
		pp.dir=args[0];
		//pp.dir="C:\\Users\\Administrator\\Documents\\data\\wn18\\";
		pp.modelindex=Integer.parseInt(args[1]);
		RandomWalkModel.emb_force_1=Boolean.parseBoolean(args[2]);
		//pp.modelindex=0;
		
		pp.Processing();
	}
	
}
