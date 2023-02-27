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

import org.apache.commons.net.ntp.NTPUDPClient;
import org.tinylog.Logger;

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
        	final var fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss.SSS");

        	if (Logger.isDebugEnabled()) {
        		Logger.debug("Servidor NTP: {}", NTP_SERVER);
        		if (Logger.isTraceEnabled()) {
        			watchStart = System.nanoTime();
        			Logger.trace("Início do calculo do tempo total");
        		}
        	}

            client.open();
            var timeInfo = client.getTime(inetAddr);
            
            var returnTime = timeInfo
        		.getMessage()
        		.getTransmitTimeStamp()
        		.getTime();

			var time = LocalDateTime
				.ofInstant(Instant
				.ofEpochMilli(returnTime), defaultZoneId);
			var timeStr = time.format(fmt);
			
			if (Logger.isDebugEnabled()) {
				Logger.debug("Data/Hora obitda: {}", timeStr);
			}

			var systemTime = new SYSTEMTIME();
			systemTime.wYear = (short) time.getYear();
	        systemTime.wMonth = (short) time.getMonth().getValue();
	        systemTime.wDay = (short) time.getDayOfMonth();
	        systemTime.wHour = (short) time.getHour();
	        systemTime.wMinute = (short) time.getMinute();
	        systemTime.wSecond = (short) time.getSecond();
	        systemTime.wMilliseconds = (short) time.get(ChronoField.MILLI_OF_SECOND);
	        
	        var result = Kernel32.INSTANCE.SetLocalTime(systemTime);
	        
	        if (result) {
	        	Logger.info("Data/Hora definida: {}", timeStr);
	        } else {
	        	Logger.error("Erro na definição de Data/Hora");
	        }
        } finally {
            client.close();
            if (Logger.isTraceEnabled()) {
            	watchEnd = System.nanoTime();
            	Logger.trace("Tempo Total (nano): {}", (watchEnd-watchStart));
    		}
        }
	}
}
