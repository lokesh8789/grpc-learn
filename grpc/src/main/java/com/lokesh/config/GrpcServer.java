package com.lokesh.config;

import com.lokesh.sec01.BankService;
import io.grpc.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GrpcServer {

    private final Server server;

    private GrpcServer(Server server) {
        this.server = server;
    }

    public static GrpcServer create(BindableService... bindableService) {
        return create(6565, bindableService);
    }

    public static GrpcServer create(int port, BindableService... bindableServices) {
        var serverBuilder = ServerBuilder.forPort(port);
        Arrays.asList(bindableServices).forEach(serverBuilder::addService);
        return new GrpcServer(serverBuilder.build());
    }

    public GrpcServer start() {
        List<String> services = server.getServices()
                .stream()
                .map(ServerServiceDefinition::getServiceDescriptor)
                .map(ServiceDescriptor::getName)
                .toList();
        try {
            server.start();
            System.out.println("Server started, listening on " + server.getPort() + ", Services: " + services);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public void block() {
        try {
            server.awaitTermination();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        server.shutdownNow();
        System.out.println("Server shut down");
    }
}