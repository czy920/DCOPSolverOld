package org.cqu.algorithm.hybridmbdpop;

import org.cqu.algorithm.dpop.MultiDimensionData;
import org.cqu.core.control.Message;


public class UtilMessage extends Message{
	
	private MultiDimensionData mdData;
	private ContextWrapped context;
	
	public UtilMessage(int idSender, int idReceiver, int type, MultiDimensionData mdData, ContextWrapped context) {
		super(idSender, idReceiver, type, null);
		// TODO Auto-generated constructor stub
		this.mdData=mdData;
		this.context=context;
	}

	public MultiDimensionData getMdData() {
		return mdData;
	}

	public ContextWrapped getContext() {
		return context;
	}

}
