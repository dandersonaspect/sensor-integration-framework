package org.sifappscanplugin.publisher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.sif.core.authentication.BaseAuthenticator;
import org.sif.core.authentication.NTLMSchemeFactory;

public class ASEAuthenticator extends BaseAuthenticator
{

	private Log log = LogFactory.getLog(ASEAuthenticator.class);
	
	private String location;
	private String domain;
	private String user;
	private String password;
	private boolean acceptSsl;
	//private static PrintStream outStream;

	public ASEAuthenticator(String location, String domain, String user, String password, boolean acceptSsl)
	{
		super(location, domain, user, password, acceptSsl);
	}

	public void connect()
	{
		DefaultHttpClient httpClient = null;
		try
		{
			if (acceptSsl)
				httpClient = getHttpClientTrustingAllSSLCerts();
			else
				httpClient = new DefaultHttpClient();
		}
		catch (KeyManagementException e1)
		{
			log.error("Key management", e1);
			e1.printStackTrace();
		}
		catch (NoSuchAlgorithmException e1)
		{
			log.error("No such algorithm", e1);
		}

		if (httpClient != null)
		{
			httpClient.getAuthSchemes().register("ntlm", new NTLMSchemeFactory());
			String localHostname;
			try
			{
				localHostname = InetAddress.getLocalHost().getCanonicalHostName();
			}
			catch (UnknownHostException e1)
			{
				log.error("Cannot get local hostname", e1);
				localHostname = "localhost";
			}
			httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY,
					new NTCredentials(user, password, localHostname, domain));

			HttpGet request = new HttpGet(location);
			//outStream.println("New HTTP Request to: " + location);
			HttpResponse response = null;
			try
			{
				response = httpClient.execute(request);
				//outStream.println("Response: " + response.getStatusLine());
				log.debug(response.getStatusLine());

			}
			catch (ClientProtocolException e)
			{
				log.error("Illegal protocol", e);
			}
			catch (IOException e)
			{
				log.error("Error sending request", e);
			}

			if (response != null)
			{
				HttpEntity entity = response.getEntity();

				// If the response does not enclose an entity, there is no need to worry about
				// connection release
				if (entity != null)
				{
					InputStream inStream = null;
					try
					{
						inStream = entity.getContent();

						BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));

						// Just print out the response
						log.debug(reader.readLine());
						//outStream.println("Response: " + reader.readLine());

					}
					catch (IOException ex)
					{

						// In case of an IOException the connection will be released back to the
						// connection manager automatically
						log.error("Error reading response", ex);
						// throw ex;

					}
					catch (RuntimeException ex)
					{

						// In case of an unexpected exception you may want to abort the HTTP request
						// in order to shut down the underlying connection and release it back to
						// the connection manager.
						request.abort();
						throw ex;

					}
					finally
					{

						// Closing the input stream will trigger connection release
						if (inStream != null)
							try
							{
								inStream.close();
							}
							catch (IOException e)
							{
								// Ignore
							}

					}

					// When HttpClient instance is no longer needed, shut down the connection
					// manager to ensure immediate deallocation of all system resources
					httpClient.getConnectionManager().shutdown();
				}
			}
		}
	}
	
	public static void main(String[] args)
	{

		if (args.length < 4)
			System.out.println("java -jar NTLMv2Client.jar <url> <domain> <username> <password>");
		String location = args[0];
		String domain = args[1];
		String user = args[2];
		String password = args[3];
		boolean acceptSsl = false;
		if (args.length > 4 && args[4].length() > 0 && args[4].matches("[-]{0,2}acceptssl"))
			acceptSsl = true;

		ASEAuthenticator app = new ASEAuthenticator(location, domain, user, password, acceptSsl);
		app.connect();

	}

}
