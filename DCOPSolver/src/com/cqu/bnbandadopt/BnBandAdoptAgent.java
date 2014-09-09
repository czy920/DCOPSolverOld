package com.cqu.bnbandadopt;

import com.cqu.core.Agent;

/**
 * 这里仅仅是一个将adopt与bnbadopt进行简单合并算法的测试，并且只对规定的问题生效，
 * 可以将这个包下的东西忽略掉，仅为后面作一个知识准备
 * @author hechen
 *
 */

public class BnBandAdoptAgent {
	
	private Agent agent;
	private String choose;
	
	public BnBandAdoptAgent(int id, String name, int level, int[] domain) {
		// just want to observe the result 
		// in correspond to a fixed test instance ""problems/RandomDCOP_10_3_2.xml"
		if(id==4||id==6||id==1||id==5||id==2||id==3||id==8){
			agent=new BnBAdoptAgentTwo(id,name,level,domain);
			this.choose="bnbadopt";
		}else if(id==7||id==9){
			agent=new AdoptAgentTwo(id,name,level,domain);
			this.choose="adopt";
		}else{
			agent=new AdoptAgentTwo(id,name,level,domain);
			this.choose="adopt";
		}
	}

	public Agent getAgent() {
		return agent;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	public String getChoose() {
		return choose;
	}

	public void setChoose(String choose) {
		this.choose = choose;
	}

	
}
