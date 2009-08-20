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

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.InitialContext;

import org.hornetq.utils.TokenBucketLimiter;
import org.hornetq.utils.TokenBucketLimiterImpl;

/**
 * 
 * A PerfBase
 *
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 *
 *
 */
public abstract class PerfBase
{
   private static final Logger log = Logger.getLogger(PerfSender.class.getName());

   private static final String DEFAULT_PERF_PROPERTIES_FILE_NAME = "perf.properties";
   
   private static byte[] randomByteArray(final int length)
   {
      byte[] bytes = new byte[length];
      
      Random random = new Random();
      
      for (int i = 0; i < length; i++)
      {
         bytes[i] = Integer.valueOf(random.nextInt()).byteValue();
      }
      
      return bytes;      
   }

   protected static String getPerfFileName(String[] args)
   {
      String fileName;

      if (args.length > 0)
      {
         fileName = args[0];
      }
      else
      {
         fileName = DEFAULT_PERF_PROPERTIES_FILE_NAME;
      }

      return fileName;
   }

   protected static PerfParams getParams(final String fileName) throws Exception
   {
      Properties props = null;

      InputStream is = null;

      try
      {
         is = new FileInputStream(fileName);

         props = new Properties();

         props.load(is);
      }
      finally
      {
         if (is != null)
         {
            is.close();
         }
      }

      int noOfMessages = Integer.valueOf(props.getProperty("num-messages"));
      int noOfWarmupMessages = Integer.valueOf(props.getProperty("num-warmup-messages"));
      int messageSize = Integer.valueOf(props.getProperty("message-size"));
      boolean durable = Boolean.valueOf(props.getProperty("durable"));
      boolean transacted = Boolean.valueOf(props.getProperty("transacted"));
      int batchSize = Integer.valueOf(props.getProperty("batch-size"));
      boolean drainQueue = Boolean.valueOf(props.getProperty("drain-queue"));
      String destinationLookup = props.getProperty("destination-lookup");
      String connectionFactoryLookup = props.getProperty("connection-factory-lookup");
      int throttleRate = Integer.valueOf(props.getProperty("throttle-rate"));
      boolean dupsOK = Boolean.valueOf(props.getProperty("dups-ok-acknowlege"));
      boolean disableMessageID = Boolean.valueOf(props.getProperty("disable-message-id"));
      boolean disableTimestamp = Boolean.valueOf(props.getProperty("disable-message-timestamp"));

      log.info("num-messages: " + noOfMessages);
      log.info("num-warmup-messages: " + noOfWarmupMessages);
      log.info("message-size: " + messageSize);
      log.info("durable: " + durable);
      log.info("transacted: " + transacted);
      log.info("batch-size: " + batchSize);
      log.info("drain-queue: " + drainQueue);
      log.info("throttle-rate: " + throttleRate);
      log.info("connection-factory-lookup: " + connectionFactoryLookup);
      log.info("destination-lookup: " + destinationLookup);
      log.info("disable-message-id: " + disableMessageID);
      log.info("disable-message-timestamp: " + disableTimestamp);
      log.info("dups-ok-acknowledge: " + dupsOK);

      PerfParams perfParams = new PerfParams();
      perfParams.setNoOfMessagesToSend(noOfMessages);
      perfParams.setNoOfWarmupMessages(noOfWarmupMessages);
      perfParams.setMessageSize(messageSize);
      perfParams.setDurable(durable);
      perfParams.setSessionTransacted(transacted);
      perfParams.setBatchSize(batchSize);
      perfParams.setDrainQueue(drainQueue);
      perfParams.setConnectionFactoryLookup(connectionFactoryLookup);
      perfParams.setDestinationLookup(destinationLookup);
      perfParams.setThrottleRate(throttleRate);
      perfParams.setDisableMessageID(disableMessageID);
      perfParams.setDisableTimestamp(disableTimestamp);
      perfParams.setDupsOK(dupsOK);

      return perfParams;
   }

   private final PerfParams perfParams;

   protected PerfBase(final PerfParams perfParams)
   {
      this.perfParams = perfParams;
   }

   private ConnectionFactory factory;

   private Connection connection;

   private Session session;

   private Destination destination;

   private long start;

   private void init() throws Exception
   {
      InitialContext ic = new InitialContext();

      factory = (ConnectionFactory)ic.lookup(perfParams.getConnectionFactoryLookup());

      destination = (Destination)ic.lookup(perfParams.getDestinationLookup());

      connection = factory.createConnection();

      session = connection.createSession(perfParams.isSessionTransacted(),
                                         perfParams.isDupsOK() ? Session.DUPS_OK_ACKNOWLEDGE : Session.AUTO_ACKNOWLEDGE);

      ic.close();
   }

   private void displayAverage(final long numberOfMessages, final long start, final long end)
   {
      double duration = (1.0 * end - start) / 1000; // in seconds
      double average = (1.0 * numberOfMessages / duration);
      log.info(String.format("average: %.2f msg/s (%d messages in %2.2fs)", average, numberOfMessages, duration));
   }

   protected void runSender()
   {
      try
      {
         init();

         if (perfParams.isDrainQueue())
         {
            drainQueue();
         }

         start = System.currentTimeMillis();
         log.info("warming up by sending " + perfParams.getNoOfWarmupMessages() + " messages");
         sendMessages(perfParams.getNoOfWarmupMessages(),
                      perfParams.getBatchSize(),
                      perfParams.isDurable(),
                      perfParams.isSessionTransacted(),
                      false,
                      perfParams.getThrottleRate(),
                      perfParams.getMessageSize());
         log.info("warmed up");
         start = System.currentTimeMillis();
         sendMessages(perfParams.getNoOfMessagesToSend(),
                      perfParams.getBatchSize(),
                      perfParams.isDurable(),
                      perfParams.isSessionTransacted(),
                      true,
                      perfParams.getThrottleRate(),
                      perfParams.getMessageSize());
         long end = System.currentTimeMillis();
         displayAverage(perfParams.getNoOfMessagesToSend(), start, end);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      finally
      {
         if (session != null)
         {
            try
            {
               session.close();
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }
         }
      }
   }

   protected void runListener()
   {
      try
      {
         init();

         if (perfParams.isDrainQueue())
         {
            drainQueue();
         }

         MessageConsumer consumer = session.createConsumer(destination);

         connection.start();

         log.info("READY!!!");

         CountDownLatch countDownLatch = new CountDownLatch(1);
         consumer.setMessageListener(new PerfListener(countDownLatch, perfParams));
         countDownLatch.await();
         long end = System.currentTimeMillis();
         // start was set on the first received message
         displayAverage(perfParams.getNoOfMessagesToSend(), start, end);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      finally
      {
         if (session != null)
         {
            try
            {
               session.close();
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }
         }
      }
   }

   private void drainQueue() throws Exception
   {
      log.info("Draining queue");

      Session drainSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

      MessageConsumer consumer = drainSession.createConsumer(destination);

      connection.start();

      Message message = null;

      int count = 0;
      do
      {
         message = consumer.receive(3000);

         if (message != null)
         {
            message.acknowledge();

            count++;
         }
      }
      while (message != null);

      drainSession.close();

      log.info("Drained " + count + " messages");
   }

   private void sendMessages(final int numberOfMessages,
                             final int txBatchSize,
                             final boolean durable,
                             final boolean transacted,
                             final boolean display,
                             final int throttleRate,
                             final int messageSize) throws Exception
   {
      MessageProducer producer = session.createProducer(destination);

      producer.setDeliveryMode(perfParams.isDurable() ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT);

      producer.setDisableMessageID(perfParams.isDisableMessageID());

      producer.setDisableMessageTimestamp(perfParams.isDisableTimestamp());

      BytesMessage message = session.createBytesMessage();

      byte[] payload = randomByteArray(messageSize);

      message.writeBytes(payload);

      final int modulo = 2000;

      TokenBucketLimiter tbl = throttleRate != -1 ? new TokenBucketLimiterImpl(throttleRate, false) : null;

      boolean committed = false;
      for (int i = 1; i <= numberOfMessages; i++)
      {
         producer.send(message);

         if (transacted)
         {
            if (i % txBatchSize == 0)
            {
               session.commit();
               committed = true;
            }
            else
            {
               committed = false;
            }
         }

         if (display && (i % modulo == 0))
         {
            double duration = (1.0 * System.currentTimeMillis() - start) / 1000;
            log.info(String.format("sent %6d messages in %2.2fs", i, duration));
         }

         if (tbl != null)
         {
            tbl.limit();
         }
      }
      if (transacted && !committed)
      {
         session.commit();
      }
   }

   private class PerfListener implements MessageListener
   {
      private final CountDownLatch countDownLatch;

      private final PerfParams perfParams;

      private boolean warmingUp = true;

      private boolean started = false;

      private final int modulo;

      private final AtomicLong count = new AtomicLong(0);

      public PerfListener(final CountDownLatch countDownLatch, final PerfParams perfParams)
      {
         this.countDownLatch = countDownLatch;
         this.perfParams = perfParams;
         warmingUp = perfParams.getNoOfWarmupMessages() > 0;
         this.modulo = 2000;
      }

      public void onMessage(final Message message)
      {
         try
         {
            if (warmingUp)
            {
               boolean committed = checkCommit();
               if (count.incrementAndGet() == perfParams.getNoOfWarmupMessages())
               {
                  log.info("warmed up after receiving " + count.longValue() + " msgs");
                  if (!committed)
                  {
                     checkCommit();
                  }
                  warmingUp = false;
               }
               return;
            }

            if (!started)
            {
               started = true;
               // reset count to take stats
               count.set(0);
               start = System.currentTimeMillis();
            }

            long currentCount = count.incrementAndGet();
            boolean committed = checkCommit();
            if (currentCount == perfParams.getNoOfMessagesToSend())
            {
               if (!committed)
               {
                  checkCommit();
               }
               countDownLatch.countDown();
            }
            if (currentCount % modulo == 0)
            {
               double duration = (1.0 * System.currentTimeMillis() - start) / 1000;
               log.info(String.format("received %6d messages in %2.2fs", currentCount, duration));
            }
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      private boolean checkCommit() throws Exception
      {
         if (perfParams.isSessionTransacted())
         {
            if (count.longValue() % perfParams.getBatchSize() == 0)
            {
               session.commit();

               return true;
            }
         }
         return false;
      }
   }

}