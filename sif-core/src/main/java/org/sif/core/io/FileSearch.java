package org.sif.core.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class supports regex searching of text files.
 * 
 * All search methods in this class must be given a regex pattern containing one
 * and only one capturing group. The search results will be the test values that
 * match the capturing group.
 * 
 * @author David Anderson
 * 
 */
public class FileSearch
{
	private final static Logger logger = LoggerFactory.getLogger( FileSearch.class );


	/**
	 * Find the first occurrence of the given regex pattern in the given file.
	 * 
	 * @param file
	 * @param patternText
	 * @return
	 * @throws IOException
	 */
	public static String findFirst(File file, String patternText) throws IOException
	{
		return findFirst( file, patternText, 0 );
	}


	/**
	 * Find the first occurrence of the given regex pattern in the given file.
	 * Flags can also be passed to control how the pattern is interpreted.
	 * 
	 * The flags supported are: CASE_INSENSITIVE, MULTILINE, DOTALL,
	 * UNICODE_CASE, CANON_EQ, UNIX_LINES, LITERAL, UNICODE_CHARACTER_CLASS and
	 * COMMENTS
	 * 
	 * @param file
	 * @param patternText
	 * @param flags
	 * @return
	 * @throws IOException
	 * 
	 * @see java.util.regex.Pattern#compile(String, int)
	 */
	public static String findFirst(File file, String patternText, int flags) throws IOException
	{
		return findFirst( readFileContent( file ).toString(), patternText, flags );
	}


	/**
	 * Find the first occurrence of the given regex pattern in the given String.
	 * 
	 * @param text
	 * @param patternText
	 * @return
	 */
	public static String findFirst(String text, String patternText)
	{
		return findFirst( text, patternText, 0 );
	}


	/**
	 * Find the first occurrence of the given regex pattern in the given String.
	 * Flags can also be passed to control how the pattern is interpreted.
	 * 
	 * The flags supported are: CASE_INSENSITIVE, MULTILINE, DOTALL,
	 * UNICODE_CASE, CANON_EQ, UNIX_LINES, LITERAL, UNICODE_CHARACTER_CLASS and
	 * COMMENTS
	 * 
	 * @param text
	 * @param patternText
	 * @param flags
	 * @return
	 * 
	 * @see java.util.regex.Pattern#compile(String, int)
	 */
	public static String findFirst(String text, String patternText, int flags)
	{
		String foundText = null;

		Pattern pattern = Pattern.compile( patternText, flags );
		Matcher matcher = pattern.matcher( text );
		if ( matcher.find() )
		{
			foundText = matcher.group( 1 ); // Assumes one backreference
			logger.debug( "Found value: " + foundText );
		}

		return foundText;
	}


	/**
	 * Find all occurrences of the given regex pattern in the given file.
	 * 
	 * FIXME: Should redesign to take a list of regex's and only perform the
	 * file I/O once.
	 * 
	 * @param file
	 * @param patternText
	 * @return
	 * @throws IOException
	 */
	public static List<String> find(File file, String patternText) throws IOException
	{
		return find( file, patternText, 0 );
	}


	/**
	 * Find all occurrences of the given regex pattern in the given file. Flags
	 * can also be passed to control how the pattern is interpreted.
	 * 
	 * The flags supported are: CASE_INSENSITIVE, MULTILINE, DOTALL,
	 * UNICODE_CASE, CANON_EQ, UNIX_LINES, LITERAL, UNICODE_CHARACTER_CLASS and
	 * COMMENTS
	 * 
	 * @param file
	 * @param patternText
	 * @param flags
	 * @return
	 * @throws IOException
	 * 
	 * @see java.util.regex.Pattern#compile(String, int)
	 */
	public static List<String> find(File file, String patternText, int flags) throws IOException
	{
		return find( readFileContent( file ).toString(), patternText, flags );
	}


	/**
	 * Find all occurrences of the given regex pattern in the given String.
	 * 
	 * @param text
	 * @param patternText
	 * @return
	 */
	public static List<String> find(String text, String patternText)
	{
		return find( text, patternText, 0 );
	}


	/**
	 * Find all occurrences of the given regex pattern in the given String.
	 * Flags can also be passed to control how the pattern is interpreted.
	 * 
	 * The flags supported are: CASE_INSENSITIVE, MULTILINE, DOTALL,
	 * UNICODE_CASE, CANON_EQ, UNIX_LINES, LITERAL, UNICODE_CHARACTER_CLASS and
	 * COMMENTS
	 * 
	 * @param text
	 * @param patternText
	 * @param flags
	 * @return
	 * 
	 * @see java.util.regex.Pattern#compile(String, int)
	 */
	public static List<String> find(String text, String patternText, int flags)
	{
		List<String> foundList = new ArrayList<String>();
		String foundText = null;

		Pattern pattern = Pattern.compile( patternText, flags );
		Matcher matcher = pattern.matcher( text );
		while ( matcher.find() )
		{
			foundText = matcher.group( 1 ); // Assumes one backreference
			foundList.add( foundText );
			logger.debug( "Found value: " + foundText );
		}

		return foundList;
	}


	/**
	 * Get the number of occurrences of the given regex pattern in the given
	 * file.
	 * 
	 * @param file
	 * @param patternText
	 * @return
	 * @throws IOException
	 */
	public static int countOccurrences(File file, String patternText) throws IOException
	{
		return countOccurrences( readFileContent( file ).toString(), patternText );
	}


	/**
	 * Get the number of occurrences of the given regex pattern in the given
	 * String.
	 * 
	 * @param text
	 * @param patternText
	 * @return
	 */
	public static int countOccurrences(String text, String patternText)
	{
		int count = 0;
		String foundText = null;

		Pattern pattern = Pattern.compile( patternText );
		Matcher matcher = pattern.matcher( text );
		for ( count = 0; matcher.find(); count++ )
		{
			foundText = matcher.group().trim();
			logger.debug( "Found value: " + foundText );
		}

		return count;
	}


	private static String readFileContent(File file) throws IOException
	{
		StringBuilder content = new StringBuilder();
		FileReader in = new FileReader( file );
		try
		{
			char[] buffer = new char[4096];
			int read = 0;
			do
			{
				content.append( buffer, 0, read );
				read = in.read( buffer );
			}
			while ( read >= 0 );
		}
		finally
		{
			if ( in != null )
			{
				try
				{
					in.close();
				}
				catch (Exception ignore)
				{
				}
			}
		}

		return content.toString();
	}


	public static void main(String[] args)
	{
		String line = "192.168.0.2    desktop\n" + "192.168.0.3    laptop\n";
		String hostname = FileSearch.findFirst( line, "[0-9\\.]+\\s*([_A-Za-z]+)" );
		String ipAddress = FileSearch.findFirst( line, "([0-9\\.]+)\\s*[_A-Za-z]+" );
		logger.debug( "find result: hostname = " + hostname );
		logger.debug( "find result: ipAddress = " + ipAddress );
	}
}
