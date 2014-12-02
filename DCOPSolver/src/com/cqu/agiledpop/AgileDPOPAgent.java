package com.cqu.agiledpop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.core.Agent;
import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.core.ResultDPOP;
import com.cqu.dpop.DPOPAgent;
import com.cqu.dpop.Dimension;
import com.cqu.dpop.MultiDimensionData;
import com.cqu.dpop.ReductDimensionResult;
import com.cqu.util.CollectionUtil;
import com.cqu.util.FormatUtil;
import com.cqu.util.StatisticUtil;

public class AgileDPOPAgent extends Agent{

	public final static int TYPE_VALUE_MESSAGE=0;
	public final static int TYPE_UTIL_MESSAGE=1;
	
	public final static String KEY_TOTAL_COST="KEY_TOTAL_COST";
	public final static String KEY_UTIL_MESSAGE_SIZES="KEY_UTIL_MESSAGE_SIZES";
	
	public static int MAX_DIEMNSION_FEASIBLE=2;
	
	private Integer[] parentLevels;
	private int disposedChildrenCount;
	
	private List<MultiDimensionData> localMDDatas;
	private List<MultiDimensionData> receivedMDDatas;
	private List<MultiDimensionData> allMDDatas;
	
	private Map<String, List<Dimension>> reductDimensionDimensionListMap;
	private Map<String, int[]> reductDimensionResultIndexMap;
	
	private List<int[]> reductDimensionsResult;
	private List<Dimension> reductDimensionsResultDimensions;
	
	private List<Integer> utilMsgSizes;
	
	private int totalCost;
	
	{
		//表示消息不丢失
		QUEUE_CAPACITY=-1;
	}
	
	public AgileDPOPAgent(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
		// TODO Auto-generated constructor stub
		disposedChildrenCount=0;
		totalCost=0;
		
		localMDDatas=new ArrayList<MultiDimensionData>();
		receivedMDDatas=new ArrayList<MultiDimensionData>();
		allMDDatas=new ArrayList<MultiDimensionData>();
		
		reductDimensionsResult=new ArrayList<int[]>();
		
		utilMsgSizes=new ArrayList<Integer>();
	}
	
	@Override
	protected void initRun() {
		// TODO Auto-generated method stub
		super.initRun();
		
		parentLevels=new Integer[allParents.length];
		for(int i=0;i<allParents.length;i++)
		{
			parentLevels[i]=neighbourLevels.get(allParents[i]);
		}
		
		if(this.isRootAgent()==false)
		{
			localMDDatas=this.computeLocalUtils();
		}
		if(this.isLeafAgent()==true)
		{
			this.mergeLocalAndReceivedMDData();
			mergeAndReduct(this.allMDDatas);
			
			sendUtilMessage();
		}
	}
	
	private void mergeLocalAndReceivedMDData()
	{
		allMDDatas.addAll(receivedMDDatas);
		allMDDatas.addAll(localMDDatas);
	}
	
	private void allMDDataReceived()
	{
		List<Dimension> receivedMDDatasDimensions=this.fakeMergedDimensions(receivedMDDatas);
		List<Dimension> localMDDatasDimensions=this.fakeMergedDimensions(localMDDatas);
		if(receivedMDDatasDimensions.get(0).getName().equals(this.id+"")==true&&
				localMDDatasDimensions.get(localMDDatasDimensions.size()-1).getName().equals(this.id+"")==true)
		{
			//表示this.level以下节点均可降维
			List<Dimension> dimensionsNew=new ArrayList<Dimension>();
			dimensionsNew.add(new Dimension(receivedMDDatasDimensions.get(0)));
			int[] dataNew=new int[this.domain.length];
			int[] dimensionValueIndexes=new int[receivedMDDatasDimensions.size()];
			
			//compute periods for each mddata
			List<int[]> periodsList=new ArrayList<int[]>();
			for(int i=0;i<receivedMDDatas.size();i++)
			{
				MultiDimensionData tempMDData=receivedMDDatas.get(i);
				int[] periods=new int[receivedMDDatasDimensions.size()];
				for(int j=0;j<receivedMDDatasDimensions.size();j++)
				{
					int index=tempMDData.indexOf(receivedMDDatasDimensions.get(i).getName());
					if(index==-1)
					{
						periods[j]=0;
					}else
					{
						int temp=1;
						for(int k=index+1;k<tempMDData.getDimensions().size();k++)
						{
							temp*=tempMDData.getDimensions().get(k).getSize();
						}
						periods[j]=temp;
					}
				}
				periodsList.add(periods);
			}
			
			int totalSize=1;
			for(int i=0;i<receivedMDDatasDimensions.size();i++)
			{
				totalSize=totalSize*receivedMDDatasDimensions.get(i).getSize();
			}
			
			int minCostSum=Integer.MAX_VALUE;
			int curDimension=dimensionValueIndexes.length-1;
			int[] receivedMDDatasIndexes=new int[receivedMDDatas.size()];
			Arrays.fill(receivedMDDatasIndexes, 0);
			int dataIndex=0;
			int[] minCostDimensionValueIndexes=new int[dimensionValueIndexes.length-1];
			while(dataIndex<totalSize)
			{
				int tempCostSum=0;
				for(int i=0;i<receivedMDDatas.size();i++)
				{
					tempCostSum+=receivedMDDatas.get(i).getData()[receivedMDDatasIndexes[i]];
				}
				if(minCostSum>tempCostSum)
				{
					minCostSum=tempCostSum;
					for(int i=1;i<dimensionValueIndexes.length;i++)
					{
						minCostDimensionValueIndexes[i-1]=dimensionValueIndexes[i];
					}
				}
				
				dimensionValueIndexes[curDimension]+=1;
				for(int i=0;i<receivedMDDatas.size();i++)
				{
					receivedMDDatasIndexes[i]+=periodsList.get(i)[curDimension];
				}
				while(dimensionValueIndexes[curDimension]>=receivedMDDatasDimensions.get(curDimension).getSize())
				{
					for(int i=0;i<receivedMDDatas.size();i++)
					{
						receivedMDDatasIndexes[i]-=periodsList.get(i)[curDimension]*dimensionValueIndexes[curDimension];
					}
					dimensionValueIndexes[curDimension]=0;
					
					curDimension-=1;
					if(curDimension==-1)
					{
						break;
					}
					if(curDimension==0)
					{
						//表示本agent value index 变化
						dataNew[dimensionValueIndexes[curDimension]]=minCostSum;
						reductDimensionsResult.add(minCostDimensionValueIndexes);
						minCostDimensionValueIndexes=new int[dimensionValueIndexes.length-1];
					}
					dimensionValueIndexes[curDimension]+=1;
					for(int i=0;i<receivedMDDatas.size();i++)
					{
						receivedMDDatasIndexes[i]+=periodsList.get(i)[curDimension];
					}
				}
				curDimension=dimensionValueIndexes.length-1;
				dataIndex++;
			}
			
			receivedMDDatas=new ArrayList<MultiDimensionData>();
			receivedMDDatas.add(new MultiDimensionData(dimensionsNew, dataNew));
			
			receivedMDDatasDimensions.remove(0);
			reductDimensionsResultDimensions=receivedMDDatasDimensions;
		}
		//先与localMDData融合，再尽量降维
		this.mergeLocalAndReceivedMDData();
		mergeAndReduct(this.allMDDatas);
	}
	
	private void mergeAndReduct(List<MultiDimensionData> allDatas)
	{
		boolean reductable=true;
		while(reductable==true)
		{
			List<Dimension> allDatasDimensions=this.fakeMergedDimensions(allDatas);
			int i=0;
			for(i=allDatasDimensions.size()-1;i>-1;i--)
			{
				if(this.tryReduct(allDatas, allDatasDimensions.get(i).getName())==true)
				{
					break;
				}
			}
			if(i<0)
			{
				reductable=false;
			}
		}
	}
	
	private boolean tryReduct(List<MultiDimensionData> allDatas, String dimensionName)
	{
		List<MultiDimensionData> involvedDatas=new ArrayList<MultiDimensionData>();
		for(int i=0;i<allDatas.size();i++)
		{
			if(MultiDimensionData.indexOf(allDatas.get(i).getDimensions(), dimensionName)!=-1)
			{
				involvedDatas.add(allDatas.get(i));
			}
		}
		List<Dimension> involvedDatasDimensions=this.fakeMergedDimensions(involvedDatas);
		if(involvedDatasDimensions.size()>MAX_DIEMNSION_FEASIBLE)
		{
			return false;
		}else
		{
			MultiDimensionData mergedData=involvedDatas.get(0);
			for(int i=1;i<involvedDatas.size();i++)
			{
				mergedData=mergedData.mergeDimension(involvedDatas.get(i));
			}
			ReductDimensionResult result=mergedData.reductDimension(dimensionName, ReductDimensionResult.REDUCT_DIMENSION_WITH_MIN);
			reductDimensionDimensionListMap.put(dimensionName, result.getMdData().getDimensions());
			reductDimensionResultIndexMap.put(dimensionName, result.getResultIndex());
			
			for(int i=0;i<involvedDatas.size();i++)
			{
				allDatas.remove(involvedDatas.get(i));
			}
			allDatas.add(result.getMdData());
			
			return true;
		}
	}
	
	private List<Dimension> fakeMergedDimensions(List<MultiDimensionData> datas)
	{
		MultiDimensionData tempData=datas.get(0);
		for(int i=1;i<datas.size();i++)
		{
			tempData=tempData.testMergeDimension(datas.get(i));
		}
		return tempData.getDimensions();
	}
	
	@Override
	protected void runFinished() {
		// TODO Auto-generated method stub
		super.runFinished();
		
		Map<String, Object> result=new HashMap<String, Object>();
		result.put(DPOPAgent.KEY_ID, this.id);
		result.put(DPOPAgent.KEY_NAME, this.name);
		result.put(DPOPAgent.KEY_VALUE, this.domain[valueIndex]);
		if(this.isRootAgent()==true)
		{
			result.put(DPOPAgent.KEY_TOTAL_COST, this.totalCost);
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
			int id_=(Integer) result.get(DPOPAgent.KEY_ID);
			String name_=(String) result.get(DPOPAgent.KEY_NAME);
			int value_=(Integer) result.get(DPOPAgent.KEY_VALUE);
			if(result.containsKey(DPOPAgent.KEY_TOTAL_COST))
			{
				totalCost=(Integer) result.get(DPOPAgent.KEY_TOTAL_COST);
			}
			sizeList.addAll((List<Integer>)result.get(DPOPAgent.KEY_UTIL_MESSAGE_SIZES));
			
			String displayStr="Agent "+name_+": id="+id_+" value="+value_;
			System.out.println(displayStr);
		}
		
		Integer[] sizeArr=new Integer[sizeList.size()];
		sizeList.toArray(sizeArr);
		int[] minMaxAvg=StatisticUtil.minMaxAvg(CollectionUtil.toInt(sizeArr));
		System.out.println("utilMsgCount: "+sizeArr.length+" utilMsgSizeMin: "+FormatUtil.format(minMaxAvg[0])+" utilMsgSizeMax: "+
		FormatUtil.format(minMaxAvg[2])+" utilMsgSizeAvg: "+FormatUtil.format(minMaxAvg[4]));
		
		System.out.println("totalCost: "+Infinity.infinityEasy(totalCost));
		
		ResultDPOP ret=new ResultDPOP();
		ret.totalCost=totalCost;
		ret.utilMsgCount=sizeArr.length;
		ret.utilMsgSizeMin=minMaxAvg[0];
		ret.utilMsgSizeMax=minMaxAvg[2];
		ret.utilMsgSizeAvg=minMaxAvg[4];
		return ret;
	}

	@Override
	public String easyMessageContent(Message msg, Agent sender, Agent receiver) {
		// TODO Auto-generated method stub
		return "from "+sender.getName()+" to "+receiver.getName()+" type "+DPOPAgent.messageContent(msg);
	}
	
	public static String messageContent(Message msg)
	{
		switch (msg.getType()) {
		case DPOPAgent.TYPE_VALUE_MESSAGE:
		{
			int valueIndex=(Integer) msg.getValue();
			return "value["+valueIndex+"]";
		}
		case DPOPAgent.TYPE_UTIL_MESSAGE:
		{
			MultiDimensionData mdData=(MultiDimensionData) msg.getValue();
			return "util[dimensions="+mdData.getDimensions().toString()+"]";
		}
		default:
			return "unknown";
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void disposeMessage(Message msg) {
		// TODO Auto-generated method stub
		int type=msg.getType();
		if(type==TYPE_VALUE_MESSAGE)
		{
			disposeValueMessage(msg);
		}else if(type==TYPE_UTIL_MESSAGE)
		{
			List<MultiDimensionData> datas=(List<MultiDimensionData>) msg.getValue();
			int size=0;
			for(MultiDimensionData data : datas)
			{
				size+=data.size();
			}
			utilMsgSizes.add(size);
			
			disposeUtilMessage(msg);
		}
	}

	@Override
	protected void messageLost(Message msg) {
		// TODO Auto-generated method stub
		System.out.println("Error occurs because no message can be lost!");
	}
	
	private void sendUtilMessage()
	{
		if(this.isRootAgent()==true)
		{
			return;
		}
		Message utilMsg=new Message(this.id, this.parent, TYPE_UTIL_MESSAGE, allMDDatas);
		this.sendMessage(utilMsg);
	}
	
	private List<MultiDimensionData> computeLocalUtils()
	{
		List<MultiDimensionData> ret=new ArrayList<MultiDimensionData>();
		for(int i=0;i<allParents.length;i++)
		{
			List<Dimension> dimensions=new ArrayList<Dimension>();
			
			int parentId=allParents[i];
			int row=neighbourDomains.get(parentId).length;
			int col=this.domain.length;
			dimensions.add(new Dimension(parentId+"", row, parentLevels[i]));
			dimensions.add(new Dimension(this.id+"", this.domain.length, this.level));
			
			int[] data=new int[row*col];
			int[][] costs=this.constraintCosts.get(allParents[i]);
			for(int j=0;j<row;j++)
			{
				for(int k=0;k<col;k++)
				{
					data[j*col+k]=costs[j][k];
				}
			}
			
			ret.add(new MultiDimensionData(dimensions, data));
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	private void disposeUtilMessage(Message msg)
	{
		List<MultiDimensionData> tempData=(List<MultiDimensionData>) msg.getValue();
		receivedMDDatas.addAll(tempData);
		
		disposedChildrenCount++;
		if(disposedChildrenCount>=this.children.length)
		{
			allMDDataReceived();
			
			if(this.isRootAgent()==true)
			{
				ReductDimensionResult result=allMDDatas.get(0).reductDimension(this.id+"", ReductDimensionResult.REDUCT_DIMENSION_WITH_MIN);
				this.totalCost=result.getMdData().getData()[0];
				this.valueIndex=result.getResultIndex()[0];
				
				Map<Integer, Integer> valueIndexes=new HashMap<Integer, Integer>();
				valueIndexes.put(this.id, this.valueIndex);
				
				if(reductDimensionsResultDimensions!=null)
				{
					int[] values=reductDimensionsResult.get(this.valueIndex);
					for(int i=0;i<reductDimensionsResultDimensions.size();i++)
					{
						valueIndexes.put(Integer.parseInt(reductDimensionsResultDimensions.get(i).getName()), values[i]);
					}
				}
				
				sendValueMessage(valueIndexes);
				
				this.stopRunning();
			}else
			{
				sendUtilMessage();
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
			Message valueMsg=new Message(this.id, this.children[i], TYPE_VALUE_MESSAGE, CollectionUtil.copy(valueIndexes));
			this.sendMessage(valueMsg);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void disposeValueMessage(Message msg)
	{
		Map<Integer, Integer> valueIndexes=(Map<Integer, Integer>) msg.getValue();
		if(reductDimensionsResultDimensions!=null)
		{
			int[] values=reductDimensionsResult.get(this.valueIndex);
			for(int i=0;i<reductDimensionsResultDimensions.size();i++)
			{
				valueIndexes.put(Integer.parseInt(reductDimensionsResultDimensions.get(i).getName()), values[i]);
			}
		}
		
		while(this.reductDimensionResultIndexMap.size()>0)
		{
			for(String dimensionName : this.reductDimensionDimensionListMap.keySet())
			{
				this.calValueIndex(Integer.parseInt(dimensionName), 
						this.reductDimensionDimensionListMap.get(dimensionName), 
						this.reductDimensionResultIndexMap.get(dimensionName), valueIndexes);
			}
		}
		this.valueIndex=valueIndexes.get(this.id);

		this.sendValueMessage(valueIndexes);
		
		this.stopRunning();
	}
	
	private boolean calValueIndex(int agentId, List<Dimension> dimensions, int[] reductDimensionResultIndexes, Map<Integer, Integer> otherDimensionValueIndexes)
	{
		int[] dimensionValueIndexes=new int[dimensions.size()];
		for(int i=0;i<dimensions.size();i++)
		{
			Integer dimensionId=Integer.parseInt(dimensions.get(i).getName());
			if(otherDimensionValueIndexes.containsKey(dimensionId)==false)
			{
				return false;
			}
			dimensionValueIndexes[i]=otherDimensionValueIndexes.get(dimensionId);
		}
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
			index+=dimensionValueIndexes[i]*periods[i];
		}
		otherDimensionValueIndexes.put(agentId, reductDimensionResultIndexes[index]);
		return true;
	}

}
