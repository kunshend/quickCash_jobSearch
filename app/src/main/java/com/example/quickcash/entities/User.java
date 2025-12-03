package com.example.quickcash.entities;

public class User {

   private String username;
    private String email;
    private String role;
    private String firstName;
    private String lastName;

    //Default constructor is required by firebase
    public User(){}
    public User(String username, String email, String role, String firstName, String lastName){
        this.username = username;
        this.email = email;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFirstName(){return this.firstName;}
    public void setFirstName(String firstName){this.firstName = firstName;}

    public String getLastName(){return this.lastName;}
    public void setLastName(String lastName){this.lastName = lastName;}

}
