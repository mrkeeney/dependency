/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.util;

import java.io.PrintWriter;

/**
 * A helper class for printing indented text.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision: 17424 $
 */
public class IndentPrinter {

    private int indentLevel;
    private String indent;
    private PrintWriter out;
    private final boolean addNewlines;

    /**
     * Creates a PrintWriter pointing to System.out, with an indent of two 
     * spaces.
     * @see #IndentPrinter(PrintWriter, String)
     */
    public IndentPrinter() {
        this(new PrintWriter(System.out), "  ");
    }

    /**
     * Create an IndentPrinter to the given PrintWriter, with an indent of two 
     * spaces.
     * @param out PrintWriter to output to
     * @see #IndentPrinter(PrintWriter, String)
     */
    public IndentPrinter(PrintWriter out) {
        this(out, "  ");
    }

    /**
     * Create an IndentPrinter to the given PrintWriter
     * @param out PrintWriter to output to
     * @param indent character(s) used to indent each line
     */
    public IndentPrinter(PrintWriter out, String indent) {
        this(out, indent, true);
    }

    public IndentPrinter(PrintWriter out, String indent, boolean addNewlines) {
        this.addNewlines = addNewlines;
        if (out == null) {
            /** @todo temporary hack */
            out = new PrintWriter(System.out);
            //throw new IllegalArgumentException("Must specify a PrintWriter");
        }
        this.out = out;
        this.indent = indent;
    }

    public void println(String text) {
        out.print(text);
        println();
    }

    public void print(String text) {
        out.print(text);
    }

    public void print(char c) {
        out.print(c);
    }

    public void printIndent() {
        for (int i = 0; i < indentLevel; i++) {
            out.print(indent);
        }
    }

    public void println() {
        if (addNewlines) out.print("\n");
    }

    public void incrementIndent() {
        ++indentLevel;
    }

    public void decrementIndent() {
        --indentLevel;
    }

    public int getIndentLevel() {
        return indentLevel;
    }

    public void setIndentLevel(int indentLevel) {
        this.indentLevel = indentLevel;
    }

    public void flush() {
        out.flush();
    }
}
