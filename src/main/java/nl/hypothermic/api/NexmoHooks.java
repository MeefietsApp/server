package nl.hypothermic.api;

import java.io.IOException;

import com.nexmo.client.NexmoClient;
import com.nexmo.client.NexmoClientException;
import com.nexmo.client.auth.AuthMethod;
import com.nexmo.client.auth.TokenAuthMethod;
import com.nexmo.client.sms.SmsSubmissionResult;
import com.nexmo.client.sms.messages.TextMessage;

/*
 * -----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <admin@hypothermic.nl> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.
 * -----------------------------------------------------------------------------
 */

public class NexmoHooks {
	
	private AuthMethod auth;
	private NexmoClient client;
	
	public NexmoHooks(String nexmoKey, String nexmoSecret) {
		auth = new TokenAuthMethod(nexmoKey, nexmoSecret);
		client = new NexmoClient(auth);
	}

	public void sendTextMessage(String sender, String receiver, String msg) throws IOException, NexmoClientException {
		TextMessage message = new TextMessage(sender, receiver, msg);
		SmsSubmissionResult[] responses = client.getSmsClient().submitMessage(message);
		for (SmsSubmissionResult response : responses) {
			System.out.println(response);
		}
	}
}
