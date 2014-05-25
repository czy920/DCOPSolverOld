package com.cqu.core;

public class Message {
	
	private int idSender;
	private int idReceiver;
	private Object value;
	
	public Message(int idSender, int idReceiver, Object value) {
		super();
		this.idSender = idSender;
		this.idReceiver = idReceiver;
		this.value = value;
	}
	
	public int getIdSender() {
		return idSender;
	}
	
	public void setIdSender(int idSender) {
		this.idSender = idSender;
	}
	
	public int getIdReceiver() {
		return idReceiver;
	}
	
	public void setIdReceiver(int idReceiver) {
		this.idReceiver = idReceiver;
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
}
