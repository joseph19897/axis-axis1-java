package org.apache.axis.om.util;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Axis team
 * Date: Sep 27, 2004
 * Time: 5:38:37 PM
 *
 * Holder for all the constants
 * It is helpful to have a single constants container
 * to avoid confusions
 */
public class OMConstants {

    public static final int DEFAULT_INT_VALUE = -1;
    public static final String DEFAULT_STRING_VALUE = "-1";

    //update constants for the table model
    public static final int UPDATE_NEXT_SIBLING = 1;
    public static final int UPDATE_DONE = 2;
    public static final int UPDATE_NAMESPACE = 3;
    public static final int UPDATE_VALUE = 4;
    public static final int UPDATE_FIRST_CHILD = 5;
    public static final int UPDATE_FIRST_ATTRIBUTE = 6;
    public static final int UPDATE_ELEMENT_LOCALNAME = 7;

    //Node type constants
    public static final int ELEMENT = 100;
    public static final int TEXT = 200;
    public static final int COMMENT = 300;
    public static final int CDATA = 400;
    public static final int ATTRIBUTE = 500;
    public static final int NAMESPACE = 600;

    //value keys
//    public static final String LOCAL_NAME_KEY = "n";
//    public static final String ID_KEY = "id";
//    public static final String PARENT_ID_KEY = "pid";
//    public static final String VALUE_KEY = "val";
//    public static final String NEXT_SIBLING_KEY = "ns";
//    public static final String NEXT_SIBLING_TYPE_KEY = "nst";
//    public static final String NAME_SPACE_KEY = "nsk";
//    public static final String DONE_KEY = "d";
//    public static final String NAMESPACE_URI_KEY = "nsu";
//    public static final String NAMESPACE_PREFIX_KEY = "nsp";
//    public static final String TYPE_KEY = "t";
//    public static final String REFERENCE_KEY = "r";
//    public static final String FIRST_CHILD_KEY = "fc";
//    public static final String FIRST_CHILD_TYPE_KEY = "fct";
//    public static final String FIRST_ATTRIBUTE_KEY = "fat";


    /*
Attribute	    Element	        text	    Namespace	Event
ID	            ID	            ID	        ID	        ID
PARENT	        PARENT	        PARENT	    PARENT	    TYPE
NEXT_SIBLING	NEXT_SIBLING	NEXTSIBLING	URI	        REFERENCE
NAME_SPACE	    NAME_SPACE	    TYPE	    PREFIX
VALUE	        DONE	        VALUE
LOCALNAME	    LOCALNAME	    NEXT_SIBLING_TYPE
	            NEXT_SIBLING_TYPE
	            FIRST_CHILD
	            FIRST_CHILD_TYPE
	            FIRST_ATTRIBUTE

    */

    public static int ID_INDEX = 0;

    public static int PARENT_INDEX = 1;
    public static int EVENT_TYPE_INDEX = 1;

    public static int NEXT_SIBLING_INDEX = 2;
    public static int NAMESPACE_URI_INDEX = 2;
    public static int EVENT_REFERENCE_INDEX = 2;

    public static int NAMESPACE_INDEX = 3;
    public static int TEXT_TYPE_INDEX = 3;
    public static int NAMESPACE_PREFIX_INDEX = 3;

    public static int VALUE_INDEX = 4;
    public static int ELEMENT_DONE_INDEX = 4;

    public static int LOCAL_NAME_INDEX = 5;
    public static int TEXT_NEXTSIBLING_TYPE_INDEX = 5;

    public static int ELEMENT_NEXTSIBLING_TYPE_INDEX = 6;

    public static int FIRST_CHILD_INDEX = 7;

    public static int FIRST_CHILD_TYPE_INDEX = 8;

    public static int FIRST_ATTRIBUTE_INDEX = 9;


}
