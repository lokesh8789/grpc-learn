package com.lokesh;

import com.lokesh.config.GrpcServer;
import com.lokesh.sec01.BankService;
import com.lokesh.sec01.FlowControlService;

public class Demo {

    public static void main(String[] args) {
        GrpcServer.create(new BankService(), new FlowControlService())
                .start()
                .block();
    }
}
