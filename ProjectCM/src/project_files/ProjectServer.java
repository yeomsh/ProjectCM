package project_files;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
import kr.ac.konkuk.ccslab.cm.entity.CMMember;
import kr.ac.konkuk.ccslab.cm.entity.CMServerInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMSession;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMCommManager;
import kr.ac.konkuk.ccslab.cm.manager.CMConfigurator;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSUserAccessSimulator;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

public class ProjectServer extends JFrame {

	private JTextPane m_outTextPane;
	private JTextField m_inTextField;
	private JButton m_startStopButton;
	private CMServerStub m_serverStub;
	private CMWinServerEventHandler m_eventHandler;
	private CMSNSUserAccessSimulator m_uaSim;
	Vector<CMServerInfo> m_addServerList;
	
	ProjectServer()
	{
		
		MyKeyListener cmKeyListener = new MyKeyListener();
		MyActionListener cmActionListener = new MyActionListener();
		setTitle("CM Server");
		setSize(500, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setMenus();
		setLayout(new BorderLayout());
		
		m_outTextPane = new JTextPane();
		m_outTextPane.setEditable(false);

		StyledDocument doc = m_outTextPane.getStyledDocument();
		addStylesToDocument(doc);

		add(m_outTextPane, BorderLayout.CENTER);
		JScrollPane scroll = new JScrollPane (m_outTextPane, 
				   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		add(scroll);
		
		m_inTextField = new JTextField();
		m_inTextField.addKeyListener(cmKeyListener);
		add(m_inTextField, BorderLayout.SOUTH);
		
		JPanel topButtonPanel = new JPanel();
		topButtonPanel.setLayout(new FlowLayout());
		add(topButtonPanel, BorderLayout.NORTH);
		
		m_startStopButton = new JButton("Start Server CM");
		m_startStopButton.addActionListener(cmActionListener);
		m_startStopButton.setEnabled(false);
		//add(startStopButton, BorderLayout.NORTH);
		topButtonPanel.add(m_startStopButton);
		
		setVisible(true);

		// create CM stub object and set the event handler
		m_serverStub = new CMServerStub();
		m_eventHandler = new CMWinServerEventHandler(m_serverStub, this);
		m_uaSim = new CMSNSUserAccessSimulator();

		// start cm
		startCM();		
	}
	
	public void printAllMenus()
	{
		printMessage("---------------------------------- Help\n");
		printMessage("0: show all menus\n");
		printMessage("---------------------------------- Start/Stop\n");
		printMessage("100: start CM, 999: terminate CM\n");
		printMessage("---------------------------------- Information\n");
		printMessage("1: show session information, 2: show group information\n");
		printMessage("3: test input network throughput, 4: test output network throughput\n");
		printMessage("5: show current channels, 6: show login users\n");
		printMessage("7: show all configurations, 8: change configuration\n");
		printMessage("---------------------------------- File Transfer\n");
		printMessage("20: set file path, 21: request file, 22: push file\n");
		printMessage("23: cancel receiving file, 24: cancel sending file\n");
		printMessage("25: print sending/receiving file info\n");
		printMessage("---------------------------------- Multi-server\n");
		printMessage("30: register to default server, 31: deregister from default server\n");
		printMessage("32: connect to default server, 33: disconnect from default server\n");
		printMessage("---------------------------------- Social Network Service\n");
		printMessage("40: set attachment download scheme\n");
		printMessage("---------------------------------- Channel\n");
		printMessage("50: add channel, 51: remove channel\n");
		printMessage("---------------------------------- MQTT\n");
		printMessage("60: find session info, 61: print all session info, 62: print all retain info\n");
		printMessage("---------------------------------- Other CM Tests\n");
		printMessage("101: configure SNS user access simulation, 102: start SNS user access simulation\n");
		printMessage("103: start SNS user access simulation and measure prefetch accuracy\n");
		printMessage("104: start and write recent SNS access history simulation to CM DB\n");
		printMessage("105: send event with wrong bytes, 106: send event with wrong type\n");
	}
	
	public void terminateCM()
	{
		m_serverStub.terminateCM();
		printMessage("Server CM terminates.\n");
		m_startStopButton.setText("Start Server CM");
		updateTitle();
	}
	
	public void connectToDefaultServer()
	{
		printMessage("====== connect to the default server\n");
		boolean bRet = m_serverStub.connectToServer();
		printMessage("======\n");
		if(bRet)
			updateTitle();
		return;
	}
	
	public void disconnectFromDefaultServer()
	{
		printMessage("====== disconnect from the default server\n");
		boolean bRet = m_serverStub.disconnectFromServer();
		printMessage("======\n");
		if(bRet)
			updateTitle();
		return;
	}
	
	public void requestServerReg()
	{
		String strServerName = null;

		printMessage("====== request registration to the default server\n");
		strServerName = JOptionPane.showInputDialog("Enter registered server name");
		if(strServerName != null)
		{
			m_serverStub.requestServerReg(strServerName);
		}

		printMessage("======\n");
		return;
	}

	public void requestServerDereg()
	{
		printMessage("====== request deregistration from the default server\n");
		boolean bRet = m_serverStub.requestServerDereg();
		printMessage("======\n");
		if(bRet)
			updateTitle();
		
		return;
	}
	
	public void printSessionInfo()
	{
		printMessage("------------------------------------------------------\n");
		printMessage(String.format("%-20s%-20s%-10s%-10s%n", "session name", "session addr", "port", "#users"));
		printMessage("------------------------------------------------------\n");
		
		CMInteractionInfo interInfo = m_serverStub.getCMInfo().getInteractionInfo();
		Iterator<CMSession> iter = interInfo.getSessionList().iterator();
		while(iter.hasNext())
		{
			CMSession session = iter.next();
			printMessage(String.format("%-20s%-20s%-10d%-10d%n", session.getSessionName(), session.getAddress()
					, session.getPort(), session.getSessionUsers().getMemberNum()));
		}
		return;
	}
	
	public void printGroupInfo()
	{
		String strSessionName = null;
		
		printMessage("====== print group information\n");
		strSessionName = JOptionPane.showInputDialog("Session Name");
		if(strSessionName == null)
		{
			return;
		}
		
		CMInteractionInfo interInfo = m_serverStub.getCMInfo().getInteractionInfo();
		CMSession session = interInfo.findSession(strSessionName);
		if(session == null)
		{
			printMessage("Session("+strSessionName+") not found.\n");
			return;
		}
		
		printMessage("------------------------------------------------------------------\n");
		printMessage(String.format("%-20s%-20s%-10s%-10s%n", "group name", "multicast addr", "port", "#users"));
		printMessage("------------------------------------------------------------------\n");

		Iterator<CMGroup> iter = session.getGroupList().iterator();
		while(iter.hasNext())
		{
			CMGroup gInfo = iter.next();
			printMessage(String.format("%-20s%-20s%-10d%-10d%n", gInfo.getGroupName(), gInfo.getGroupAddress()
					, gInfo.getGroupPort(), gInfo.getGroupUsers().getMemberNum()));
		}

		printMessage("======\n");
		return;
	}
	
	public void printCurrentChannelInfo()
	{
		printMessage("========== print current channel info\n");
		String strChannels = m_serverStub.getCurrentChannelInfo();
		printMessage(strChannels);
	}
	
	public void printLoginUsers()
	{
		printMessage("========== print login users\n");
		CMMember loginUsers = m_serverStub.getLoginUsers();
		if(loginUsers == null)
		{
			printStyledMessage("The login users list is null!\n", "bold");
			return;
		}
		
		printMessage("Currently ["+loginUsers.getMemberNum()+"] users are online.\n");
		Vector<CMUser> loginUserVector = loginUsers.getAllMembers();
		Iterator<CMUser> iter = loginUserVector.iterator();
		int nPrintCount = 0;
		while(iter.hasNext())
		{
			CMUser user = iter.next();
			printMessage(user.getName()+" ");
			nPrintCount++;
			if((nPrintCount % 10) == 0)
			{
				printMessage("\n");
				nPrintCount = 0;
			}
		}
	}
	
	public void printConfigurations()
	{
		String[] strConfigurations;
		printMessage("========== print all current configurations\n");
		Path confPath = m_serverStub.getConfigurationHome().resolve("cm-server.conf");
		strConfigurations = CMConfigurator.getConfigurations(confPath.toString());
		
		printMessage("configuration file path: "+confPath.toString()+"\n");
		for(String strConf : strConfigurations)
		{
			String[] strFieldValuePair;
			strFieldValuePair = strConf.split("\\s+");
			printMessage(strFieldValuePair[0]+" = "+strFieldValuePair[1]+"\n");
		}
	}
	
	public void measureInputThroughput()
	{
		String strTarget = null;
		float fSpeed = -1; // MBps
		printMessage("========== test input network throughput\n");
		
		strTarget = JOptionPane.showInputDialog("Target node");
		if(strTarget == null || strTarget.equals("")) 
			return;

		fSpeed = m_serverStub.measureInputThroughput(strTarget);
		if(fSpeed == -1)
			printMessage("Test failed!\n");
		else
			printMessage(String.format("Input network throughput from [%s] : %.2f MBps%n", strTarget, fSpeed));
	}
	
	public void measureOutputThroughput()
	{
		String strTarget = null;
		float fSpeed = -1; // MBps
		printMessage("========== test output network throughput\n");
		
		strTarget = JOptionPane.showInputDialog("Target node");
		if(strTarget == null || strTarget.equals("")) 
			return;

		fSpeed = m_serverStub.measureOutputThroughput(strTarget);
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
				startCM();
				break;
			case "terminate CM":
				terminateCM();
				break;
			case "connect to default server":
				connectToDefaultServer();
				break;
			case "disconnect from default server":
				disconnectFromDefaultServer();
				break;
			case "register to default server":
				requestServerReg();
				break;
			case "deregister from default server":
				requestServerDereg();
				break;
			case "show session information":
				printSessionInfo();
				break;
			case "show group information":
				printGroupInfo();
				break;
			case "show current channels":
				printCurrentChannelInfo();
				break;
			case "show login users":
				printLoginUsers();
				break;
			case "show all configurations":
				printConfigurations();
				break;
//			case "change configuration":
//				changeConfiguration();
//				break;
			case "test input network throughput":
				measureInputThroughput();
				break;
			case "test output network throughput":
				measureOutputThroughput();
				break;
			case "send CMDummyEvent":
				sendCMDummyEvent();
				break;
				
//			case "set file path":
//				setFilePath();
//				break;
//			case "request file":
//				requestFile();
//				break;
//			case "push file":
//				pushFile();
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
//			case "set attachment download scheme":
//				setAttachDownloadScheme();
//				break;
//			case "add channel":
//				addChannel();
//				break;
//			case "remove channel":
//				removeChannel();
//				break;
//			case "configure SNS user access simulation":
//				configureUserAccessSimulation();
//				break;
//			case "start SNS user access simulation":
//				startUserAccessSimulation();
//				break;
//			case "start SNS user access simulation and measure prefetch accuracy":
//				startUserAccessSimulationAndCalPrecRecall();
//				break;
//			case "start and write recent SNS access history simulation to CM DB":
//				writeRecentAccHistoryToDB();
//				break;
//			case "find session info":
//				findMqttSessionInfo();
//				break;
//			case "print all session info":
//				printAllMqttSessionInfo();
//				break;
//			case "print all retain info":
//				printAllMqttRetainInfo();
//				break;
			}
		}
	}
	
	public void startCM()
	{
		boolean bRet = false;
		
		// get current server info from the server configuration file
		String strSavedServerAddress = null;
		String strCurServerAddress = null;
		int nSavedServerPort = -1;
		
		strSavedServerAddress = m_serverStub.getServerAddress();
		strCurServerAddress = CMCommManager.getLocalIP();
		nSavedServerPort = m_serverStub.getServerPort();
		
		// ask the user if he/she would like to change the server info
		JTextField serverAddressTextField = new JTextField(strCurServerAddress);
		JTextField serverPortTextField = new JTextField(String.valueOf(nSavedServerPort));
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
			if(!strNewServerAddress.equals(strSavedServerAddress) || nNewServerPort != nSavedServerPort)
				m_serverStub.setServerInfo(strNewServerAddress, nNewServerPort);
		}
		
		// start cm
		bRet = m_serverStub.startCM();
		if(!bRet)
		{
			printStyledMessage("CM initialization error!\n", "bold");
		}
		else
		{
			printStyledMessage("Server CM starts.\n", "bold");
			printMessage("Type \"0\" for menu.\n");					
			// change button to "stop CM"
			m_startStopButton.setEnabled(true);
			m_startStopButton.setText("Stop Server CM");
			updateTitle();					
		}

		m_inTextField.requestFocus();

	}
	
	private void addStylesToDocument(StyledDocument doc)
	{
		Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

		Style regularStyle = doc.addStyle("regular", defStyle);
		StyleConstants.setFontFamily(regularStyle, "SansSerif");
		
		Style boldStyle = doc.addStyle("bold", defStyle);
		StyleConstants.setBold(boldStyle, true);
	}
	
	public void setMenus()
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

		JMenu multiServerSubMenu = new JMenu("Multi-server");
		JMenuItem connectDefaultMenuItem = new JMenuItem("connect to default server");
		connectDefaultMenuItem.addActionListener(menuListener);
		multiServerSubMenu.add(connectDefaultMenuItem);
		JMenuItem disconnectDefaultMenuItem = new JMenuItem("disconnect from default server");
		disconnectDefaultMenuItem.addActionListener(menuListener);
		multiServerSubMenu.add(disconnectDefaultMenuItem);
		JMenuItem regDefaultMenuItem = new JMenuItem("register to default server");
		regDefaultMenuItem.addActionListener(menuListener);
		multiServerSubMenu.add(regDefaultMenuItem);
		JMenuItem deregDefaultMenuItem = new JMenuItem("deregister from default server");
		deregDefaultMenuItem.addActionListener(menuListener);
		multiServerSubMenu.add(deregDefaultMenuItem);
		
		cmNetworkMenu.add(multiServerSubMenu);
		menuBar.add(cmNetworkMenu);
		
		JMenu serviceMenu = new JMenu("Services");
		
		JMenu infoSubMenu = new JMenu("Information");
		JMenuItem showSessionMenuItem = new JMenuItem("show session information");
		showSessionMenuItem.addActionListener(menuListener);
		infoSubMenu.add(showSessionMenuItem);
		JMenuItem showGroupMenuItem = new JMenuItem("show group information");
		showGroupMenuItem.addActionListener(menuListener);
		infoSubMenu.add(showGroupMenuItem);
		JMenuItem showChannelMenuItem = new JMenuItem("show current channels");
		showChannelMenuItem.addActionListener(menuListener);
		infoSubMenu.add(showChannelMenuItem);
		JMenuItem showUsersMenuItem = new JMenuItem("show login users");
		showUsersMenuItem.addActionListener(menuListener);
		infoSubMenu.add(showUsersMenuItem);
		JMenuItem inputThroughputMenuItem = new JMenuItem("test input network throughput");
		inputThroughputMenuItem.addActionListener(menuListener);
		infoSubMenu.add(inputThroughputMenuItem);
		JMenuItem outputThroughputMenuItem = new JMenuItem("test output network throughput");
		outputThroughputMenuItem.addActionListener(menuListener);
		infoSubMenu.add(outputThroughputMenuItem);
		JMenuItem showAllConfMenuItem = new JMenuItem("show all configurations");
		showAllConfMenuItem.addActionListener(menuListener);
		infoSubMenu.add(showAllConfMenuItem);
		JMenuItem changeConfMenuItem = new JMenuItem("change configuration");
		changeConfMenuItem.addActionListener(menuListener);
		infoSubMenu.add(changeConfMenuItem);
		
		serviceMenu.add(infoSubMenu);
		
		serviceMenu.add(infoSubMenu);
		
		JMenu eventTransmissionSubMenu = new JMenu("Event Transmission");
		JMenuItem sendDummyEventMenuItem = new JMenuItem("send CMDummyEvent");
		sendDummyEventMenuItem.addActionListener(menuListener);
		eventTransmissionSubMenu.add(sendDummyEventMenuItem);
		
		serviceMenu.add(eventTransmissionSubMenu);
		
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
		
		serviceMenu.add(fileTransferSubMenu);
		
		JMenu snsSubMenu = new JMenu("Social Network Service");
		JMenuItem attachSchemeMenuItem = new JMenuItem("set attachment download scheme");
		attachSchemeMenuItem.addActionListener(menuListener);
		snsSubMenu.add(attachSchemeMenuItem);		

		serviceMenu.add(snsSubMenu);
		
		JMenu channelSubMenu = new JMenu("Channel");
		JMenuItem addChannelMenuItem = new JMenuItem("add channel");
		addChannelMenuItem.addActionListener(menuListener);
		channelSubMenu.add(addChannelMenuItem);
		JMenuItem removeChannelMenuItem = new JMenuItem("remove channel");
		removeChannelMenuItem.addActionListener(menuListener);
		channelSubMenu.add(removeChannelMenuItem);
		
		serviceMenu.add(channelSubMenu);
		
		JMenu pubsubSubMenu = new JMenu("Publish/Subscribe");
		JMenuItem findSessionMenuItem = new JMenuItem("find session info");
		findSessionMenuItem.addActionListener(menuListener);
		pubsubSubMenu.add(findSessionMenuItem);
		JMenuItem printAllSessionMenuItem = new JMenuItem("print all session info");
		printAllSessionMenuItem.addActionListener(menuListener);
		pubsubSubMenu.add(printAllSessionMenuItem);
		JMenuItem printAllRetainInfoMenuItem = new JMenuItem("print all retain info");
		printAllRetainInfoMenuItem.addActionListener(menuListener);
		pubsubSubMenu.add(printAllRetainInfoMenuItem);
		
		serviceMenu.add(pubsubSubMenu);		
		
		JMenu otherSubMenu = new JMenu("Other CM Tests");
		JMenuItem configUserAccessSimMenuItem = new JMenuItem("configure SNS user access simulation");
		configUserAccessSimMenuItem.addActionListener(menuListener);
		otherSubMenu.add(configUserAccessSimMenuItem);
		JMenuItem startUserAccessSimMenuItem = new JMenuItem("start SNS user access simulation");
		startUserAccessSimMenuItem.addActionListener(menuListener);
		otherSubMenu.add(startUserAccessSimMenuItem);
		JMenuItem prefetchAccSimMenuItem = new JMenuItem("start SNS user access simulation and measure prefetch accuracy");
		prefetchAccSimMenuItem.addActionListener(menuListener);
		otherSubMenu.add(prefetchAccSimMenuItem);
		JMenuItem recentAccHistorySimMenuItem = new JMenuItem("start and write recent SNS access history simulation to CM DB");
		recentAccHistorySimMenuItem.addActionListener(menuListener);
		otherSubMenu.add(recentAccHistorySimMenuItem);
		
		serviceMenu.add(otherSubMenu);		
		menuBar.add(serviceMenu);
		
		setJMenuBar(menuBar);
	}
	public void sendCMDummyEvent()
	{
		String strMessage = null;
		String strTarget = null;
		String strSession = null;
		String strGroup = null;
		CMDummyEvent de = null;
		printMessage("====== test event transmission\n");

		JTextField messageField = new JTextField();
		JTextField targetField = new JTextField();
		JTextField sessionField = new JTextField();
		JTextField groupField = new JTextField();
		
		Object[] msg = {
			"Dummy message: ", messageField,
			"Target name (for send()): ", targetField,
			"Target session (for cast() or broadcast()): ", sessionField,
			"Target group (for cast() or broadcast()): ", groupField
		};
		int option = JOptionPane.showConfirmDialog(null, msg, "CMDummyEvent Transmission", 
				JOptionPane.OK_CANCEL_OPTION);
		if(option == JOptionPane.OK_OPTION)
		{
			strMessage = messageField.getText().trim();
			strTarget = targetField.getText().trim();
			strSession = sessionField.getText().trim();
			strGroup = groupField.getText().trim();
			
			if(strMessage.isEmpty())
			{
				printStyledMessage("No input message\n", "bold");
				return;
			}
			
			de = new CMDummyEvent();
			de.setDummyInfo(strMessage);
			de.setHandlerSession(strSession);
			de.setHandlerGroup(strGroup);
			
			if(!strTarget.isEmpty())
			{
				m_serverStub.send(de, strTarget);
			}
			else
			{
				if(strSession.isEmpty()) strSession = null;
				if(strGroup.isEmpty()) strGroup = null;
				m_serverStub.cast(de, strSession, strGroup);
			}
		}
			
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
		}
		
		public void keyReleased(KeyEvent e){}
		public void keyTyped(KeyEvent e){}
	}
	
	public class MyActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e)
		{
			JButton button = (JButton) e.getSource();
			if(button.getText().equals("Start Server CM"))
			{
				// start cm
				boolean bRet = m_serverStub.startCM();
				if(!bRet)
				{
					printStyledMessage("CM initialization error!\n", "bold");
				}
				else
				{
					printStyledMessage("Server CM starts.\n", "bold");
					printMessage("Type \"0\" for menu.\n");					
					// change button to "stop CM"
					button.setText("Stop Server CM");
				}
				// check if default server or not
				if(CMConfigurator.isDServer(m_serverStub.getCMInfo()))
				{
					setTitle("CM Default Server (\"SERVER\")");
				}
				else
				{
					setTitle("CM Additional Server (\"?\")");
				}					
				m_inTextField.requestFocus();
			}
			else if(button.getText().equals("Stop Server CM"))
			{
				// stop cm
				m_serverStub.terminateCM();
				printMessage("Server CM terminates.\n");
				// change button to "start CM"
				button.setText("Start Server CM");
			}
		}
	}
	
	public static void main(String[] args) {
		ProjectServer server = new ProjectServer();
		CMServerStub cmStub = server.getServerStub();
		cmStub.setAppEventHandler(server.getServerEventHandler());
	}
	
	public CMServerStub getServerStub()
	{
		return m_serverStub;
	}
	
	public CMWinServerEventHandler getServerEventHandler()
	{
		return m_eventHandler;
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
	
	public void updateTitle()
	{
		CMUser myself = m_serverStub.getMyself();
		if(CMConfigurator.isDServer(m_serverStub.getCMInfo()))
		{
			setTitle("CM Default Server [\""+myself.getName()+"\"]");
		}
		else
		{
			if(myself.getState() < CMInfo.CM_LOGIN)
			{
				setTitle("CM Additional Server [\"?\"]");
			}
			else
			{
				setTitle("CM Additional Server [\""+myself.getName()+"\"]");
			}			
		}
	}
	
	public void printMessage(String strText)
	{
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

}
