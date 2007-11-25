package org.apache.activemq;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

public class ActiveMQConnectionFactory implements ConnectionFactory {
	public ActiveMQConnectionFactory(String brokerURL) {
	}

	public Connection createConnection() throws JMSException {
		return null;
	}

	public Connection createConnection(String arg0, String arg1) throws JMSException {
		return null;
	}

}
