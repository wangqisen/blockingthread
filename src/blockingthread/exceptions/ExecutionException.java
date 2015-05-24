package blockingthread.exceptions;


/**
 * Created by xiaowang on 2015/5/23.
 */
public class ExecutionException extends  RuntimeException{
	private String name;
	private String message;
	
	public ExecutionException(){}
	
	public ExecutionException(String name,String message){
		this.name=name;
		this.message=message;
	}
	
	public ExecutionException(String message){
		this("ExecutionException",message);
	} 
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String toString(){
		return "ExecutionException name:"+name+" , "+"info:"+message;
	}	
}
