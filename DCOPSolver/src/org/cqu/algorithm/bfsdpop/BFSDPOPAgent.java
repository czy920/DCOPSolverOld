package org.cqu.algorithm.bfsdpop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cqu.algorithm.dpop.Dimension;
import org.cqu.algorithm.dpop.MultiDimensionData;
import org.cqu.algorithm.dpop.ReductDimensionResult;
import org.cqu.common.constant.keys.IAKeys;
import org.cqu.core.TreeNodeAgent;
import org.cqu.core.control.Message;
import org.cqu.core.control.MessageSink;
import org.cqu.core.control.MessageTypes;
import org.cqu.statistics.StatisticsListener;
import org.cqu.utility.CollectionUtil;


public class BFSDPOPAgent extends TreeNodeAgent{
	
	public BFSDPOPAgent(int id, String name, int[] domain, int[] neighbours,
			MessageSink msgSink, StatisticsListener statisticsListener,
			int capacity, boolean[] consideredConstraints) {
		super(id, name, domain, neighbours, msgSink, statisticsListener, capacity);
		// TODO Auto-generated constructor stub
        this.consideredConstraints=consideredConstraints;
		
		disposedChildrenCount=0;
		totalCost=0;
		reductDimensionResultIndexList=new HashMap<Integer, int[]>();
		dimensionLists=new HashMap<Integer, List<Dimension>>();
		
		utilMsgSizes=new ArrayList<Integer>();
	}

	private int disposedChildrenCount;
	private MultiDimensionData rawMDData;
	
	private List<Integer> utilMsgSizes;
	private int totalCost;
	private boolean[] isCrossNeighbours;
	private boolean[] consideredConstraints;
	
	private Map<Integer, int[]> reductDimensionResultIndexList;
	private Map<Integer, List<Dimension>> dimensionLists;
	
	
	
	@Override
	public void initProcess() {
		// TODO Auto-generated method stub
		this.isCrossNeighbours=new boolean[this.neighbours.length];
		for(int i=0;i<this.isCrossNeighbours.length;i++)
		{
			if(this.neighbours[i]==this.parent||CollectionUtil.indexOf(this.children, this.neighbours[i])!=-1)
			{
				this.isCrossNeighbours[i]=false;
			}else
			{
				this.isCrossNeighbours[i]=true;
			}
		}
		
		if(this.isRootAgent()==false)
		{
			rawMDData=this.computeLocalUtils();
		}
		if(this.isLeafAgent()==true)
		{
			if(rawMDData.isReductable(this.id+"")==true)
			{
				ReductDimensionResult result=rawMDData.reductDimension(this.id+"", ReductDimensionResult.REDUCT_DIMENSION_WITH_MIN);
				rawMDData=result.getMdData();
				dimensionLists.put(this.id, rawMDData.getDimensions());
				reductDimensionResultIndexList.put(this.id, result.getResultIndex());
			}
			sendUtilMessage(rawMDData);
		}
	}
	
	@Override
	protected void processEnded() {
		// TODO Auto-generated method stub
		Map<String, Object> result=new HashMap<String, Object>();
		result.put(IAKeys.ID, this.id);
		result.put(IAKeys.NAME, this.name);
		result.put(IAKeys.VALUE, this.domain[valueIndex]);
		if(this.isRootAgent()==true)
		{
			result.put(IAKeys.TOTALCOST, this.totalCost);
		}
		result.put(IAKeys.UTIL_MESSAGE_SIZES, this.utilMsgSizes);
		
		this.statisticsListener.newStatistics(name, "result", result);
		
		System.out.println("Agent "+this.name+" stopped!");
	}

	/*@SuppressWarnings("unchecked")
	@Override
	public Object printResults(List<Map<String, Object>> results) {
		// TODO Auto-generated method stub
		int totalCost=-1;
		Map<String, Object> result;
		List<Integer> sizeList=new ArrayList<Integer>();
		for(int i=0;i<results.size();i++)
		{
			result=results.get(i);
			int id_=(Integer) result.get(DPOPAgent.ID);
			String name_=(String) result.get(DPOPAgent.NAME);
			int value_=(Integer) result.get(DPOPAgent.VALUE);
			if(result.containsKey(DPOPAgent.KEY_TOTAL_COST))
			{
				totalCost=(Integer) result.get(DPOPAgent.KEY_TOTAL_COST);
			}
			sizeList.addAll((List<Integer>)result.get(BFSDPOPAgent.KEY_UTIL_MESSAGE_SIZES));
			
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
		
		ret.totalTime=2*(this.msgMailer.getAgentManager().getTreeHeight()-1)*Settings.settings.getCommunicationTimeInDPOPs();
		
		return ret;
	}

	@Override
	public String easyMessageContent(Message msg, Agent sender, Agent receiver) {
		// TODO Auto-generated method stub
		return "from "+sender.getName()+" to "+receiver.getName()+" type "+DPOPAgent.messageContent(msg);
	}*/
	
	public static String messageContent(Message msg)
	{
		switch (msg.getType()) {
		case MessageTypes.VALUE:
		{
			int valueIndex=(Integer) msg.getValue();
			return "value["+valueIndex+"]";
		}
		case MessageTypes.UTILITY:
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
		if(type==MessageTypes.VALUE)
		{
			disposeValueMessage(msg);
		}else if(type==MessageTypes.UTILITY)
		{
			utilMsgSizes.add(((MultiDimensionData) msg.getValue()).size());
			
			disposeUtilMessage(msg);
		}
	}
	
	private void sendUtilMessage(MultiDimensionData multiDimentionalData)
	{
		if(this.isRootAgent()==true)
		{
			return;
		}
		Message utilMsg=new Message(this.id, this.parent, MessageTypes.UTILITY, multiDimentionalData);
		this.sendMessage(utilMsg);
	}
	
	private MultiDimensionData computeLocalUtils()
	{
		int dataLength=1;
		List<Dimension> dimensions=new ArrayList<Dimension>();
		List<Integer> relatedNodes=new ArrayList<Integer>();
		for(int i=0;i<neighbours.length;i++)
		{
			if(this.isCrossNeighbours[i]==true)
			{
				int crossNeighbourId=neighbours[i];
				//for crossing constraint edges
				if(this.consideredConstraints[i]==true)
				{
					int dimensionSize=neighbourDomains.get(crossNeighbourId).length;
					dimensions.add(new Dimension(crossNeighbourId+"", dimensionSize, neighbourLayers.get(crossNeighbourId), Integer.MAX_VALUE, 1));
					dataLength=dataLength*dimensionSize;
					relatedNodes.add(crossNeighbourId);
				}
			}
		}
		{
			int parentId=this.parent;
			int dimensionSize=neighbourDomains.get(parentId).length;
			dimensions.add(new Dimension(parentId+"", dimensionSize, neighbourLayers.get(parentId), Integer.MAX_VALUE, 1));
			dataLength=dataLength*dimensionSize;
			relatedNodes.add(parentId);
		}
		{
			dimensions.add(new Dimension(this.id+"", this.domain.length, this.layer, this.neighbours.length, relatedNodes.size()));
			dataLength=dataLength*this.domain.length;
		}
		
		//set data
		int[] agentValueIndexes=new int[relatedNodes.size()+1];
		int[] data=new int[dataLength];
		int dataIndex=0;
		int curDimention=agentValueIndexes.length-1;
		while(dataIndex<data.length)
		{
			int costSum=0;
			for(int i=0;i<relatedNodes.size();i++)
			{
				//原始数据中id小的为行，id大的为列
				if(this.id<relatedNodes.get(i))
				{
					costSum+=this.constraintCosts.get(relatedNodes.get(i))[agentValueIndexes[agentValueIndexes.length-1]][agentValueIndexes[i]];
				}else
				{
					costSum+=this.constraintCosts.get(relatedNodes.get(i))[agentValueIndexes[i]][agentValueIndexes[agentValueIndexes.length-1]];
				}
			}
			data[dataIndex]=costSum;
			
			agentValueIndexes[curDimention]+=1;
			while(agentValueIndexes[curDimention]>=dimensions.get(curDimention).getSize())
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
		
		return new MultiDimensionData(dimensions, data);
	}
	
	private void disposeUtilMessage(Message msg)
	{
		if(this.isRootAgent()==true&&rawMDData==null)
		{
			rawMDData=(MultiDimensionData) msg.getValue();
		}else
		{
			rawMDData=rawMDData.mergeDimension((MultiDimensionData) msg.getValue());
		}
		rawMDData.getDimensions().get(rawMDData.indexOf(this.id+"")).setConstraintCountTotal(this.neighbours.length);
		
		disposedChildrenCount++;
		if(disposedChildrenCount>=this.children.length)
		{
			//所有子节点(包括伪子节点)的UtilMessage都已收集完毕，
			//则可以进行针对本节点的降维，将最终得到的UtilMessage再往父节点发送
			List<Dimension> dimens=rawMDData.getDimensions();
			for(int i=dimens.size()-1;i>-1;i--)
			{
				Dimension dimen=dimens.get(i);
				if(rawMDData.isReductable(dimen.getName())==true)
				{
					int agentId=Integer.parseInt(dimen.getName());
					ReductDimensionResult result=rawMDData.reductDimension(dimen.getName(), ReductDimensionResult.REDUCT_DIMENSION_WITH_MIN);
					reductDimensionResultIndexList.put(agentId, result.getResultIndex());
					rawMDData=result.getMdData();
					dimensionLists.put(agentId, rawMDData.getDimensions());
				}
			}
			
			if(this.isRootAgent()==true)
			{
				this.totalCost=rawMDData.getData()[0];
				this.valueIndex=reductDimensionResultIndexList.get(this.id)[0];
				Map<Integer, Integer> valueIndexes=new HashMap<Integer, Integer>();
				valueIndexes.put(this.id, this.valueIndex);
				Integer[] keys=new ListSizeComparator<Dimension>(dimensionLists).sort();
				if(dimensionLists.size()>1)
				{
					for(int i=1;i<keys.length;i++)
					{
						this.calValueIndex(keys[i], dimensionLists.get(keys[i]), reductDimensionResultIndexList.get(keys[i]), valueIndexes);
					}
				}
				
				sendValueMessage(valueIndexes);
				
				this.stop();
			}else
			{
				this.sendUtilMessage(rawMDData);
			}
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
			Message valueMsg=new Message(this.id, this.children[i], MessageTypes.VALUE, CollectionUtil.copy(valueIndexes));
			this.sendMessage(valueMsg);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void disposeValueMessage(Message msg)
	{
		Map<Integer, Integer> valueIndexes=(Map<Integer, Integer>) msg.getValue();

		Integer[] keys=new ListSizeComparator<Dimension>(dimensionLists).sort();
		for(int i=0;i<keys.length;i++)
		{
			this.calValueIndex(keys[i], dimensionLists.get(keys[i]), reductDimensionResultIndexList.get(keys[i]), valueIndexes);
		}
		this.valueIndex=valueIndexes.get(this.id);
		
		valueIndexes.remove(this.parent);
		this.sendValueMessage(valueIndexes);
		
		this.stop();
	}
	
	private Map<Integer, Integer> calValueIndex(int agentId, List<Dimension> dimensions, int[] reductDimensionResultIndexes, Map<Integer, Integer> otherDimensionValueIndexes)
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
			index+=otherDimensionValueIndexes.get(Integer.parseInt(dimensions.get(i).getName()))*periods[i];
		}
		otherDimensionValueIndexes.put(agentId, reductDimensionResultIndexes[index]);
		return otherDimensionValueIndexes;
	}
}
