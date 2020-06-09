package kr.ac.konkuk.ccslab.cm.event;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import kr.ac.konkuk.ccslab.cm.entity.CMServerInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;

public class ServerListEvent extends CMEvent {

   private Vector<CMServerInfo> p_serverList;
   private String recStr = "";
   private String filePath = "DB.txt";
   private CMServerInfo cs;
   private HashMap<String, Integer> serverHashMap;

   public ServerListEvent() {
      m_nType = CMInfo.CM_SERVER_LIST_EVENT;
      p_serverList = new Vector<CMServerInfo>();
      cs = new CMServerInfo();
      serverHashMap = new HashMap<>();
   }

   public ServerListEvent(ByteBuffer msg) {
      this();
      unmarshall(msg);
   }

   public ServerListEvent(Vector<CMServerInfo> s_list) {
      this();
   }
   public ServerListEvent(String m_strServerName, String m_strServerAddress, int m_nServerPort, int m_nServerUDPPort, int cnt) {
      this();
      recStr = m_strServerName+m_strServerAddress+m_nServerPort+m_nServerPort+cnt;
      serverHashMap.put(m_strServerAddress, cnt);
   }

   public void setServerListInfo(Vector<CMServerInfo> s_list) {
      if (s_list != null) {
         p_serverList = s_list;
      }
      for (int i = 0; i < s_list.size(); i++) {
         serverHashMap.put(s_list.get(i).getServerAddress(), 0);
         recStr += (s_list.get(i)+serverHashMap.get(s_list.get(i).getServerAddress()).toString());
      }
   }
   
   public void setServerUserCount(String serverIp/*serverName*/,Integer cnt) {
      serverHashMap.put(serverIp, cnt);
   }

   public Vector<CMServerInfo> getServerListInfo() {
      return p_serverList;
   }
   
   public HashMap<String,Integer> getServerHashMap() {
      return serverHashMap;
   }

   protected int getByteNum() {
      int nByteNum = 0;
      nByteNum = super.getByteNum(); // get header length
//      System.out.println(recStr);
//      System.out.println(CMInfo.STRING_LEN_BYTES_LEN);
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
      String[] arr = recStr.split(", ");
      System.out.println(arr[0]+"," + arr[1] + "," + arr[2] + "," + arr[3]+","+arr[4]);
      cs.setServerAddress(arr[0]);
      cs.setServerName(arr[1]);
      cs.setServerPort(Integer.parseInt(arr[2]));
      //cs.setServerUDPPort(Integer.parseInt(arr[3]));
      p_serverList.add(cs);
      serverHashMap.put(arr[1], Integer.parseInt(arr[4]));
   }

   public String getRecStr() {
      return recStr;
   }

   public void readServerList() {
      // TODO Auto-generated method stub
      recStr = "";
      BufferedReader in = null;
      try {
         in = new BufferedReader(new FileReader(filePath));
         String s;
         while ((s = in.readLine()) != null) {
            recStr += (s + "\n");
         }
         in.close();
         System.out.print(recStr);
      } catch (Exception e) {
      }
   }
}