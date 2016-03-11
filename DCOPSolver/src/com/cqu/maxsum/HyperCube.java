package com.cqu.maxsum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HyperCube {

	public static final String HYPER_CUBE_OPERATE_SUM_PRODDUCT="add";
	public static final String HYPER_CUBE_OPERATE_MAX_SUM="multiply";
	
	private String[] variableNames;
	private int[][] domains;
	private int[] utils;
	private List<Map<Integer, Integer>> stepMaps=new ArrayList<Map<Integer,Integer>>();
	private List<HyperCube> cubes=new ArrayList<HyperCube>();
	private boolean recordMaxAssignment;
	private Map<Integer, Map<String, Integer>> maxAssignment;
	
	public HyperCube(String[] varibleNames,int[][] domains,int[] utils) {
		this.variableNames=varibleNames.clone();
		this.domains=domains.clone();
		this.utils=utils.clone();
		this.recordMaxAssignment = false;
		constructStepMap();
	}
	
	public HyperCube(String[] varibleNames,int[][] domains,int[] utils, boolean recordMaxAssignment, Map<Integer, Map<String, Integer>> maxAssignment) {
		this.variableNames=varibleNames.clone();
		this.domains=domains.clone();
		this.utils=utils.clone();
		if (maxAssignment != null) {
			this.maxAssignment = new HashMap<Integer, Map<String,Integer>>();
			for (int val: maxAssignment.keySet()){
				this.maxAssignment.put(val, maxAssignment.get(val));
			}
		}
		this.recordMaxAssignment = recordMaxAssignment;
		constructStepMap();
	}
	public void setRecordMaxAssignment(boolean recordMaxAssignment) {
		this.recordMaxAssignment = recordMaxAssignment;
	}
	
	
	public HyperCube(String[] variableNames,int[] domainLength,int[] utils){
		this.variableNames=variableNames.clone();
		this.utils=utils.clone();
		domains=new int[domainLength.length][];
		for(int i=0;i<domainLength.length;i++){
			domains[i]=new int[domainLength[i]];
			for(int j=0;j<domains[i].length;j++)
			{
				domains[i][j]=j;
			}
		}
		constructStepMap();
	}
	
	private void constructStepMap(){
		stepMaps.clear();
		for(int i=domains.length-1;i>=0;i--){
			Map<Integer, Integer> stepMap=new HashMap<Integer, Integer>();
			int previousBlockSize=calcuBlockCount(i);
			for(int j=0;j<domains[i].length;j++){
				stepMap.put(domains[i][j], j*previousBlockSize);
			}
			stepMaps.add(0,stepMap);
		}
	}
	
	private int calcuBlockCount(int current)
	{
		int blockSize=0;
		for(int i=current+1;i<variableNames.length;i++)
		{
			blockSize+=stepMaps.get(i-current-1).get(domains[i][domains[i].length-1]);
		}
		return blockSize+1;
	}
	
	public int indexUtils(int[] assignment){
		int index=calcuIndex(assignment);
		if (index<0) {
			return Integer.MIN_VALUE;
		}
		return utils[index];	
	}
	
	public int indexUtils(Map<String, Integer> assignment){
		if (assignment.keySet().size()!=variableNames.length) {
			return Integer.MIN_VALUE;
		}
		int[] ass=new int[variableNames.length];
		for(int i=0;i<variableNames.length;i++){
			Integer integer=assignment.get(variableNames[i]);
			if (integer==null) {
				return Integer.MIN_VALUE;
			}
			ass[i]=integer;
		}
		return indexUtils(ass);
	}
	
	public int indexUtils(int longIndex){
		if (longIndex<0||longIndex>=utils.length) {
			return Integer.MIN_VALUE;
		}
		return utils[longIndex];
	}
	
	private int calcuIndex(int[] assignment){
		if (assignment.length!=variableNames.length) {
			return -1;
		}
		int utilIndex=0;
		for (int i=assignment.length-1;i>=0;i--){
			Integer step=stepMaps.get(i).get(assignment[i]);
			if (step==null) {
				return -1;
			}
			utilIndex+=step;
		}
		return utilIndex;
	}
	
	public int getVariableCount(){
		return variableNames.length;
	}
	
	public String getVariableName(int index){
		if (index<0||index>=variableNames.length) {
			return null;
		}
		return variableNames[index];
	}
	
	public int[] getDomain(int index){
		if (index<0||index>=variableNames.length) {
			return null;
		}
		return domains[index];
	}
	
	public int[] getDomain(String variableName){
		for(int i=0;i<variableNames.length;i++){
			if (variableNames[i].equals(variableName)) {
				return domains[i];
			}
		}
		return null;
	}
	
	public int getUtilLength(){
		return utils.length;
	}
	
	public void join(HyperCube otherCube){
		cubes.add(otherCube);
	}
	
	public HyperCube resolve(String operate){
		int[] newUtil=utils.clone();		
		for(HyperCube cube:cubes){
			if (cube.getVariableCount()!=1||!cube.getVariableName(0).equals(variableNames[0])||cube.getUtilLength()!=utils.length) {
				return null;
			}
			for(int j=0;j<utils.length;j++){
				if(operate.equals(HYPER_CUBE_OPERATE_MAX_SUM)){
					newUtil[j]+=cube.indexUtils(j);
				}
				else if (operate.equals(HYPER_CUBE_OPERATE_SUM_PRODDUCT)) {
					newUtil[j]*=cube.indexUtils(j);
				}
				else {
					return null;
				}
			}
		}
		cubes.clear();
		return new HyperCube(variableNames,domains,newUtil);		
	}
	
	public HyperCube resovle(String operate,String targetVariable,Map<Integer, Integer> fixedAssignment){
		int targetIndex=-1;
		Map<Integer, Map<String, Integer>> maxAssignment = null;
		if (recordMaxAssignment) {
			maxAssignment = new HashMap<Integer, Map<String,Integer>>();
		}
		for(int i=0;i<variableNames.length;i++){
			if (targetVariable.equals(variableNames[i])) {
				targetIndex=i;
				break;
			}
		}
		if (targetIndex==-1) {
			return null;
		}				
		int[] targetDomain=domains[targetIndex];		
		int[] targetUtil=new int[targetDomain.length];
		if (operate.equals(HYPER_CUBE_OPERATE_MAX_SUM)) {
			Arrays.fill(targetUtil, Integer.MIN_VALUE);
		}
		else if(operate.equals(HYPER_CUBE_OPERATE_SUM_PRODDUCT)) {
			Arrays.fill(targetUtil, 0);
		}
		else {
			return null;
		}
		List<HyperCube> freeCubes;
		List<HyperCube> fixedCubes=new ArrayList<HyperCube>();
		if (fixedAssignment==null) {
			freeCubes=cubes;
		}
		else {
			freeCubes=new ArrayList<HyperCube>();
			for(HyperCube cube:cubes){
				if (!fixedAssignment.containsKey(Integer.parseInt(cube.getVariableName(0)))) {
					freeCubes.add(cube);
				}				
				else {
					fixedCubes.add(cube);
				}
			}
		}
		int targetUtilIndex=0;
		for(int targetValue : targetDomain){
			boolean flag=true;
			int[] indeies=new int[freeCubes.size()];
			internal: while (flag) {				
				int accumulator;
				if (operate.equals(HYPER_CUBE_OPERATE_MAX_SUM)) {
					accumulator=0;
				}
				else {
					accumulator=1;
				}
				Map<String, Integer> assignMap=new HashMap<String, Integer>();
				if (freeCubes.size()==0) {
					flag=false;
				}
				for(int i=0;i<freeCubes.size();i++){
					if (indeies[i]==freeCubes.get(i).getDomain(0).length) {
						if (i!=0) {
							indeies[i]=0;
							indeies[i-1]++;
							continue internal;
						}
						else {
							flag=false;
							break internal;
						}
						
					}	
					assignMap.put(freeCubes.get(i).getVariableName(0), indeies[i]);
					if (operate.equals(HYPER_CUBE_OPERATE_MAX_SUM)) {
						accumulator+=freeCubes.get(i).indexUtils(new int[]{indeies[i]});
					}
					else {
						accumulator*=freeCubes.get(i).indexUtils(new int[]{indeies[i]});
					}
				}
				for(HyperCube cube: fixedCubes){
					int valueIndex=fixedAssignment.get(Integer.parseInt(cube.getVariableName(0)));
					assignMap.put(cube.getVariableName(0), valueIndex);
					if (operate.equals(HYPER_CUBE_OPERATE_MAX_SUM)) {
						accumulator+=cube.indexUtils(new int[]{valueIndex});
					}
					else {
						accumulator*=cube.indexUtils(new int[]{valueIndex});
					}
				}
				assignMap.put(targetVariable, targetValue);
				int localUtil=indexUtils(assignMap);
				if (operate.equals(HYPER_CUBE_OPERATE_MAX_SUM)) {
					accumulator+=localUtil;
					if (targetUtil[targetUtilIndex]<accumulator) {
						targetUtil[targetUtilIndex]=accumulator;
						if (recordMaxAssignment) {
							maxAssignment.put(targetUtilIndex, assignMap);
						}						
					}
				}
				else {
					accumulator*=localUtil;
					targetUtil[targetUtilIndex]+=accumulator;
				}
				if(indeies.length>0)
					indeies[indeies.length-1]++;
			}
			targetUtilIndex++;
		}
		cubes.clear();
		return new HyperCube(new String[]{targetVariable}, new int[][]{targetUtil}, targetUtil,recordMaxAssignment,maxAssignment);
	}
	
	public HyperCube resovle(String operate,String targetVariable){
		return resovle(operate, targetVariable, null);
	}
	
	public static HyperCube createSimpleHyperCube(String name,int domainSize,int utilValue){
		int[] util=new int[domainSize];
		Arrays.fill(util, utilValue);
		return new HyperCube(new String[]{name}, new int[]{domainSize}, util);
	}
	
	public void printMaxAssignment(int val){
		if (maxAssignment == null) {
			return;
		}
		Map<String, Integer> map = maxAssignment.get(val);
		if (map != null) {
			System.out.println(map);
		}
	}
}
