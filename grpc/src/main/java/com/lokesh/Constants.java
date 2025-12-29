package com.lokesh;

import io.grpc.Context;
import io.grpc.Metadata;

public class Constants {
    public static final Metadata.Key<String> USER_TOKEN_KEY = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
    public static final String BEARER = "Bearer";
    public static final Context.Key<UserRole> USER_ROLE_KEY = Context.key("user-role");
}
