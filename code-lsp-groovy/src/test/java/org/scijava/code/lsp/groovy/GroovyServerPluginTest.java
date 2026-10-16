package org.scijava.code.lsp.groovy;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.services.LanguageServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.code.lsp.Environment;
import org.scijava.code.lsp.LanguageServerService;
import org.scijava.code.lsp.Positions;

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

    /** Plain Groovy, as the server's clients send it. */
    @Test
    public void testCompletion() throws Exception {
        LanguageServer server = servers.launch("groovy", Environment.NONE);
        server.initialize(new InitializeParams()).get();
        String code = "def s = 'hello'\ns.toUpp";
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(
            new TextDocumentItem("untitled:/a.groovy", "groovy", 1, code)));
        List<String> labels = server.getTextDocumentService().completion(
            new CompletionParams(new TextDocumentIdentifier("untitled:/a.groovy"),
                Positions.position(code, code.length()))).get(30, TimeUnit.SECONDS)
            .getRight().getItems().stream().map(CompletionItem::getLabel)
            .collect(Collectors.toList());
        assertTrue(labels.contains("toUpperCase"), labels.toString());
    }
}

