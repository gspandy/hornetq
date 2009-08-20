/*
   * JBoss, Home of Professional Open Source
   * Copyright 2005-2008, Red Hat Middleware LLC, and individual contributors
   * by the @authors tag. See the copyright.txt in the distribution for a
   * full listing of individual contributors.
   *
   * This is free software; you can redistribute it and/or modify it
   * under the terms of the GNU Lesser General Public License as
   * published by the Free Software Foundation; either version 2.1 of
   * the License, or (at your option) any later version.
   *
   * This software is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty of
   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
   * Lesser General Public License for more details.
   *
   * You should have received a copy of the GNU Lesser General Public
   * License along with this software; if not, write to the Free
   * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
   * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
   */
package org.hornetq.jms.example;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueRequestor;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

import org.hornetq.common.example.JBMExample;
import org.hornetq.jms.JBossQueue;
import org.hornetq.jms.client.JBossSession;
import org.hornetq.jms.server.management.impl.JMSManagementHelper;

/**
 * 
 * This example demonstrates the use of JBoss Messaging "pre-acknowledge" functionality where
 * messages are acknowledged before they are delivered to the consumer.
 * 
 * Please see the readme.html for more details.
 *
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 *
 */
public class PreacknowledgeExample extends JBMExample
{
   public static void main(String[] args)
   {
      new PreacknowledgeExample().run(args);
   }

   public boolean runExample() throws Exception
   {
      Connection connection = null;
      
      InitialContext initialContext = null;
      try
      {
         // Step 1. Create an initial context to perform the JNDI lookup.
         initialContext = getContext(0);

         // Step 2. Perform the look-ups
         Queue queue = (Queue)initialContext.lookup("/queue/exampleQueue");

         ConnectionFactory cf = (ConnectionFactory)initialContext.lookup("/ConnectionFactory");

         // Step 3. Create a the JMS objects
         connection = cf.createConnection();

         Session session = connection.createSession(false, JBossSession.PRE_ACKNOWLEDGE);

         MessageProducer producer = session.createProducer(queue);
         
         MessageConsumer messageConsumer = session.createConsumer(queue);

         // Step 4. Create and send a message
         TextMessage message1 = session.createTextMessage("This is a text message 1");

         producer.send(message1);

         System.out.println("Sent message: " + message1.getText());

         // Step 5. Print out the message count of the queue. The queue contains one message as expected
         // delivery has not yet started on the queue
         int count = getMessageCount(connection);
         
         System.out.println("Queue message count is " + count);

         // Step 6. Start the Connection, delivery will now start. Give a little time for delivery to occur.
         connection.start();

         Thread.sleep(1000);

         // Step 7. Print out the message countof the queue. It should now be zero, since the message has
         // already been acknowledged even before the consumer has received it.
         count = getMessageCount(connection);
         
         System.out.println("Queue message count is now " + count);
         
         if (count != 0)
         {
            return false;
         }

         // Step 8. Finally, receive the message
         TextMessage messageReceived = (TextMessage)messageConsumer.receive(5000);

         System.out.println("Received message: " + messageReceived.getText());
       
         return true;
      }
      finally
      {
         // Step 9. Be sure to close our resources!
         if (initialContext != null)
         {
            initialContext.close();
         }
         if (connection != null)
         {
            connection.close();
         }
      }
   }

   // To do this we send a management message to get the message count.
   // In real life you wouldn't create a new session every time you send a management message
   private int getMessageCount(final Connection connection) throws Exception
   {
      QueueSession session = ((QueueConnection)connection).createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

      Queue managementQueue = new JBossQueue("jbm.management", "jbm.management");
      
      QueueRequestor requestor = new QueueRequestor(session, managementQueue);
      
      connection.start();

      Message m = session.createMessage();

      JMSManagementHelper.putAttribute(m, "jms.queue.exampleQueue", "messageCount");

      Message response = requestor.request(m);

      int messageCount = (Integer)JMSManagementHelper.getResult(response);
      
      return messageCount;
   }

}