package org.cqu.core.control;

/**
 * 0-1000为基类消息类型定义区间</br>
 * 1001-2000为扩展子类消息类型定义区间</br>
 * 9999为TERMINATE消息
 * @author hz
 *
 */
public class MessageTypes {
	
	public final static int VALUE=0;
	public final static int COST=1;
	public final static int THRESHOLD=2;
	public final static int UTILITY=3;
	public final static int NOGOOD=4;
	public final static int CPA=5;
	public final static int TERMINATE=9999;

}
