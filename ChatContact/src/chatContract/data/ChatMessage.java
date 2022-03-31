/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatContract.data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * @author Tomo
 */
public class ChatMessage implements Serializable {
    
    private String username;
    private String message;
    private LocalDateTime time;

    public ChatMessage(String username, String message, LocalDateTime time) {
        this.username = username;
        this.message = message;
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "ChatMessage{" + "username=" + username + ", message=" + message + ", time=" + time + '}';
    }
}
