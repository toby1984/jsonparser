/**
 * Copyright 2017 Tobias Gierke <tobias.gierke@code-sourcery.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codesourcery.jsonparser;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;

import de.codesourcery.jsonparser.ast.ASTNode;
import de.codesourcery.jsonparser.util.ASTPrinter;
import de.codesourcery.jsonparser.util.MyParseException;

public class ParserTest {

    @Test
    public void testFailures()
    {
        // test escape sequences
        roundtripFails("{},");
        roundtripFails("");
        roundtripFails("{");
        roundtripFails("}");
        roundtripFails("{\"a\":\"\\u1\"}");
        roundtripFails("{\"a\":\"\\u12\"}");
        roundtripFails("{\"a\":\"\\u123\"}");
        roundtripFails("{\"a\":\"\\u123x\"}");
        roundtripFails("{\"a\":\"\\u123x\"}");
        roundtripFails("{\"a:\"\"}");
        roundtripFails("{a:\"\"}");
        roundtripFails("{a\":\"\"}");
        roundtripFails("{\"a\":}");
        roundtripFails("{\"a\":x}");
        roundtripFails("{\"a\":1.a}");
        roundtripFails("{\"a\":1.}");
        roundtripFails("{\"a\":[}");
        roundtripFails("{\"a\":]}");
        roundtripFails("{\"a\":[x]}");
        roundtripFails("{\"a\":[1,]}");
        roundtripFails("{\"a\":[1,x]}");
    }
    
    @Test
    public void testNoFailures() {
        // test escape sequences
        roundtrip("{\"x\":\"\\\"\"}");
        roundtrip("{\"x\":\"\\\"\"}");
        roundtrip("{\"x\":\"\\u1234\"}");
        roundtrip("{\"x\":\"\\ubeef\"}");
        roundtrip("{\"x\":\"\\/\"}");
        roundtrip("{\"x\":\"\\b\"}");
        roundtrip("{\"x\":\"\\f\"}");
        roundtrip("{\"x\":\"\\n\"}");
        roundtrip("{\"x\":\"\\r\"}");
        roundtrip("{\"x\":\"\\t\"}");
        //
        roundtrip("{}");
        roundtrip("{\"a\":\"\"}");
        roundtrip("{\"a\":\"x\"}");
        roundtrip("{\"a\":1}");
        roundtrip("{\"a\":-1}");
        roundtrip("{\"a\":-1.5}");
        roundtrip("{\"a\":\"test\"}");
        roundtrip("{\"a\":1.1}");
        roundtrip("{\"a\":12.34}");
        roundtrip("{\"a\":true}");
        roundtrip("{\"a\":false}");
        roundtrip("{\"a\":null}");
        roundtrip("{\"a\":[]}");
        roundtrip("{\"a\":[1,2,3]}");
        roundtrip("{\"a\":{}}");
        // nested objects
        roundtrip("{\"x\":{}}");
        roundtrip("{\"x\":{\"a\":1}}");
        roundtrip("{\"x\":{\"a\":\"test\"}}");
        roundtrip("{\"x\":{\"a\":1.1}}");
        roundtrip("{\"x\":{\"a\":12.34}}");
        roundtrip("{\"x\":{\"a\":true}}");
        roundtrip("{\"x\":{\"a\":false}}");
        roundtrip("{\"x\":{\"a\":null}}");
        roundtrip("{\"x\":{\"a\":[]}}");
        roundtrip("{\"x\":{\"a\":[1,2,3]}}");
        roundtrip("{\"x\":{\"a\":{}}}");    
        // nested arrays
        roundtrip("{\"x\":[[1,2],[true,false]]}");
        // nested arrays and objects
        roundtrip("{\"x\":[[1,2],[true,false],{\"a\":\"b\"}]}");
    }
    
    private void roundtripFails(String s) 
    {
        try {
            new Parser().parse( s );
            fail("Should've failed");
        } catch(MyParseException e) {
            // ok
        }
    }
    
    private void roundtrip(String s) 
    {
        final ASTNode ast = new Parser().parse( s );
        final ASTPrinter printer = new ASTPrinter();
        printer.setPrettyPrint( false );
        Assert.assertEquals( s , printer.print( ast ) );
    }
}
