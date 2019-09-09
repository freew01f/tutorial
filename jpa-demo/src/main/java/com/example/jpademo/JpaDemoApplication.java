package com.example.jpademo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class JpaDemoApplication implements CommandLineRunner {

    @Autowired
    StudentRepository studentRepository;
    @Autowired
    SchoolRepository schoolRepository;
    @Autowired
    DetailRepository detailRepository;
    @Autowired
    SubjectRepository subjectRepository;

    public static void main(String[] args) {
        SpringApplication.run(JpaDemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        Student s1 = new Student();
        s1.setAge(37);
        s1.setName("freewolf");

        Detail detail = new Detail();
        detail.setHeight(6);
        detail.setWeight(100);
        s1.setDetail(detail);

        Student s2 = new Student();
        s2.setAge(17);
        s2.setName("hello");

        Student s3 = new Student();
        s3.setAge(27);
        s3.setName("world");

        Subject sub1 = new Subject();
        sub1.setName("Math");

        Subject sub2 = new Subject();
        sub2.setName("Test");

        School school = new School();
        school.setName("Beijing");

        Set<Student>  students= new HashSet<>();
        students.add(s1);
        students.add(s2);
        students.add(s3);

        school.setStudents(students);
        schoolRepository.saveAndFlush(school);

    }
}

interface StudentRepository extends JpaRepository<Student, Long> {}
interface SchoolRepository extends JpaRepository<School, Long> {}
interface SubjectRepository extends JpaRepository<Subject, Long> {}
interface DetailRepository extends JpaRepository<Detail, Long> {}

@Entity
@Getter
@Setter
@ToString
class Student{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private Integer age;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="school_id")
    private School school;

    @ManyToMany
    @JoinTable(name = "student_subject",joinColumns = @JoinColumn(name = "subject_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id"))
    private Set<Subject> subjects;

    @OneToOne
    @JoinColumn(name="detail_id")
    private Detail detail;
}

@Entity
@Getter
@Setter
@ToString
class Subject{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;

    @ManyToMany
    @JoinTable(name = "student_subject",joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id"))
    private Set<Student> students;

}

@Entity
@Getter
@Setter
@ToString
class School{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;

    @OneToMany(fetch=FetchType.EAGER, cascade = CascadeType.ALL, mappedBy="school")
    private Set<Student> students;
}

@Entity
@Getter
@Setter
@ToString
class Detail{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Integer height;
    private Integer weight;

    @OneToOne
    @JoinColumn(name="student_id")
    private Student student;

}

@RestController
class StudentController{
    @Autowired
    private SchoolRepository schoolRepository;

    @GetMapping("/school/{id}")
    public School school(@PathVariable Long id){
        return this.schoolRepository.getOne(id);
    }

    @GetMapping("/hello")
    public String hello(){
        return "Hello World";
    }
}