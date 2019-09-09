package com.example.jpajsondemo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.persistence.Converter;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class JpaJsonDemoApplication implements CommandLineRunner {
    @Autowired
    UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(JpaJsonDemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        User u = new User();
        u.setId(1L);
        u.setUsername("freewolf");
        List<Nation> nations = new ArrayList<>();
        nations.add(new Nation("USA"));
        nations.add(new Nation("china"));
        u.setNations(nations);
        this.userRepository.save(u);

        User user = this.userRepository.findById(1L).orElse(null);
        System.out.println(user);
    }
}