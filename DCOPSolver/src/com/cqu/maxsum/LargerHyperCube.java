package com.cqu.maxsum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LargerHyperCube {
	
	public static final String HYPER_CUBE_OPERATE_SUM_PRODDUCT="add";
	public static final String HYPER_CUBE_OPERATE_MAX_SUM="multiply";
	private static final int KEY_TAOTAL_UTIL = -1;
	
	private int[] variableIds;
	private int mainId;
	private Map<Integer, int[][]> utils;
	private List<LargerHyperCube> cubes;
	private int domainSize;
	
	public LargerHyperCube(int[] variableIds, int mainId, Map<Integer, int[][]> costs ) {
		this.variableIds = variableIds.clone();
		this.mainId = mainId;
		this.utils = new HashMap<Integer, int[][]>();
		for (int id:costs.keySet()){
			int[][] subCost = costs.get(id).clone();
			int[][] subUtil = new int[subCost.length][subCost[0].length];
			for (int i = 0; i < subCost.length; i++)
				for (int j = 0; j < subCost[i].length; j++)
					subUtil[i][j] = -subCost[i][j];			
			this.utils.put(id, subUtil);
		}
		cubes = new ArrayList<LargerHyperCube>();
	}
	
	public void setDomainSize(int domainSize) {
		this.domainSize = domainSize;
	}
	
	public static int indexUtils(Map<Integer, int[][]> utils, int mainId, Map<Integer, Integer> assignment)
	{
		int mainVal = assignment.get(mainId);
		int util = 0;
		for (int id:assignment.keySet()){			
			int[][] subUtil = utils.get(id);
			if (subUtil == null || (id == mainId && assignment.keySet().size() != 1)) {
				continue;
			}
			if (mainId < id) {
				util += subUtil[mainVal][assignment.get(id)];
			}
			else {
				util += subUtil[assignment.get(id)][mainVal];
			}
		}
		return util;
	}
	
	public int indexUtils(Map<Integer, Integer> assignment) {
		return indexUtils(this.utils, this.mainId, assignment);
	}
	
	public int indexUtils(int assignment) {
		if (variableIds.length != 1) {
			throw new UnsupportedOperationException();
		}
		return utils.get(mainId)[assignment][assignment];
	}
	
	public void join(LargerHyperCube largerHyperCube){
		cubes.add(largerHyperCube);
	}
	
	public int utilLength() {
		if (variableIds.length == 1) {
			return utils.get(mainId).length;
		}
		int length = 0;
		for(int id:utils.keySet()){
			int[][] subUtil = utils.get(id);
			for(int[] vec:subUtil){
				length += vec.length;
			}
		}
		return length;
	}
	
	public int domainLength(int index){
		if (index == mainId) {
			return domainSize;
		}
		int[][] subUtil = utils.get(index);
		if (mainId < index) {
			return subUtil[0].length;
		}
		else {
			return subUtil.length;
		}
	}
	
	public int getVariableCount(){
		return variableIds.length;
	}
	
	public int getMainVariable(){
		return mainId;
	}
	
	public LargerHyperCube resovle(String operate) {
		int[] resovledUtil = new int[utilLength()];
		for(LargerHyperCube cube:cubes){
			if (cube.getVariableCount() != 1||cube.getMainVariable() != mainId) {
				throw new UnsupportedOperationException();
			}
			for(int i = 0; i < resovledUtil.length; i++){
				if (operate.equals(HYPER_CUBE_OPERATE_MAX_SUM)) {
					resovledUtil[i] += cube.indexUtils(i);
				}
				else if (operate.equals(HYPER_CUBE_OPERATE_SUM_PRODDUCT)) {
					resovledUtil[i] *= cube.indexUtils(i);
				}
				else {
					throw new UnsupportedOperationException();
				}
			}
		}		
		cubes.clear();
		return createSimpleLargerUtil(mainId, resovledUtil);
	}
	
	public LargerHyperCube resovle(int target, String operate, Map<Integer, Integer> fixedAssignment) {
		int[] targetUtil = new int[domainLength(target)];
		if (operate.equals(HYPER_CUBE_OPERATE_MAX_SUM)) {
			Arrays.fill(targetUtil, Integer.MIN_VALUE);
		}
		else if(operate.equals(HYPER_CUBE_OPERATE_SUM_PRODDUCT)) {
			throw new UnsupportedOperationException();
		}
		else {
			return null;
		}
//		List<LargerHyperCube> freeCubes;
//		List<LargerHyperCube> fixedCubes=new ArrayList<LargerHyperCube>();
//		if (fixedAssignment==null) {
//			freeCubes=cubes;
//		}
//		else {
//			freeCubes=new ArrayList<LargerHyperCube>();
//			for(LargerHyperCube cube:cubes){
//				if (!fixedAssignment.containsKey(cube.getMainVariable())) {
//					freeCubes.add(cube);
//				}				
//				else {
//					fixedCubes.add(cube);
//				}
//			}
//		}
		Map<Integer, int[][]> tmpUtil = generateTmpUtil();
		List<Map<Integer, Integer>> optimalList = calcuOptimals(tmpUtil, target);
		for(int i = 0; i < targetUtil.length; i++){
			targetUtil[i] = calcuMaxUtility(optimalList, tmpUtil, target, i);
		}
		cubes.clear();
		return createSimpleLargerUtil(target, targetUtil);
	}
	private Map<Integer, int[][]> generateTmpUtil(){
		Map<Integer, int[][]> tmpUtil = new HashMap<Integer, int[][]>();
		for(LargerHyperCube cube: cubes){
			assert cube.getVariableCount() == 1:"multi variable found!!!";
			int[][] subUtil = utils.get(cube.getMainVariable());
			if (subUtil == null) {
				assert cube.getMainVariable() == mainId:"main variable not consist!!";
				subUtil = new int[domainSize][domainSize];
			}
			int[][] newUtil = new int[subUtil.length][subUtil[0].length];
			for (int i = 0; i < subUtil.length; i++)
				for(int j = 0; j < subUtil[0].length; j++)
				{
					if (mainId < cube.getMainVariable()) {
						newUtil[i][j] = subUtil[i][j] + cube.indexUtils(j);
					}
					else {
						newUtil[i][j] = subUtil[i][j] + cube.indexUtils(i);
					}
				}
			tmpUtil.put(cube.getMainVariable(), newUtil);
		}
		for(int id : utils.keySet()){
			if (!tmpUtil.containsKey(id)) {
				tmpUtil.put(id, utils.get(id));
			}
		}
		return tmpUtil;
	}
	
	private List<Map<Integer, Integer>> calcuOptimals(Map<Integer, int[][]> tmpUtil, int target){
		List<Map<Integer, Integer>> optimals = new ArrayList<Map<Integer,Integer>>();
		Map<Integer, Integer> assignMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < domainSize; i++){
			Map<Integer, Integer> optiMap = new HashMap<Integer, Integer>();
			int totalUtil = 0;
			for (int id : tmpUtil.keySet()){
				assignMap.put(mainId, i);
				if (id == target) {
					continue;
				}
				if (id != mainId) {
					int domainLength = domainLength(id);
					int max = Integer.MIN_VALUE;
					int maxIndex = -1;
					for (int j = 0; j < domainLength; j++){
						assignMap.put(id, j);
						int value = indexUtils(tmpUtil,mainId,assignMap);
						if (value > max) {
							max = value;
							maxIndex = j;
						}
					}
					totalUtil += max;
					optiMap.put(id, maxIndex);
					assignMap.clear();
				}
				else {
					int value = indexUtils(tmpUtil,mainId,assignMap);
					totalUtil += value;
					optiMap.put(id, i);
				}
				
			}
			optiMap.put(KEY_TAOTAL_UTIL, totalUtil);
			totalUtil = 0;
			optimals.add(optiMap);
		}
		return optimals;
	}
	
	private int calcuMaxUtility(List<Map<Integer,Integer>> optimals, Map<Integer, int[][]> tmpUtil, int target, int targetVal)
	{
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		if (target == mainId) {
			map.put(target, targetVal);
			return indexUtils(tmpUtil, target, map) + optimals.get(targetVal).get(KEY_TAOTAL_UTIL);
		}
		int maxUtility = Integer.MIN_VALUE;		
		for (int i = 0; i < domainSize; i++){
			int util = optimals.get(i).get(KEY_TAOTAL_UTIL);			
			map.put(mainId, i);
			map.put(target, targetVal);
			util += indexUtils(tmpUtil,mainId,map);
			if (util > maxUtility) {
				maxUtility = util;
			}
			map.clear();
		}
		return maxUtility;
	}
	
	public static LargerHyperCube createSimpleLargerUtil(int id, int domainLength, int defaultVal) {
		int[][] util = new int[domainLength][domainLength];
		for (int i = 0; i < domainLength; i++) {
			util[i][i] = -defaultVal;
		}
		Map<Integer, int[][]> utilMap = new HashMap<Integer, int[][]>();
		utilMap.put(id, util);
		return new LargerHyperCube(new int[]{id}, id, utilMap);
	}
	
	public static LargerHyperCube createSimpleLargerUtil(int id, int[] vals){
		int[][] util = new int[vals.length][vals.length];
		for (int i = 0; i < vals.length; i++){
			util[i][i] = -vals[i];
		}
		Map<Integer, int[][]> utilMap = new HashMap<Integer, int[][]>();
		utilMap.put(id, util);
		return new LargerHyperCube(new int[]{id}, id, utilMap);
	}


	
	
	
}
