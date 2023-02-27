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
 * Serviço de obtenção de horário NTP.
 * @author Oto Soares Coelho Junior
 * @since 27/02/2023
 */
public class NtpClientService implements AutoCloseable {
	private static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();
	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss.SSS");

	private final String server;
	private final InetAddress inetAddr;
	private final NTPUDPClient client;

	private long watchStart = 0L;
	private long watchEnd = 0L;

	/**
	 * Construtor do serviço de obtenção do NTP.
	 * @param server Servidor NTP.
	 * @throws Exception
	 */
	public NtpClientService(final String server) throws Exception {
		if (Logger.isDebugEnabled()) {
    		Logger.debug("Servidor NTP: {}", server);
    		if (Logger.isTraceEnabled()) {
    			this.watchStart = System.nanoTime();
    			Logger.trace("Início do calculo do tempo total");
    		}
    	}
		
		this.server = server;
		this.inetAddr = InetAddress.getByName(this.server);
		this.client = new NTPUDPClient();
		this.client.open();
	}

	/**
	 * Obtém horário NTP.
	 * @return Horário NTP.
	 * @throws Exception
	 */
	public LocalDateTime getNtpTime() throws Exception {
		var timeInfo = client.getTime(inetAddr);
		
		var returnTime = timeInfo
			.getMessage()
			.getTransmitTimeStamp()
			.getTime();
		
		var time = LocalDateTime
			.ofInstant(Instant
			.ofEpochMilli(returnTime), DEFAULT_ZONE_ID);
		
		if (Logger.isDebugEnabled()) {
			Logger.debug("Data/Hora obitda: {}", time.format(FMT));
		}
		
		return time;
	}

	/**
	 * Define a hora local do Windows
	 * @param time Horário obtido do servidor NTP.
	 */
	public void setLocalTime(final LocalDateTime time) {
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
        	Logger.info("Data/Hora definida: {}", time.format(FMT));
        } else {
        	Logger.error("Erro na definição de Data/Hora");
        }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws Exception {
		client.close();
        
		if (Logger.isTraceEnabled()) {
        	this.watchEnd = System.nanoTime();
        	Logger.trace("Tempo Total (nano): {}", (this.watchEnd - this.watchStart));
		}
	}
}
