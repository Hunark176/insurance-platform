package de.hunar.insurance.config;

import org.h2.tools.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.sql.SQLException;

@Configuration
@Profile("dev")
@ConditionalOnProperty(prefix = "insurance.dev.h2.tcp", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DevH2TcpServerConfiguration {

    @Bean(initMethod = "start", destroyMethod = "stop")
    Server devH2TcpServer() throws SQLException {
        return Server.createTcpServer("-tcpPort", "9123", "-tcpDaemon");
    }
}
