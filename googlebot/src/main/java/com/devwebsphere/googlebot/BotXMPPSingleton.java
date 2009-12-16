//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and 
//study, (b) in order to develop applications designed to run with an IBM 
//WebSphere product, either for customer's own internal use or for redistribution 
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2005
//All Rights Reserved * Licensed Materials - Property of IBM
//
package com.devwebsphere.googlebot;

import java.util.StringTokenizer;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;

import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.plugins.ObjectGridEventGroup;
import com.ibm.websphere.objectgrid.plugins.ObjectGridEventListener;
import com.ibm.websphere.objectgrid.plugins.ObjectGridEventGroup.ShardEvents;

/**
 * This bean is created when the primary for the singleton is activated in a container JVM.
 * The shardActivated method is called and it then registers the bot with the XMPP server. It
 * receives new chat request events and then message events for a chat.
 * The class keeps a reference to the state grid used to store chat information
 * in a scalable manner.
 * 
 * This class which is specified in the grid objectgrid.xml configuration must implement ObjectGridEventListener and
 * typically one or more of the ObjectGridEventGroup interfaces to indicate which events are required.
 * @author bnewport
 *
 */
public class BotXMPPSingleton extends BotBase implements ObjectGridEventListener,ObjectGridEventGroup.ShardEvents, ChatManagerListener, MessageListener
{
	String xmppHost;
	Integer xmppPort;
	String username;
	String password;
	
	/**
	 * Once activated, a reference to the chat server is kept here.
	 */
	XMPPConnection server;

	/**
	 * This method is called when the BOT singleton is assigned to this
	 * JVM. There is one instance of this class per active partition in
	 * a container JVM. The shard parameters is a local reference to
	 * the state for this partition. A BOT can keep non scalable state
	 * in this shard. Data which needs to scale from a capacity point
	 * of view should be stored in the state grid.
	 * @see ShardEvents#shardActivated(ObjectGrid)
	 */
	@Override
	public void shardActivated(ObjectGrid shard) 
	{
		super.shardActivated(shard);
		try
		{
			// connect to xmpp server
			ConnectionConfiguration cfg = new ConnectionConfiguration(xmppHost, xmppPort, "gmail.com");
			cfg.setSASLAuthenticationEnabled(true);
			server = new XMPPConnection(cfg);
			server.connect();
			server.login(username, password);
			Presence p = new Presence(Type.available);
			p.setStatus("WXS Bot");
			server.sendPacket(p);

			// receive new chat events
			server.getChatManager().addChatListener(this);
		}
		catch(XMPPException e)
		{
			System.out.println("Exception logging in to gtalk server:" + e.toString());
		}
	}

	/**
	 * If new container JVMs start then it's possible for the primary
	 * to move to a new container. This method is called to tell the
	 * singleton to clean up and deactivate.
	 * @see ShardEvents#shardDeactivate(ObjectGrid)
	 */
	@Override
	public void shardDeactivate(ObjectGrid shard) 
	{
		if(server.isConnected())
		{
			if(server.isAuthenticated())
			{
				Presence p = new Presence(Type.unavailable);
				p.setStatus("WXS Bot");
				server.sendPacket(p);
			}
			server.disconnect();
		}
	}
	
	/**
	 * Call by the chat manager when a new chat is started.
	 * The bot tracks how many chats it has serviced using
	 * a bot local counter. This is only one of these counters
	 * so we keep it in the singleton state.
	 * @see ChatManagerListener#chatCreated(Chat, boolean)
	 */
	@Override
	public void chatCreated(Chat chat, boolean isLocal) 
	{
		chat.addMessageListener(this);
		// update chat counter
		incrementCounter(botSingleton, "State", "s:chatCount");
	}

	/**
	 * Called when a message from a current chat is received. The
	 * bot tracks how many inquiries it receives from clients. There
	 * could be hundreds of thousands of clients so we cannot keep
	 * these counters in the singleton state as it's just a single partition. It's kept instead
	 * in the state grid which scales.
	 * @see MessageListener#processMessage(Chat, Message)
	 */
	@Override
	public void processMessage(Chat chat, Message m) 
	{
		try
		{
			if(m.getBody() != null)
			{
				// chat buddy has form email/clientid
				StringTokenizer tok = new StringTokenizer(chat.getParticipant(), "/");
				// extract the email only
				String buddyEmail = tok.nextToken();
				
				// the bot logic here just keeps a counter from each buddy and
				// increments it on every message. The counter is stored in the bots
				// grid keyed on buddy name
				int count = incrementCounter(botStateClient, "CounterMap", "c:" + buddyEmail);
				// return the message count as a response
				chat.sendMessage("Count:" + count);
			}
		}
		catch(XMPPException e)
		{
			System.out.println("processMessage Exception:" + e.toString());
		}
	}
	
	/**
	 * Helper methods for properties
	 * @return
	 */
	public String getXmppHost() {
		return xmppHost;
	}

	public void setXmppHost(String xmppHost) {
		this.xmppHost = xmppHost;
	}

	public Integer getXmppPort() {
		return xmppPort;
	}

	public void setXmppPort(Integer xmppPort) {
		this.xmppPort = xmppPort;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
