package org.sif.core.authentication;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;


public interface Authenticator
{
	public void login() throws ClientProtocolException, IOException;
    public HttpResponse send(HttpUriRequest request) throws ClientProtocolException, IOException;
    public void shutdown();
	public String getLocation();

}
