package project_files;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import kr.ac.konkuk.ccslab.cm.entity.CMGroup;
import kr.ac.konkuk.ccslab.cm.entity.CMGroupInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMServer;
import kr.ac.konkuk.ccslab.cm.entity.CMSession;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMInterestEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

public class ProjectClient extends JFrame {
	
	private JTextPane m_outTextPane;
	private JTextField m_inTextField;
	private JButton m_startStopButton;
	private JButton m_loginLogoutButton;
	private JButton m_loginLogoutButton2;
	private JButton m_composeSNSContentButton;
	private JButton m_readNewSNSContentButton;
	private JButton m_readNextSNSContentButton;
	private JButton m_readPreviousSNSContentButton;
	private JButton m_findUserButton;
	private JButton m_addFriendButton;
	private JButton m_removeFriendButton;
	private JButton m_friendsButton;
	private JButton m_friendRequestersButton;
	private JButton m_biFriendsButton;
	private MyMouseListener cmMouseListener;
	private CMClientStub m_clientStub;
	private CMWinClientEventHandler m_eventHandler;
	
	ProjectClient()
	{		
		MyKeyListener cmKeyListener = new MyKeyListener();
		MyActionListener cmActionListener = new MyActionListener();
		cmMouseListener = new MyMouseListener();
		setTitle("CM Client");
		setSize(600, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setMenus();
		setLayout(new BorderLayout());

		m_outTextPane = new JTextPane();
		m_outTextPane.setBackground(new Color(245,245,245));
		//m_outTextPane.setForeground(Color.WHITE);
		m_outTextPane.setEditable(false);

		StyledDocument doc = m_outTextPane.getStyledDocument();
		addStylesToDocument(doc);
		add(m_outTextPane, BorderLayout.CENTER);
		JScrollPane centerScroll = new JScrollPane (m_outTextPane, 
				   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		//add(centerScroll);
		getContentPane().add(centerScroll, BorderLayout.CENTER);
		
		m_inTextField = new JTextField();
		m_inTextField.addKeyListener(cmKeyListener);
		add(m_inTextField, BorderLayout.SOUTH);
		
		JPanel topButtonPanel = new JPanel();
		topButtonPanel.setBackground(new Color(220,220,220));
		topButtonPanel.setLayout(new FlowLayout());
		add(topButtonPanel, BorderLayout.NORTH);
		
		m_startStopButton = new JButton("Start Client CM");
		//m_startStopButton.setBackground(Color.LIGHT_GRAY);	// not work on Mac
		m_startStopButton.addActionListener(cmActionListener);
		m_startStopButton.setEnabled(false);
		//add(startStopButton, BorderLayout.NORTH);
		topButtonPanel.add(m_startStopButton);
		
		m_loginLogoutButton = new JButton("Login");
		m_loginLogoutButton.addActionListener(cmActionListener);
		m_loginLogoutButton.setEnabled(false);
		topButtonPanel.add(m_loginLogoutButton);
		
		//프로젝트 추가
		m_loginLogoutButton2 = new JButton("Login2");
		m_loginLogoutButton2.addActionListener(cmActionListener);
		m_loginLogoutButton2.setEnabled(false);
		topButtonPanel.add(m_loginLogoutButton2);
		
		setVisible(true);

		// create a CM object and set the event handler
		m_clientStub = new CMClientStub();
		m_eventHandler = new CMWinClientEventHandler(m_clientStub, this);
		
		// start CM
		testStartCM();
		
		m_inTextField.requestFocus();
	}
	
	private void addStylesToDocument(StyledDocument doc)
	{
		Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

		Style regularStyle = doc.addStyle("regular", defStyle);
		StyleConstants.setFontFamily(regularStyle, "SansSerif");
		
		Style boldStyle = doc.addStyle("bold", defStyle);
		StyleConstants.setBold(boldStyle, true);
		
		Style linkStyle = doc.addStyle("link", defStyle);
		StyleConstants.setForeground(linkStyle, Color.BLUE);
		StyleConstants.setUnderline(linkStyle, true);
	}
	
	public void testChat()
	{
		String strTarget = null;
		String strMessage = null;

		printMessage("====== chat\n");

		JTextField targetField = new JTextField();
		JTextField messageField = new JTextField();
		Object[] message = {
				"Target(/b, /s, /g, or /username): ", targetField,
				"Message: ", messageField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Chat Input", JOptionPane.OK_CANCEL_OPTION);
		if(option == JOptionPane.OK_OPTION)
		{
			strTarget = targetField.getText();
			strMessage = messageField.getText();
			m_clientStub.chat(strTarget, strMessage);
		}
		
		printMessage("======\n");
	}	
	
	public void testPrintGroupInfo()
	{
		// check local state
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMUser myself = interInfo.getMyself();
		
		if(myself.getState() != CMInfo.CM_SESSION_JOIN)
		{
			printMessage("You should join a session and a group.\n");
			return;
		}
		
		CMSession session = interInfo.findSession(myself.getCurrentSession());
		Iterator<CMGroup> iter = session.getGroupList().iterator();
		printMessage("---------------------------------------------------------\n");
		printMessage(String.format("%-20s%-20s%-20s%n", "group name", "multicast addr", "multicast port"));
		printMessage("---------------------------------------------------------\n");
		
		while(iter.hasNext())
		{
			CMGroupInfo gInfo = iter.next();
			printMessage(String.format("%-20s%-20s%-20d%n", gInfo.getGroupName(), gInfo.getGroupAddress()
					, gInfo.getGroupPort()));
		}
		
		return;
	}
	
	public void testCurrentUserStatus()
	{
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMUser myself = interInfo.getMyself();
		CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();

		printMessage("------ for the default server\n");
		printMessage("name("+myself.getName()+"), session("+myself.getCurrentSession()+"), group("
				+myself.getCurrentGroup()+"), udp port("+myself.getUDPPort()+"), state("
				+myself.getState()+"), attachment download scheme("+confInfo.getAttachDownloadScheme()+").\n");
		
		// for additional servers
		Iterator<CMServer> iter = interInfo.getAddServerList().iterator();
		while(iter.hasNext())
		{
			CMServer tserver = iter.next();
			if(tserver.getNonBlockSocketChannelInfo().findChannel(0) != null)
			{
				printMessage("------ for additional server["+tserver.getServerName()+"]\n");
				printMessage("current session("+tserver.getCurrentSessionName()+
						"), current group("+tserver.getCurrentGroupName()+"), state("
						+tserver.getClientState()+").");
				
			}
		}
		
		return;
	}
	
	public void testPrintCurrentChannelInfo()
	{
		printMessage("========== print current channel info\n");
		String strChannels = m_clientStub.getCurrentChannelInfo();
		printMessage(strChannels);
	}
	
	public void testMulticastChat()
	{
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
		//System.out.println("====== test multicast chat in current group");
		printMessage("====== test multicast chat in current group\n");

		// check user state
		CMUser myself = interInfo.getMyself();
		if(myself.getState() != CMInfo.CM_SESSION_JOIN)
		{
			//System.out.println("You must join a session and a group for multicasting.");
			printMessage("You must join a session and a group for multicasting.\n");
			return;
		}

		// check communication architecture
		if(!confInfo.getCommArch().equals("CM_PS"))
		{
			//System.out.println("CM must start with CM_PS mode which enables multicast per group!");
			printMessage("CM must start with CM_PS mode which enables multicast per group!\n");
			return;
		}

		// receive a user input message
		String strMessage = JOptionPane.showInputDialog("Chat Message");
		if(strMessage == null) return;
		
		// make a CMInterestEvent.USER_TALK event
		CMInterestEvent ie = new CMInterestEvent();
		ie.setID(CMInterestEvent.USER_TALK);
		ie.setHandlerSession(myself.getCurrentSession());
		ie.setHandlerGroup(myself.getCurrentGroup());
		ie.setUserName(myself.getName());
		ie.setTalk(strMessage);
		
		m_clientStub.multicast(ie, myself.getCurrentSession(), myself.getCurrentGroup());

		ie = null;
		return;
	}
	
	public void testRequestServerInfo()
	{
		printMessage("====== request additional server information\n");
		m_clientStub.requestServerInfo();
	}
	
	public void testPrintGroupInfoOfServer()
	{
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMUser myself = interInfo.getMyself();
		
		String strServerName = null;
		
		printMessage("====== print group information a designated server\n");
		strServerName = JOptionPane.showInputDialog("Input a server name: ");
		if(strServerName == null) return;
		
		if(strServerName.equals(m_clientStub.getDefaultServerName()))
		{
			testPrintGroupInfo();
			return;
		}
		
		CMServer server = interInfo.findAddServer(strServerName);
		if(server == null)
		{
			printMessage("server("+strServerName+") not found in the add-server list!\n");
			return;
		}
		
		CMSession session = server.findSession(myself.getCurrentSession());
		Iterator<CMGroup> iter = session.getGroupList().iterator();
		printMessage("---------------------------------------------------------\n");
		printMessage(String.format("%-20s%-20s%-20s%n", "group name", "multicast addr", "multicast port"));
		printMessage("---------------------------------------------------------\n");
		
		while(iter.hasNext())
		{
			CMGroupInfo gInfo = iter.next();
			printMessage(String.format("%-20s%-20s%-20d%n", gInfo.getGroupName(), gInfo.getGroupAddress()
					, gInfo.getGroupPort()));
		}

		return;
	}

	public void testMeasureInputThroughput()
	{
		String strTarget = null;
		float fSpeed = -1; // MBps
		printMessage("========== test input network throughput\n");
		
		strTarget = JOptionPane.showInputDialog("Target node (empty for the default server)");
		if(strTarget == null) 
			return;
		else if(strTarget.equals(""))
			strTarget = m_clientStub.getDefaultServerName();

		fSpeed = m_clientStub.measureInputThroughput(strTarget);
		if(fSpeed == -1)
			printMessage("Test failed!\n");
		else
			printMessage(String.format("Input network throughput from [%s] : %.2f MBps%n", strTarget, fSpeed));
	}
	
	public void testMeasureOutputThroughput()
	{
		String strTarget = null;
		float fSpeed = -1; // MBps
		printMessage("========== test output network throughput\n");
		
		strTarget = JOptionPane.showInputDialog("Target node (empty for the default server)");
		if(strTarget == null) 
			return;
		else if(strTarget.equals(""))
			strTarget = m_clientStub.getDefaultServerName();

		fSpeed = m_clientStub.measureOutputThroughput(strTarget);
		if(fSpeed == -1)
			printMessage("Test failed!\n");
		else
			printMessage(String.format("Output network throughput to [%s] : %.2f MBps%n", strTarget, fSpeed));
	}
	
	public class MyMenuListener implements ActionListener {
		public void actionPerformed(ActionEvent e)
		{
			String strMenu = e.getActionCommand();
			switch(strMenu)
			{
			case "show all menus":
				printAllMenus();
				break;
			case "start CM":
				testStartCM();
				break;
			case "terminate CM":
				testTerminateCM();
				break;
			case "connect to default server":
				testConnectionDS();
				break;
			case "disconnect from default server":
				testDisconnectionDS();
				break;
			case "connect to designated server":
				testConnectToServer();
				break;
			case "disconnect from designated server":
				testDisconnectFromServer();
				break;
			case "login to default server":
				testLoginDS();
				break;
			case "synchronously login to default server":
				testSyncLoginDS();
				break;
			case "logout from default server":
				testLogoutDS();
				break;
			case "login to designated server":
				testLoginServer();
				break;
			case "logout from designated server":
				testLogoutServer();
				break;
//			case "request session information from default server":
//				testSessionInfoDS();
//				break;
//			case "synchronously request session information from default server":
//				testSyncSessionInfoDS();
//				break;
//			case "join session of default server":
//				testJoinSession();
//				break;
//			case "synchronously join session of default server":
//				testSyncJoinSession();
//				break;
//			case "leave session of default server":
//				testLeaveSession();
//				break;
//			case "change group of default server":
//				testChangeGroup();
//				break;
//			case "print group members":
//				testPrintGroupMembers();
//				break;
//			case "request session information from designated server":
//				testRequestSessionInfoOfServer();
//				break;
//			case "join session of designated server":
//				testJoinSessionOfServer();
//				break;
//			case "leave session of designated server":
//				testLeaveSessionOfServer();
//				break;
			case "chat":
				testChat();
				break;
			case "multicast chat in current group":
				testMulticastChat();
				break;
//			case "test CMDummyEvent":
//				testDummyEvent();
//				break;
//			case "test CMUserEvent":
//				testUserEvent();
//				break;
//			case "test datagram event":
//				testDatagram();
//				break;
//			case "test user position":
//				testUserPosition();
//				break;
//			case "test sendrecv":
//				testSendRecv();
//				break;
//			case "test castrecv":
//				testCastRecv();
//				break;
//			case "test asynchronous sendrecv":
//				testAsyncSendRecv();
//				break;
//			case "test asynchronous castrecv":
//				testAsyncCastRecv();
//				break;
			case "show group information of default server":
				testPrintGroupInfo();
				break;
			case "show current user status":
				testCurrentUserStatus();
				break;
			case "show current channels":
				testPrintCurrentChannelInfo();
				break;
			case "show current server information":
				testRequestServerInfo();
				break;
			case "show group information of designated server":
				testPrintGroupInfoOfServer();
				break;
			case "measure input network throughput":
				testMeasureInputThroughput();
				break;
			case "measure output network throughput":
				testMeasureOutputThroughput();
				break;
//			case "show all configurations":
//				testPrintConfigurations();
//				break;
//			case "change configuration":
//				testChangeConfiguration();
//				break;
//			case "add channel":
//				testAddChannel();
//				break;
//			case "remove channel":
//				testRemoveChannel();
//				break;
//			case "test blocking channel":
//				testBlockingChannel();
//				break;
//			case "set file path":
//				testSetFilePath();
//				break;
//			case "request file":
//				testRequestFile();
//				break;
//			case "push file":
//				testPushFile();
//				break;
//			case "cancel receiving file":
//				cancelRecvFile();
//				break;
//			case "cancel sending file":
//				cancelSendFile();
//				break;
//			case "print sending/receiving file info":
//				printSendRecvFileInfo();
//				break;
//			case "request content list":
//				testDownloadNewSNSContent();
//				break;
//			case "request next content list":
//				testDownloadNextSNSContent();
//				break;
//			case "request previous content list":
//				testDownloadPreviousSNSContent();
//				break;
//			case "request attached file":
//				testRequestAttachedFileOfSNSContent();
//				break;
//			case "upload content":
//				testSNSContentUpload();
//				break;
//			case "register new user":
//				testRegisterUser();
//				break;
//			case "deregister user":
//				testDeregisterUser();
//				break;
//			case "find registered user":
//				testFindRegisteredUser();
//				break;
//			case "add new friend":
//				testAddNewFriend();
//				break;
//			case "remove friend":
//				testRemoveFriend();
//				break;
//			case "show friends":
//				testRequestFriendsList();
//				break;
//			case "show friend requesters":
//				testRequestFriendRequestersList();
//				break;
//			case "show bi-directional friends":
//				testRequestBiFriendsList();
//				break;
//			case "test forwarding scheme":
//				testForwarding();
//				break;
//			case "test delay of forwarding scheme":
//				testForwardingDelay();
//				break;
//			case "test repeated request of SNS content list":
//				testRepeatedSNSContentDownload();
//				break;
//			case "pull/push multiple files":
//				testSendMultipleFiles();
//				break;
//			case "split file":
//				testSplitFile();
//				break;
//			case "merge files":
//				testMergeFiles();
//				break;
//			case "distribute and merge file":
//				testDistFileProc();
//				break;
//			case "connect MQTT service":
//				testMqttConnect();
//				break;
//			case "publish":
//				testMqttPublish();
//				break;
//			case "subscribe":
//				testMqttSubscribe();
//				break;
//			case "print session info":
//				testPrintMqttSessionInfo();
//				break;
//			case "unsubscribe":
//				testMqttUnsubscribe();
//				break;
//			case "disconnect MQTT service":
//				testMqttDisconnect();
//				break;
//			case "test csc file transfer":
//				testCSCFileTransfer();
//				break;
//			case "test c2c file transfer":
//				testC2CFileTransfer();
//				break;
			}
		}
	}
	
	private void setMenus()
	{
		MyMenuListener menuListener = new MyMenuListener();
		JMenuBar menuBar = new JMenuBar();
		
		JMenu helpMenu = new JMenu("Help");
		//helpMenu.setMnemonic(KeyEvent.VK_H);
		JMenuItem showAllMenuItem = new JMenuItem("show all menus");
		showAllMenuItem.addActionListener(menuListener);
		showAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.ALT_MASK));
		
		helpMenu.add(showAllMenuItem);
		menuBar.add(helpMenu);
		
		JMenu cmNetworkMenu = new JMenu("Network Participation");
		
		JMenu startStopSubMenu = new JMenu("Start/Stop");
		JMenuItem startMenuItem = new JMenuItem("start CM");
		startMenuItem.addActionListener(menuListener);
		startStopSubMenu.add(startMenuItem);
		JMenuItem terminateMenuItem = new JMenuItem("terminate CM");
		terminateMenuItem.addActionListener(menuListener);
		startStopSubMenu.add(terminateMenuItem);
		
		cmNetworkMenu.add(startStopSubMenu);
		
		JMenu connectSubMenu = new JMenu("Connection");
		JMenuItem connDefaultMenuItem = new JMenuItem("connect to default server");
		connDefaultMenuItem.addActionListener(menuListener);
		connectSubMenu.add(connDefaultMenuItem);
		JMenuItem disconnDefaultMenuItem = new JMenuItem("disconnect from default server");
		disconnDefaultMenuItem.addActionListener(menuListener);
		connectSubMenu.add(disconnDefaultMenuItem);
		JMenuItem connDesigMenuItem = new JMenuItem("connect to designated server");
		connDesigMenuItem.addActionListener(menuListener);
		connectSubMenu.add(connDesigMenuItem);
		JMenuItem disconnDesigMenuItem = new JMenuItem("disconnect from designated server");
		disconnDesigMenuItem.addActionListener(menuListener);
		connectSubMenu.add(disconnDesigMenuItem);

		cmNetworkMenu.add(connectSubMenu);
		
		JMenu loginSubMenu = new JMenu("Login");
		JMenuItem loginDefaultMenuItem = new JMenuItem("login to default server");
		loginDefaultMenuItem.addActionListener(menuListener);
		loginDefaultMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.ALT_MASK));
		loginSubMenu.add(loginDefaultMenuItem);
		JMenuItem syncLoginDefaultMenuItem = new JMenuItem("synchronously login to default server");
		syncLoginDefaultMenuItem.addActionListener(menuListener);
		loginSubMenu.add(syncLoginDefaultMenuItem);
		JMenuItem logoutDefaultMenuItem = new JMenuItem("logout from default server");
		logoutDefaultMenuItem.addActionListener(menuListener);
		loginSubMenu.add(logoutDefaultMenuItem);
		JMenuItem loginDesigMenuItem = new JMenuItem("login to designated server");
		loginDesigMenuItem.addActionListener(menuListener);
		loginSubMenu.add(loginDesigMenuItem);
		JMenuItem logoutDesigMenuItem = new JMenuItem("logout from designated server");
		logoutDesigMenuItem.addActionListener(menuListener);
		loginSubMenu.add(logoutDesigMenuItem);

		cmNetworkMenu.add(loginSubMenu);

		JMenu sessionSubMenu = new JMenu("Session/Group");
		JMenuItem reqSessionInfoDefaultMenuItem = new JMenuItem("request session information from default server");
		reqSessionInfoDefaultMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(reqSessionInfoDefaultMenuItem);
		JMenuItem syncReqSessionInfoDefaultMenuItem = new JMenuItem("synchronously request session information "
				+ "from default server");
		syncReqSessionInfoDefaultMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(syncReqSessionInfoDefaultMenuItem);
		JMenuItem joinSessionDefaultMenuItem = new JMenuItem("join session of default server");
		joinSessionDefaultMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(joinSessionDefaultMenuItem);
		JMenuItem syncJoinSessionDefaultMenuItem = new JMenuItem("synchronously join session of default server");
		syncJoinSessionDefaultMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(syncJoinSessionDefaultMenuItem);
		JMenuItem leaveSessionDefaultMenuItem = new JMenuItem("leave session of default server");
		leaveSessionDefaultMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(leaveSessionDefaultMenuItem);
		JMenuItem changeGroupDefaultMenuItem = new JMenuItem("change group of default server");
		changeGroupDefaultMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(changeGroupDefaultMenuItem);
		JMenuItem printGroupMembersMenuItem = new JMenuItem("print group members");
		printGroupMembersMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(printGroupMembersMenuItem);
		JMenuItem reqSessionInfoDesigMenuItem = new JMenuItem("request session information from designated server");
		reqSessionInfoDesigMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(reqSessionInfoDesigMenuItem);
		JMenuItem joinSessionDesigMenuItem = new JMenuItem("join session of designated server");
		joinSessionDesigMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(joinSessionDesigMenuItem);
		JMenuItem leaveSessionDesigMenuItem = new JMenuItem("leave session of designated server");
		leaveSessionDesigMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(leaveSessionDesigMenuItem);

		cmNetworkMenu.add(sessionSubMenu);
		menuBar.add(cmNetworkMenu);
		
		JMenu cmServiceMenu = new JMenu("Services");
		
		JMenu eventSubMenu = new JMenu("Event Transmission");
		JMenuItem chatMenuItem = new JMenuItem("chat");
		chatMenuItem.addActionListener(menuListener);
		chatMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK));
		eventSubMenu.add(chatMenuItem);
		JMenuItem multicastMenuItem = new JMenuItem("multicast chat in current group");
		multicastMenuItem.addActionListener(menuListener);
		eventSubMenu.add(multicastMenuItem);
		JMenuItem dummyEventMenuItem = new JMenuItem("test CMDummyEvent");
		dummyEventMenuItem.addActionListener(menuListener);
		eventSubMenu.add(dummyEventMenuItem);
		JMenuItem userEventMenuItem = new JMenuItem("test CMUserEvent");
		userEventMenuItem.addActionListener(menuListener);
		eventSubMenu.add(userEventMenuItem);
		JMenuItem datagramMenuItem = new JMenuItem("test datagram event");
		datagramMenuItem.addActionListener(menuListener);
		eventSubMenu.add(datagramMenuItem);
		JMenuItem posMenuItem = new JMenuItem("test user position");
		posMenuItem.addActionListener(menuListener);
		eventSubMenu.add(posMenuItem);
		JMenuItem sendrecvMenuItem = new JMenuItem("test sendrecv");
		sendrecvMenuItem.addActionListener(menuListener);
		eventSubMenu.add(sendrecvMenuItem);
		JMenuItem castrecvMenuItem = new JMenuItem("test castrecv");
		castrecvMenuItem.addActionListener(menuListener);
		eventSubMenu.add(castrecvMenuItem);
		JMenuItem asyncSendRecvMenuItem = new JMenuItem("test asynchronous sendrecv");
		asyncSendRecvMenuItem.addActionListener(menuListener);
		eventSubMenu.add(asyncSendRecvMenuItem);
		JMenuItem asyncCastRecvMenuItem = new JMenuItem("test asynchronous castrecv");
		asyncCastRecvMenuItem.addActionListener(menuListener);
		eventSubMenu.add(asyncCastRecvMenuItem);
		
		cmServiceMenu.add(eventSubMenu);
		
		JMenu infoSubMenu = new JMenu("Information");
		JMenuItem groupInfoMenuItem = new JMenuItem("show group information of default server");
		groupInfoMenuItem.addActionListener(menuListener);
		infoSubMenu.add(groupInfoMenuItem);
		JMenuItem userStatMenuItem = new JMenuItem("show current user status");
		userStatMenuItem.addActionListener(menuListener);
		infoSubMenu.add(userStatMenuItem);
		JMenuItem channelInfoMenuItem = new JMenuItem("show current channels");
		channelInfoMenuItem.addActionListener(menuListener);
		infoSubMenu.add(channelInfoMenuItem);
		JMenuItem serverInfoMenuItem = new JMenuItem("show current server information");
		serverInfoMenuItem.addActionListener(menuListener);
		infoSubMenu.add(serverInfoMenuItem);
		JMenuItem groupInfoDesigMenuItem = new JMenuItem("show group information of designated server");
		groupInfoDesigMenuItem.addActionListener(menuListener);
		infoSubMenu.add(groupInfoDesigMenuItem);
		JMenuItem inThroughputMenuItem = new JMenuItem("measure input network throughput");
		inThroughputMenuItem.addActionListener(menuListener);
		infoSubMenu.add(inThroughputMenuItem);
		JMenuItem outThroughputMenuItem = new JMenuItem("measure output network throughput");
		outThroughputMenuItem.addActionListener(menuListener);
		infoSubMenu.add(outThroughputMenuItem);
		JMenuItem showAllConfMenuItem = new JMenuItem("show all configurations");
		showAllConfMenuItem.addActionListener(menuListener);
		infoSubMenu.add(showAllConfMenuItem);
		JMenuItem changeConfMenuItem = new JMenuItem("change configuration");
		changeConfMenuItem.addActionListener(menuListener);
		infoSubMenu.add(changeConfMenuItem);
		
		cmServiceMenu.add(infoSubMenu);
		
		JMenu channelSubMenu = new JMenu("Channel");
		JMenuItem addChannelMenuItem = new JMenuItem("add channel");
		addChannelMenuItem.addActionListener(menuListener);
		channelSubMenu.add(addChannelMenuItem);
		JMenuItem removeChannelMenuItem = new JMenuItem("remove channel");
		removeChannelMenuItem.addActionListener(menuListener);
		channelSubMenu.add(removeChannelMenuItem);
		JMenuItem blockChannelMenuItem = new JMenuItem("test blocking channel");
		blockChannelMenuItem.addActionListener(menuListener);
		channelSubMenu.add(blockChannelMenuItem);
		
		cmServiceMenu.add(channelSubMenu);
		
		JMenu fileTransferSubMenu = new JMenu("File Transfer");
		JMenuItem setPathMenuItem = new JMenuItem("set file path");
		setPathMenuItem.addActionListener(menuListener);
		fileTransferSubMenu.add(setPathMenuItem);
		JMenuItem reqFileMenuItem = new JMenuItem("request file");
		reqFileMenuItem.addActionListener(menuListener);
		reqFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.ALT_MASK));
		fileTransferSubMenu.add(reqFileMenuItem);
		JMenuItem pushFileMenuItem = new JMenuItem("push file");
		pushFileMenuItem.addActionListener(menuListener);
		pushFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.ALT_MASK));
		fileTransferSubMenu.add(pushFileMenuItem);
		JMenuItem cancelRecvMenuItem = new JMenuItem("cancel receiving file");
		cancelRecvMenuItem.addActionListener(menuListener);
		fileTransferSubMenu.add(cancelRecvMenuItem);
		JMenuItem cancelSendMenuItem = new JMenuItem("cancel sending file");
		cancelSendMenuItem.addActionListener(menuListener);
		fileTransferSubMenu.add(cancelSendMenuItem);
		JMenuItem printSendRecvFileInfoMenuItem = new JMenuItem("print sending/receiving file info");
		printSendRecvFileInfoMenuItem.addActionListener(menuListener);
		fileTransferSubMenu.add(printSendRecvFileInfoMenuItem);
		
		cmServiceMenu.add(fileTransferSubMenu);
		
		JMenu snsSubMenu = new JMenu("Social Network Service");
		JMenuItem reqContentMenuItem = new JMenuItem("request content list");
		reqContentMenuItem.addActionListener(menuListener);
		snsSubMenu.add(reqContentMenuItem);
		JMenuItem reqNextMenuItem = new JMenuItem("request next content list");
		reqNextMenuItem.addActionListener(menuListener);
		snsSubMenu.add(reqNextMenuItem);
		JMenuItem reqPrevMenuItem = new JMenuItem("request previous content list");
		reqPrevMenuItem.addActionListener(menuListener);
		snsSubMenu.add(reqPrevMenuItem);
		JMenuItem reqAttachMenuItem = new JMenuItem("request attached file");
		reqAttachMenuItem.addActionListener(menuListener);
		snsSubMenu.add(reqAttachMenuItem);
		JMenuItem uploadMenuItem = new JMenuItem("upload content");
		uploadMenuItem.addActionListener(menuListener);
		snsSubMenu.add(uploadMenuItem);
		
		cmServiceMenu.add(snsSubMenu);
		
		JMenu userSubMenu = new JMenu("User");
		JMenuItem regUserMenuItem = new JMenuItem("register new user");
		regUserMenuItem.addActionListener(menuListener);
		userSubMenu.add(regUserMenuItem);
		JMenuItem deregUserMenuItem = new JMenuItem("deregister user");
		deregUserMenuItem.addActionListener(menuListener);
		userSubMenu.add(deregUserMenuItem);
		JMenuItem findUserMenuItem = new JMenuItem("find registered user");
		findUserMenuItem.addActionListener(menuListener);
		userSubMenu.add(findUserMenuItem);
		JMenuItem addFriendMenuItem = new JMenuItem("add new friend");
		addFriendMenuItem.addActionListener(menuListener);
		userSubMenu.add(addFriendMenuItem);
		JMenuItem removeFriendMenuItem = new JMenuItem("remove friend");
		removeFriendMenuItem.addActionListener(menuListener);
		userSubMenu.add(removeFriendMenuItem);
		JMenuItem showFriendsMenuItem = new JMenuItem("show friends");
		showFriendsMenuItem.addActionListener(menuListener);
		userSubMenu.add(showFriendsMenuItem);
		JMenuItem showRequestersMenuItem = new JMenuItem("show friend requesters");
		showRequestersMenuItem.addActionListener(menuListener);
		userSubMenu.add(showRequestersMenuItem);
		JMenuItem showBiFriendsMenuItem = new JMenuItem("show bi-directional friends");
		showBiFriendsMenuItem.addActionListener(menuListener);
		userSubMenu.add(showBiFriendsMenuItem);
		
		cmServiceMenu.add(userSubMenu);
		
		JMenu pubsubSubMenu = new JMenu("Publish/Subscribe");
		JMenuItem connectMenuItem = new JMenuItem("connect MQTT service");
		connectMenuItem.addActionListener(menuListener);
		pubsubSubMenu.add(connectMenuItem);
		JMenuItem pubMenuItem = new JMenuItem("publish");
		pubMenuItem.addActionListener(menuListener);
		pubsubSubMenu.add(pubMenuItem);
		JMenuItem subMenuItem = new JMenuItem("subscribe");
		subMenuItem.addActionListener(menuListener);
		pubsubSubMenu.add(subMenuItem);
		JMenuItem sessionInfoMenuItem = new JMenuItem("print session info");
		sessionInfoMenuItem.addActionListener(menuListener);
		pubsubSubMenu.add(sessionInfoMenuItem);
		JMenuItem unsubMenuItem = new JMenuItem("unsubscribe");
		unsubMenuItem.addActionListener(menuListener);
		pubsubSubMenu.add(unsubMenuItem);
		JMenuItem disconMenuItem = new JMenuItem("disconnect MQTT service");
		disconMenuItem.addActionListener(menuListener);
		pubsubSubMenu.add(disconMenuItem);
		
		cmServiceMenu.add(pubsubSubMenu);
		
		JMenu otherSubMenu = new JMenu("Other CM Test");
		JMenuItem forwardMenuItem = new JMenuItem("test forwarding scheme");
		forwardMenuItem.addActionListener(menuListener);
		otherSubMenu.add(forwardMenuItem);
		JMenuItem forwardDelayMenuItem = new JMenuItem("test delay of forwarding scheme");
		forwardDelayMenuItem.addActionListener(menuListener);
		otherSubMenu.add(forwardDelayMenuItem);
		JMenuItem repeatSNSMenuItem = new JMenuItem("test repeated request of SNS content list");
		repeatSNSMenuItem.addActionListener(menuListener);
		otherSubMenu.add(repeatSNSMenuItem);
		JMenuItem multiFilesMenuItem = new JMenuItem("pull/push multiple files");
		multiFilesMenuItem.addActionListener(menuListener);
		otherSubMenu.add(multiFilesMenuItem);
		JMenuItem splitFileMenuItem = new JMenuItem("split file");
		splitFileMenuItem.addActionListener(menuListener);
		otherSubMenu.add(splitFileMenuItem);
		JMenuItem mergeFilesMenuItem = new JMenuItem("merge files");
		mergeFilesMenuItem.addActionListener(menuListener);
		otherSubMenu.add(mergeFilesMenuItem);
		JMenuItem distMergeMenuItem = new JMenuItem("distribute and merge file");
		distMergeMenuItem.addActionListener(menuListener);
		otherSubMenu.add(distMergeMenuItem);
		JMenuItem cscFtpMenuItem = new JMenuItem("test csc file transfer");
		cscFtpMenuItem.addActionListener(menuListener);
		otherSubMenu.add(cscFtpMenuItem);
		JMenuItem c2cFtpMenuItem = new JMenuItem("test c2c file transfer");
		c2cFtpMenuItem.addActionListener(menuListener);
		otherSubMenu.add(c2cFtpMenuItem);
		
		cmServiceMenu.add(otherSubMenu);

		menuBar.add(cmServiceMenu);
	
		setJMenuBar(menuBar);

	}
	
	public CMClientStub getClientStub()
	{
		return m_clientStub;
	}
	
	public CMWinClientEventHandler getClientEventHandler()
	{
		return m_eventHandler;
	}
	
	public void testConnectionDS()
	{
		printMessage("====== connect to default server\n");
		boolean ret = m_clientStub.connectToServer();
		if(ret)
		{
			printMessage("Successfully connected to the default server.\n");
		}
		else
		{
			printMessage("Cannot connect to the default server!\n");
		}
		printMessage("======\n");
	}
	
	public void testDisconnectionDS()
	{
		printMessage("====== disconnect from default server\n");
		boolean ret = m_clientStub.disconnectFromServer();
		if(ret)
		{
			printMessage("Successfully disconnected from the default server.\n");
		}
		else
		{
			printMessage("Error while disconnecting from the default server!");
		}
		printMessage("======\n");
		
		setButtonsAccordingToClientState();
		setTitle("CM Client");
	}
	
	public void printAllMenus()
	{
		printMessage("---------------------------------- Help\n");
		printMessage("0: show all menus\n");
		printMessage("---------------------------------- Start/Stop\n");
		printMessage("100: start CM, 999: terminate CM\n");
		printMessage("---------------------------------- Connection\n");
		printMessage("1: connect to default server, 2: disconnect from default server\n");
		printMessage("3: connect to designated server, 4: disconnect from designated server\n");
		printMessage("---------------------------------- Login\n");
		printMessage("10: login to default server, 11: synchronously login to default server\n");
		printMessage("12: logout from default server\n");
		printMessage("13: login to designated server, 14: logout from designated server\n");
		printMessage("---------------------------------- Session/Group\n");
		printMessage("20: request session information from default server\n");
		printMessage("21: synchronously request session information from default server\n");
		printMessage("22: join session of default server, 23: synchronously join session of default server\n");
		printMessage("24: leave session of default server, 25: change group of default server\n");
		printMessage("26: print group members\n");
		printMessage("27: request session information from designated server\n");
		printMessage("28: join session of designated server, 29: leave session of designated server\n");
		printMessage("---------------------------------- Event Transmission\n");
		printMessage("40: chat, 41: multicast chat in current group\n");
		printMessage("42: test CMDummyEvent, 43: test CMUserEvent, 44: test datagram event, 45: test user position\n");
		printMessage("46: test sendrecv, 47: test castrecv\n");
		printMessage("48: test asynchronous sendrecv, 49: test asynchronous castrecv\n");
		printMessage("---------------------------------- Information\n");
		printMessage("50: show group information of default server, 51: show current user status\n");
		printMessage("52: show current channels, 53: show current server information\n");
		printMessage("54: show group information of designated server\n");
		printMessage("55: measure input network throughput, 56: measure output network throughput\n");
		printMessage("57: show all configurations, 58: change configuration\n");
		printMessage("---------------------------------- Channel\n");
		printMessage("60: add channel, 61: remove channel, 62: test blocking channel\n");
		printMessage("---------------------------------- File Transfer\n");
		printMessage("70: set file path, 71: request file, 72: push file\n");
		printMessage("73: cancel receiving file, 74: cancel sending file\n");
		printMessage("75: print sending/receiving file info\n");
		printMessage("---------------------------------- Social Network Service\n");
		printMessage("80: request content list, 81: request next content list, 82: request previous content list\n");
		printMessage("83: request attached file, 84: upload content\n");
		printMessage("---------------------------------- User\n");
		printMessage("90: register new user, 91: deregister user, 92: find registered user\n");
		printMessage("93: add new friend, 94: remove friend, 95: show friends, 96: show friend requesters\n");
		printMessage("97: show bi-directional friends\n");
		printMessage("---------------------------------- MQTT\n");
		printMessage("200: connect, 201: publish, 202: subscribe, 203: print session info\n");
		printMessage("204: unsubscribe, 205: disconnect \n");
		printMessage("---------------------------------- Other CM Tests\n");
		printMessage("101: test forwarding scheme, 102: test delay of forwarding scheme\n");
		printMessage("103: test repeated request of SNS content list\n");
		printMessage("104: pull/push multiple files, 105: split file, 106: merge files, 107: distribute and merge file\n");
		printMessage("108: send event with wrong # bytes, 109: send event with wrong type\n");
		printMessage("110: test csc file transfer, 111: test c2c file transfer\n");
	}
	
	public void testConnectToServer()
	{
		printMessage("====== connect to a designated server\n");
		String strServerName = null;
		
		strServerName = JOptionPane.showInputDialog("Input a server name: ");
		if(strServerName != null)
			m_clientStub.connectToServer(strServerName);
		
		return;
	}

	public void testDisconnectFromServer()
	{
		printMessage("===== disconnect from a designated server\n");
		
		String strServerName = null;
		
		strServerName = JOptionPane.showInputDialog("Input a server name: ");
		if(strServerName != null)
			m_clientStub.disconnectFromServer(strServerName);

		return;
	}
	
	public void testSyncLoginDS()
	{
		String strUserName = null;
		String strPassword = null;
		CMSessionEvent loginAckEvent = null;

		printMessage("====== synchronous login to default server\n");
		JTextField userNameField = new JTextField();
		JPasswordField passwordField = new JPasswordField();
		Object[] message = {
				"User Name:", userNameField,
				"Password:", passwordField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Login Input", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION)
		{
			strUserName = userNameField.getText();
			strPassword = new String(passwordField.getPassword()); // security problem?
			
			m_eventHandler.setStartTime(System.currentTimeMillis());
			loginAckEvent = m_clientStub.syncLoginCM(strUserName, strPassword);
			long lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
			if(loginAckEvent != null)
			{
				// print login result
				if(loginAckEvent.isValidUser() == 0)
				{
					printMessage("This client fails authentication by the default server!\n");		
				}
				else if(loginAckEvent.isValidUser() == -1)
				{
					printMessage("This client is already in the login-user list!\n");
				}
				else
				{
					printMessage("return delay: "+lDelay+" ms.\n");
					printMessage("This client successfully logs in to the default server.\n");
					CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
					
					// Change the title of the client window
					setTitle("CM Client ("+interInfo.getMyself().getName()+")");

					// Set the appearance of buttons in the client frame window
					setButtonsAccordingToClientState();
				}				
			}
			else
			{
				printStyledMessage("failed the login request!\n", "bold");
			}
			
		}
		
		printMessage("======\n");		
	}
	
	public void testLoginServer()
	{
		String strServerName = null;
		String user = null;
		String password = null;
						
		printMessage("====== log in to a designated server\n");
		strServerName = JOptionPane.showInputDialog("Server Name: ");
		if(strServerName == null) return;

		if( strServerName.equals(m_clientStub.getDefaultServerName()) )	// login to a default server
		{
			JTextField userNameField = new JTextField();
			JPasswordField passwordField = new JPasswordField();
			Object[] message = {
					"User Name:", userNameField,
					"Password:", passwordField
			};
			int option = JOptionPane.showConfirmDialog(null, message, "Login Input", JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION)
			{
				user = userNameField.getText();
				String strPassword = new String(passwordField.getPassword()); // security problem?

				m_clientStub.loginCM(user, strPassword);
			}
		}
		else // use the login info for the default server
		{
			CMUser myself = m_clientStub.getCMInfo().getInteractionInfo().getMyself();
			user = myself.getName();
			password = myself.getPasswd();
			m_clientStub.loginCM(strServerName, user, password);
		}
		
		printMessage("======\n");
		
		return;
	}

	public void testLogoutServer()
	{
		String strServerName = null;
		
		printMessage("====== log out from a designated server\n");
		strServerName = JOptionPane.showInputDialog("Server Name: ");
		if(strServerName != null)
			m_clientStub.logoutCM(strServerName);
		
		printMessage("======\n");
	}
	

	public class MyKeyListener implements KeyListener {
		public void keyPressed(KeyEvent e)
		{
			int key = e.getKeyCode();
			if(key == KeyEvent.VK_ENTER)
			{
				JTextField input = (JTextField)e.getSource();
				String strText = input.getText();
				printMessage(strText+"\n");
				// parse and call CM API
//				processInput(strText);
				input.setText("");
				input.requestFocus();
			}
			else if(key == KeyEvent.VK_ALT)
			{
				
			}
		}
		
		public void keyReleased(KeyEvent e){}
		public void keyTyped(KeyEvent e){}
	}
	
	public class MyActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e)
		{
			JButton button = (JButton) e.getSource();
			if(button.getText().equals("Start Client CM"))
			{
				testStartCM();
			}
			else if(button.getText().equals("Stop Client CM"))
			{
				testTerminateCM();
			}
			else if(button.getText().equals("Login"))
			{
				// login to the default cm server
				testLoginDS();
			}
			else if(button.getText().equals("Logout"))
			{
				// logout from the default cm server
				testLogoutDS();
			}
			//프로젝트 추가
			else if(button.getText().equals("Login2")){
				testLoginDS2();
			}
//			else if(button.equals(m_composeSNSContentButton))
//			{
//				testSNSContentUpload();
//			}
//			else if(button.equals(m_readNewSNSContentButton))
//			{
//				testDownloadNewSNSContent();
//			}
//			else if(button.equals(m_readNextSNSContentButton))
//			{
//				testDownloadNextSNSContent();
//			}
//			else if(button.equals(m_readPreviousSNSContentButton))
//			{
//				testDownloadPreviousSNSContent();
//			}
//			else if(button.equals(m_findUserButton))
//			{
//				testFindRegisteredUser();
//			}
//			else if(button.equals(m_addFriendButton))
//			{
//				testAddNewFriend();
//			}
//			else if(button.equals(m_removeFriendButton))
//			{
//				testRemoveFriend();
//			}
//			else if(button.equals(m_friendsButton))
//			{
//				testRequestFriendsList();
//			}
//			else if(button.equals(m_friendRequestersButton))
//			{
//				testRequestFriendRequestersList();
//			}
//			else if(button.equals(m_biFriendsButton))
//			{
//				testRequestBiFriendsList();
//			}

			m_inTextField.requestFocus();
		}
	}
	
	public void testLoginDS()
	{
		String strUserName = null;
		String strPassword = null;
		boolean bRequestResult = false;

		printMessage("====== login to default server\n");
		JTextField userNameField = new JTextField();
		JPasswordField passwordField = new JPasswordField();
		Object[] message = {
				"User Name:", userNameField,
				"Password:", passwordField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Login Input", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION)
		{
			strUserName = userNameField.getText();
			strPassword = new String(passwordField.getPassword()); // security problem?
			
			m_eventHandler.setStartTime(System.currentTimeMillis());
			bRequestResult = m_clientStub.loginCM(strUserName, strPassword);
			long lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
			if(bRequestResult)
			{
				printMessage("successfully sent the login request.\n");
				printMessage("return delay: "+lDelay+" ms.\n");
			}
			else
			{
				printStyledMessage("failed the login request!\n", "bold");
				m_eventHandler.setStartTime(0);
			}
		}
		
		printMessage("======\n");
	}
	
	public void testLoginDS2()
	{
		String strUserName = null;
		String strPassword = null;
		boolean bRequestResult = false;

		printMessage("====== 처리량은 서버로 자동 연결하기\n");
		JTextField userNameField = new JTextField();
		JPasswordField passwordField = new JPasswordField();
		Object[] message = {
				"User Name:", userNameField,
				"Password:", passwordField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Login Input", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION)
		{
			strUserName = userNameField.getText();
			strPassword = new String(passwordField.getPassword()); // security problem?
			
			m_eventHandler.setStartTime(System.currentTimeMillis());
			bRequestResult = m_clientStub.loginCM(strUserName, strPassword);
			long lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
			if(bRequestResult)
			{
				printMessage("successfully sent the login request.\n");
				printMessage("return delay: "+lDelay+" ms.\n");
			}
			else
			{
				printStyledMessage("failed the login request!\n", "bold");
				m_eventHandler.setStartTime(0);
			}
		}
		
		printMessage("======\n");
	}
	
	public void testStartCM()
	{
		boolean bRet = false;
		
		// get current server info from the server configuration file
		String strCurServerAddress = null;
		int nCurServerPort = -1;
		
		strCurServerAddress = m_clientStub.getServerAddress();
		nCurServerPort = m_clientStub.getServerPort();
		
		// ask the user if he/she would like to change the server info
		JTextField serverAddressTextField = new JTextField(strCurServerAddress);
		JTextField serverPortTextField = new JTextField(String.valueOf(nCurServerPort));
		Object msg[] = {
				"Server Address: ", serverAddressTextField,
				"Server Port: ", serverPortTextField
		};
		int option = JOptionPane.showConfirmDialog(null, msg, "Server Information", JOptionPane.OK_CANCEL_OPTION);

		// update the server info if the user would like to do
		if (option == JOptionPane.OK_OPTION) 
		{
			String strNewServerAddress = serverAddressTextField.getText();
			int nNewServerPort = Integer.parseInt(serverPortTextField.getText());
			if(!strNewServerAddress.equals(strCurServerAddress) || nNewServerPort != nCurServerPort)
				m_clientStub.setServerInfo(strNewServerAddress, nNewServerPort);
		}
		
		bRet = m_clientStub.startCM();
		if(!bRet)
		{
			printStyledMessage("CM initialization error!\n", "bold");
		}
		else
		{
			m_startStopButton.setEnabled(true);
			m_loginLogoutButton.setEnabled(true);
			m_loginLogoutButton2.setEnabled(true);
			printStyledMessage("Client CM starts.\n", "bold");
			printStyledMessage("Type \"0\" for menu.\n", "regular");
			// change the appearance of buttons in the client window frame
			setButtonsAccordingToClientState();
		}
	}
	
	public void testTerminateCM()
	{
		//m_clientStub.disconnectFromServer();
		m_clientStub.terminateCM();
		printMessage("Client CM terminates.\n");
		// change the appearance of buttons in the client window frame
		initializeButtons();
		setTitle("CM Client");
	}
	
	public void initializeButtons()
	{
		m_startStopButton.setText("Start Client CM");
		m_loginLogoutButton.setText("Login");
		//m_leftButtonPanel.setVisible(false);
		//m_westScroll.setVisible(false);
		revalidate();
		repaint();
	}
	
	public void printStyledMessage(String strText, String strStyleName)
	{
		StyledDocument doc = m_outTextPane.getStyledDocument();
		try {
			doc.insertString(doc.getLength(), strText, doc.getStyle(strStyleName));
			m_outTextPane.setCaretPosition(m_outTextPane.getDocument().getLength());

		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return;
	}
	
	public void setButtonsAccordingToClientState()
	{
		int nClientState;
		nClientState = m_clientStub.getCMInfo().getInteractionInfo().getMyself().getState();
		
		// nclientState: CMInfo.CM_INIT, CMInfo.CM_CONNECT, CMInfo.CM_LOGIN, CMInfo.CM_SESSION_JOIN
		switch(nClientState)
		{
		case CMInfo.CM_INIT:
			m_startStopButton.setText("Stop Client CM");
			m_loginLogoutButton.setText("Login");
			//m_leftButtonPanel.setVisible(false);
			//m_westScroll.setVisible(false);
			break;
		case CMInfo.CM_CONNECT:
			m_startStopButton.setText("Stop Client CM");
			m_loginLogoutButton.setText("Login");
			//m_leftButtonPanel.setVisible(false);
			//m_westScroll.setVisible(false);
			break;
		case CMInfo.CM_LOGIN:
			m_startStopButton.setText("Stop Client CM");
			m_loginLogoutButton.setText("Logout");
			//m_leftButtonPanel.setVisible(false);
			//m_westScroll.setVisible(false);
			break;
		case CMInfo.CM_SESSION_JOIN:
			m_startStopButton.setText("Stop Client CM");
			m_loginLogoutButton.setText("Logout");
			//m_leftButtonPanel.setVisible(true);
			//m_westScroll.setVisible(true);
			break;
		default:
			m_startStopButton.setText("Start Client CM");
			m_loginLogoutButton.setText("Login");
			//m_leftButtonPanel.setVisible(false);
			//m_westScroll.setVisible(false);
			break;
		}
		revalidate();
		repaint();
	}
	
	public void printMessage(String strText)
	{
		/*
		m_outTextArea.append(strText);
		m_outTextArea.setCaretPosition(m_outTextArea.getDocument().getLength());
		*/
		StyledDocument doc = m_outTextPane.getStyledDocument();
		try {
			doc.insertString(doc.getLength(), strText, null);
			m_outTextPane.setCaretPosition(m_outTextPane.getDocument().getLength());

		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return;
	}
	
	public void testLogoutDS()
	{
		boolean bRequestResult = false;
		printMessage("====== logout from default server\n");
		bRequestResult = m_clientStub.logoutCM();
		if(bRequestResult)
			printMessage("successfully sent the logout request.\n");
		else
			printStyledMessage("failed the logout request!\n", "bold");
		printMessage("======\n");

		// Change the title of the login button
		setButtonsAccordingToClientState();
		setTitle("CM Client");
	}
	
	private void accessAttachedFile(String strFileName)
	{
		boolean bRet = m_clientStub.accessAttachedFileOfSNSContent(strFileName);
		if(!bRet)
			printMessage(strFileName+" not found in the downloaded content list!\n");
		
		return;
	}
	
	private void requestAttachedFile(String strFileName)
	{		
		boolean bRet = m_clientStub.requestAttachedFileOfSNSContent(strFileName);
		if(bRet)
			m_eventHandler.setReqAttachedFile(true);
		else
			printMessage(strFileName+" not found in the downloaded content list!\n");
			
		return;
	}
	
	public class MyMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			if(e.getSource() instanceof JLabel)
			{
				JLabel pathLabel = (JLabel)e.getSource();
				String strPath = pathLabel.getText();
				File fPath = new File(strPath);
				try {
					int index = strPath.lastIndexOf(File.separator);
					String strFileName = strPath.substring(index+1, strPath.length()); 
					if(fPath.exists())
					{
						accessAttachedFile(strFileName);
						Desktop.getDesktop().open(fPath);
					}
					else
					{
						requestAttachedFile(strFileName);
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			if(e.getSource() instanceof JLabel)
			{
				Cursor cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
				setCursor(cursor);
			}
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			if(e.getSource() instanceof JLabel)
			{
				Cursor cursor = Cursor.getDefaultCursor();
				setCursor(cursor);
			}
		}
		
	}
	
	public static void main(String[] args) {
		ProjectClient client = new ProjectClient();
		CMClientStub cmStub = client.getClientStub();
		cmStub.setAppEventHandler(client.getClientEventHandler());
	}

}
