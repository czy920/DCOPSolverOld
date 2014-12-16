package com.cqu.hybridmbdpop;

import com.cqu.core.Context;
import com.cqu.core.Message;
import com.cqu.dpop.MultiDimensionData;

public class UtilMessage extends Message{
	
	private MultiDimensionData mdData;
	private Context context;
	
	public UtilMessage(int idSender, int idReceiver, int type, MultiDimensionData mdData, Context context) {
		super(idSender, idReceiver, type, null);
		// TODO Auto-generated constructor stub
		this.mdData=mdData;
		this.context=context;
	}

	public MultiDimensionData getMdData() {
		return mdData;
	}

	public Context getContext() {
		return context;
	}

}
