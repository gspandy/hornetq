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

package org.hornetq.tests.unit.core.paging.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.hornetq.core.buffers.ChannelBuffers;
import org.hornetq.core.journal.SequentialFile;
import org.hornetq.core.journal.SequentialFileFactory;
import org.hornetq.core.journal.impl.NIOSequentialFileFactory;
import org.hornetq.core.paging.PagedMessage;
import org.hornetq.core.paging.impl.PageImpl;
import org.hornetq.core.paging.impl.PagedMessageImpl;
import org.hornetq.core.remoting.spi.MessagingBuffer;
import org.hornetq.core.server.ServerMessage;
import org.hornetq.core.server.impl.ServerMessageImpl;
import org.hornetq.tests.unit.core.journal.impl.fakes.FakeSequentialFileFactory;
import org.hornetq.tests.util.UnitTestCase;
import org.hornetq.utils.SimpleString;

/**
 * 
 * @author <a href="mailto:clebert.suconic@jboss.com">Clebert Suconic</a>
 *
 */
public class PageImplTest extends UnitTestCase
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------
 
   public void testPageWithNIO() throws Exception
   {
      recreateDirectory(getTestDir());
      testAdd(new NIOSequentialFileFactory(getTestDir()), 1000);
   }

   public void testDamagedDataWithNIO() throws Exception
   {
      recreateDirectory(getTestDir());
      testDamagedPage(new NIOSequentialFileFactory(getTestDir()), 1000);
   }

   public void testPageFakeWithoutCallbacks() throws Exception
   {
      testAdd(new FakeSequentialFileFactory(1, false), 10);
   }

   /** Validate if everything we add is recovered */
   public void testDamagedPage() throws Exception
   {
      testDamagedPage(new FakeSequentialFileFactory(1, false), 100);
   }
   
   /** Validate if everything we add is recovered */
   protected void testAdd(final SequentialFileFactory factory, final int numberOfElements) throws Exception
   {

      SequentialFile file = factory.createSequentialFile("00010.page", 1);

      PageImpl impl = new PageImpl(factory, file, 10);

      assertEquals(10, impl.getPageId());

      impl.open();

      assertEquals(1, factory.listFiles("page").size());

      SimpleString simpleDestination = new SimpleString("Test");

      ArrayList<MessagingBuffer> buffers = addPageElements(simpleDestination, impl, numberOfElements);

      impl.sync();
      impl.close();

      file = factory.createSequentialFile("00010.page", 1);
      file.open();
      impl = new PageImpl(factory, file, 10);

      List<PagedMessage> msgs = impl.read();

      assertEquals(numberOfElements, msgs.size());

      assertEquals(numberOfElements, impl.getNumberOfMessages());

      for (int i = 0; i < msgs.size(); i++)
      {
         assertEquals(i, (msgs.get(i).getMessage(null)).getMessageID());

         assertEquals(simpleDestination, (msgs.get(i).getMessage(null)).getDestination());

         assertEqualsByteArrays(buffers.get(i).array(), (msgs.get(i).getMessage(null)).getBody().array());
      }

      impl.delete();

      assertEquals(0, factory.listFiles(".page").size());

   }

   
   
   protected void testDamagedPage(final SequentialFileFactory factory, final int numberOfElements) throws Exception
   {

      SequentialFile file = factory.createSequentialFile("00010.page", 1);

      PageImpl impl = new PageImpl(factory, file, 10);

      assertEquals(10, impl.getPageId());

      impl.open();

      assertEquals(1, factory.listFiles("page").size());

      SimpleString simpleDestination = new SimpleString("Test");

      ArrayList<MessagingBuffer> buffers = addPageElements(simpleDestination, impl, numberOfElements);

      impl.sync();

      long positionA = file.position();

      // Add one record that will be damaged
      addPageElements(simpleDestination, impl, 1);
      
      long positionB = file.position();
      
      // Add more 10 as they will need to be ignored
      addPageElements(simpleDestination, impl, 10);
      

      // Damage data... position the file on the middle between points A and B
      file.position(positionA + (positionB - positionA) / 2);
      
      ByteBuffer buffer = ByteBuffer.allocate((int)(positionB - file.position()));
      
      for (int i = 0; i< buffer.capacity(); i++)
      {
         buffer.put((byte)'Z');
      }
      
      buffer.rewind();
      
      file.write(buffer, true);
      
      impl.close();

      file = factory.createSequentialFile("00010.page", 1);
      file.open();
      impl = new PageImpl(factory, file, 10);

      List<PagedMessage> msgs = impl.read();

      assertEquals(numberOfElements, msgs.size());

      assertEquals(numberOfElements, impl.getNumberOfMessages());

      for (int i = 0; i < msgs.size(); i++)
      {
         assertEquals(i, (msgs.get(i).getMessage(null)).getMessageID());

         assertEquals(simpleDestination, (msgs.get(i).getMessage(null)).getDestination());

         assertEqualsByteArrays(buffers.get(i).array(), (msgs.get(i).getMessage(null)).getBody().array());
      }

      impl.delete();

      assertEquals(0, factory.listFiles("page").size());

      assertEquals(1, factory.listFiles("invalidPage").size());

   }
   
   /**
    * @param simpleDestination
    * @param page
    * @param numberOfElements
    * @return
    * @throws Exception
    */
   protected ArrayList<MessagingBuffer> addPageElements(SimpleString simpleDestination, PageImpl page, int numberOfElements) throws Exception
   {
      ArrayList<MessagingBuffer> buffers = new ArrayList<MessagingBuffer>();
      
      int initialNumberOfMessages = page.getNumberOfMessages();

      for (int i = 0; i < numberOfElements; i++)
      {
         MessagingBuffer buffer = ChannelBuffers.buffer(10); 

         for (int j = 0; j < buffer.capacity(); j++)
         {
            //buffer.writeByte(RandomUtil.randomByte());
            buffer.writeByte((byte)'b');
         }

         buffers.add(buffer);

         ServerMessage msg = new ServerMessageImpl((byte)1,
                                                   true,
                                                   0,
                                                   System.currentTimeMillis(),
                                                   (byte)0,
                                                   buffer);

         msg.setMessageID(i);

         msg.setDestination(simpleDestination);

         page.write(new PagedMessageImpl(msg));

         assertEquals(initialNumberOfMessages + i + 1, page.getNumberOfMessages());
      }
      return buffers;
   }



   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}