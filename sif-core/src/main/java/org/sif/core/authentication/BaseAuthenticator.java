package org.sif.core.authentication;

import java.io.IOException;
import java.io.PrintStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;


public class BaseAuthenticator implements Authenticator
{
	private Log log = LogFactory.getLog(BaseAuthenticator.class);
	
	private String location;
	private String domain;
	private String user;
	private String password;
	private boolean acceptSsl;
	
	HttpClient client;
	HttpContext httpContext;
	
	public BaseAuthenticator(String location, String domain, String user, String password, boolean acceptSsl)
	{
		//outStream = outputStream;
		log.debug("Starting client to connect to " + location + " on domain " + domain + " with credentials "
				+ user + ":" + password);
		
		//outStream.println("Starting client to connect to " + location + " on domain " + domain + " with credentials "
			//	+ user + ":" + password);
		
		if (acceptSsl){
			log.debug("!!! Ignoring SSL server certificate verification errors");
			//outStream.println("!!! Ignoring SSL server certificate verification errors");
		}
		this.location = location;
		this.domain = domain;
		this.user = user;
		this.password = password;
		this.acceptSsl = acceptSsl;		
	}
	
	public void login() throws ClientProtocolException, IOException
	{
		// POST to /services/login
		//Content-Type: application/x-www-form-urlencoded
		//userid: element containing the user's login id.
		//password: element containing the user's password.

		try
		{
			if (isAcceptSsl())
				client = getHttpClientTrustingAllSSLCerts();
			else
				client = new DefaultHttpClient();
		}
		catch (KeyManagementException e1)
		{
			log.error("Key management", e1);
		}
		catch (NoSuchAlgorithmException e1)
		{
			log.error("No such algorithm", e1);
		}
		
		CookieStore cookieStore = new BasicCookieStore();
		httpContext = new BasicHttpContext();
		httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

		HttpPost request = new HttpPost(getLocation() + "/services/login");
		
	    // Setup the request parameters
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("userid", getUser()));
		nameValuePairs.add(new BasicNameValuePair("password", getPassword()));
		request.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		HttpResponse response = null;
		try
		{
		    // Make the request
			//outStream.println("New HTTP Request: " + request.getRequestLine());
			response = client.execute(request, httpContext);
			log.debug(response.getStatusLine());
			//outStream.println("Response: " + response.getStatusLine());

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
		    HttpEntity responseEntity = response.getEntity();

		    log.debug(response.getStatusLine());
		    if(responseEntity != null) {
		    	log.debug("Response content length: " + responseEntity.getContentLength());
		    }

		    String jsonResultString = EntityUtils.toString(responseEntity);
		    EntityUtils.consume(responseEntity);		    
		}
	}
	
	protected DefaultHttpClient getHttpClientTrustingAllSSLCerts() throws NoSuchAlgorithmException,
    	KeyManagementException
    {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        
        // Use a TrustManager that trusts any server cert.
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, getTrustingManager(), new java.security.SecureRandom());
        
        // Use a hostname verifier that allows any hostname in the server cert.
        SSLSocketFactory socketFactory = new SSLSocketFactory(sc,
        		org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Scheme sch = new Scheme("https", 443, socketFactory);
        httpclient.getConnectionManager().getSchemeRegistry().register(sch);
        return httpclient;
    }
    
    private TrustManager[] getTrustingManager()
    {
    TrustManager[] trustAllCerts = new TrustManager[]
    { new X509TrustManager()
    {
    
    	public void checkClientTrusted(X509Certificate[] chain, String authType)
    			throws CertificateException
    	{
    		// Do nothing
    	}
    
    	public void checkServerTrusted(X509Certificate[] chain, String authType)
    			throws CertificateException
    	{
    		// Do nothing
    	}
    
    	public X509Certificate[] getAcceptedIssuers()
    	{
    		return null;
    	}
    } };
    return trustAllCerts;
    }
    
    public HttpResponse send(HttpUriRequest request) throws ClientProtocolException, IOException
    {
		return client.execute(request, httpContext);
    }

    public void shutdown()
    {
    	client.getConnectionManager().shutdown();
    }
    
    public HttpClient getHttpClient()
    {
    	return client;
    }


	public String getLocation()
	{
		return location;
	}

	public void setLocation(String location)
	{
		this.location = location;
	}

	public String getDomain()
	{
		return domain;
	}

	public void setDomain(String domain)
	{
		this.domain = domain;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public boolean isAcceptSsl()
	{
		return acceptSsl;
	}

	public void setAcceptSsl(boolean acceptSsl)
	{
		this.acceptSsl = acceptSsl;
	}


}
