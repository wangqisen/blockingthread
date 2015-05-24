# blockingthread

This is a BlockingThread API used for those who are java newers.

When you create a Thread and make it beginning to run with codes like this:

```
Thread a=new Thread(){
  public void run(){
    //do something here
    System.out.println("do work");
  }
};
```

There is a problem when the task costs a long time and you wish it to end if you can not get the thread's working result with 
limit time.But you dont's want to use the java concurrency API,which is quite unfamiliar for a beginner.

So The BlockingThread is just for new programmers like you at this point.And,here comes the quesion,What can the BlockingThread
do ?

You just need to start a new BlockingThread in the same way,and use the get() method to wait it end.What is more,you can set a time
to wait,and if the BlockingThread doesn't end in limited time,it will throw and exception which you can catch and use the cancel()
method to cancel it in the catch block.

Here is a example:
```
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
```
