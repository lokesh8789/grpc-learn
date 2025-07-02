package com.lokesh.sec02;

import com.lokesh.models.sec02.Person;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProtoDemo {
    private static final Path path = Path.of("person.out");
    public static void main(String[] args) throws IOException {
        Person person = create();
        Person person1 = create();
        System.out.println(person);
        System.out.println(person.equals(person1));
        System.out.println(person == person1);

        Person person2 = Person.newBuilder()
                .setIsActive(false)
                .build();
        System.out.println(person2.toByteArray().length);
        serialize(person);
        Person deserialize = deserialize(person);
        System.out.println(deserialize);
    }

    private static Person create() {
        return Person.newBuilder()
                .setAge(15)
                .setName("Lokesh")
                .setData(876575L)
                .setIsActive(true)
                .setSalary(45326.55)
                .setNeg(-23)
                .build();
    }

    private static void serialize(Person person) throws IOException {
        try (var stream = Files.newOutputStream(path)) {
            person.writeTo(stream);
        }
    }

    private static Person deserialize(Person person) throws IOException {
        try (var stream = Files.newInputStream(path)) {
            return Person.parseFrom(stream);
        }
    }
}
