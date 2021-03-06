/*
* JBoss, Home of Professional Open Source.
* Copyright 2010, Red Hat, Inc., and individual contributors
* as indicated by the @author tags. See the copyright.txt file in the
* distribution for a full listing of individual contributors.
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
package org.hornetq.tests.unit.ra;

import org.hornetq.ra.HornetQResourceAdapter;
import org.hornetq.tests.util.UnitTestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


/**
 * This test is used to generate the commented out configs in the src/config/ra.xml. If you add a setter to the HornetQResourceAdapter
 * this test should fail, if it does paste the new commented out configs into the ra.xml file and in here. dont forget to
 * add a description for each new property added and try and put it in the config some where appropriate.
 *
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 *
 */
public class HornetQResourceAdapterConfigTest extends UnitTestCase
{
   private static String config = "" +
         "<config-property>\n"+
      "         <description>\n"+
      "            The transport type. Multiple connectors can be configured by using a comma separated list,\n"+
      "            i.e. org.hornetq.core.remoting.impl.invm.InVMConnectorFactory,org.hornetq.core.remoting.impl.invm.InVMConnectorFactory.\n"+
      "         </description>\n"+
      "         <config-property-name>ConnectorClassName</config-property-name>\n"+
      "         <config-property-type>java.lang.String</config-property-type>\n"+
      "         <config-property-value>org.hornetq.core.remoting.impl.invm.InVMConnectorFactory</config-property-value>\n"+
      "      </config-property>\n"+
      "      <config-property>\n"+
      "         <description>The transport configuration. These values must be in the form of key=val;key=val;,\n"+
      "            if multiple connectors are used then each set must be separated by a comma i.e. host=host1;port=5445,host=host2;port=5446.\n"+
      "            Each set of params maps to the connector classname specified.\n"+
      "         </description>\n"+
      "         <config-property-name>ConnectionParameters</config-property-name>\n"+
      "         <config-property-type>java.lang.String</config-property-type>\n"+
      "         <config-property-value>server-id=0</config-property-value>\n"+
      "      </config-property>";

   private static String commentedOutConfigs = "" +
         "      <config-property>\n" +
         "        <description>Does we support HA</description>\n" +
         "        <config-property-name>HA</config-property-name>\n" +
         "        <config-property-type>boolean</config-property-type>\n" +
         "        <config-property-value>false</config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>Use A local Transaction instead of XA?</description>\n" +
         "        <config-property-name>UseLocalTx</config-property-name>\n" +
         "        <config-property-type>boolean</config-property-type>\n" +
         "        <config-property-value>false</config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>The user name used to login to the JMS server</description>\n" +
         "        <config-property-name>UserName</config-property-name>\n" +
         "        <config-property-type>java.lang.String</config-property-type>\n" +
         "        <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>The password used to login to the JMS server</description>\n" +
         "        <config-property-name>Password</config-property-name>\n" +
         "        <config-property-type>java.lang.String</config-property-type>\n" +
         "        <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>The jndi params to use to look up the jms resources if local jndi is not to be used</description>\n" +
         "        <config-property-name>JndiParams</config-property-name>\n" +
         "        <config-property-type>java.lang.String</config-property-type>\n" +
         "        <config-property-value>java.naming.factory.initial=org.jnp.interfaces.NamingContextFactory;java.naming.provider.url=jnp://localhost:1199;java.naming.factory.url.pkgs=org.jboss.naming:org.jnp.interfaces</config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>The discovery group address</description>\n" +
         "        <config-property-name>DiscoveryLocalBindAddress</config-property-name>\n" +
         "        <config-property-type>java.lang.String</config-property-type>\n" +
         "        <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>The discovery group local bind address</description>\n" +
         "        <config-property-name>DiscoveryAddress</config-property-name>\n" +
         "        <config-property-type>java.lang.String</config-property-type>\n" +
         "        <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>The discovery group port</description>\n" +
         "        <config-property-name>DiscoveryPort</config-property-name>\n" +
         "        <config-property-type>int</config-property-type>\n" +
         "        <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>The discovery refresh timeout</description>\n" +
         "        <config-property-name>DiscoveryRefreshTimeout</config-property-name>\n" +
         "        <config-property-type>long</config-property-type>\n" +
         "        <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>The discovery initial wait timeout</description>\n" +
         "        <config-property-name>DiscoveryInitialWaitTimeout</config-property-name>\n" +
         "        <config-property-type>long</config-property-type>\n" +
         "        <config-property-value></config-property-value>\n" +
         "      </config-property> \n" +
         "      <config-property>\n" +
         "         <description>The class to use for load balancing connections</description>\n" +
         "         <config-property-name>ConnectionLoadBalancingPolicyClassName</config-property-name>\n" +
         "         <config-property-type>java.lang.String</config-property-type>\n" +
         "         <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "         <description>number of reconnect attempts for connections after failover occurs</description>\n" +
         "         <config-property-name>ReconnectAttempts</config-property-name>\n" +
         "         <config-property-type>int</config-property-type>\n" +
         "         <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>The client failure check period</description>\n" +
         "        <config-property-name>ClientFailureCheckPeriod</config-property-name>\n" +
         "        <config-property-type>long</config-property-type>\n" +
         "        <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>The connection TTL</description>\n" +
         "        <config-property-name>ConnectionTTL</config-property-name>\n" +
         "        <config-property-type>long</config-property-type>\n" +
         "        <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>The call timeout</description>\n" +
         "        <config-property-name>CallTimeout</config-property-name>\n" +
         "        <config-property-type>long</config-property-type>\n" +
         "        <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>The dups ok batch size</description>\n" +
         "        <config-property-name>DupsOKBatchSize</config-property-name>\n" +
         "        <config-property-type>int</config-property-type>\n" +
         "        <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>The transaction batch size</description>\n" +
         "        <config-property-name>TransactionBatchSize</config-property-name>\n" +
         "        <config-property-type>int</config-property-type>\n" +
         "        <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>The consumer window size</description>\n" +
         "        <config-property-name>ConsumerWindowSize</config-property-name>\n" +
         "        <config-property-type>int</config-property-type>\n" +
         "        <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>The consumer max rate</description>\n" +
         "        <config-property-name>ConsumerMaxRate</config-property-name>\n" +
         "        <config-property-type>int</config-property-type>\n" +
         "        <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>The confirmation window size</description>\n" +
         "        <config-property-name>ConfirmationWindowSize</config-property-name>\n" +
         "        <config-property-type>int</config-property-type>\n" +
         "        <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>The producer max rate</description>\n" +
         "        <config-property-name>ProducerMaxRate</config-property-name>\n" +
         "        <config-property-type>int</config-property-type>\n" +
         "        <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>The min large message size</description>\n" +
         "        <config-property-name>MinLargeMessageSize</config-property-name>\n" +
         "        <config-property-type>int</config-property-type>\n" +
         "        <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>The block on acknowledge</description>\n" +
         "        <config-property-name>BlockOnAcknowledge</config-property-name>\n" +
         "        <config-property-type>boolean</config-property-type>\n" +
         "        <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>The block on non durable send</description>\n" +
         "        <config-property-name>BlockOnNonDurableSend</config-property-name>\n" +
         "        <config-property-type>boolean</config-property-type>\n" +
         "        <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>The block on durable send</description>\n" +
         "        <config-property-name>BlockOnDurableSend</config-property-name>\n" +
         "        <config-property-type>boolean</config-property-type>\n" +
         "        <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>The auto group</description>\n" +
         "        <config-property-name>AutoGroup</config-property-name>\n" +
         "        <config-property-type>boolean</config-property-type>\n" +
         "        <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>The pre acknowledge</description>\n" +
         "        <config-property-name>PreAcknowledge</config-property-name>\n" +
         "        <config-property-type>boolean</config-property-type>\n" +
         "        <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>The retry interval</description>\n" +
         "        <config-property-name>RetryInterval</config-property-name>\n" +
         "        <config-property-type>long</config-property-type>\n" +
         "        <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>The retry interval multiplier</description>\n" +
         "        <config-property-name>RetryIntervalMultiplier</config-property-name>\n" +
         "        <config-property-type>java.lang.Double</config-property-type>\n" +
         "        <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "        <description>The client id</description>\n" +
         "        <config-property-name>ClientID</config-property-name>\n" +
         "        <config-property-type>java.lang.String</config-property-type>\n" +
         "        <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "         <description>use global pools for client</description>\n" +
         "         <config-property-name>UseGlobalPools</config-property-name>\n" +
         "         <config-property-type>boolean</config-property-type>\n" +
         "         <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "         <description>max number of threads for scheduled threrad pool</description>\n" +
         "         <config-property-name>ScheduledThreadPoolMaxSize</config-property-name>\n" +
         "         <config-property-type>int</config-property-type>\n" +
         "         <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "         <description>max number of threads in pool</description>\n" +
         "         <config-property-name>ThreadPoolMaxSize</config-property-name>\n" +
         "         <config-property-type>int</config-property-type>\n" +
         "         <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "         <description>whether to use jndi for looking up destinations etc</description>\n" +
         "         <config-property-name>UseJNDI</config-property-name>\n" +
         "         <config-property-type>boolean</config-property-type>\n" +
         "         <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>\n" +
         "         <description>how long in milliseconds to wait before retry on failed MDB setup</description>\n" +
         "         <config-property-name>SetupInterval</config-property-name>\n" +
         "         <config-property-type>long</config-property-type>\n" +
         "         <config-property-value></config-property-value>\n" +
         "      </config-property>\n" +
         "      <config-property>" + 
         "         <description></description>" + 
         "         <config-property-name>TransactionManagerLocatorMethod</config-property-name>" + 
         "         <config-property-type>java.lang.String</config-property-type>" + 
         "         <config-property-value></config-property-value>" + 
         "      </config-property>" + 
         "      <config-property>" + 
         "         <description></description>" + 
         "         <config-property-name>TransactionManagerLocatorClass</config-property-name>" + 
         "         <config-property-type>java.lang.String</config-property-type>" + 
         "         <config-property-value></config-property-value>" + 
         "      </config-property>" + 
         "      <config-property>\n" +
         "         <description>How many attempts should be made when connecting the MDB</description>\n" +
         "         <config-property-name>SetupAttempts</config-property-name>\n" +
         "         <config-property-type>int</config-property-type>\n" +
         "         <config-property-value></config-property-value>\n" +
         "      </config-property>"+ 
         "      <config-property>\n" +
         "         <description>Add a new managed connection factory</description>\n" +
         "         <config-property-name>ManagedConnectionFactory</config-property-name>\n" +
         "         <config-property-type>org.hornetq.ra.HornetQRAManagedConnectionFactory</config-property-type>\n" +
         "         <config-property-value></config-property-value>\n" +
         "      </config-property>";


   private static String rootConfig = "<root>" + config + commentedOutConfigs + "</root>";

   public void testConfiguration() throws Exception
   {
      Method[] methods = HornetQResourceAdapter.class.getMethods();
      Map<String,Method> methodList = new HashMap<String, Method>();
      for (Method method : methods)
      {
         if(method.getName().startsWith("set"))
         {
            methodList.put(method.getName(), method);
         }
      }
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      InputStream io = new ByteArrayInputStream(rootConfig.getBytes());
      Document dom = db.parse(new InputSource(io));

      Element docEle = dom.getDocumentElement();

      NodeList nl = docEle.getElementsByTagName("config-property");

      for(int i = 0 ; i < nl.getLength();i++)
      {
         Element el = (Element)nl.item(i);
         NodeList elementsByTagName = el.getElementsByTagName("config-property-name");
         assertEquals(el.toString(), elementsByTagName.getLength(), 1);
         Node configPropertyNameNode = elementsByTagName.item(0);
         String configPropertyName = configPropertyNameNode.getTextContent();
         System.out.println("configPropertyName = " + configPropertyName);
         Method setter = methodList.remove("set" + configPropertyName);
         assertNotNull("setter " + configPropertyName + " does not exist", setter);
         Class c = lookupType(setter);
         elementsByTagName = el.getElementsByTagName("config-property-type");
         assertEquals("setter " + configPropertyName + " has no type set", elementsByTagName.getLength(), 1);
         Node configPropertyTypeNode = elementsByTagName.item(0);
         String configPropertyTypeName = configPropertyTypeNode.getTextContent();
         assertEquals(c.getName(), configPropertyTypeName);
      }
      if(!methodList.isEmpty())
      {
         StringBuffer newConfig = new StringBuffer(commentedOutConfigs);
         newConfig.append("\n");
         for (Method method : methodList.values())
         {
            newConfig.append("\"      <config-property>\" + \n");
            newConfig.append("\"         <description>***add***</description>\" + \n");
            newConfig.append("\"         <config-property-name>").append(method.getName().substring(3)).append("</config-property-name>\" + \n");
            newConfig.append("\"         <config-property-type>").append(lookupType(method).getName()).append("</config-property-type>\" + \n");
            newConfig.append("\"         <config-property-value></config-property-value>\" + \n");
            newConfig.append("\"      </config-property>\" + \n");
         }
         System.out.println(newConfig);
         fail("methods not shown please see previous and add");
      }
      else
      {
         System.out.println(commentedOutConfigs);
      }
   }

   /**
    * @param setter
    * @return
    */
   private Class<?> lookupType(Method setter)
   {
      Class<?> clzz = setter.getParameterTypes()[0];
      
      if (clzz == Boolean.class)
      {
         return Boolean.TYPE;
      }
      else if (clzz == Long.class)
      {
         return Long.TYPE;
      }
      else if (clzz == Integer.class)
      {
         return Integer.TYPE;
      }
      else
      {
         return clzz;
      }
   }
}
