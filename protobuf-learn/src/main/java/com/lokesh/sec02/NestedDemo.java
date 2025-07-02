package com.lokesh.sec02;

import com.google.protobuf.FloatValue;
import com.lokesh.models.sec01.PersonOuterClass;
import com.lokesh.models.sec02.*;

import java.util.Arrays;

public class NestedDemo {
    public static void main(String[] args) {

        Student f1 = Student.newBuilder()
                .setName("f1")
                .build();
        Student f2 = Student.newBuilder()
                .setName("f2")
                .build();

        Address address = Address.newBuilder()
                .setCity("Mewar")
                .build();
        School school = School.newBuilder()
                .setName("DAV")
                .setAddress(address.toBuilder().setCity("Berlin"))
                .build();
        Student lokesh = Student.newBuilder()
                .setName("Lokesh")
                .setAddress(address)
                .putMarks("C1", 23)
                .putMarks("C2", 24)
                .setSchool(school)
                .setStatus(Status.INACTIVE)
                .addAllFriends(Arrays.asList(f1, f2))
                .setPerson(PersonOuterClass.Person.newBuilder().setName("John").build())
                .setEmail(Email.newBuilder().setId("abc@gmail.com").build())
                .setPhone(Phone.newBuilder().setNumber("123-456-7890").build())
                .setFloatVal(FloatValue.newBuilder().setValue(23.4f).build())
                .build();

        System.out.println(lokesh);
        System.out.println(school);
    }
}
