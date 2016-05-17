package com.cqu.benchmark;

import com.cqu.benchmark.scalefreenetworks.ScaleFreeNetworkGenerator;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by YanChenDeng on 2016/4/18.
 */
public class ContentWriter {

    public static final String PROBLEM_SCALE_FREE_NETWORK = "scale free network";

    private int nbInstance;
    private String dirPath;
    private int nbAgent;
    private int domainSize;
    private int minCost;
    private int maxCost;
    private String problemType;
    private Map<String,Object> extraParameter;

    public ContentWriter(int nbInstance, String dirPath, int nbAgent, int domainSize, int minCost, int maxCost, String problemType,Map<String,Object> extraParameter) {
        this.nbInstance = nbInstance;
        this.dirPath = dirPath;
        this.nbAgent = nbAgent;
        this.domainSize = domainSize;
        this.minCost = minCost;
        this.maxCost = maxCost;
        this.problemType = problemType;
        this.extraParameter = extraParameter;
    }

    public void setExtraParameter(Map<String, Object> extraParameter) {
        this.extraParameter = extraParameter;
    }

    public void generate() throws Exception{
        Format format = Format.getPrettyFormat();
        format.setEncoding("UTF-8");
        XMLOutputter outputter = new XMLOutputter(format);
        int base = 0;
        String filenameBase =dirPath + "\\" + problemType + "_" + nbAgent + "_" + domainSize + "_";
        for (String key : extraParameter.keySet()){
            filenameBase += key + "_";
            filenameBase += extraParameter.get(key) + "_";
        }
        while (true){
            String fileName = filenameBase + base + ".xml";
            if (!new File(fileName).exists())
                break;
            base++;
        }
        for (int i = 0; i < nbInstance; i++){
            FileOutputStream stream = new FileOutputStream(filenameBase+ (base + i) + ".xml");
            Element root = new Element("instance");
            if (problemType.equals(PROBLEM_SCALE_FREE_NETWORK)) {
                AbstractGraph graph = new ScaleFreeNetworkGenerator("instance" + i,nbAgent,domainSize,minCost,maxCost,(Integer)extraParameter.get("m1"),(Integer)extraParameter.get("m2"));
                graph.generateConstraint();
                root.addContent(graph.getPresentation());
                root.addContent(graph.getAgents());
                root.addContent(graph.getDomains());
                root.addContent(graph.getVariables());
                root.addContent(graph.getConstraints());
                root.addContent(graph.getRelations());
                root.addContent(graph.getGuiPresentation());

            }
            outputter.output(root,stream);
            stream.close();
        }
    }

    public static void main(String[] args) throws Exception{
        Map<String,Object> para = new HashMap<>();
        para.put("m1",2);
        para.put("m2",1);
        ContentWriter writer = new ContentWriter(1,"C:\\Users\\YanChenDeng\\Desktop\\新建文件夹",15,9,1,10,PROBLEM_SCALE_FREE_NETWORK,para);
        writer.generate();
    }
}
