import java.lang.Exception;
import java.util.ArrayList;
import java.net.Socket;
import java.util.Scanner;
import java.io.*;

public class HiloServidor extends Thread 
{
	private Socket skCliente;
	private int PUERTO_CONTROLADOR;
    private String IP_CONTROLADOR;
    private String NOMBRE_PAGINA="./index.html";
	
	public HiloServidor(Socket p_cliente, String ip_c, int p_c)
	{
		this.skCliente = p_cliente;
        IP_CONTROLADOR=ip_c;
        PUERTO_CONTROLADOR=p_c;
	}

    /**
     * Leemos peticion http del navegador  y enviamos peticion a escribir socket al navegador
     * @param p_sk socket para la comunicación con el navegador
     */
	public void leeSocketHttp (Socket p_sk)
	{
		try
		{
			System.out.println("Hilo mini servidor HTTP escuchando...");
			Scanner lee=new Scanner (p_sk.getInputStream());
			boolean ok=true;
			
			if(lee.hasNext())
			{
                /**
                 * Leemos el método que debe ser "GET", la ruta y el fichero y la versión http
                 * Si la peticion http no es GET no aceptaremos la petición enviando error
                 */
				String metodo=lee.next(); 
				if(!metodo.equals("GET"))
					ok=false;
				
				String fichero = "." + lee.next();
                String http_version = "." + lee.next();

                /**
                 * Creamos http con datos leidos enviandolo a escribeSocketHttp para enviar respuesta al navegador
                 */
				Http http = new Http(metodo, fichero, http_version);	
				escribeSocketHttp(p_sk, http, ok);
			}
		}
		catch (Exception e)
		{
			System.out.println("Error: " + e.toString());
		}
	}

    /**
     * Si la petición del navegador no es GET enviamos error
     * Si no, lee fichero html y si lo encuentra se lo envía al navegadpr, si no error 404
     * Si hay argumentos llama al controlador y envía al navegador pagina html dada por el controlador
     * @param p_sk socket para comunicarse con el navegador
     * @param http contenido de la peticion http del navegador
     * @param ok true si la petición es GET si no, false
     */
	public void escribeSocketHttp (Socket p_sk, Http http, boolean ok)
	{
		try
		{
            /**
             * Fichero que se ha leido de peticion http
             */
			String fichero_http=http.getFichero();
			String respuesta="";

            if(ok==false)
			{
                /**
                 * Si no es GET devolvemos 405 "metodo no soportado"
                 */
                String html_error=leeFicheroHtml("error_recurso.html");
				respuesta = "HTTP1.1 405 MétodoNoSoportado\n";
				respuesta+="Connection: close\n";
				respuesta+="Content-Length:"+ Integer.toString(html_error.getBytes().length) +"\n";
				respuesta+="Content-Type: text/html\n";
				respuesta+="Server: diseño de un sistema distribuido de gestión de máquinas de vending\n\n";
                System.out.println("Fichero no encontrado");
                respuesta+= html_error;
			}
			else if(!fichero_http.contains("&"))
			{
                /**
                 * Si han pedido un html sin argumentos obtenemos y leemos el html pedido
                 * de la carpeta donde estamos y enviamos a http
                 */
                String pagina_html=leeFicheroHtml(fichero_http);

                if(!pagina_html.equals(""))
                {
                    /**
                     * Enviamos respuesta con contenido de la pagina si se ha encontrado el html y no está vacío
                     */
                    respuesta = "HTTP1.1 200 OK\n";
                    respuesta += "Connection: close\n";
                    respuesta += "Content-Length:" + Integer.toString(pagina_html.getBytes().length) + "\n";
                    respuesta += "Content-Type: text/html\n";
                    respuesta += "Server: diseñoo de un sistema distribuido de gestión de máquinas de vending\n\n";
                    respuesta += pagina_html;
                }
                else
                {
                    /**
                     * Enviamos respuesta con error (no encuentra el archivo) pues el string a venido vacio
                     */
                    String html_error=leeFicheroHtml("error_recurso.html");
                    respuesta = "HTTP1.1 404 NoEncontrado\n";
                    respuesta+="Connection: close\n";
                    respuesta+="Content-Length:"+Integer.toString(html_error.getBytes().length)+"\n";
                    respuesta+="Content-Type: text/html\n";
                    respuesta+="Server: diseño de un sistema distribuido de gestión de máquinas de vending\n\n";
                    respuesta+=html_error;
                }
			}
            else
            {
                /**
                 * Si pide html con argumentos mandamos parametros al controlador para que cambie los valores
                 * Despues leemos pagina html que nos devuelve el controlador
                 */
                Socket clienteDeControlador = new Socket(IP_CONTROLADOR , PUERTO_CONTROLADOR);
                escribeSocketControlador(fichero_http, clienteDeControlador);
                String html_controlador=leeSocketControlador(clienteDeControlador);

                /**
                 * Enviamos respuesta con contenido de la página que nos ha dado el controlador
                 */
                respuesta = "HTTP1.1 200 OK\n";
                respuesta += "Connection: close\n";
                respuesta += "Content-Length:" + Integer.toString(html_controlador.getBytes().length) + "\n";
                respuesta += "Content-Type: text/html\n";
                respuesta += "Server: diseño de un sistema distribuido de gestión de máquinas de vending\n\n";
                respuesta += html_controlador;
            }

            /**
             * Enviamos respuesta elaborada
             * Creamos OutputStream para crear el PrintWriter que escribirá respuesta html al cliente navegadpr
             */
            OutputStream aux = p_sk.getOutputStream();
            PrintWriter escrito = new PrintWriter(aux);
            escrito.print(respuesta);
			escrito.flush();
		}
		catch (Exception e)
		{
			System.out.println("Error: " + e.toString());
		}
	}

    /**
     * Se encarga de buscar el archivo dado por parámetro y enviarlo en String
     * @param nombre_archivo ruta y nombre del archivo a buscar
     * @return contenido del archivo a buscar
     */
    public String leeFicheroHtml(String nombre_archivo)
    {
        File archivo = null;
        FileReader fr = null;
        BufferedReader br = null;
        String texto="";

        try
        {
            /**
             * Apertura del fichero y creacion de BufferedReader para poder hacer una lectura comoda (metodo readLine())
             */
            archivo = new File (nombre_archivo);
            fr = new FileReader (archivo);
            br = new BufferedReader(fr);

            /**
             * Lectura del fichero
             */
            String linea;
            while((linea=br.readLine())!=null)
                texto+=linea;
        }
        catch(Exception e)
        {
            /**
             * Si hay error dejamos el string vacío indicando que no se ha encontrado el fichero pedido
             */
            texto="";
        }
        finally
        {
            /**
             * En el finally cerramos el fichero para asegurarnos que se cierra siempre
             */
            try
            {
                if( null != fr )
                    fr.close();
            }
            catch (Exception e2)
            {
                texto="";
            }
        }
        return texto;
    }

    /**
     * ServidorHttp se comunica con el controlador enviándole la url de la petición http del navegador
     * @param url petición http del navegador (ruta, fichero pedido y argumentos)
     * @param clienteDeControlador socket para comunicarse con el controlador
     */
    public void escribeSocketControlador(String url, Socket clienteDeControlador)
    {
        System.out.println("Hilo servidor mini HTTP leyendo html del controlador por puerto "+PUERTO_CONTROLADOR);
        try
        {
            OutputStream aux = clienteDeControlador.getOutputStream();
            DataOutputStream flujo= new DataOutputStream( aux );
            flujo.writeUTF(url);
        }
        catch(Exception e)
        {
            System.out.println("Error en cliente: " + e.toString());
            System.out.println("Error en cliente: " + e.getMessage().toString());
        }
    }

    /**
     * ServidorHttp se comunica como cliente con el controlador y recibe un String que será la página html
     * que el ServidorHttp mandará a su cliente el Navegador
     * @param clienteDeControlador Socket para comunicarse con el controlador
     * @return pagina html que devuelve el controlador
     */
    public String leeSocketControlador(Socket clienteDeControlador)
    {
        System.out.println("Hilo servidor mini HTTP leyendo html del controlador por puerto "+PUERTO_CONTROLADOR);
        String pagina_html="";
        try
        {
            InputStream aux2 = clienteDeControlador.getInputStream();
            DataInputStream flujo2 = new DataInputStream( aux2 );
            pagina_html=flujo2.readUTF().toString();
        }
        catch(Exception e)
        {
            System.out.println("Error en cliente: " + e.getMessage().toString());
        }
        return pagina_html;
    }

    /**
     * Hilo servidor creado de un servidor concurrente para aceptar peticiones http concurrentemente
     */
	public void run() 
    {
		try 
		{	
			this.leeSocketHttp (skCliente);
	    	skCliente.close();
			//System.exit(0);				
        }
        catch (Exception e)
        {
          System.out.println("Error: " + e.toString());
        }
      }
}