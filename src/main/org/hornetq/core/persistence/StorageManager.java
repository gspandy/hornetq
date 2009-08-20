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

package org.hornetq.core.persistence;

import java.util.List;
import java.util.Map;

import javax.transaction.xa.Xid;

import org.hornetq.core.paging.PageTransactionInfo;
import org.hornetq.core.paging.PagingManager;
import org.hornetq.core.postoffice.Binding;
import org.hornetq.core.server.LargeServerMessage;
import org.hornetq.core.server.MessageReference;
import org.hornetq.core.server.MessagingComponent;
import org.hornetq.core.server.Queue;
import org.hornetq.core.server.ServerMessage;
import org.hornetq.core.transaction.ResourceManager;
import org.hornetq.utils.Pair;
import org.hornetq.utils.SimpleString;
import org.hornetq.utils.UUID;

/**
 * 
 * A StorageManager
 * 
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * @author <a href="mailto:clebert.suconic@jboss.com">Clebert Suconic</a>
 * @author <a href="mailto:andy.taylor@jboss.org>Andy Taylor</a>
 *
 */
public interface StorageManager extends MessagingComponent
{
   // Message related operations
   
   UUID getPersistentID();
   
   void setPersistentID(UUID id) throws Exception;

   long generateUniqueID();
   
   long getCurrentUniqueID();
   
   void setUniqueIDSequence(long id);

   void storeMessage(ServerMessage message) throws Exception;
   
   void storeReference(long queueID, long messageID) throws Exception;

   void deleteMessage(long messageID) throws Exception;

   void storeAcknowledge(long queueID, long messageID) throws Exception;

   void updateDeliveryCount(MessageReference ref) throws Exception;

   void updateScheduledDeliveryTime(MessageReference ref) throws Exception;

   void storeDuplicateID(SimpleString address, byte[] duplID, long recordID) throws Exception;

   void deleteDuplicateID(long recordID) throws Exception;

   void storeMessageTransactional(long txID, ServerMessage message) throws Exception;
   
   void storeReferenceTransactional(long txID, long queueID, long messageID) throws Exception;

   void storeAcknowledgeTransactional(long txID, long queueID, long messageID) throws Exception;

   void updateScheduledDeliveryTimeTransactional(long txID, MessageReference ref) throws Exception;

   void deleteMessageTransactional(long txID, long queueID, long messageID) throws Exception;

   void storeDuplicateIDTransactional(long txID, SimpleString address, byte[] duplID, long recordID) throws Exception;

   void updateDuplicateIDTransactional(long txID, SimpleString address, byte[] duplID, long recordID) throws Exception;

   void deleteDuplicateIDTransactional(long txID, long recordID) throws Exception;

   LargeServerMessage createLargeMessage();

   void prepare(long txID, Xid xid) throws Exception;

   void commit(long txID) throws Exception;

   void rollback(long txID) throws Exception;

   void storePageTransaction(long txID, PageTransactionInfo pageTransaction) throws Exception;

   void deletePageTransactional(long txID, long recordID) throws Exception;

   void loadMessageJournal(PagingManager pagingManager,
                           ResourceManager resourceManager,
                           Map<Long, Queue> queues,
                           Map<SimpleString, List<Pair<byte[], Long>>> duplicateIDMap) throws Exception;

   // Bindings related operations

   void addQueueBinding(Binding binding) throws Exception;
   
   void deleteQueueBinding(long queueBindingID) throws Exception;
   
   void loadBindingJournal(List<QueueBindingInfo> queueBindingInfos) throws Exception;
}