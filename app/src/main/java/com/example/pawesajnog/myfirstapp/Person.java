package com.example.pawesajnog.myfirstapp;

import java.io.Serializable;

/**
 * Created by Paweł Sajnóg .
 */

public class Person implements Serializable {
    private String name;
    private String password;

    public Person() {
    }

    public Person(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return name + "\n" + password;
    }
}
