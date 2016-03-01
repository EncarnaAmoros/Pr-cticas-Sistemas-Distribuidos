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
public class HiloServidor extends Thread {
    
    static final int PUERTO=1234;
    
    private Socket cliente;
    
    public HiloServidor(Socket p_cliente)
	{
		this.cliente = p_cliente;
	}
    
    public static void main(String[] args)
    {
    	
    	System.out.println("Nuevo hilo servidor");
        
        try {
            
            ServerSocket servidor = new ServerSocket(PUERTO);
            
            //for(;;)
            //{
                Socket cliente = servidor.accept();
                
                InputStream aux = cliente.getInputStream();
                DataInputStream flujo = new DataInputStream( aux );
                System.out.println( "El cliente me ha escrito: "+flujo.readUTF() );
            
                OutputStream aux2 = cliente.getOutputStream();
                DataOutputStream flujo2= new DataOutputStream( aux2 );
                flujo2.writeUTF( "Hola cliente, soy el servidor" );
           // }
        
        }
        catch(Exception e)
        {
            System.out.println("Error en servidor: " + e.toString());
        }
        
    }
    
}
