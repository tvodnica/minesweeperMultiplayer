/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatContract.service;

import chatContract.data.ChatMessage;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 *
 * @author Tomo
 */
public interface MessengerService extends Remote {
    void sendMessage(ChatMessage message) throws RemoteException;
    
    List<ChatMessage> getChatHistory() throws RemoteException;
    
    ChatMessage getLastMessage() throws RemoteException;
}
