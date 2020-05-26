package kr.ac.konkuk.ccslab.cm.event;

import java.nio.ByteBuffer;
import java.util.Vector;

import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;

public class ServerListEvent extends CMEvent{
	
	private Vector<CMUser> m_serverList;
	private String recStr;
	
	public ServerListEvent() {
		m_nType = CMInfo.CM_SERVER_LIST_EVENT;
		m_serverList = new Vector<CMUser>();
	}
	
	public ServerListEvent(Vector<CMUser> s_list) {
		this();
	}
	
	public void setServerListInfo(Vector<CMUser> s_list) {
		if(s_list!=null){
			m_serverList = s_list;
		}
		for(int i=0;i<s_list.size();i++) {
			recStr += s_list.get(i);
		}
	}
	
	public Vector<CMUser> getServerListInfo(){
		return m_serverList;
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
}
