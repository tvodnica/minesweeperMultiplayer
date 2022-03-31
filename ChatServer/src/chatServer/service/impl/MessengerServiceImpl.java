/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatServer.service.impl;

import chatContract.data.ChatMessage;
import chatContract.service.MessengerService;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tomo
 */
public class MessengerServiceImpl implements MessengerService {

    List<ChatMessage> chatHistory = new ArrayList<>();
    
    @Override
    public void sendMessage(ChatMessage chatMessage) throws RemoteException {
        chatHistory.add(chatMessage);
    }

    @Override
    public List<ChatMessage> getChatHistory() throws RemoteException {
        return chatHistory;
    }

    @Override
    public ChatMessage getLastMessage() throws RemoteException {
        return chatHistory.get(chatHistory.size() - 1);
    }
    
}
