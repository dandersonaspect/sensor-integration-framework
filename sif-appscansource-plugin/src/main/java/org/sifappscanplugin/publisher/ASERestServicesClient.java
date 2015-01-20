package org.sifappscanplugin.publisher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.sifappscanplugin.publisher.ASEAuthenticator;
import org.sif.core.authentication.Authenticator;

/**
 * This class connects to an AppScan Enterprise (ASE) instance to perform various functions that are not
 * supported by the AppScan Source for Security Edition (ASSE) CLI.  It relies on the AppScan REST services
 * as well as the web interface.  NTLM and Jazz authentication are supported.  NTLMv2 is even supported, 
 * even though ASSE itself does not currently support it.  This class has been tested with ASE 8.6.
 * 
 * @author David Anderson
 * @date February, 2013
 *
 */
public class ASERestServicesClient
{
	private Log log = LogFactory.getLog(ASERestServicesClient.class);	
	PrintWriter writer;
	private PrintStream outputStream;

	Authenticator authenticator;
	String location;

	XPath xpath;
	String aseUri = "appscanreporting.enterprise.irs.gov/ase";
	
	boolean loggingInit = false;
	
	static boolean testMode = false;	// Reads XML files from local file-system rather than from an ASE server.
	
	private void setupLogging(){
		try {
			writer = new PrintWriter("C:\\Plugin_Log.log", "UTF-8");
			loggingInit = true;
			writer.println();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ASERestServicesClient()
	{
		// Initialization of XPath utilities
		XPathFactory factory = XPathFactory.newInstance();
		xpath = factory.newXPath();
		xpath.setNamespaceContext(_nsContext);
		
		if(!loggingInit){
//			Disabled to diagnose errors with 9.0.1
//			setupLogging();
		}
		
		
	}

	
	public ASERestServicesClient(String location, String domain, String user, String password, boolean acceptSsl)
	{
		this();
		//outStream.println("Invoking ASEAuthenticator with params: " + location + ", " + domain + ", " + user + ", " + password + ", " + acceptSsl + ".");
		authenticator = new ASEAuthenticator(location, domain, user, password, acceptSsl);
		this.location = location;
	}
	
	public ASERestServicesClient(String location)
	{
		this();
		this.location = location;
	}
	
	/**
	 * Get the folder id that corresponds to the given folder path on the ASE instance.
	 * 
	 * @param folder
	 * @return
	 */
	public Integer getFolderIdService(String folder)
	{
		Integer id = null;
		
		try
		{
			if (!testMode)
			{
				authenticator.login();
			}

			id = getFolderId(folder);
		}
		catch (ClientProtocolException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (SAXException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (!testMode)
			{
				authenticator.shutdown();
			}
		}
		
		return id;
	}
	
	/**
	 * Create a new folder at the given folder path on the ASE instance.
	 * 
	 * @param folder
	 * @return
	 */
	public boolean viewFolderService(String folder)
	{
		boolean result = false;
		
		try
		{
			if (!testMode)
			{
				authenticator.login();
			}

			result = viewFolder(folder);
		}
		catch (ClientProtocolException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (SAXException e)
		{
			e.printStackTrace();
		}
		catch (ASEClientException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (!testMode)
			{
				authenticator.shutdown();
			}
		}

		return result;
	}
	
	public int checkForFolderAtParentId(int parentId, String folderName)
	{
		// FIXME: Hack!
		int folderId = -99; //a return value of -99 means the folder was not found
		
		String folderCheckURI = this.location + "/services/folders/" + parentId + "/folders";
		
		outputStream.println("URL: " + folderCheckURI);
		
		Document response = null;
		try {
			response = sendRESTRequest(folderCheckURI, "");
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    
		if (response != null)
		{
    		try
    		{
    			//outputStream.println("Response is not null.");
    			checkForError(response);
    		}
    		catch (ASEClientException e)
    		{
    			e.printStackTrace();
    		}
		}

		if (response != null)
		{
			try
			{		
				
			
			Object folderResult = xpath.evaluate("/ase:folder | /ase:folders/ase:folder", response, XPathConstants.NODESET);
			NodeList folderNodes = (NodeList) folderResult;
		
			for (int i = 0; i < folderNodes.getLength(); i++)
			{
				outputStream.println("Looking for folder in a new node...");
				Element folderNode = (Element) folderNodes.item(i);
				if (xpath.evaluate("ase:name/text()", folderNode, XPathConstants.STRING).toString().equals(folderName)){
					outputStream.println("Folder element name: " + folderName);
					String folderIdString = (String) xpath.evaluate("ase:id/text()", folderNode, XPathConstants.STRING);
					outputStream.println("Folder element id: " + folderIdString);
					folderId = Integer.parseInt(folderIdString);
					outputStream.println("Found folder: " + folderName + "!");
					outputStream.println("Folder ID: " + folderId);
					break;
				}
			}
			outputStream.println("Finished with that node..");
			outputStream.println("response start--------------");
			printDocument(response, outputStream);
			outputStream.println("response end----------------");
			
		}
		catch (XPathExpressionException e)
		{
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

		return folderId;
	}
	
	public void createFolder(int parentId, String folder, String description, String contact) throws ParserConfigurationException, SAXException, IOException, ASEClientException{
		ASElogin();
		outputStream.println("Sending Create Folder Post...");
		sendCreateFolderPost(folder, parentId, description, contact, 0);
		ASElogout();
	}
	
	private void ASElogin() throws ClientProtocolException, IOException{
		authenticator.login();
		outputStream.println("Logged in.");
	}
	
	private void ASElogout(){
		outputStream.println("Logging out..");
		authenticator.shutdown();
	}
	
	public int createFolderStructure(int parentId, String folders[], String description, String contact) throws ParserConfigurationException, SAXException, IOException, ASEClientException
	{
		outputStream.println("");
		outputStream.println("Called createFolderStructure...");
		int folderId = -99;
		try
		{
			ASElogin();
		
			for (int currentFolder = 0; currentFolder < folders.length; currentFolder++){
				outputStream.println("");
				outputStream.println("createFolderService params: " + parentId + ", " + folders[currentFolder] + ", " + description + ", " + contact + ".");
				//Check if the folder we are looking for exists at the parentID. If not, -99 will be returned.
				folderId = checkForFolderAtParentId(parentId, folders[currentFolder]);
				outputStream.println("Folder ID for " + folders[currentFolder] + " at parentId " + parentId + "is: " + folderId);
				//Our folder was found
				if (folderId != -99){
					outputStream.println("Folder " + folders[currentFolder] + " was found with folderId " + folderId + " and will not be created.");
					parentId = folderId;
					//Folder exists, so we keep moving..
					continue;					
				} else{
					ASElogout();
					outputStream.println("Calling createFolder: " + parentId + ": " + folders[currentFolder]);
					createFolder(parentId, folders[currentFolder], "", "");
					ASElogin();
					outputStream.println("Confirming we actually created the folder..");
					folderId = checkForFolderAtParentId(parentId, folders[currentFolder]);
					outputStream.println("FolderId found: " + folderId);
					if (folderId == -99){
						//This should probably actually throw an exception, since it means something didn't work
						outputStream.println("Folder was not actually created...");
						outputStream.println("");						
					} else{
						outputStream.println("New Folder ID: " + folderId);
						outputStream.println("Folder creation confirmed!");
						parentId = folderId;
					}
				}		
			}
		}
		finally
		{
			//if (!testMode)
			//{
				ASElogout();
				outputStream.println("");
//				outputStream.flush();
//				outputStream.close();
			//}
		}
		outputStream.println("Returning folderId: " + folderId);
		return folderId;

	}
	/*
	/**
	 * Create a new folder structure at the given folder path on the ASE instance.
	 * 
	 * @param folder
	 * @return
	 * @throws ASEClientException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 
	public int createFolderService(int parentId, String folders[], String description, String contact, int currentFolder) throws ParserConfigurationException, SAXException, IOException, ASEClientException
	{
		//If we are about to go out of bounds, return the last folder we found/created 
		if(currentFolder == folders.length)
			return parentId;

		outputStream.println("createFolderService params: " + parentId + ", " + folders[currentFolder] + ", " + description + ", " + contact + ", " + currentFolder + ".");
		
		try
		{
			//Log into ASE
			if (!testMode)
				{
					authenticator.login();
					outputStream.println("Logged in.");
				}
			
			//Check if the folder we are looking for exists at the parentID. If not, -99 will be returned.
			int folderId = checkForFolderAtParentId(parentId, folders[currentFolder]);
			outputStream.println("Folder ID after running getFolderIdAtParent is: " + folderId);
			//Our folder was found
			if (folderId != -99){
				outputStream.println("Folder " + folders[currentFolder] + " was found and will not be created.");
				//Try to create the next folder (or at least check if it exists)
				createFolderService(folderId, folders, "", "", ++currentFolder);					
			} else{
				createFolder(parentId, folders[currentFolder], "", "");
				outputStream.println("Confirming we actually created the folder..");
				folderId = checkForFolderAtParentId(parentId, folders[currentFolder]);
				if (folderId == -99){
					//This should probably actually throw an exception, since it means something didn't work
					outputStream.println("Folder was not actually created...");
				} else{
					outputStream.println("New Folder ID: " + folderId);
					outputStream.println("Folder creation confirmed!");
					createFolderService(folderId, folders, "", "", ++currentFolder);
				}
			}		
		}
		finally
		{
			if (!testMode)
			{
				outputStream.println("Logging out..");
				authenticator.shutdown();
				outputStream.flush();
				outputStream.close();
			}
		}

		return parentId;
	}
/*	
	Integer getFolderId(String folder) throws ParserConfigurationException, SAXException, IOException
	{
		String[] folders = folder.split("/");
		int parentId = 0;
		Integer id = null;
		
		for(int currentFolder = 0; currentFolder < folders.length; currentFolder++){
			id = checkForFolderAtParentId(parentId, folder);	
		}
		
		
		Map<String, Integer> folders = new Hashtable<String, Integer>();
		Stack<String> pathElements = new Stack<String>();

		mapFolders(this.location + "/services/folders", folders, pathElements);
		
		id = folders.get(folder);
		
		return id;
	}
	*/
	
	Integer getFolderId(String folder) throws ParserConfigurationException, SAXException, IOException
	{
		Integer id = null;
		
		Map<String, Integer> folders = new Hashtable<String, Integer>();
		Stack<String> pathElements = new Stack<String>();

		mapFolders(this.location + "/services/folders", folders, pathElements);
		
		id = folders.get(folder);
		
		return id;
	}
	
	Integer getFolderId(String folder, Map<String, Integer> folders) throws ParserConfigurationException, SAXException, IOException
	{
		Integer id = null;
		
		id = folders.get(folder);
		
		return id;
	}
	
	Map<String, Integer> mapFolders(String rootFolderUrl, Map<String, Integer> folders, Stack<String> pathElements) 
			throws ParserConfigurationException, SAXException, IOException
	{
		outputStream.println("Mapping ASE folders structure at " + rootFolderUrl);
		
		Document response = null;
		if (testMode)
		{
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true); // never forget this!

			File subfoldersFile = translateUrlToFilesystemPath(rootFolderUrl);
			DocumentBuilder builder;

			builder = domFactory.newDocumentBuilder();
			if (subfoldersFile.exists())
				response = builder.parse(subfoldersFile);
			else
				outputStream.println("WARNING: Missing node.xml at " + subfoldersFile);
		}
		else
		{
    		response = sendRESTRequest(rootFolderUrl, "");
    
    		if (response != null)
    		{
        		try
        		{
        			checkForError(response);
        		}
        		catch (ASEClientException e)
        		{
        			e.printStackTrace();
        		}
    		}
		}

    	if (response != null)
		{
    		try
    		{
    			String folderName = null;
    			Integer folderId = null;
    			
    			Object folderResult = xpath.evaluate("/ase:folder | /ase:folders/ase:folder", response, XPathConstants.NODESET);
    			NodeList folderNodes = (NodeList) folderResult;
    			for (int i = 0; i < folderNodes.getLength(); i++)
    			{
    				Element folderNode = (Element) folderNodes.item(i);
    				folderName = (String) xpath.evaluate("ase:name/text()", folderNode, XPathConstants.STRING);
    				outputStream.println("Folder element name: " + folderName);
    				
    				String folderIdString = (String) xpath.evaluate("ase:id/text()", folderNode, XPathConstants.STRING);
    				outputStream.println("Folder element id: " + folderIdString);
    				folderId = Integer.parseInt(folderIdString);
    				
    				if (folderName != null && folderId != null)
    				{
        				if (pathElements.size() >= 32)
        				{
        					outputStream.println("Stopping the mapping of a folder branch that exceeds the depth limit of 32.");
        					continue;
        				}
        				pathElements.push(folderName);
        				String path = convertListToString(pathElements, "/");
        				outputStream.println("#### Mapping folder path " + path + " with id " + folderId);
        				folders.put(path, folderId);
    
        				String subfoldersUrl = (String) xpath.evaluate("ase:folders/@href", folderNode, XPathConstants.STRING);
            			
            			// Recurse into a sub-folder
            			mapFolders(subfoldersUrl, folders, pathElements);
            			pathElements.pop();
    				}
    			}
    			
    		}
    		catch (XPathExpressionException e)
    		{
    			e.printStackTrace();
    		}
		}

		return folders;
	}
	
	private File translateUrlToFilesystemPath(String url)
	{
		outputStream.println("Translating URL " + url);
		
		File file = null;
		
		int doubleSlashPos = url.indexOf("//");
		int slashPos = url.indexOf("/", doubleSlashPos + 2);
		// Skip the following "/ase/services/"
		String context = "/ase/services/";
		if (url.substring(slashPos, slashPos + context.length()).equals(context))
			file = new File("test/" + url.substring(slashPos + context.length()) + "/node.xml");
		else
			throw new RuntimeException("Cannot find XML docuemnt for URL " + url + " at file: " + file);

		return file;
	}
	
	private String convertListToString(List<String> list, String delimiter)
	{
		String result;
		
		if (list != null)
		{
			StringBuffer resultBuffer = new StringBuffer();
			Iterator<String> i = list.iterator();
			while (i.hasNext())
			{
				resultBuffer.append(i.next());
				if (i.hasNext())
					resultBuffer.append(delimiter);
			}
			
			result = resultBuffer.toString();
		}
		else
		{
			result = "";
		}
		
		return result;
	}
	
	public static String trimRight(String s, String characters)
	{
    	String trimmed = s;
    	 
    	boolean done = false;
    	for (int i = s.length() - 1; i >= 0 && !done; i--)
    	{
        	if (characters.indexOf(s.charAt(i)) == -1)
        	{
        		trimmed = s.substring(0, i + 1);
        		done = true;
        	}
    	}
    	if (!done)
    		trimmed = "";
    	 
    	return trimmed;
	}
	
	/**
	 * 
	 * @param service
	 *            - URL of the REST service call
	 * @param data
	 *            - (optional) Content of the POST data (XML). When empty, using GET.
	 * @return Response (XML)
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws Exception 
	 */
	private Document sendRESTRequest(String service, String data) throws ParserConfigurationException, IOException, SAXException {
		outputStream.println("Sending REST request to " + service);
		//outStream.println("Sending REST request to " + service);
		
		Document document = null;
		
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true); // never forget this!

		DocumentBuilder builder = domFactory.newDocumentBuilder();
		
		HttpUriRequest request;
		if (data != null && data.length() > 0)
		{
			HttpPost postRequest = new HttpPost(service);
			postRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");
			StringEntity entity = new StringEntity(data);
			postRequest.setEntity(entity);
			request = postRequest;
		}
		else
		{
			request = new HttpGet(service);
		}
		

		HttpResponse response = null;
		try
		{
		    // Make the request
			response = authenticator.send(request);
			outputStream.println("REST response: " + response.getStatusLine());
			outputStream.println(response.getStatusLine());

		}
		catch (ClientProtocolException e)
		{
			outputStream.println("Illegal protocol" + e.getMessage());
		}
		catch (IOException e)
		{
			outputStream.println("Error sending request" + e.getMessage());
		}

		if (response != null)
		{
		    HttpEntity responseEntity = response.getEntity();

		    outputStream.println(response.getStatusLine());
		    if(responseEntity != null) {
		    	outputStream.println("Response content length: " + responseEntity.getContentLength());
		    }	    

			document = builder.parse(responseEntity.getContent());
		}

		return document;
	}
	
	//This method does not seem to accurately return whether or not the folder is created. 
	//This method also does not accurately report if the path begins with ASE
	//FIXME: This method should be checked for logic issues
	public boolean createFolder(String folder, String description, String contact) 
			throws ParserConfigurationException, SAXException, IOException, ASEClientException
	{
		boolean result = false;
		
		String parentFolder = null;
		
		if (folder != null && folder.length() > 0)
		{
			folder.replaceAll("\\\\", "/");
			trimRight(folder, "/");
			
			String[] pathElements = folder.split("/");
			if (pathElements.length > 1)
			{
    			ArrayList<String> ancestorList = new ArrayList<String>();
    			
        		Map<String, Integer> folders = new Hashtable<String, Integer>();
        		mapFolders(this.location + "/services/folders", folders, new Stack<String>());

        		// Verify that pathElements[0].equals("ASE");
        		if (pathElements[0].equals("ASE"))
        		{
        			// Create each ancestor directory path that is not in 'folders'
        			for (int i = 0; i < pathElements.length; i++)
        			{
        				String ancestorPathElement = pathElements[i];
    					parentFolder = convertListToString(ancestorList, "/");	
    					ancestorList.add(ancestorPathElement);
    					String thisFolder = convertListToString(ancestorList, "/");	
            			
    					if (i > 0) // Skip the root node ("ASE\")
    					{
                			// For each 'parentFolder' check if it is in 'folders'
                			Integer parentFolderId = folders.get(parentFolder);
                			Integer thisFolderId = folders.get(thisFolder);
                			if (thisFolderId == null)
                			{    				
            					// FIXME: NPE if parentFolder is one we created, since it isn't in 'folders'
                 				// Create the folder
                				outputStream.println("Creating ASE folder in " + parentFolder + " with name " + ancestorPathElement);
                				//outStream.println("Creating ASE folder in " + parentFolder + " with name " + ancestorPathElement);
                				sendCreateFolderPost(ancestorPathElement, parentFolderId.intValue(), description, contact, 0);
                				
                        		mapFolders(this.location + "/services/folders", folders, new Stack<String>());
                    		}
                			else{
                				outputStream.println("ASE folder " + thisFolder + " already exists");
                				//outStream.println("ASE folder " + thisFolder + " already exists");
                			}
    					}
        			}
        			
        			result = true;
        		}
        		else
        		{
        			outputStream.println("Invalid ASE folder " + folder + " does not begin with \"ASE\\\"");
        		}    			
			}
		}
		
		return result;
	}
	
	//numTimesRequested helps us keep track of how many times we've sent this request. From outside this method, it should always be set to 0.
	private void sendCreateFolderPost(String name, int parentFolderId, String description, String contact, int numTimesRequested)
			throws ParserConfigurationException, SAXException, IOException, ASEClientException
	{
		if (name != null)
		{
			if (description == null)
				description = "";
			if (contact == null)
				contact = "";
			
			HttpPost request = new HttpPost(this.location + "/AddFolder.aspx?fid=" + parentFolderId);
			outputStream.println("HTTP Request: " + request.toString());
			BasicNameValuePair[] params = 
				{
					new BasicNameValuePair("ctl00$ctl00$ctl00$MCH$RCH$PageContentPlaceHolder$Identification$Name", name),
					new BasicNameValuePair("ctl00$ctl00$ctl00$MCH$RCH$PageContentPlaceHolder$Identification$Description", description),
					new BasicNameValuePair("ctl00$ctl00$ctl00$MCH$RCH$PageContentPlaceHolder$Identification$Contact", contact),
					new BasicNameValuePair("__LASTFOCUS", ""),
					new BasicNameValuePair("__EVENTTARGET", "ctl00$ctl00$ctl00$MCH$RCH$SaveApplyCancel$SaveButton"),
					new BasicNameValuePair("__EVENTARGUMENT", ""),
					new BasicNameValuePair("ctl00$ctl00$ctl00$MCH$RCH$ScrollerLeft=ScrollerLeft", "ScrollerLeft"),
					new BasicNameValuePair("ctl00$ctl00$ctl00$MCH$RCH$ScrollerTop=ScrollerTop", "ScrollerTop"),
					new BasicNameValuePair("ctl00$ctl00$ctl00$MCH$RCH$SaveApplyCancel$SaveButton", "Create"),
				};
						
			UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(Arrays.asList(params));
			//urlEncodedFormEntity.setContentEncoding(HTTP.UTF_8);
			request.setEntity(urlEncodedFormEntity);
			request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
//			request.addHeader("Referer", "https://localhost/ase/AddFolder.aspx?fid=1");
			request.addHeader("Referer", this.location + "/AddFolder.aspx?fid=1");
			
			Header[] headers = request.getAllHeaders();
			outputStream.println("Request Headers:");
			for(int p = 0; p < headers.length; p++){
				outputStream.println(headers[p]);
			}
			outputStream.println("End of headers!");
			HttpResponse response = null;

			// Make the request
			response = authenticator.send(request);
			outputStream.println(response.getStatusLine());
			outputStream.println("HTTP Response: " + response.getStatusLine());
			
			// Read the full response and drop it.
			HttpEntity responseEntity = response.getEntity();
			InputStream responseIn = responseEntity.getContent();
			
			outputStream.println("Response Content: ");
			BufferedReader reader = new BufferedReader(new InputStreamReader(responseIn));
			String line;
			while ((line = reader.readLine()) != null){
				outputStream.println(line);
			}
			EntityUtils.consume(responseEntity);
			// Let the caller know if it succeeded
			int responseCode = response.getStatusLine().getStatusCode();
			outputStream.println("Response Code: " + responseCode);
			headers = response.getAllHeaders();
			outputStream.println("Response Headers:");
			for(int p = 0; p < headers.length; p++){
				outputStream.println(headers[p]);
			}
			outputStream.println("End of headers!");
	
			if (responseCode != 200){
				//This is to get around the check browser issue. ASE often responds with a 302 redirect to CheckBrowserVersion.aspx on the first request
				//But the second request goes through..
				if(responseCode == 302){
					boolean incompatibleBrowser = false;
					for(int p = 0; p < headers.length; p++){
						if(headers[p].getValue().contains("CheckBrowserVersion.aspx")){
							outputStream.println("We're being redirected to due to an incompatible browser.. ");
							incompatibleBrowser = true;
							//This check makes sure we only re-send the request once...
							if(numTimesRequested == 0){
								//This should eventually be changed, since it's really inefficient to re-run this entire method.. we should just make the request and parse the response.
								sendCreateFolderPost(name, parentFolderId, description, contact, numTimesRequested++);
								outputStream.println("We'll try the request one more time..");
							}else{
								outputStream.println("Already tried to request twice, so we won't do it again..");
							}	
						}
					}
					if(!incompatibleBrowser)
						outputStream.println("We're being redirected, but not due to an incompatible brower.. this is probably normal.");
				} else
					throw new ASEClientException("Failed to create folder on the ASE instance with response code " + responseCode);
			}
		}
	}
	
	public boolean viewFolder(String folder) 
			throws ParserConfigurationException, SAXException, IOException, ASEClientException
	{
		boolean result = false;
				
		if (folder != null && folder.length() > 0)
		{
			folder.replaceAll("\\\\", "/");
			trimRight(folder, "/");
			
			String[] pathElements = folder.split("/");
			if (pathElements.length > 1)
			{    			
        		Map<String, Integer> folders = new Hashtable<String, Integer>();
        		mapFolders(this.location + "/services/folders", folders, new Stack<String>());

        		// Verify that pathElements[0].equals("ASE");
        		if (pathElements[0].equals("ASE"))
        		{
        			Integer folderId = folders.get(folder);

        			if (folderId != null)
        			{
        				outputStream.println("Getting ASE page for folder " + folder + " with folder id " + folderId);
        				sendViewFolderGet(folderId.intValue());
        			}
        			else
        				outputStream.println("ASE folder " + folder + " does not exist");
        			
        			result = true;
        		}
        		else
        		{
        			outputStream.println("Invalid ASE folder " + folder + " does not begin with \"ASE\\\"");
        		}    			
			}
		}
		
		return result;
	}
	
	private void sendViewFolderGet(int folderId)
			throws ParserConfigurationException, SAXException, IOException, ASEClientException
	{
		HttpGet request = new HttpGet(this.location + "/FolderExplorer.aspx?fid=" + folderId);
		
		HttpResponse response = null;

		// Make the request
		response = authenticator.send(request);
		outputStream.println(response.getStatusLine());
		
		// Read the full response and drop it.
		HttpEntity responseEntity = response.getEntity();
		EntityUtils.consume(responseEntity);
		
		// Let the caller know if it succeeded
		int responseCode = response.getStatusLine().getStatusCode();
		if (responseCode < 200 || responseCode >= 400)
			throw new ASEClientException("Failed to get folder page on the ASE instance with response code " + responseCode);
	}
	
	private HttpResponse getParentFolderPage(String url)
	{
		HttpResponse response = null;
		
		try
		{
			HttpUriRequest request = new HttpGet(url);
			response = authenticator.send(request);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return response;
	}

	/**
	 * Not neccessary for ASE 8.6, but it is nice to have just in case.
	 * 
	 * @param response
	 * @return
	 */
	private String findViewState(HttpResponse response)
	{
		String viewstate = null;
		
		if (response != null)
		{
			HttpEntity responseEntity = response.getEntity();
			try
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(responseEntity.getContent()));
				String line;
				while ((line = reader.readLine()) != null)
				{
					int idPos = line.lastIndexOf("id=\"__VIEWSTATE\"");
					if (idPos > 0)
					{
						//Pattern pattern = Pattern.compile("<input.* id=\"__VIEWSTATE\".* value=\"(.*)\"");
						Pattern pattern = Pattern.compile("id=\"__VIEWSTATE\".* value=\"(.*)\"");
						Matcher matcher = pattern.matcher(line);
						if (matcher.find())
						{
							viewstate = matcher.group(1);
							outputStream.println("Found viewstate: " + viewstate);
						}
					}
				}
				EntityUtils.consume(responseEntity);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		return viewstate;
	}
	
	private void checkForError(Document doc) throws ASEClientException {
		Element rootElement = doc.getDocumentElement();
		if ("error".equalsIgnoreCase(rootElement.getTagName())) {
			outputStream.println("*** Error Occurred ***");
			NodeList nodes = rootElement.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				String nodeName = node.getLocalName();

				if ("code".equalsIgnoreCase(nodeName) || "message".equalsIgnoreCase(nodeName)) {
					outputStream.println(node.getChildNodes().item(0).getNodeValue());
				} else if ("help".equalsIgnoreCase(nodeName)) {
					outputStream.println(node.getAttributes().item(0).getNodeValue());
				}
			}
			throw new ASEClientException("Unexpected error.");
		}
	}
	
	/**
	 * Setup namespace context to be used by XPath expressions
	 */
	private static NamespaceContext _nsContext;
	static {
		_nsContext = new NamespaceContext() {
			public String getNamespaceURI(String prefix) {
				if (prefix.equalsIgnoreCase("ase"))
					return "http://www.ibm.com/Rational/AppScanEnterprise";
				return XMLConstants.NULL_NS_URI;
			}

			public String getPrefix(String arg0) {
				return null;
			}

			public Iterator<?> getPrefixes(String arg0) {
				return null;
			}
		};
	}
	
	private static void print(Object message)
	{
		System.out.println(message);
	}
	
	public void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer = tf.newTransformer();
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

	    transformer.transform(new DOMSource(doc), 
	         new StreamResult(new OutputStreamWriter(out, "UTF-8")));
	}
	
	private static Map<String, String> parseOptions(String[] args)
	{
		Map<String, String> argsMap = new Hashtable<String, String>();
		
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].startsWith("-") && args.length > i + 1)
			{
				argsMap.put(args[i].toLowerCase(), args[i + 1]);
			}
		}
		
		return argsMap;
	}

	public static void main(String[] args)
	{
		Map<String, String> argsMap = parseOptions(args);
		ASERestServicesClient app = null;		
		
		if (args.length == 0)
		{
			print("java -jar ASEClient.jar -command getid -url <url> -folder <folder> -domain <domain> -user <username> -password <password> [-acceptssl true|false]");
			print("java -jar ASEClient.jar -command create -url <url> -folder <folder> [-description <description>] [-contact <contact>] -domain <domain> -user <username> -password <password> [-acceptssl true|false]");
			print("");
			print("For test mode, the given URL maps to a test subdirectory of the current directory and all XML");
			print("documents are named node.xml:");
			print("java -jar ASEClient.jar -test true -command getid -url <url> -folder <folder>");
			print("java -jar ASEClient.jar -test true -command create -url <url> -folder <folder> [-description <description>] [-contact <contact>]");
		}

		String folder = null;
		String description = null;
		String contact = null;
		
		// Handle connection related parameters
		if (Boolean.valueOf(argsMap.get("-test")))
		{
			testMode = true;
    		String location = argsMap.get("-url");
    		
    		app = new ASERestServicesClient(location);
		}
		else
		{
    		String location = argsMap.get("-url");		
    		String domain = argsMap.get("-domain");
    		String user = argsMap.get("-user");
    		String password = argsMap.get("-password");
    		boolean acceptSsl = Boolean.valueOf(argsMap.get("-acceptssl"));
    		
    		app = new ASERestServicesClient(location, domain, user, password, acceptSsl);
		}
		
		// Execute commands
		if (argsMap.get("-command").equals("getid"))
		{
        	folder = argsMap.get("-folder");
    		Integer id = app.getFolderIdService(folder);
    		print(id);
		}
		/*
		else if (argsMap.get("-command").equals("create"))
		{
        	folder = argsMap.get("-folder");
        	description = argsMap.get("-description");		
        	contact = argsMap.get("-contact");
        	if (app.createFolderService(folder, description, contact))
        		print("Folder created");
        	else
        		print("Folder not created");
		}
		*/
		else
			System.err.println("Illegal argument: " + args[0]);		
	}
	
}