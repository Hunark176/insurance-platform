package de.hunar.insurance.config;

import org.h2.tools.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.sql.SQLException;

@Configuration
@Profile("dev")
public class DevH2TcpServerConfiguration {

    @Bean(initMethod = "start", destroyMethod = "stop")
    Server devH2TcpServer() throws SQLException {
        return Server.createTcpServer("-tcpPort", "9123", "-tcpDaemon");
    }
}
