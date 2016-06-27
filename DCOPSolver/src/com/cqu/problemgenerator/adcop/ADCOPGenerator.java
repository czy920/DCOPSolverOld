package com.cqu.problemgenerator.adcop;

/**
 * Created by YanChenDeng on 2016/4/11.
 */
public class ADCOPGenerator {
    private int nbAgent;
    private int nbDomain;
    private int nbConstraints;
    private double p1;
    private double p2;
    private String problemDir;

    public ADCOPGenerator(int nbAgent, int nbDomain, int nbConstraints, double p1, double p2, String problemDir) {
        this.nbAgent = nbAgent;
        this.nbDomain = nbDomain;
        this.nbConstraints = nbConstraints;
        this.p1 = p1;
        this.p2 = p2;
        this.problemDir = problemDir;
    }

    public int getNbAgent() {
        return nbAgent;
    }

    public void setNbAgent(int nbAgent) {
        this.nbAgent = nbAgent;
    }

    public int getNbDomain() {
        return nbDomain;
    }

    public void setNbDomain(int nbDomain) {
        this.nbDomain = nbDomain;
    }

    public int getNbConstraints() {
        return nbConstraints;
    }

    public void setNbConstraints(int nbConstraints) {
        this.nbConstraints = nbConstraints;
    }

    public double getP1() {
        return p1;
    }

    public void setP1(double p1) {
        this.p1 = p1;
    }

    public double getP2() {
        return p2;
    }

    public void setP2(double p2) {
        this.p2 = p2;
    }

    public String getProblemDir() {
        return problemDir;
    }

    public void setProblemDir(String problemDir) {
        this.problemDir = problemDir;
    }

    private void generate(){
        for (int i = 0; i < nbAgent; i++){
            for (int j = 0; j < nbAgent; j++ ){
                if (i == j)
                    continue;

            }
        }
    }
}
