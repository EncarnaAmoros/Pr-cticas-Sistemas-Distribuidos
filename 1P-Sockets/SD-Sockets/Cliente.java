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
public class Cliente {
    
    static final int PUERTO=1234;
    static final String IP="127.0.0.1";
    
    public static void main(String[] args) {
    
        try {

            Socket cliente = new Socket(IP , PUERTO);

            OutputStream aux = cliente.getOutputStream();
            DataOutputStream flujo= new DataOutputStream( aux );
            flujo.writeUTF( "Hola servidor: "+IP+", soy el cliente te hablo desde el puerto: "+PUERTO );
            
            InputStream aux2 = cliente.getInputStream();
            DataInputStream flujo2 = new DataInputStream( aux2 );
            System.out.println( "El servidor me ha escrito: "+flujo2.readUTF() );
            
            OutputStream aux = cliente.getOutputStream();
            DataOutputStream flujo= new DataOutputStream( aux );
            flujo.writeUTF( "Fin" );
            
            cliente.close();
        }
        catch(Exception e)
        {
            System.out.println("Error en cliente: " + e.toString());
        }
    
    }
}