package test;

import java.util.concurrent.TimeUnit;

import blockingthread.BlockingThread;


public class Test {
	public static void main(String args[]){
		Thread thread=new Thread(){
			public void run(){
				//IO busy or computing busy task which has iteration and costs long time
				for(int i=0;i<100000000L;i++){
					//NOTE:if you want to make the cancellation functionality working well,make sure you check 
					//the Thread.currentThread().interrupted() to break the iteraion.
					if(Thread.currentThread().interrupted())
						return;
					i*=89;
					i/=89;
				}
			}
		};
		BlockingThread blockingThread=new BlockingThread(thread);
		blockingThread.start();
		try{
			blockingThread.get(5,TimeUnit.SECONDS);
		}catch(Exception e){
			e.printStackTrace();
			//You can cancel the long thread if the time is over the limit you set,which is 5 seconds here.  
			blockingThread.cancel(true);
		}
	}
}
