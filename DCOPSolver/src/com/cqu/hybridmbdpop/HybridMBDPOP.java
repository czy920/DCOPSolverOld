package com.cqu.hybridmbdpop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.core.Agent;
import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.core.ResultDPOP;
import com.cqu.dpop.Dimension;
import com.cqu.dpop.MultiDimensionData;
import com.cqu.dpop.ReductDimensionResult;
import com.cqu.settings.Settings;
import com.cqu.util.CollectionUtil;
import com.cqu.util.FormatUtil;
import com.cqu.util.StatisticUtil;

public class HybridMBDPOP extends Agent{
	
	public final static int TYPE_VALUE_MESSAGE=0;
	public final static int TYPE_UTIL_MESSAGE=1;
	public final static int TYPE_NEXTITERATION_MESSAGE=2;
	public final static int TYPE_ITERATIONOVER_MESSAGE=3;
	
	public final static String KEY_TOTAL_COST="KEY_TOTAL_COST";
	public final static String KEY_UTIL_MESSAGE_SIZES="KEY_UTIL_MESSAGE_SIZES";
	
	private List<Integer> utilMsgSizes;
	private int totalCost;
	
	private boolean isSearchingPolicy;
	private Map<Integer, Boolean> isNeighborSearchingPolicy;
	
	private Map<Integer, MultiDimensionData> localMDDatas;
	private Map<Integer, MultiDimensionData> pseudoChildrenMDDatas;
	private Map<Integer, MultiDimensionData> receivedMDDatas;
	
	private ContextWrapped contextWrapped;
	private int bestCost;
	private ContextWrapped bestContext;
	private boolean nextIteration;
	private MultiDimensionData mdDataToSend;
	private boolean iterationOver;
	private boolean[] neighbourPolicy;
	
	private int[] reductDimensionResultIndexes;
	private List<Dimension> dimensions;
	{
		//表示消息不丢失
		QUEUE_CAPACITY=-1;
	}
	
	public HybridMBDPOP(int id, String name, int level, int[] domain, boolean isSearchingPolicy, boolean[] neighbourPolicy, ContextWrapped contextWrapped) {
		super(id, name, level, domain);
		// TODO Auto-generated constructor stub
		totalCost=0;
		
		this.isSearchingPolicy=isSearchingPolicy;
		this.isNeighborSearchingPolicy=new HashMap<Integer, Boolean>();
		
		this.neighbourPolicy=neighbourPolicy;
		this.contextWrapped=contextWrapped;
		this.bestCost=Integer.MAX_VALUE;
		this.bestContext=new ContextWrapped(this.contextWrapped);
		this.nextIteration=true;
		this.mdDataToSend=null;
		this.iterationOver=false;
		
		utilMsgSizes=new ArrayList<Integer>();
		localMDDatas=new HashMap<Integer, MultiDimensionData>();
		receivedMDDatas=new HashMap<Integer, MultiDimensionData>();
		pseudoChildrenMDDatas=new HashMap<Integer, MultiDimensionData>();
	}
	
	@Override
	protected void initRun() {
		// TODO Auto-generated method stub
		super.initRun();
		
		for(int i=0;i<this.neighbours.length;i++)
		{
			this.isNeighborSearchingPolicy.put(this.neighbours[i], neighbourPolicy[i]);
		}
		if(this.pseudoChildren==null)
		{
			this.pseudoChildren=new int[0];
		}
		
		computeLocalUtils();
		if(this.isLeafAgent()==true)
		{
			startFromLeaf();
		}
	}
	
	private void startFromLeaf()
	{
		this.nextIteration=false;
		this.mdDataToSend=null;
		if(this.isSearchingPolicy==true)
		{
			sendUtilMessage(localMDDatas.get(parent).shrinkDimension(this.id+"", this.contextWrapped.getValueIndex(this.id)));
		}else
		{
			MultiDimensionData mdData=localMDDatas.get(this.parent);
			for(Integer key : localMDDatas.keySet())
			{
				if(key.equals(this.parent)==false)
				{
					MultiDimensionData mdDataTemp=localMDDatas.get(key);
					if(isNeighborSearchingPolicy.get(key)==true)
					{
						mdDataTemp=mdDataTemp.shrinkDimension(key+"", this.contextWrapped.getValueIndex(key));
					}
					mdData=mdData.mergeDimension(mdDataTemp);
				}
			}
			ReductDimensionResult result=mdData.reductDimension(this.id+"", ReductDimensionResult.REDUCT_DIMENSION_WITH_MIN);
			if(iterationOver==true)
			{
				this.dimensions=result.getMdData().getDimensions();
				this.reductDimensionResultIndexes=result.getResultIndex();
			}
			mdData=result.getMdData();
			sendUtilMessage(mdData);
		}
	}
	
	private void computeLocalUtils()
	{
		for(int i=0;i<allParents.length;i++)
		{
			List<Dimension> dimensions=new ArrayList<Dimension>();
			
			int parentId=allParents[i];
			int row=neighbourDomains.get(parentId).length;
			int col=this.domain.length;
			dimensions.add(new Dimension(parentId+"", row, this.neighbourLevels.get(parentId)));
			dimensions.add(new Dimension(this.id+"", this.domain.length, this.level));
			
			int[] data=new int[row*col];
			int[][] costs=this.constraintCosts.get(parentId);
			//原始数据中id小的为行，id大的为列
			if(this.id<parentId)
			{
				for(int j=0;j<row;j++)
				{
					for(int k=0;k<col;k++)
					{
						data[j*col+k]=costs[k][j];
					}
				}
			}else
			{
				for(int j=0;j<row;j++)
				{
					for(int k=0;k<col;k++)
					{
						data[j*col+k]=costs[j][k];
					}
				}
			}
			
			localMDDatas.put(parentId, new MultiDimensionData(dimensions, data));
		}
		
		for(int i=0;i<pseudoChildren.length;i++)
		{
			List<Dimension> dimensions=new ArrayList<Dimension>();
			
			int pseudoChildId=pseudoChildren[i];
			int row=this.domain.length;
			int col=neighbourDomains.get(pseudoChildId).length;
			dimensions.add(new Dimension(this.id+"", this.domain.length, this.level));
			dimensions.add(new Dimension(pseudoChildId+"", row, this.neighbourLevels.get(pseudoChildId)));
			
			int[] data=new int[row*col];
			int[][] costs=this.constraintCosts.get(pseudoChildId);
			//原始数据中id小的为行，id大的为列
			if(this.id<pseudoChildId)
			{
				for(int j=0;j<row;j++)
				{
					for(int k=0;k<col;k++)
					{
						data[j*col+k]=costs[j][k];
					}
				}
			}else
			{
				for(int j=0;j<row;j++)
				{
					for(int k=0;k<col;k++)
					{
						data[j*col+k]=costs[k][j];
					}
				}
			}
			
			pseudoChildrenMDDatas.put(pseudoChildId, new MultiDimensionData(dimensions, data));
		}
	}
	
	@Override
	protected void runFinished() {
		// TODO Auto-generated method stub
		super.runFinished();
		
		Map<String, Object> result=new HashMap<String, Object>();
		result.put(KEY_ID, this.id);
		result.put(KEY_NAME, this.name);
		result.put(KEY_VALUE, this.domain[valueIndex]);
		if(this.isRootAgent()==true)
		{
			result.put(HybridMBDPOP.KEY_TOTAL_COST, this.totalCost);
		}
		result.put(KEY_UTIL_MESSAGE_SIZES, this.utilMsgSizes);
		
		this.msgMailer.setResult(result);
		
		System.out.println("Agent "+this.name+" stopped!");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object printResults(List<Map<String, Object>> results) {
		// TODO Auto-generated method stub
		int totalCost=-1;
		Map<String, Object> result;
		List<Integer> sizeList=new ArrayList<Integer>();
		for(int i=0;i<results.size();i++)
		{
			result=results.get(i);
			int id_=(Integer) result.get(KEY_ID);
			String name_=(String) result.get(KEY_NAME);
			int value_=(Integer) result.get(KEY_VALUE);
			if(result.containsKey(HybridMBDPOP.KEY_TOTAL_COST))
			{
				totalCost=(Integer) result.get(KEY_TOTAL_COST);
			}
			sizeList.addAll((List<Integer>)result.get(KEY_UTIL_MESSAGE_SIZES));
			
			String displayStr="Agent "+name_+": id="+id_+" value="+value_;
			System.out.println(displayStr);
		}
		
		Integer[] sizeArr=new Integer[sizeList.size()];
		sizeList.toArray(sizeArr);
		int[] minMaxAvg=StatisticUtil.minMaxAvg(CollectionUtil.toInt(sizeArr));
		System.out.println("utilMsgCount: "+sizeArr.length+" utilMsgSizeMin: "+FormatUtil.formatSize(minMaxAvg[0])+" utilMsgSizeMax: "+
		FormatUtil.formatSize(minMaxAvg[2])+" utilMsgSizeAvg: "+FormatUtil.formatSize(minMaxAvg[4]));
		
		System.out.println("totalCost: "+Infinity.infinityEasy(totalCost));
		
		ResultDPOP ret=new ResultDPOP();
		ret.totalCost=totalCost;
		ret.utilMsgCount=sizeArr.length;
		ret.utilMsgSizeMin=minMaxAvg[0];
		ret.utilMsgSizeMax=minMaxAvg[2];
		ret.utilMsgSizeAvg=minMaxAvg[4];
		
		ret.otherResults.put("ContextSize", this.contextWrapped.size());
		System.out.println("ContextSize: "+this.contextWrapped.size());
		
		ret.totalTime=2*(this.msgMailer.getAgentManager().getTreeHeight()-1)*Settings.settings.getCommunicationTimeInDPOPs();
		return ret;
	}

	@Override
	public String easyMessageContent(Message msg, Agent sender, Agent receiver) {
		// TODO Auto-generated method stub
		return "from "+sender.getName()+" to "+receiver.getName()+" type "+messageContent(msg);
	}
	
	public static String messageContent(Message msg)
	{
		switch (msg.getType()) {
		case TYPE_VALUE_MESSAGE:
		{
			int valueIndex=(Integer) msg.getValue();
			return "value["+valueIndex+"]";
		}
		case TYPE_UTIL_MESSAGE:
		{
			MultiDimensionData mdData=(MultiDimensionData) msg.getValue();
			return "util[dimensions="+mdData.getDimensions().toString()+"]";
		}
		default:
			return "unknown";
		}
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
			utilMsgSizes.add(((UtilMessage) msg).getMdData().size());
			
			disposeUtilMessage(msg);
		}else if(type==TYPE_NEXTITERATION_MESSAGE)
		{
			disposeNextIterationMessage(msg);
		}else if(type==TYPE_ITERATIONOVER_MESSAGE)
		{
			disposeIterationOverMessage(msg);
		}
	}

	@Override
	protected void messageLost(Message msg) {
		// TODO Auto-generated method stub
		System.out.println("Error occurs because no message can be lost!");
	}
	
	private void sendUtilMessage(MultiDimensionData multiDimentionalData)
	{
		if(this.isRootAgent()==true)
		{
			return;
		}
		Message utilMsg=new UtilMessage(this.id, this.parent, TYPE_UTIL_MESSAGE, multiDimentionalData, this.contextWrapped);
		this.sendMessage(utilMsg);
	}
	
	private void disposeNextIterationMessage(Message msg)
	{
		//System.out.println("disposeNextIterationMessage id="+this.id);
		this.nextIteration=true;
		if(this.isLeafAgent()==true)
		{
			if(this.iterationOver==false)
			{
				if(this.contextWrapped.next()==true)
				{
					startFromLeaf();
				}
			}
		}else
		{
			if(this.mdDataToSend!=null)
			{
				this.nextIteration=false;
				MultiDimensionData mdData=this.mdDataToSend;
				this.mdDataToSend=null;
				sendUtilMessage(mdData);
				sendNextIterationMessage();
			}
		}
	}
	
	private void disposeIterationOverMessage(Message msg)
	{
		this.contextWrapped=new ContextWrapped((ContextWrapped) msg.getValue());
		this.iterationOver=true;
		if(this.isLeafAgent()==true)
		{
			startFromLeaf();
		}else
		{
			sendIterationOverMessage();
		}
	}
	
	private void disposeUtilMessage(Message msg)
	{
		UtilMessage utilMsg=(UtilMessage) msg;
		receivedMDDatas.put(msg.getIdSender(), utilMsg.getMdData());
		if(receivedMDDatas.size()>=this.children.length)
		{
			this.contextWrapped=new ContextWrapped(utilMsg.getContext());
			MultiDimensionData mdData=null;
			for(Integer id : receivedMDDatas.keySet())
			{
				if(mdData==null)
				{
					mdData=receivedMDDatas.get(id);
				}else
				{
					mdData=mdData.mergeDimension(receivedMDDatas.get(id));
				}
			}
			//pseudoChildren
			for(int i=0;i<this.pseudoChildren.length;i++)
			{
				if(this.isNeighborSearchingPolicy.get(this.pseudoChildren[i])==true)
				{
					mdData=mdData.mergeDimension(this.pseudoChildrenMDDatas.get(this.pseudoChildren[i]).shrinkDimension(this.pseudoChildren[i]+"", this.contextWrapped.getValueIndex(this.pseudoChildren[i])));
				}
			}
			//allParents
			if(this.isSearchingPolicy==true)
			{
				//root节点不会执行此段代码
				mdData=mdData.mergeDimension(localMDDatas.get(parent));
				mdData=mdData.shrinkDimension(this.id+"", this.contextWrapped.getValueIndex(this.id));
			}else
			{
				for(Integer key : localMDDatas.keySet())
				{
					MultiDimensionData mdDataTemp=localMDDatas.get(key);
					if(isNeighborSearchingPolicy.get(key)==true)
					{
						mdDataTemp=mdDataTemp.shrinkDimension(key+"", this.contextWrapped.getValueIndex(key));
					}
					mdData=mdData.mergeDimension(mdDataTemp);
				}
				
				ReductDimensionResult result=mdData.reductDimension(this.id+"", ReductDimensionResult.REDUCT_DIMENSION_WITH_MIN);
				if(iterationOver==true)
				{
					this.dimensions=result.getMdData().getDimensions();
					this.reductDimensionResultIndexes=result.getResultIndex();
				}
				mdData=result.getMdData();
				if(this.isRootAgent()==true&&iterationOver==true)
				{
					this.totalCost=result.getMdData().getData()[0];
					this.valueIndex=this.reductDimensionResultIndexes[0];
					
					Map<Integer, Integer> valueIndexes=new HashMap<Integer, Integer>();
					for(Integer key : this.bestContext.keySet())
					{
						valueIndexes.put(key, this.bestContext.getValueIndex(key));
					}
					valueIndexes.put(this.id, this.valueIndex);
					sendValueMessage(valueIndexes);
					
					this.stopRunning();
					receivedMDDatas=new HashMap<Integer, MultiDimensionData>();
					return;
				}
			}
			if(this.isRootAgent()==true)
			{
				if(bestCost>mdData.getData()[0])
				{
					bestCost=mdData.getData()[0];
					bestContext=new ContextWrapped(this.contextWrapped);
				}
				if(this.contextWrapped.iterationOver()==true)
				{
					//iteration over
					this.iterationOver=true;
					this.contextWrapped=bestContext;
					receivedMDDatas=new HashMap<Integer, MultiDimensionData>();
					sendIterationOverMessage();
					sendNextIterationMessage();
					return;
				}else
				{
					sendNextIterationMessage();
				}
			}else
			{
				if(this.nextIteration==true)
				{
					this.nextIteration=false;
					this.mdDataToSend=null;
					sendUtilMessage(mdData);
					sendNextIterationMessage();
				}else
				{
					this.mdDataToSend=mdData;
				}
			}
			
			receivedMDDatas=new HashMap<Integer, MultiDimensionData>();
		}
	}
	
	private void sendValueMessage(Map<Integer, Integer> valueIndexes)
	{
		if(this.isLeafAgent()==true)
		{
			return;
		}
		//only send valueIndexes to children
		for(int i=0;i<this.children.length;i++)
		{
			Message valueMsg=new Message(this.id, this.children[i], TYPE_VALUE_MESSAGE, CollectionUtil.copy(valueIndexes));
			this.sendMessage(valueMsg);
		}
	}
	
	private void sendNextIterationMessage()
	{
		if(this.isLeafAgent()==true)
		{
			return;
		}
		for(int i=0;i<this.children.length;i++)
		{
			Message nextIterationMsg=new Message(this.id, this.children[i], TYPE_NEXTITERATION_MESSAGE, null);
			this.sendMessage(nextIterationMsg);
		}
	}
	
	private void sendIterationOverMessage()
	{
		if(this.isLeafAgent()==true)
		{
			return;
		}
		for(int i=0;i<this.children.length;i++)
		{
			Message nextIterationMsg=new Message(this.id, this.children[i], TYPE_ITERATIONOVER_MESSAGE, this.contextWrapped);
			this.sendMessage(nextIterationMsg);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void disposeValueMessage(Message msg)
	{
		Map<Integer, Integer> valueIndexes=(Map<Integer, Integer>) msg.getValue();
		if(valueIndexes.containsKey(this.id)==false)
		{
			int[] periods=new int[dimensions.size()];
			for(int i=0;i<dimensions.size();i++)
			{
				int temp=1;
				for(int j=i+1;j<dimensions.size();j++)
				{
					temp*=dimensions.get(j).getSize();
				}
				periods[i]=temp;
			}
			int index=0;
			for(int i=0;i<periods.length;i++)
			{
				index+=valueIndexes.get(Integer.parseInt(dimensions.get(i).getName()))*periods[i];
			}
			this.valueIndex=this.reductDimensionResultIndexes[index];
			
			valueIndexes.put(this.id, this.valueIndex);
		}else
		{
			this.valueIndex=valueIndexes.get(this.id);
		}
		this.sendValueMessage(valueIndexes);
		this.stopRunning();
	}
}
