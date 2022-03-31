/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatServer;

import chatContract.service.MessengerService;
import chatServer.service.impl.MessengerServiceImpl;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tomo
 */
public class Server {

   public static void main(String[] args) {
        
        System.out.println("Server pokrenut!");
        
        MessengerService server = new MessengerServiceImpl();
        try {
            MessengerService stub = (MessengerService) UnicastRemoteObject
                    .exportObject((MessengerService) server, 0);
            
            System.out.println("Servis kreiran!");
            
            Registry registry = LocateRegistry.createRegistry(1099);
            
            registry.rebind("MessengerService", stub);
            
            System.out.println("Servis registriran!");
        } catch (RemoteException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("Čekam na poruke od klijenta...");
    }
    

}
