package com.cqu.adopt;

import com.cqu.core.Agent;
import com.cqu.core.Message;

public class AdoptAgent extends Agent{

	public final static int TYPE_VALUE_MESSAGE=0;
	public final static int TYPE_COST_MESSAGE=1;
	public final static int TYPE_THRESHOLD_MESSAGE=2;
	public final static int TYPE_TERMINATE_MESSAGE=3;
	
	//private 
	
	public AdoptAgent(int id, String name, int[] domain) {
		super(id, name, domain);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void initRun() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void dispose(Message msg) {
		// TODO Auto-generated method stub
		
	}
}
