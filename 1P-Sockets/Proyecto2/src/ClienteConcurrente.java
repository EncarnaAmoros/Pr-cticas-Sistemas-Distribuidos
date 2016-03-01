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
public class ClienteConcurrente {
    
    static final int PUERTO=1234;
    static final String IP="127.0.0.1";
    
    public static void main(String[] args) {
    
        try {

            Socket cliente = new Socket(IP , PUERTO);

            OutputStream aux = cliente.getOutputStream();
            DataOutputStream flujo= new DataOutputStream( aux );
            flujo.writeUTF( "Hola servidor: "+IP+", soy el cliente 1 y te hablo desde el puerto: "+PUERTO );
            
            InputStream aux2 = cliente.getInputStream();
            DataInputStream flujo2 = new DataInputStream( aux2 );
            System.out.println( "El servidor me ha escrito: "+flujo2.readUTF() );
            
            /*Socket cliente2 = new Socket(IP , PUERTO);

            OutputStream aux3 = cliente2.getOutputStream();
            DataOutputStream flujo3= new DataOutputStream( aux3 );
            flujo.writeUTF( "Hola servidor: "+IP+", soy el cliente 2 y te hablo desde el puerto: "+PUERTO );
            
            InputStream aux4 = cliente2.getInputStream();
            DataInputStream flujo4 = new DataInputStream( aux4 );
            System.out.println( "El servidor me ha escrito: "+flujo4.readUTF() );*/
            
            cliente.close();
            //cliente2.close();
        }
        catch(Exception e)
        {
            System.out.println("Error en cliente: " + e.toString());
        }
    
    }
}