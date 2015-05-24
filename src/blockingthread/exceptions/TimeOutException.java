package blockingthread.exceptions;


/**
 * Created by xiaowang on 2015/5/23.
 */
public class TimeOutException extends RuntimeException{
	private String name;
	private String message;
	
	public TimeOutException(){}
	
	public TimeOutException(String name,String message){
		this.name=name;
		this.message=message;
	}
	
	public TimeOutException(String message){
		this("TimeOutException",message);
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
		return "TimeOutException name:"+name+" , "+"info:"+message;
	}
}
