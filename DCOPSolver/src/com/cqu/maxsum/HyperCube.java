package com.cqu.maxsum;

import java.util.*;

/**
 * Created by YanChenDeng on 2016/5/17.
 */
public class HyperCube {

    private int mainId;
    private Map<Integer,int[][]> utils;
    private int[] ids;
    private List<HyperCube> cubes;
    private int domainLength;
    private boolean isSingleConstraint = false;

    public HyperCube(int mainId,Map<Integer,int[][]> utils,int[] ids,int domainLength,boolean isRawUtil){
        this.mainId = mainId;
        this.ids = ids.clone();
        this.cubes = new LinkedList<>();
        this.domainLength = domainLength;
        this.utils = new HashMap<>();
        for (int id : utils.keySet()){
            int[][] rawUtil = utils.get(id);
            int[][] util = new int[rawUtil.length][rawUtil[0].length];
            for (int row = 0; row < rawUtil.length; row++){
                for (int col = 0; col < rawUtil[0].length; col++){
                    if (isRawUtil){
                        util[row][col] = -rawUtil[row][col];
                    }
                    else {
                        util[row][col] = rawUtil[row][col];
                    }
                }
                this.utils.put(id,util);
            }
        }
        if (utils.size() == ids.length)
            return;
        int[][] util = new int[domainLength][domainLength];
        for (int row = 0; row < domainLength; row++){
            for (int col = 0; col < domainLength; col++){
                util[row][col] = 0;
            }
        }
        this.utils.put(mainId,util);
    }

    public void join(HyperCube cube){
        cubes.add(cube);
    }

    public void setSingleConstraint(boolean singleConstraint) {
        isSingleConstraint = singleConstraint;
    }

    private static int indexUtil(Map<Integer,int[][]> utils, Map<Integer,Integer> assignment, int mainId, boolean skipMainId){
        assert assignment.get(mainId) != null : "main variable value can't be null!";
        int sum = 0;
        int mainAssignment = assignment.get(mainId);
        for (int id : assignment.keySet()){
            if (id == mainId && skipMainId)
                continue;
            sum += utils.get(id)[mainAssignment][assignment.get(id)];
        }
        return sum;
    }

    public int indexUtil(Map<Integer,Integer> assignment){
        return indexUtil(utils,assignment,mainId,false);
    }

    public int indexUtil(int index){
        return utils.get(mainId)[index][index];
    }

    public int getVariableCount(){
        return ids.length;
    }

    public int getMainId() {
        return mainId;
    }

    public int getDomainLength() {
        return domainLength;
    }

    public int getDomainLength(int id){
        return utils.get(id)[0].length;
    }

    public HyperCube resolveVariable(){
        int[] tmpUtil = new int[domainLength];
        for (HyperCube cube : cubes){
            if (cube.getMainId() != mainId || cube.getDomainLength() != domainLength || cube.getVariableCount() != 1){
                throw new RuntimeException("cube " + cube + " is invalid!");
            }
            for (int i = 0; i < tmpUtil.length; i++){
                tmpUtil[i] += cube.indexUtil(i);
            }
        }
        cubes.clear();
        return createSingleHyperCube(mainId,tmpUtil,false);
    }

    public HyperCube resolveFunctions(int targetId, Map<Integer,Integer> fixAssignment){
        Map<Integer,int[][]> tmpUtils = sumUpUtils();
        List<Map<Integer,Integer>> optimalList = calculateOptimalList(targetId,tmpUtils,fixAssignment);
        int[] targetUtil = new int[getDomainLength(targetId)];
        for (int i = 0; i < targetUtil.length; i++) {
            targetUtil[i] = calculateMaxUtility(tmpUtils, optimalList, targetId, i);
        }
        cubes.clear();
        return createSingleHyperCube(targetId,targetUtil,false);
    }


    private Map<Integer,int[][]> sumUpUtils(){
        Map<Integer,int[][]> newUtil = new HashMap<>();
        for (HyperCube cube : cubes){
            if (cube.getVariableCount() != 1){
                throw new RuntimeException("cube " + cube + " is invalid!");
            }
            int[][] targetUtil = utils.get(cube.getMainId());
            int[][] tmpTargetUtil = new int[targetUtil.length][targetUtil[0].length];
            for (int row = 0; row < targetUtil.length; row++){
                for (int col = 0; col < targetUtil[0].length; col++){
                    tmpTargetUtil[row][col] = targetUtil[row][col] + cube.indexUtil(col);
                }
            }
            newUtil.put(cube.getMainId(),tmpTargetUtil);
        }
        for (int id : utils.keySet()){
            if (!newUtil.containsKey(id))
                newUtil.put(id,utils.get(id));
        }
        return newUtil;
    }

    private List<Map<Integer,Integer>> calculateOptimalList(int targetId, Map<Integer,int[][]> tmpUtil, Map<Integer,Integer> fixAssignment){
        List<Map<Integer,Integer>> optimalList = new LinkedList<>();
        for (int i = 0; i < domainLength; i++){
            Map<Integer,Integer> optimalMap = new HashMap<>();
            int totalUtil = 0;
            for (int id : tmpUtil.keySet()){
                int[] util = tmpUtil.get(id)[i];
                if (id == targetId){
                    continue;
                }
                else if (fixAssignment != null && fixAssignment.containsKey(id)){
                    optimalMap.put(id,fixAssignment.get(id));
                    totalUtil += util[fixAssignment.get(id)];
                }
                else if (id == mainId){
                    optimalMap.put(mainId,i);
                    totalUtil += util[i];
                }
                else {
                    int max = Integer.MIN_VALUE;
                    int maxIndex = 0;
                    for (int j = 0; j < util.length; j++){
                        if (util[j] > max){
                            max = util[j];
                            maxIndex = j;
                        }
                    }
                    totalUtil += max;
                    optimalMap.put(id,maxIndex);
                }
            }
            optimalMap.put(-1,totalUtil);
            optimalList.add(optimalMap);
        }
        return optimalList;
    }

    public int calculateMaxUtility(Map<Integer,int[][]> tmpUtil,List<Map<Integer,Integer>> optimalList,int targetId,int targetValue){
        Map<Integer,Integer> assignment = new HashMap<>();
        if (targetId == mainId){
            assignment.put(targetId,targetValue);
            return optimalList.get(targetValue).get(-1) + indexUtil(tmpUtil,assignment,targetId,false);
        }
        int maxUtility = Integer.MIN_VALUE;
        for (int i = 0; i < optimalList.size(); i++){
            int totalUtil = optimalList.get(i).get(-1);
            assignment.put(mainId,i);
            assignment.put(targetId,targetValue);
            totalUtil += indexUtil(tmpUtil,assignment,mainId,true);
            if (totalUtil > maxUtility){
                maxUtility = totalUtil;
            }
        }
        return maxUtility;
    }


    public static HyperCube createSingleHyperCube(int mainId,int[] utils,boolean isRaw){
        int[][] rectifyUtil = new int[utils.length][utils.length];
        for (int i = 0; i < utils.length; i++){
            if (isRaw){
                rectifyUtil[i][i] = -utils[i];
            }
            else {
                rectifyUtil[i][i] = utils[i];
            }
        }
        Map<Integer,int[][]> util = new HashMap<>();
        util.put(mainId,rectifyUtil);
        return new HyperCube(mainId,util,new int[]{mainId},utils.length,false);
    }

    public static HyperCube createZeroHyperCube(int mainId,int domainLength){
        int[] utils = new int[domainLength];
        return createSingleHyperCube(mainId,utils,true);
    }

    @Override
    public String toString() {
        if (ids.length != 1)
            return super.toString();
        int[][] util = utils.get(mainId);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[ ");
        for (int i = 0; i < domainLength; i++){
            stringBuilder.append(util[i][i] + " ");
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }
}
