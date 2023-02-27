/**
 * 
 */
package br.io.otojunior.ntpclient;

import java.net.InetAddress;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;

import org.apache.commons.net.ntp.NTPUDPClient;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase.SYSTEMTIME;

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

			var systemTime = new SYSTEMTIME();
			systemTime.wYear = (short) time.getYear();
	        systemTime.wMonth = (short) time.getMonth().getValue();
	        systemTime.wDay = (short) time.getDayOfMonth();
	        systemTime.wHour = (short) time.getHour();
	        systemTime.wMinute = (short) time.getMinute();
	        systemTime.wSecond = (short) time.getSecond();
	        systemTime.wMilliseconds = (short) time.get(ChronoField.MILLI_OF_SECOND);
	        
	        var result = Kernel32.INSTANCE.SetLocalTime(systemTime);
	        System.out.println(result
	        	? "Data e hora do sistema definidas com sucesso"
    			: "Falha ao definir a data e hora do sistema");

	        System.out.println("NTP time: " + time.format(fmthora));
            System.out.println("NTP date: " + time.format(fmtdata));
        } finally {
            client.close();
            watchEnd = System.currentTimeMillis();
            System.out.println("Calculate time: " + (watchEnd-watchStart));
        }
	}
}
