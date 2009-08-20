/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005-2009, Red Hat Middleware LLC, and individual contributors
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

package org.hornetq.tests.unit.core.config.impl;

import org.hornetq.core.config.impl.FileConfiguration;
import org.hornetq.tests.util.UnitTestCase;
import org.hornetq.utils.XMLUtil;
import org.w3c.dom.Element;

/**
 * A ConfigurationValidationTr
 *
 * @author jmesnil
 * 
 * Created 22 janv. 2009 14:53:19
 *
 *
 */
public class ConfigurationValidationTest extends UnitTestCase
{

   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   public void testMinimalConfiguration() throws Exception
   {
      String xml = "<configuration xmlns='urn:jboss:messaging'>" 
                 + "</configuration>";
      Element element = org.hornetq.utils.XMLUtil.stringToElement(xml);
      assertNotNull(element);
      element = XMLUtil.stringToElement(xml);
      assertNotNull(element);
      XMLUtil.validate(element, "jbm-configuration.xsd");
   }

   public void testFullConfiguration() throws Exception
   {
      FileConfiguration fc = new FileConfiguration();      
      fc.setConfigurationUrl("ConfigurationTest-full-config.xml");      
      fc.start();
      
      assertEquals(true, fc.isPersistDeliveryCountBeforeDelivery());
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}