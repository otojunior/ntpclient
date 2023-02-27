/**
 * 
 */
package br.io.otojunior.ntpclient;

import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @author Oto Soares Coelho Junior
 * @since 26/02/2023
 */
public class NtpClient {
	private static final String NTP_SERVER = "pool.ntp.br";

	/**
	 * @param args
	 * @throws Exception 
	 * @throws SocketException 
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args) throws Exception {
        try (var service = new NtpClientService(NTP_SERVER)) {
        	service.setLocalTime(service.getNtpTime());
        }
	}
}
