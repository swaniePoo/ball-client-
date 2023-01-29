package me.dinozoid.strife.websocket.user;

public class User {

    private String clientUsername, accountUsername, uid, rank;

    public User(final String clientUsername, final String uid, final String rank) {
        this.clientUsername = clientUsername;
        this.accountUsername = clientUsername;
        this.uid = uid;
        this.rank = rank;
    }

    public String accountUsername() {
        return accountUsername;
    }

    public void accountUsername(final String username) {
        this.accountUsername = username;
    }

    public String uid() {
        return uid;
    }

    public void uid(final String uid) {
        this.uid = uid;
    }

    public String rank() {
        return rank;
    }

    public void rank(final String rank) {
        this.rank = rank;
    }

    public String clientUsername() {
        return clientUsername;
    }

    public void clientUsername(String clientUsername) {
        this.clientUsername = clientUsername;
    }
}
