package kr.ac.konkuk.ccslab.cm.event;

import java.nio.ByteBuffer;
import java.util.Vector;

import kr.ac.konkuk.ccslab.cm.entity.CMServerInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;

public class ServerListEvent extends CMEvent{
	
	private Vector<CMServerInfo> p_serverList;
	private String recStr;
	
	public ServerListEvent() {
		m_nType = CMInfo.CM_SERVER_LIST_EVENT;
		p_serverList = new Vector<CMServerInfo>();
	}
	
	
	public ServerListEvent(ByteBuffer msg)
	{
		this();
		unmarshall(msg);
	}
	
	public ServerListEvent(Vector<CMServerInfo> s_list) {
		this();
	}
	
	public void setServerListInfo(Vector<CMServerInfo> s_list) {
		if(s_list!=null){
			p_serverList = s_list;
		}
		for(int i=0;i<s_list.size();i++) {
			recStr += s_list.get(i);
		}
	}
	
	public Vector<CMServerInfo> getServerListInfo(){
		return p_serverList;
	}
	
	protected int getByteNum()
	{
		int nByteNum = 0;
		nByteNum = super.getByteNum(); // get header length
		
		nByteNum += CMInfo.STRING_LEN_BYTES_LEN + recStr.getBytes().length; // get body length
		
		return nByteNum;
	}

	@Override
	protected void marshallBody() {
		// TODO Auto-generated method stub
		putStringToByteBuffer(recStr);
	}

	@Override
	protected void unmarshallBody(ByteBuffer msg) {
		// TODO Auto-generated method stub
		recStr = getStringFromByteBuffer(msg);
	}

	public void tempSetRecStr(String str) {
		// TODO Auto-generated method stub
		recStr = str;
	}
}
