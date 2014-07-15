package com.cqu.dpop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.core.Agent;
import com.cqu.core.Message;
import com.cqu.util.ArrayIndexComparator;

public class DPOPAgent extends Agent{

	public final static int TYPE_VALUE_MESSAGE=0;
	public final static int TYPE_UTIL_MESSAGE=1;
	
	private Integer[] parentLevels;
	private Integer[] parentLevelSortIndexes;
	private Map<Integer, MulitiDimentionalData> allChildrenUtils;
	
	{
		//表示消息不丢失
		QUEUE_CAPACITY=-1;
	}
	
	public DPOPAgent(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void initRun() {
		// TODO Auto-generated method stub
		super.initRun();
		
		allChildrenUtils=new HashMap<Integer, MulitiDimentionalData>();
		
		parentLevels=new Integer[allParents.length];
		for(int i=0;i<allParents.length;i++)
		{
			parentLevels[i]=neighbourLevels.get(allParents[i]);
		}
		ArrayIndexComparator<Integer> comparator=new ArrayIndexComparator<Integer>(parentLevels);
		parentLevelSortIndexes=comparator.sort();
		
		if(this.isLeafAgent()==true)
		{
			sendUtilMessage();
		}
	}

	@Override
	public void printResults(List<Map<String, Object>> results) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String easyMessageContent(Message msg, Agent sender, Agent receiver) {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	protected void disposeMessage(Message msg) {
		// TODO Auto-generated method stub
		int type=msg.getType();
		if(type==TYPE_VALUE_MESSAGE)
		{
			disposeValueMessage(msg);
		}else if(type==TYPE_UTIL_MESSAGE)
		{
			disposeUtilMessage(msg);
		}
	}

	@Override
	protected void messageLost(Message msg) {
		// TODO Auto-generated method stub
		
	}
	
	private void sendUtilMessage()
	{
		if(this.isRootAgent()==true)
		{
			return;
		}
		MulitiDimentionalData multiDimentionalData=this.computeLocalUtils();
		Message utilMsg=new Message(this.id, this.parent, TYPE_UTIL_MESSAGE, multiDimentionalData);
		this.sendMessage(utilMsg);
	}
	
	private MulitiDimentionalData computeLocalUtils()
	{
		int dataLength=1;
		Map<String, Integer> dimentionNames=new HashMap<String, Integer>();
		int[] dimentionLengths=new int[allParents.length+1];
		for(int i=0;i<allParents.length;i++)
		{
			int parentId=allParents[parentLevelSortIndexes[i]];
			dimentionNames.put(parentId+"", i);
			dimentionLengths[i]=neighbourDomains.get(parentId).length;
			dataLength=dataLength*dimentionLengths[i];
		}
		dimentionLengths[dimentionLengths.length-1]=this.domain.length;
		dimentionNames.put(this.id+"", dimentionLengths.length-1);
		dataLength=dataLength*this.domain.length;
		//set data
		int[] agentValueIndexes=new int[allParents.length+1];
		int[] data=new int[dataLength];
		int dataIndex=0;
		int curDimention=agentValueIndexes.length-1;
		while(dataIndex<data.length)
		{
			int costSum=0;
			for(int i=0;i<allParents.length;i++)
			{
				//保证id小的为行，id大的为列
				if(this.id<allParents[i])
				{
					costSum+=this.constraintCosts.get(allParents[i])[agentValueIndexes[agentValueIndexes.length-1]][agentValueIndexes[i]];
				}else
				{
					costSum+=this.constraintCosts.get(allParents[i])[agentValueIndexes[i]][agentValueIndexes[agentValueIndexes.length-1]];
				}
			}
			data[dataIndex]=costSum;
			
			agentValueIndexes[curDimention]+=1;
			while(agentValueIndexes[curDimention]>=dimentionLengths[curDimention])
			{
				agentValueIndexes[curDimention]=0;
				curDimention-=1;
				if(curDimention==-1)
				{
					//all data has been set
					break;
				}
				agentValueIndexes[curDimention]+=1;
			}
			curDimention=agentValueIndexes.length-1;
			dataIndex++;
		}
		
		MulitiDimentionalData multiDimentionalData=new MulitiDimentionalData(data, dimentionLengths, new int[]{}, dimentionNames);
		multiDimentionalData=multiDimentionalData.reductDimention(this.id+"", MulitiDimentionalData.REDUCT_DIMENTION_WITH_MIN);
		return multiDimentionalData;
	}
	
	private void disposeUtilMessage(Message msg)
	{
		if(this.isRootAgent()==true)
		{
			sendValueMessage();
		}
		allChildrenUtils.put(msg.getIdSender(), (MulitiDimentionalData) msg.getValue());
		if(allChildrenUtils.size()>=allChildren.length)
		{
			//所有子节点(包括伪子节点)的UtilMessage都以收集完毕，
			//则可以进行针对本节点的加和和降维，将最终得到的UtilMessage再往父节点发送
		}
	}
	
	private void sendValueMessage()
	{
		if(this.isLeafAgent()==true)
		{
			return;
		}
	}
	
	private void disposeValueMessage(Message msg)
	{
		
	}

}
