package com.cqu.benchmark.scalefreenetworks;

import com.cqu.benchmark.ContentWriter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by YanChenDeng on 2016/4/27.
 */
public class DensityConvertor {
    private double p1;
    private List<Integer> m1;
    private List<Integer> m2;
    private String dirPath;
    private int nbAgent;
    private int domainSize;
    private int highCost;
    private int epsilon;

    public DensityConvertor(double p1, String dirPath, int nbAgent, int domainSize, int highCost,int epsilon) {
        this.p1 = p1;
        this.dirPath = dirPath;
        this.nbAgent = nbAgent;
        this.domainSize = domainSize;
        this.highCost = highCost;
        m1 = new LinkedList<>();
        m2 = new LinkedList<>();
        this.epsilon = epsilon;
    }

    public void setP1(double p1) {
        this.p1 = p1;
    }

    public void generate(){
        int edgeCount = (int) (p1 * nbAgent * (nbAgent - 1) / 2);
        Map<String,Object> extraParameter = new HashMap<>();
        ContentWriter writer = new ContentWriter(1,dirPath,nbAgent,domainSize,1,highCost,ContentWriter.PROBLEM_SCALE_FREE_NETWORK,null);
        for (int i = 1; i <= nbAgent; i++){
           for (int j = 1; j <= i; j++){
               int generatedEdgeCount = (i - 1) + (nbAgent - i) * j;
               if (Math.abs(generatedEdgeCount - edgeCount) < epsilon){
                   extraParameter.put("m1",i);
                   extraParameter.put("m2",j);
                   writer.setExtraParameter(extraParameter);
                   try {
                       writer.generate();
                   } catch (Exception e) {
                       System.out.println("m1:" + i + " m2:" + j + "generate failed");
                   }
               }
           }
        }
    }

    public static void main(String[] args){
        DensityConvertor convertor = new DensityConvertor(0.6,"",12,10,20,5);
        convertor.generate();
    }
}
