package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class ThroughputHttpServer {

    private final static String WORDS = "resources/war_and_peace.txt";
    private final static int THREADS_NUMBER = 17;


    public static void main(String[] args) throws IOException {
        final String text = new String(Files.readAllBytes(Paths.get(WORDS)));
        startServer(text);
    }

    private static void startServer(String text) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8001),0);
        server.createContext("/search", new WordHandler(text));
        Executor executor = Executors.newFixedThreadPool(THREADS_NUMBER);
        server.setExecutor(executor);
        server.start();
        System.out.println("Server started : " + server.getAddress().getHostName());
    }

    private static class WordHandler implements HttpHandler {
        private final String text;

        public WordHandler(String text) {
            this.text = text;
        }


        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String[] keyValue = query.split("=");
            String action = keyValue[0];
            String word = keyValue[1];

            if (!action.equalsIgnoreCase("word")) {
                exchange.sendResponseHeaders(400, 0);
            }

            long count = countWord(word);
            byte[] response = Long.toString(count).getBytes();
            exchange.sendResponseHeaders(200, response.length);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(response);
            outputStream.close();
        }

        private long countWord(String word) {
            long count = 0;
            int index = 0;
            while (index >= 0) {
                index = text.indexOf(word, index);
                if (index >= 0) {
                    count ++;
                    index ++;
                }
            }
            return count;
        }
    }
}