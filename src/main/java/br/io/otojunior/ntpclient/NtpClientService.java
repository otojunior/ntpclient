/**
 * 
 */
package br.io.otojunior.ntpclient;

import static java.lang.System.out;
import java.net.InetAddress;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import org.apache.commons.net.ntp.NTPUDPClient;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase.SYSTEMTIME;

/**
 * Serviço de obtenção de horário NTP.
 * @author Oto Soares Coelho Junior
 * @since 27/02/2023
 */
public class NtpClientService implements AutoCloseable {
    static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();
    static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss.SSS");
    static final Kernel32 KERNEL32 = Kernel32.INSTANCE;

    final String server;
    final InetAddress inetAddr;
    final NTPUDPClient client;

    long watchStart = 0L;
    long watchEnd = 0L;

    /**
     * Construtor do serviço de obtenção do NTP.
     * @param server Servidor NTP.
     * @throws Exception
     */
    public NtpClientService(final String server) throws Exception {
        out.println("[NtpClient] Servidor NTP: " + server);
        this.watchStart = System.nanoTime();
        out.println("[NtpClient] Início do calculo do tempo total");

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

        out.println("[NtpClient] Data/Hora obitda: " + time.format(FMT));

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

		var result = KERNEL32.SetLocalTime(systemTime);

        if (result) {
            out.println("[NtpClient] Data/Hora definida: " + time.format(FMT));
        } else {
            out.println("[NtpClient] Erro na definição de Data/Hora");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws Exception {
        client.close();
        
        this.watchEnd = System.nanoTime();
        out.println("[NtpClient] Tempo Total (nano): " + (this.watchEnd - this.watchStart));
    }
}
