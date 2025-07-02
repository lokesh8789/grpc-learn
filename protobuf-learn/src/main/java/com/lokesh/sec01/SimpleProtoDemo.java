package com.lokesh.sec01;

import com.lokesh.models.sec01.PersonOuterClass;

public class SimpleProtoDemo {

    public static void main(String[] args) {
        PersonOuterClass.Person person = PersonOuterClass.Person.newBuilder()
                .setName("Lokesh")
                .setAge(12)
                .build();
        System.out.println(person);
    }

}
