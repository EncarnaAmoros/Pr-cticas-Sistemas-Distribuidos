import java.rmi.*;

public class Registro
{         
    public static void main (String args[])     
    {
    	if(args.length>=2)
    	{
	        String URLRegistro;
            String IP_RMI=args[0];
            String PUERTO_RMI=args[1];

	        try           
	        {
                System.out.println("Registrando maquinas en el registro de IP:"+IP_RMI+" y puerto:"+PUERTO_RMI);
                /**
                 * Hacer for que vaya registrando maquinas dichas en la dirección rmi
                 */
                for(int i=2;i<args.length;i++)
	        	{
                    System.setSecurityManager(new RMISecurityManager());
	            	MaquinaVending objetoRemoto = new MaquinaVending(args[i]);                  
	            	URLRegistro = "rmi://"+IP_RMI+":"+PUERTO_RMI+"/"+args[i];
	                Naming.rebind (URLRegistro, objetoRemoto);            
	                System.out.println("Maquina de nombre: " + args[i] + " preparada");
	        	}
	        }            
	        catch (Exception ex)            
	        {                  
	            System.out.println(ex);
	        } 
    	}
    	else
    	{
    		System.err.println("Debes indicar el nombre de las maquinas a registrar Registro <Maquina1> <Maquina2> ...");
    	}
    }
}
