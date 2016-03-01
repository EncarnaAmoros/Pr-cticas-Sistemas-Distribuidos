import java.net.ServerSocket;
import java.net.Socket;

public class ServidorConcurrente 
{

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		/**
		 * Tomamos los valores para el puerto con el cliente http y el puerto y ip del controlador
		 */
        if(args.length>=3) {

            String IP_CONTROLADOR = args[0];
            int PUERTO_CONTROLADOR = Integer.parseInt(args[1]);
            int PUERTO_CLIENTE_HTTP = Integer.parseInt(args[2]);

            try {
                /**
                 * Creamos el socket por el puerto por parametro
                 */
                ServerSocket skServidor = new ServerSocket(PUERTO_CLIENTE_HTTP);
                System.out.println("Mini servidor http escuchando por el puerto " + PUERTO_CLIENTE_HTTP);

                /**
                 * Mantenemos la comunicación con el cliente
                 */
                for (; ; ) {
                    /**
                     * Se espera un cliente que quiera conectarse por puerto 80 (el
                     * navegador)
                     */
                    Socket skCliente = skServidor.accept(); // Crea objeto
                    System.out.println("Servidor HTTP sirviendo al cliente...");

                    /**
                     * Creamos los hilos de ejecucion del servidor para servir a
                     * clientes concurrentemente
                     */
                    Thread t = new HiloServidor(skCliente, IP_CONTROLADOR, PUERTO_CONTROLADOR);
                    t.start();
                }
            } catch (Exception e) {
                System.out.println("Error  : " + e.toString());
            }
        }
        else
            System.out.println("Error, debes indicar IP_CONTROLADOR, PUERTO_CONTROLADOR, PUERTO_CLIENTE_HTTP");
	}
}
