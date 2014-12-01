package com.cqu.agiledpop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
	
	private List<MultiDimensionData> rawMDDatas;
	
	private Map<Integer, List<Dimension>> reductDimensionDimensionListMap;
	private Map<Integer, int[]> reductDimensionResultIndexMap;
	
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
			rawMDDatas=this.computeLocalUtils();
		}
		if(this.isLeafAgent()==true)
		{
			mergeAndReduct();
		}
	}
	
	private void mergeAndReduct()
	{
		List<Dimension> allDimensions=this.fakeMergedDimensions(rawMDDatas);
		if(allDimensions.get(0).getName().equals(this.id+"")==true)
		{
			
		}else
		{
			
		}
	}
	
	private boolean tryReduct(String dimensionName)
	{
		List<MultiDimensionData> involvedDatas=new ArrayList<MultiDimensionData>();
		for(int i=0;i<rawMDDatas.size();i++)
		{
			if(MultiDimensionData.indexOf(rawMDDatas.get(i).getDimensions(), dimensionName)!=-1)
			{
				involvedDatas.add(rawMDDatas.get(i));
			}
		}
		List<Dimension> allDimensions=this.fakeMergedDimensions(involvedDatas);
		if(allDimensions.size()>MAX_DIEMNSION_FEASIBLE)
		{
			return false;
		}else
		{
			MultiDimensionData mergedData=rawMDDatas.get(0);
			for(int i=1;i<involvedDatas.size();i++)
			{
				mergedData=mergedData.mergeDimension(involvedDatas.get(i));
			}
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

	@Override
	protected void disposeMessage(Message msg) {
		// TODO Auto-generated method stub
		int type=msg.getType();
		if(type==TYPE_VALUE_MESSAGE)
		{
			disposeValueMessage(msg);
		}else if(type==TYPE_UTIL_MESSAGE)
		{
			utilMsgSizes.add(((MultiDimensionData) msg.getValue()).size());
			
			disposeUtilMessage(msg);
		}
	}

	@Override
	protected void messageLost(Message msg) {
		// TODO Auto-generated method stub
		System.out.println("Error occurs because no message can be lost!");
	}
	
	private void sendUtilMessage(MultiDimensionData multiDimentionalData, int target)
	{
		if(this.isRootAgent()==true)
		{
			return;
		}
		Message utilMsg=new Message(this.id, target, TYPE_UTIL_MESSAGE, multiDimentionalData);
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
	
	private void disposeUtilMessage(Message msg)
	{
		//先融合
		/*if(this.isRootAgent()==true&&rawMDData==null)
		{
			rawMDData=(MultiDimensionData) msg.getValue();
		}else
		{
			rawMDData=rawMDData.mergeDimension((MultiDimensionData) msg.getValue());
		}
		
		disposedChildrenCount++;
		if(disposedChildrenCount>=this.children.length)
		{
			//所有子节点(包括伪子节点)的UtilMessage都已收集完毕，
			//则可以进行针对本节点的降维，将最终得到的UtilMessage再往父节点发送
			ReductDimensionResult result=rawMDData.reductDimension(this.id+"", ReductDimensionResult.REDUCT_DIMENSION_WITH_MIN);
			this.reductDimensionResultIndexes=result.getResultIndex();
			dimensionsList=result.getMdData().getDimensions();
			
			if(this.isRootAgent()==true)
			{
				this.totalCost=result.getMdData().getData()[0];
				this.valueIndex=this.reductDimensionResultIndexes[0];
				
				Map<Integer, Integer> valueIndexes=new HashMap<Integer, Integer>();
				valueIndexes.put(this.id, this.valueIndex);
				sendValueMessage(valueIndexes);
				
				this.stopRunning();
			}else
			{
				this.sendUtilMessage(result.getMdData());
			}
		}*/
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
		/*Map<Integer, Integer> valueIndexes=(Map<Integer, Integer>) msg.getValue();
		
		int[] periods=new int[dimensionsList.size()];
		for(int i=0;i<dimensionsList.size();i++)
		{
			int temp=1;
			for(int j=i+1;j<dimensionsList.size();j++)
			{
				temp*=dimensionsList.get(j).getSize();
			}
			periods[i]=temp;
		}
		int index=0;
		for(int i=0;i<periods.length;i++)
		{
			index+=valueIndexes.get(Integer.parseInt(dimensionsList.get(i).getName()))*periods[i];
		}
		this.valueIndex=this.reductDimensionResultIndexes[index];
		
		valueIndexes.put(this.id, this.valueIndex);
		this.sendValueMessage(valueIndexes);
		
		this.stopRunning();*/
	}

}
