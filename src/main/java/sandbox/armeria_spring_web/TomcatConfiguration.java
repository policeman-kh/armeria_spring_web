package sandbox.armeria_spring_web;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import com.linecorp.armeria.common.AggregatedHttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.server.RequestTimeoutException;
import com.linecorp.armeria.server.tomcat.TomcatService;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;

@Configuration
public class TomcatConfiguration {
    private final ByteBuffer errorPageBytes;

    public TomcatConfiguration(ResourceLoader resourceLoader) {
        errorPageBytes = errorPageBytes(resourceLoader);
    }

    @Bean
    public TomcatService tomcatService(ServletWebServerApplicationContext applicationContext) {
        return TomcatService.of(getConnector(applicationContext));
    }

    @Bean
    public ArmeriaServerConfigurator armeriaServerConfigurator(TomcatService tomcatService) {
        return sb -> sb.service("prefix:/", tomcatService)
                       .exceptionHandler((ctx, cause) -> {
                           if (cause instanceof RequestTimeoutException) {
                               // Customize timeout error page.
                               return AggregatedHttpResponse.of(HttpStatus.SERVICE_UNAVAILABLE,
                                                                MediaType.HTML_UTF_8,
                                                                errorPageBytes.array());
                           }
                           return null;
                       });
    }

    private static Connector getConnector(ServletWebServerApplicationContext applicationContext) {
        final TomcatWebServer container = (TomcatWebServer) applicationContext.getWebServer();

        // Start the container to make sure all connectors are available.
        container.start();
        return container.getTomcat().getConnector();
    }

    private static ByteBuffer errorPageBytes(ResourceLoader resourceLoader) {
        try {
            return ByteBuffer.wrap(Files.readAllBytes(
                    resourceLoader.getResource("classpath:" + "/static/503.html")
                                  .getFile().toPath()));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
