package test.encoding;

import junit.framework.TestCase;
import org.apache.axis.Handler;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.registries.HandlerRegistry;
import org.apache.axis.server.AxisServer;
import org.apache.axis.transport.local.LocalTransport;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class TestArrayListConversions extends TestCase {
  private static final String SERVICE_NAME = "TestArrayConversions";
  
  private Call call;
  
  public TestArrayListConversions()
  {
    super("service");
  }
  
  public TestArrayListConversions(String name)
  {
    super(name);
    init();
  }
  
  private static boolean equals(List list, Object obj)
  {
    if ((list == null) || (obj == null))
      return false;
    
    if (!obj.getClass().isArray()) return false;
    
    Object [] array = (Object [])obj;
    Iterator iter = list.iterator();

    for (int i=0; i < array.length; i++) {
      if (!(array[i].equals(iter.next()))) {
          return false;
      }
    }

    return true;
  }

  public void init()
  {
    try {
      Service ss = new Service();
      AxisServer server = new AxisServer();
      HandlerRegistry hr = (HandlerRegistry) server.getHandlerRegistry();
      Handler disp = hr.find("RPCDispatcher");    
      SOAPService service = new SOAPService(disp);
      service.addOption("className", "test.encoding.TestArrayListConversions");
      service.addOption("methodName", "*");
      
      server.deployService(SERVICE_NAME, service);
      
      call = (Call) ss.createCall();
      call.setTransport( new LocalTransport(server) );
    }
    catch( Exception exp ) {
      exp.printStackTrace();
    }

  }
  
  public void testVectorConversion() throws Exception
  {
    Vector v = new Vector();
    v.addElement("Hi there!");
    v.addElement("This'll be a SOAP Array and then a LinkedList!");
    call.setOperationName( "echoLinkedList" );
    call.setProperty( Call.NAMESPACE, SERVICE_NAME );
    Object ret = call.invoke( new Object[] { v } );
    if (!equals(v, ret)) assertEquals("Echo LinkedList mangled the result.  Result is underneath.\n" + ret, v, ret);
  }
  
  public void testLinkedListConversion() throws Exception
  {
    LinkedList l = new LinkedList();
    l.add("Linked list item #1");
    l.add("Second linked list item");
    l.add("This will be a SOAP Array then a Vector!");

    call.setOperationName( "echoVector" );
    call.setProperty( Call.NAMESPACE, SERVICE_NAME );
    Object ret = call.invoke( new Object[] { l } );
    if (!equals(l, ret)) assertEquals("Echo Vector mangled the result.  Result is underneath.\n" + ret, l, ret);
  }
      
  public void testArrayConversion() throws Exception
  {
    Vector v = new Vector();
    v.addElement("Hi there!");
    v.addElement("This'll be a SOAP Array");

    call.setOperationName( "echoArray" );
    call.setProperty( Call.NAMESPACE, SERVICE_NAME );
    Object ret = call.invoke( new Object[] { v } );
    if (!equals(v, ret)) assertEquals("Echo Array mangled the result.  Result is underneath\n" + ret, v, ret);
  }

  public static void main(String [] args)
  {
    TestArrayListConversions tester =
              new TestArrayListConversions("TestArrayListConversions");
    try {
      tester.testArrayConversion();
      tester.testLinkedListConversion();
      tester.testVectorConversion();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /****************************************************************
   * 
   * Service methods - this class is also deployed as an Axis RPC
   * service for convenience.  These guys just echo various things.
   * 
   */
  public LinkedList echoLinkedList(LinkedList l)
  {
    return l;
  }
  
  public Vector echoVector(Vector v)
  {
    return v;
  }
  
  public Object [] echoArray(Object [] array)
  {
    return array;
  }
}
