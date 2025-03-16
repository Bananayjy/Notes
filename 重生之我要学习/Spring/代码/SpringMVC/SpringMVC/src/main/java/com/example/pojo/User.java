package com.example.pojo;

import java.util.Arrays;
import java.util.Date;

/**
 * @author banana
 * @create 2023-09-17 20:11
 */
public class User {

    private String username;
    private Integer age;
    private String[] hobbies;
    private Date birthday;
    private Address address;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String[] getHobbies() {
        return hobbies;
    }

    public void setHobbies(String[] hobbies) {
        this.hobbies = hobbies;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", age=" + age +
                ", hobbies=" + Arrays.toString(hobbies) +
                ", birthday=" + birthday +
                ", address=" + address +
                '}';
    }
}
