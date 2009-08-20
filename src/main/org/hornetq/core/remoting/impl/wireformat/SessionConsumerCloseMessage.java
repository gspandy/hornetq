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

package org.hornetq.core.remoting.impl.wireformat;

import org.hornetq.core.remoting.spi.MessagingBuffer;
import org.hornetq.utils.DataConstants;

/**
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * 
 * @version <tt>$Revision$</tt>
 */
public class SessionConsumerCloseMessage extends PacketImpl
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   private long consumerID;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public SessionConsumerCloseMessage(final long objectID)
   {
      super(SESS_CONSUMER_CLOSE);

      consumerID = objectID;
   }

   public SessionConsumerCloseMessage()
   {
      super(SESS_CONSUMER_CLOSE);
   }

   // Public --------------------------------------------------------

   public long getConsumerID()
   {
      return consumerID;
   }

   public int getRequiredBufferSize()
   {
      return BASIC_PACKET_SIZE + DataConstants.SIZE_LONG;
   }

   @Override
   public void encodeBody(final MessagingBuffer buffer)
   {
      buffer.writeLong(consumerID);
   }

   @Override
   public void decodeBody(final MessagingBuffer buffer)
   {
      consumerID = buffer.readLong();
   }

   @Override
   public String toString()
   {
      return getParentString() + ", consumerID=" + consumerID + "]";
   }

   @Override
   public boolean equals(final Object other)
   {
      if (other instanceof SessionConsumerCloseMessage == false)
      {
         return false;
      }

      SessionConsumerCloseMessage r = (SessionConsumerCloseMessage)other;

      return super.equals(other) && consumerID == r.consumerID;
   }
   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}