package org.scijava.code.lsp.groovy;

import org.eclipse.lsp4j.services.LanguageServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.code.lsp.Environment;
import org.scijava.code.lsp.LanguageServerService;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link GroovyServerPlugin}
 *
 * @author Gabriel Selzer
 */
public class GroovyServerPluginTest {

    private Context context;
    private LanguageServerService servers;

    @BeforeEach
    public void setUp() {
        context = new Context(LanguageServerService.class);
        servers = context.service(LanguageServerService.class);
    }

    @AfterEach
    public void tearDown() {
        context.dispose();
    }

    @Test
    public void testServerIsDiscovered() {
        assertTrue(servers.supports("groovy"));
    }

    @Test
    public void testServerLaunch() {
        LanguageServer server = servers.launch(
            "groovy",
            Environment.NONE
        );
        assertTrue(server instanceof GroovyLanguageServer);
    }
}

