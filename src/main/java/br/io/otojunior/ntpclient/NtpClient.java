/**
 * 
 */
package br.io.otojunior.ntpclient;

import java.net.InetAddress;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.commons.net.ntp.NTPUDPClient;

/**
 * @author Oto Soares Coelho Junior
 * @since 26/02/2023
 */
public class NtpClient {
	private static final String NTP_SERVER = "pool.ntp.br";

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		var client = new NTPUDPClient();

        var watchStart = 0L;
        var watchEnd = 0L;

        try {
        	final var defaultZoneId = ZoneId.systemDefault();
        	final var inetAddr = InetAddress.getByName(NTP_SERVER);
        	final var fmthora = DateTimeFormatter.ofPattern("HH:mm:ss");
        	final var fmtdata = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        	final var runtime = Runtime.getRuntime();

        	watchStart = System.currentTimeMillis();

            client.open();
            var timeInfo = client.getTime(inetAddr);

            var returnTime = timeInfo
        		.getMessage()
        		.getTransmitTimeStamp()
        		.getTime();

			var time = LocalDateTime
				.ofInstant(Instant
				.ofEpochMilli(returnTime), defaultZoneId);

			runtime.exec(new String[] {"cmd", "/C", "time", time.format(fmthora)});
			runtime.exec(new String[] {"cmd", "/C", "date", time.format(fmtdata)});

            System.out.println("NTP time: " + time.format(fmthora));
            System.out.println("NTP date: " + time.format(fmtdata));
        } finally {
            client.close();
            watchEnd = System.currentTimeMillis();
            System.out.println("Calculate time: " + (watchEnd-watchStart));
        }
	}
}
