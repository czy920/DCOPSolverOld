package org.cqu.core.control;


public class MessageNCCC extends Message{
	
	private int nccc;

	public MessageNCCC(int idSender, int idReceiver, int type, Object value) {
		super(idSender, idReceiver, type, value);
		// TODO Auto-generated constructor stub
		this.nccc=0;
	}
	
	public MessageNCCC(int idSender, int idReceiver, int type, Object value, int nccc) {
		super(idSender, idReceiver, type, value);
		// TODO Auto-generated constructor stub
		this.nccc=nccc;
	}
	
	public MessageNCCC(Message msg, int nccc)
	{
		super(msg.getIdSender(), msg.getIdReceiver(), msg.getType(), msg.getValue());
		this.nccc=nccc;
	}

	public int getNccc() {
		return nccc;
	}

	public void setNccc(int nccc) {
		this.nccc = nccc;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString()+" "+nccc;
	}
}
