package com.cqu.core;

public class Message {
	
	private int idSender;
	private int idReceiver;
	private int type;
	private Object value;
	
	public Message(int idSender, int idReceiver, int type, Object value) {
		super();
		this.idSender = idSender;
		this.idReceiver = idReceiver;
		this.type=type;
		this.value = value;
	}
	
	public int getIdSender() {
		return idSender;
	}
	
	public int getIdReceiver() {
		return idReceiver;
	}
	
	public int getType()
	{
		return this.type;
	}
	
	public Object getValue() {
		return value;
	}
}
