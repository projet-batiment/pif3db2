package fr.insa.toto.webui;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.theme.Theme;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.User;
import fr.insa.toto.webui.session.Session;
import fr.insa.toto.webui.utils.NotificationError;
import java.sql.Connection;
import java.sql.SQLException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@SpringBootApplication
@Theme("default")
public class Application extends SpringBootServletInitializer implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Component
    public class OnUIInit implements VaadinServiceInitListener {
        @Override
        public void serviceInit(ServiceInitEvent event) {
            event.getSource().addUIInitListener(uiEvent -> {
                Session.init();
            });
        }
    }
}
