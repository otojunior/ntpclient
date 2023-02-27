/**
 * 
 */
package br.io.otojunior.ntpclient;

import static java.lang.System.err;
import static java.lang.System.out;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase.SYSTEMTIME;

/**
 * @author Oto Soares Coelho Junior
 * @since 26/02/2023
 */
public class NtpClient {
	private static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss.SSS");
    private static final Kernel32 kernel32 = Kernel32.INSTANCE;

    /**
     * @param args
     * @throws Exception 
     * @throws SocketException 
     * @throws UnknownHostException 
     */
    public static void main(String[] args) throws Exception {
    	final String server = args.length == 0 ? "pool.ntp.br" : args[0];

        var watchStart = 0L;
        var watchEnd = 0L;

        out.println("Servidor NTP: " + server);
        watchStart = System.nanoTime();

        final var inetAddr = InetAddress.getByName(server);
        final var client = new NTPUDPClient();
        client.open();

        var timeInfo = client.getTime(inetAddr);

        var returnTime = timeInfo
            .getMessage()
            .getTransmitTimeStamp()
            .getTime();

        var time = LocalDateTime
            .ofInstant(Instant
            .ofEpochMilli(returnTime), DEFAULT_ZONE_ID);
        var timestr = time.format(FMT);

		out.println("Data/Hora obitda: " + timestr);
        
        var systemTime = new SYSTEMTIME();
        systemTime.wYear = (short) time.getYear();
        systemTime.wMonth = (short) time.getMonth().getValue();
        systemTime.wDay = (short) time.getDayOfMonth();
        systemTime.wHour = (short) time.getHour();
        systemTime.wMinute = (short) time.getMinute();
        systemTime.wSecond = (short) time.getSecond();
        systemTime.wMilliseconds = (short) time.get(ChronoField.MILLI_OF_SECOND);

		var result = kernel32.SetLocalTime(systemTime);

        if (result) {
        	out.println("Data/Hora definida: " + timestr);
        } else {
            err.println("Erro na definição de Data/Hora");
        }
        
        client.close();
        
        watchEnd = System.nanoTime();
        out.println("Tempo Total (nano): " + (watchEnd - watchStart));
    }
}
