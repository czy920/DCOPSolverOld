package com.cqu.ga;

        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;

        import com.cqu.core.Infinity;
        import com.cqu.core.Message;
        import com.cqu.core.ResultCycle;
        import com.cqu.core.ResultCycleAls;
        import com.cqu.cyclequeue.AgentCycle;
        import com.cqu.cyclequeue.AgentCycleAls;
        import com.cqu.dsa.AlsDsa_Agent;
        import com.cqu.main.Debugger;
        import com.cqu.settings.Settings;

public class ALSGDBAAgent extends AgentCycleAls {

    public final static int TYPE_VALUE_MESSAGE=0;
    public final static int TYPE_GAIN_MESSAGE=1;
    private static int cycleCountEnd;

    public final static String KEY_LOCALCOST="KEY_LOCALCOST";
    public final static String KEY_NCCC="KEY_NCCC";

    public final static int CYCLE_VALUE=567;
    public final static int CYCLE_GAIN=568;
    private int cycleTag = CYCLE_VALUE;
    private int nccc = 0;
    private int gainValue;
    private int selectValueIndex;
    private int receivedQuantity;
    private int cycleCount;
    private int neighboursQuantity;
    private int neighboursGain[];
    private int[] neighboursValueIndex;

    private int[] minimumCost;
    private int effCost = 0;
    private Map<Integer, int[][]> modifier;
    private boolean isNeighborsChanged = false;

    public ALSGDBAAgent(int id, String name, int level, int[] domain) {
        super(id, name, level, domain);
        // TODO Auto-generated constructor stub
    }

    protected void initRun() {
        super.initRun();

        cycleCountEnd = 1000;
        localCost=2147483647;
        valueIndex=(int)(Math.random()*(domain.length));
        selectValueIndex=0;
        receivedQuantity=0;
        cycleCount=0;
        neighboursQuantity=neighbours.length;
        neighboursValueIndex = new int[neighboursQuantity];
        neighboursGain=new int[neighboursQuantity];
        minimumCost = new int[neighboursQuantity];

        effInit();
        getMinimumCost();
        sendValueMessages();
    }

    private void getMinimumCost(){
        for(int i = 0; i < neighboursQuantity; i++){
            int minCostTemp = constraintCosts.get(neighbours[i])[0][0];
            for(int indexI = 0; indexI < domain.length; indexI++){
                for(int indexJ = 0; indexJ < neighbourDomains.get(neighbours[i]).length; indexJ++){
                    if(minCostTemp > constraintCosts.get(neighbours[i])[indexI][indexJ])
                        minCostTemp = constraintCosts.get(neighbours[i])[indexI][indexJ];
                }
            }
            minimumCost[i] = minCostTemp;
        }
    }

    private void effInit(){
        modifier = new HashMap<Integer, int[][]>();
        for(int i = 0; i < neighboursQuantity; i++){
            modifier.put(neighbours[i], new int[domain.length][neighbourDomains.get(neighbours[i]).length]);
        }
    }

    private void sendValueMessages(){
        for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
            Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_VALUE_MESSAGE, valueIndex);
            this.sendMessage(msg);
        }
    }

    private void sendGainMessages(){
        for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
            Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_GAIN_MESSAGE, gainValue);
            this.sendMessage(msg);
        }
    }

    private int localCost(){
        int localCostTemp=0;
        for(int i=0; i<neighboursQuantity; i++){
            localCostTemp+=constraintCosts.get(neighbours[i])[valueIndex][neighboursValueIndex[i]];
        }
        return localCostTemp;
    }

    private int effCost(){
        int effCostTemp = 0;
        for(int i = 0; i < neighboursQuantity; i++){
            effCostTemp += constraintCosts.get(neighbours[i])[valueIndex][neighboursValueIndex[i]] *
                    (modifier.get(neighbours[i])[valueIndex][neighboursValueIndex[i]] + 1);
        }
        return effCostTemp;
    }

    @Override
    protected void disposeMessage(Message msg) {
        // TODO 自动生成的方法存根
        if(Debugger.debugOn==true)
        {
            System.out.println(Thread.currentThread().getName()+": message got in agent "+
                    this.name+" "+this.msgMailer.easyMessageContent(msg)+" | VALUE="+this.domain[valueIndex]+" gainValue="+Infinity.infinityEasy(this.gainValue));
        }
        if(msg.getType()==TYPE_VALUE_MESSAGE){
            disposeValueMessage(msg);
        }
        else if(msg.getType()==TYPE_GAIN_MESSAGE){
            disposeGainMessage(msg);
        }
        else if(msg.getType() == AlsDsa_Agent.TYPE_ALSCOST_MESSAGE){
            disposeAlsCostMessage(msg);
        }
        else if(msg.getType() == AlsDsa_Agent.TYPE_ALSBEST_MESSAGE){
            disposeAlsBestMessage(msg);
        }
        else
            System.out.println("wrong!!!!!!!!");
    }

    private void disposeValueMessage(Message msg) {
        // TODO 自动生成的方法存根
        int senderIndex=0;
        int senderId=msg.getIdSender();
        for(int i=0; i<neighboursQuantity; i++){
            if(neighbours[i]==senderId){
                senderIndex=i;
                break;
            }
        }

		/*
		if(cycleCount == 8){
			if(neighboursValueIndex.get(senderIndex) != msg.getValue()){
				System.out.println("agent"+this.id+"_______"+"neighbour_changed"+"________"+neighbours[senderIndex]);
			}
		}
		*/

        neighboursValueIndex[senderIndex] = (Integer)(msg.getValue());

        isNeighborsChanged = true;
        if(receivedQuantity==0){

        }
    }

    private void cycleValue(){
        if(cycleCount>=cycleCountEnd){
            AlsStopRunning();
        }
        else{
            cycleCount++;
            localCost = localCost();
            effCost = effCost();
            AlsWork();

            int[] selectMinCost=new int[domain.length];
            for(int i=0; i<domain.length; i++){
                for(int j=0; j<neighboursQuantity; j++){
                    selectMinCost[i] += constraintCosts.get(neighbours[j])[i][neighboursValueIndex[j]]
                            * (modifier.get(neighbours[j])[valueIndex][neighboursValueIndex[j]] + 1)
                    ;
                }
            }
            int newLocalCost=effCost;
            for(int i=0; i<domain.length; i++){
                if(selectMinCost[i]<newLocalCost){
                    newLocalCost=selectMinCost[i];
                    selectValueIndex=i;
                }
            }
            gainValue=effCost-newLocalCost;

            if(gainValue <= 0 && isNeighborsChanged == false){
                for(int i = 0; i < neighboursQuantity; i++){
                    if(constraintCosts.get(neighbours[i])[valueIndex][neighboursValueIndex[i]] > minimumCost[i])
                        increaseMode(i);
                }
            }
            isNeighborsChanged = false;
            increaseNccc();
            //System.out.println("agent"+this.id+"_______"+cycleCount+"_______"+gainValue+"________");
            sendGainMessages();

//			if(gainValue >= 0 && Math.random() < 0.8){
//				valueIndex = selectValueIndex;
//				sendValueMessages();
//			}

        }
    }

    private void disposeGainMessage(Message msg) {
        // TODO 自动生成的方法存根
        int senderIndex=0;
        int senderId=msg.getIdSender();
        for(int i=0; i<neighboursQuantity; i++){
            if(neighbours[i]==senderId){
                senderIndex=i;
                break;
            }
        }
        neighboursGain[senderIndex]=(Integer)msg.getValue();

        if(receivedQuantity==0){

        }
    }

    private void cycleGain(){
        if(cycleCount>=cycleCountEnd){
            AlsStopRunning();
        }
        else{
            AlsWork();
            boolean go = true;
            for(int i=0; i<neighboursQuantity; i++){
                if(neighboursGain[i] > gainValue){
                    go = false;
                }
            }
            if(go == true){
                valueIndex=selectValueIndex;
                sendValueMessages();
            }

        }
    }

    protected void allMessageDisposed(){
        if(cycleTag == CYCLE_VALUE){
            cycleTag = CYCLE_GAIN;
            cycleValue();
        }
        else{
            cycleTag = CYCLE_VALUE;
            cycleGain();
        }

//		cycleValue();
    }

    private void increaseMode(int neighborIndex){
        int[][] tempMod = modifier.get(neighbours[neighborIndex]);
        for(int i = 0; i < domain.length; i++){
            for(int j = 0; j < neighbourDomains.get(neighbours[neighborIndex]).length; j++){
                tempMod[i][j]++;
            }
        }
        modifier.put(neighbours[neighborIndex], tempMod);
    }

    protected void localSearchCheck(){
        while(msgQueue.size() == 0){
            try {
                Thread.sleep(1);
                System.out.println("!!! sleep(1) !!!!!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(msgQueue.isEmpty() == true){
            System.out.println("!!!!! IsEmpty Judged Wrong !!!!!");
        }
    }

    protected void runFinished(){
        super.runFinished();

        HashMap<String, Object> result=new HashMap<String, Object>();
        result.put(KEY_ID, this.id);
        result.put(KEY_NAME, this.name);
        result.put(KEY_VALUE, this.domain[valueIndex]);
        result.put(KEY_NCCC, this.nccc);
        result.put(KEY_BESTCOST, this.bestCost);
        result.put(KEY_BESTCOSTINCYCLE, bestCostInCycle);

        this.msgMailer.setResult(result);
//		System.out.println("Agent "+this.name+" stopped!");
    }

    @Override
    public Object printResults(List<Map<String, Object>> results) {

        ResultCycleAls ret=new ResultCycleAls();
        int tag = 0;
        int totalCost = 0;
        int ncccTemp = 0;
        for(Map<String, Object> result : results){

            //int id_=(Integer)result.get(KEY_ID);
            //String name_=(String)result.get(KEY_NAME);
            //int value_=(Integer)result.get(KEY_VALUE);

            if(ncccTemp < (Integer)result.get(KEY_NCCC))
                ncccTemp = (Integer)result.get(KEY_NCCC);
            if(tag == 0){
                totalCost = ((Integer)result.get(KEY_BESTCOST));
                ret.bestCostInCycle=(double[])result.get(KEY_BESTCOSTINCYCLE);
                tag = 1;
            }
            //String displayStr="Agent "+name_+": id="+id_+" value="+value_;
            //System.out.println(displayStr);
        }

        System.out.println("totalCost: "+Infinity.infinityEasy((int)totalCost)+
                " nccc: "+Infinity.infinityEasy((int)ncccTemp));

        ret.nccc=(int)ncccTemp;
        ret.totalCost=(int)totalCost;
        return ret;
    }

    private void increaseNccc(){
        nccc++;
    }

    @Override
    public String easyMessageContent(Message msg, AgentCycle sender, AgentCycle receiver) {
        // TODO 自动生成的方法存根
        return "from "+sender.getName()+" to "+receiver.getName()+" type "+messageContent(msg);
    }

    public static String messageContent(Message msg){
        switch (msg.getType()) {
            case TYPE_VALUE_MESSAGE :
                int val=(Integer) msg.getValue();
                int valueIndex=val;
                return "value["+valueIndex+"]";
            case TYPE_GAIN_MESSAGE :
                int gainValue=(Integer) msg.getValue();
                return "gain["+gainValue+"]";
            default:
                return "unknown";
        }
    }

    @Override
    protected void messageLost(Message msg) {
        // TODO 自动生成的方法存根
        if(Debugger.debugOn==true)
        {
            System.out.println(Thread.currentThread().getName()+": message lost in agent "+
                    this.name+" "+this.msgMailer.easyMessageContent(msg));
        }
    }

}

