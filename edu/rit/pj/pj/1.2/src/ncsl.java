//******************************************************************************
//
// File:    ncsl.java
// Package: ---
// Unit:    Class ncsl
//
// This Java source file is copyright (C) 2009 by Alan Kaminsky. All rights
// reserved. For further information, contact the author, Alan Kaminsky, at
// ark@cs.rit.edu.
//
// This Java source file is part of the Parallel Java Library ("PJ"). PJ is free
// software; you can redistribute it and/or modify it under the terms of the GNU
// General Public License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
//
// PJ is distributed in the hope that it will be useful, but WITHOUT ANY
// WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
// A PARTICULAR PURPOSE. See the GNU General Public License for more details.
//
// A copy of the GNU General Public License is provided in the file gpl.txt. You
// may also obtain a copy of the GNU General Public License on the World Wide
// Web at http://www.gnu.org/licenses/gpl.html.
//
//******************************************************************************

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Class ncsl is a main program that computes the Non-Comment Source Lines
 * (NCSL) metric for each Java source file listed on the command line. The NCSL
 * metric is defined as follows:
 * <OL TYPE=1>
 * <LI>
 * Delete all characters that are part of a comment.
 * <LI>
 * Delete all leading and trailing whitespace characters on each line.
 * <LI>
 * Delete all blank lines.
 * <LI>
 * Count the remaining lines.
 * </OL>
 * The program prints the NCSL metric for each file and the total NCSL metric
 * for all the files.
 * <P>
 * Usage: java ncsl <I>file</I> . . .
 *
 * @author  Alan Kaminsky
 * @version 17-Nov-2009
 */
public class ncsl
	{

// Prevent construction.

	private ncsl()
		{
		}

// Main program.

	/**
	 * Main program.
	 */
	public static void main
		(String[] args)
		throws Exception
		{
		long totalNcsl = 0L;
		for (int i = 0; i < args.length; ++ i)
			{
			long ncsl = countNcsl (new File (args[i]));
			totalNcsl += ncsl;
			System.out.println (ncsl + "\t" + args[i]);
			}
		System.out.println (totalNcsl + "\tTotal");
		}

// Hidden operations.

	private static enum State
		{
		NOT_IN_A_COMMENT,
		SLASH_SEEN,
		IN_A_LINE_COMMENT,
		IN_A_BLOCK_COMMENT,
		IN_A_BLOCK_COMMENT_ASTERISK_SEEN,
		IN_A_STRING_LITERAL,
		IN_A_STRING_LITERAL_BACKSLASH_SEEN,
		IN_A_CHAR_LITERAL,
		IN_A_CHAR_LITERAL_BACKSLASH_SEEN,
		};

	/**
	 * Count NCSL for the given file.
	 *
	 * @param  File.
	 *
	 * @return  NCSL.
	 */
	private static long countNcsl
		(File file)
		throws IOException
		{
		Scanner scanner = new Scanner (file);
		State state = State.NOT_IN_A_COMMENT;
		long ncsl = 0L;

		// Process all lines of the file.
		while (scanner.hasNextLine())
			{
			char[] line = scanner.nextLine().toCharArray();

			// Replace each comment character on the line with a space.
			for (int i = 0; i < line.length; ++ i)
				{
				char c = line[i];
				switch (state)
					{
					case NOT_IN_A_COMMENT:
						if (c == '/')
							{
							state = State.SLASH_SEEN;
							}
						else if (c == '"')
							{
							state = State.IN_A_STRING_LITERAL;
							}
						else if (c == '\'')
							{
							state = State.IN_A_CHAR_LITERAL;
							}
						else
							{
							state = State.NOT_IN_A_COMMENT;
							}
						break;
					case SLASH_SEEN:
						if (c == '/')
							{
							line[i-1] = ' ';
							line[i] = ' ';
							state = State.IN_A_LINE_COMMENT;
							}
						else if (c == '*')
							{
							line[i-1] = ' ';
							line[i] = ' ';
							state = State.IN_A_BLOCK_COMMENT;
							}
						else
							{
							state = State.NOT_IN_A_COMMENT;
							}
						break;
					case IN_A_LINE_COMMENT:
						line[i] = ' ';
						state = State.IN_A_LINE_COMMENT;
						break;
					case IN_A_BLOCK_COMMENT:
						line[i] = ' ';
						if (c == '*')
							{
							state = State.IN_A_BLOCK_COMMENT_ASTERISK_SEEN;
							}
						else
							{
							state = State.IN_A_BLOCK_COMMENT;
							}
						break;
					case IN_A_BLOCK_COMMENT_ASTERISK_SEEN:
						line[i] = ' ';
						if (c == '/')
							{
							state = State.NOT_IN_A_COMMENT;
							}
						else if (c == '*')
							{
							state = State.IN_A_BLOCK_COMMENT_ASTERISK_SEEN;
							}
						else
							{
							state = State.IN_A_BLOCK_COMMENT;
							}
						break;
					case IN_A_STRING_LITERAL:
						if (c == '\\')
							{
							state = State.IN_A_STRING_LITERAL_BACKSLASH_SEEN;
							}
						else if (c == '"')
							{
							state = State.NOT_IN_A_COMMENT;
							}
						else
							{
							state = State.IN_A_STRING_LITERAL;
							}
						break;
					case IN_A_STRING_LITERAL_BACKSLASH_SEEN:
						state = State.IN_A_STRING_LITERAL;
						break;
					case IN_A_CHAR_LITERAL:
						if (c == '\\')
							{
							state = State.IN_A_CHAR_LITERAL_BACKSLASH_SEEN;
							}
						else if (c == '\'')
							{
							state = State.NOT_IN_A_COMMENT;
							}
						else
							{
							state = State.IN_A_CHAR_LITERAL;
							}
						break;
					case IN_A_CHAR_LITERAL_BACKSLASH_SEEN:
						state = State.IN_A_CHAR_LITERAL;
						break;
					}
				}

			// Process newline.
			switch (state)
				{
				case IN_A_BLOCK_COMMENT:
					break;
				case IN_A_BLOCK_COMMENT_ASTERISK_SEEN:
					state = State.IN_A_BLOCK_COMMENT;
					break;
				case NOT_IN_A_COMMENT:
				case SLASH_SEEN:
				case IN_A_LINE_COMMENT:
				case IN_A_STRING_LITERAL:
				case IN_A_STRING_LITERAL_BACKSLASH_SEEN:
				case IN_A_CHAR_LITERAL:
				case IN_A_CHAR_LITERAL_BACKSLASH_SEEN:
					state = State.NOT_IN_A_COMMENT;
					break;
				}

			// Find the first non-whitespace character on the line.
			int first = 0;
			while (first < line.length && Character.isWhitespace (line[first]))
				{
				++ first;
				}

			// Find the last non-whitespace character on the line.
			int last = line.length - 1;
			while (last >= 0 && Character.isWhitespace (line[last]))
				{
				-- last;
				}

			// Count NCSL if there is at least one non-whitespace character on
			// the line.
			if (last >= first) ++ ncsl;
			}

		// Return result.
		scanner.close();
		return ncsl;
		}

	}
