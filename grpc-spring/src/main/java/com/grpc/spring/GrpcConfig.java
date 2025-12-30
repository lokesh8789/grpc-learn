package com.grpc.spring;

import com.google.protobuf.util.JsonFormat;
import net.devh.boot.grpc.client.channelfactory.GrpcChannelConfigurer;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.protobuf.ProtobufJsonFormatHttpMessageConverter;

import java.util.concurrent.Executors;

@Configuration
public class GrpcConfig {
    @Bean
    public GrpcServerConfigurer serverConfigurer() {
        return serverBuilder -> serverBuilder.executor(Executors.newVirtualThreadPerTaskExecutor());
    }

    @Bean
    public GrpcChannelConfigurer channelConfigurer() {
        return (managedChannelBuilder, s) -> managedChannelBuilder.executor(Executors.newVirtualThreadPerTaskExecutor());
    }

    // For Returning Proto Built Classes like User, HelloRequest, HelloResponse etc. from RestController Methods
    @Bean
    public ProtobufJsonFormatHttpMessageConverter protobufJsonFormatHttpMessageConverter(){
        return new ProtobufJsonFormatHttpMessageConverter(
                JsonFormat.parser().ignoringUnknownFields(),
                JsonFormat.printer().omittingInsignificantWhitespace().includingDefaultValueFields()
        );
    }
}
