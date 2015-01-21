package org.sif.core.authentication;

import java.io.IOException;

import jcifs.ntlmssp.NtlmFlags;
import jcifs.ntlmssp.Type1Message;
import jcifs.ntlmssp.Type2Message;
import jcifs.ntlmssp.Type3Message;
import jcifs.util.Base64;

import org.apache.http.impl.auth.NTLMEngine;
import org.apache.http.impl.auth.NTLMEngineException;

/**
 * Delegation of Windows authentication to JCIFS based on instructions provided by Apache Software
 * Foundation.
 * 
 * @ref http://hc.apache.org/httpcomponents-client-ga/ntlm.html
 * @ref http://jcifs.samba.org/
 *
 */
public final class JCIFSEngine implements NTLMEngine
{

	private static final int TYPE_1_FLAGS = NtlmFlags.NTLMSSP_NEGOTIATE_56
			| NtlmFlags.NTLMSSP_NEGOTIATE_128 | NtlmFlags.NTLMSSP_NEGOTIATE_NTLM2
			| NtlmFlags.NTLMSSP_NEGOTIATE_ALWAYS_SIGN | NtlmFlags.NTLMSSP_REQUEST_TARGET;

	public String generateType1Msg(final String domain, final String workstation)
			throws NTLMEngineException
	{
		System.out.println("generateType1Msg");
		System.out.println(String.format("domain [%1$s] workstation [%2$s]", domain, workstation));

		final Type1Message type1Message = new Type1Message(TYPE_1_FLAGS, domain, workstation);
		String encodedString = Base64.encode(type1Message.toByteArray());

		System.out.println("Type1 Message:" + encodedString);
		return encodedString;
	}

	public String generateType3Msg(final String username, final String password,
			final String domain, final String workstation, final String challenge)
			throws NTLMEngineException
	{
		Type2Message type2Message;

		System.out.println("generateType3Msg");
		System.out.println(String.format(
				"User [%1$s] password [%2$s] domain [%3$s] workstation [%4$s]", username, password,
				domain, workstation));
		System.out.println("Challenge:" + challenge);

		try
		{
			type2Message = new Type2Message(Base64.decode(challenge));
		}
		catch (final IOException exception)
		{
			System.out.println("Invalid Type2 message");
			throw new NTLMEngineException("Invalid NTLM type 2 message", exception);
		}

		System.out.println("128: "
				+ String.valueOf(type2Message.getFlag(NtlmFlags.NTLMSSP_NEGOTIATE_128)));

		final int type2Flags = type2Message.getFlags();
		final int type3Flags = type2Flags
				& (0xffffffff ^ (NtlmFlags.NTLMSSP_TARGET_TYPE_DOMAIN | NtlmFlags.NTLMSSP_TARGET_TYPE_SERVER));
		final Type3Message type3Message = new Type3Message(type2Message, password, domain,
				username, workstation, type3Flags);
		String encodedString = Base64.encode(type3Message.toByteArray());

		System.out.println("Type3 Message:" + encodedString);
		return Base64.encode(type3Message.toByteArray());
	}

}