/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Axis" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.axis.client ;

import java.util.* ;

import org.apache.axis.* ;
import org.apache.axis.message.RPCArg;
import org.apache.axis.message.RPCBody;
import org.apache.axis.message.SOAPBody;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPHeader;
import org.apache.axis.handlers.* ;
import org.apache.axis.registries.* ;
import org.apache.axis.utils.* ;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.axis.transport.http.HTTPDispatchHandler;

import org.w3c.dom.* ;
import javax.xml.parsers.* ;

/**
 * This class is meant to be the interface that client/requestor code
 * uses to access the SOAP server.  In this class, we'll use HTTP to
 * connect to the server and send a Messaging SOAP request.
 *
 * @author Doug Davis (dug@us.ibm.com)
 */


// Need to add proxy, ssl.... other cool things - but it's a start
// Only supports String

public class HTTPMessage {
  private String  url              = null ;
  private String  action           = null ;
  private String  userID           = null ;
  private String  passwd           = null ;
  private String  encodingStyleURI = null ;

  // For testing
  public boolean doLocal = false ;

  public HTTPMessage() {
  }

  public HTTPMessage(String url) {
    this.url = url ;
  }

  public HTTPMessage(String url, String action) {
    setURL( url );
    setAction( action );
  }

  public void setURL( String url ) {
    this.url = url ;
  }

  public void setAction( String action ) {
    this.action = action ;
  }

  public void setUserID(String user) {
    this.userID = user ;
  }

  public String getUserID() {
    return( userID );
  }

  public void setPassword(String pass) {
    this.passwd = pass ;
  }

  public String getPassword() {
    return( passwd );
  }

  public void setEncodingStyleURI( String uri ) {
    encodingStyleURI = uri ;
  }

  public String getEncodingStyleURI() {
    return( encodingStyleURI );
  }

  public static void invoke(String url, String act, MessageContext mc ) 
      throws AxisFault
  {
    HTTPMessage  hm = new HTTPMessage();
    hm.setURL( url );
    hm.setAction( act );
    hm.invoke( mc );
  }

  public void invoke( MessageContext mc ) throws AxisFault {
    Debug.Print( 1, "Enter: HTTPMessage.invoke" );
    Message              inMsg = mc.getRequestMessage();

    SOAPEnvelope         reqEnv = null ;

    if ( inMsg.getCurrentForm().equals("SOAPEnvelope") )
      reqEnv = (SOAPEnvelope) inMsg.getAs("SOAPEnvelope");
    else {
      reqEnv = new SOAPEnvelope();
      if ( encodingStyleURI != null )
        reqEnv.setEncodingStyleURI( encodingStyleURI );
      SOAPBody  body = new SOAPBody( (Document) inMsg.getAs("Document") );
      reqEnv.addBody( body );
    }

    Handler              client = null ;
    Message              reqMsg = new Message( reqEnv, "SOAPEnvelope" );
    MessageContext       msgContext = new MessageContext( reqMsg );

    DocumentBuilderFactory dbf = null ;
    DocumentBuilder        db  = null ;
    Document               doc = null ;

    try {
      dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      db  = dbf.newDocumentBuilder();
      doc = db.newDocument();
    } 
    catch( Exception e ) {
      Debug.Print( 1, e );
      throw new AxisFault( e );
    }

    // For testing - skip HTTP layer
    if ( doLocal ) {
      client = new org.apache.axis.server.AxisServer();
      client.init();
      msgContext.setProperty(MessageContext.TRANS_INPUT , "HTTP.input" );
      msgContext.setProperty(MessageContext.TRANS_OUTPUT, "HTTP.output" );
      msgContext.setTargetService( action );
    }
    else {
      /* Ok, this might seem strange, but here it is...                    */
      /* Create a new AxisClient Engine and init it.  This will load any   */
      /* registries that *might* be there.  We set the target service to   */
      /* the service so that if it is registered here on the client        */
      /* we'll find it's request/response chains and invoke them.  Next we */
      /* check to see if there is any ServiceRegistry at all, or if there  */
      /* is one, check to see if a chain called HTTP.input is there.  If   */
      /* not then we need to default to just the simple HTTPDispatchHandler*/
      /* to call the server.                                               */
      /* The hard part about the client is that we can't assume *any*      */
      /* configuration has happened at all so hard-coded defaults are      */
      /* required.                                                         */
      /*********************************************************************/
      client = new AxisClient();
      client.init();
      msgContext.setTargetService( action );
      HandlerRegistry sr = (HandlerRegistry) client.getOption( 
                                                 Constants.SERVICE_REGISTRY );
      if ( sr == null || sr.find("HTTP.input") == null )
        msgContext.setProperty( MessageContext.TRANS_INPUT, "HTTPSender" );
      else
        msgContext.setProperty( MessageContext.TRANS_INPUT, "HTTP.input" );
      msgContext.setProperty(MessageContext.TRANS_OUTPUT, "HTTP.output" );
    }

    if ( Debug.getDebugLevel() > 0  ) {
      Element  elem = doc.createElementNS( Constants.URI_DEBUG, "d:Debug" );
      elem.appendChild( doc.createTextNode( ""+Debug.getDebugLevel() ) );
      SOAPHeader  header = new SOAPHeader(elem);
      header.setActor( Constants.URI_NEXT_ACTOR );

      reqEnv.addHeader( header );
    }

    msgContext.setProperty( MessageContext.TRANS_URL, url );
    msgContext.setProperty( HTTPConstants.MC_HTTP_SOAPACTION, action );
    if ( userID != null ) {
      msgContext.setProperty( MessageContext.USERID, userID );
      if ( passwd != null )
        msgContext.setProperty( MessageContext.PASSWORD, passwd );
    }

    try {
      client.invoke( msgContext );
      client.cleanup();
    }
    catch( AxisFault fault ) {
      Debug.Print( 1,  fault );
      throw fault ;
    }

    Message       resMsg = msgContext.getResponseMessage();
    SOAPEnvelope  resEnv = (SOAPEnvelope) resMsg.getAs( "SOAPEnvelope" );
    SOAPBody      resBody = resEnv.getFirstBody();

    doc = db.newDocument();
    doc.appendChild( doc.importNode( resBody.getRoot(), true ) );

    mc.setResponseMessage( new Message(doc, "Document") );

    Debug.Print( 1, "Exit: HTTPMessage.invoke" );
  }

}
