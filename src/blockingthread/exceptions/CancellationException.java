package blockingthread.exceptions;


/**
 * Created by xiaowang on 2015/5/23.
 */
public class CancellationException extends RuntimeException{
	
	private String name;
	private String message;
	
	public CancellationException(){}
	
	public CancellationException(String name,String message){
		this.name=name;
		this.message=message;
	}
	
	public CancellationException(String message){
		this("CancellationException",message);
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
		return "CancellationException name:"+name+" , "+"info:"+message;
	}	
}
