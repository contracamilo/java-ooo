package kelea.play.User;

import kelea.play.content.Content;

import java.time.LocalDateTime;

public class User {
    private String name;
    private String email;
    private LocalDateTime registerDate;



    public User(String name) {
        this.name = name;
    }

    public void watch(Content content) {
        System.out.println(name + " is watching " + content.title());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(LocalDateTime registerDate) {
        this.registerDate = registerDate;
    }
}
