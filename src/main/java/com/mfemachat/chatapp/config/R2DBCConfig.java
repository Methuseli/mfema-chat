package com.mfemachat.chatapp.config;

import com.mfemachat.chatapp.models.RequestStatus;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.postgresql.codec.EnumCodec;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class R2DBCConfig extends AbstractR2dbcConfiguration {

  @SuppressWarnings("null")
  @Bean
  ConnectionFactoryInitializer initializer(
    ConnectionFactory connectionFactory
  ) {
    ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
    initializer.setConnectionFactory(connectionFactory);
    ResourceDatabasePopulator resource = new ResourceDatabasePopulator(
      new ClassPathResource("schema.sql")
    );
    initializer.setDatabasePopulator(resource);
    return initializer;
  }

  @SuppressWarnings("null")
  @Bean
  ReactiveTransactionManager transactionManager(
    ConnectionFactory connectionFactory
  ) {
    return new R2dbcTransactionManager(connectionFactory);
  }

  @SuppressWarnings("null")
  @Bean
  @Override
  public ConnectionFactory connectionFactory() {
    return new PostgresqlConnectionFactory(
      PostgresqlConnectionConfiguration
        .builder()
        .host("localhost")
        .database("chatapp")
        .username("postgres")
        .password("#1Foofighters")
        .codecRegistrar(
          EnumCodec
            .builder()
            .withEnum("request_status_enum", RequestStatus.class)
            .build()
        )
        .build()
    );
  }

  @SuppressWarnings("null")
  @Bean
  public DatabaseClient r2dbcDatabaseClient(
    ConnectionFactory connectionFactory
  ) {
    return DatabaseClient.create(connectionFactory);
  }
}
