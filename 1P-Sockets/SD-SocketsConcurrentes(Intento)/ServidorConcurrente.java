/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.net.*;


/**
 *
 * @author Encarna
 */
public class ServidorConcurrente {
    
    static final int PUERTO=1234;
    
    public static void main(String[] args)
    {
        
        try {
            
            ServerSocket servidor = new ServerSocket(PUERTO);
		    System.out.println("Escucho el puerto " + PUERTO);
            
            for(;;)
            {
                Socket cliente = servidor.accept();
                
                 //Creamos hilos para manejar cada petición del cliente
                System.out.println("Sirviendo cliente...");
                
                Thread t=new HiloServidor();
                t.start();
            }
        
        }
        catch(Exception e)
        {
            System.out.println("Error en servidor: " + e.toString());
        }
        
    }
    
}
