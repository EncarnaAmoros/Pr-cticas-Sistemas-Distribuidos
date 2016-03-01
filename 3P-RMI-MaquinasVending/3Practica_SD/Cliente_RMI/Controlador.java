import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Controlador: Devuelve peticiones http de páginas html dinámicas
 * Se comunica vía socket con el servidor http y vía RMI con las máquinas remotas
 */
public class Controlador 
{
    /**
     * Para comunicarnos con las maquinas remotas
     */
    private static String PUERTO_RMI;
    private static String IP_RMI;
    /**
     * Cantidad maquinas, nombres, sus objetos y paginas html
     */
    private int NUMERO_MAQUINAS;
    private ArrayList<InterfazMaquinaVending> maquinas = new ArrayList<InterfazMaquinaVending>();
    private ArrayList<String> paginas_maquinas = new ArrayList<String>();
    private ArrayList<String> nombres_maquinas=new ArrayList<String>();
    /**
     * Puerto con el que comunicarse con HTTP y pagina principal index
     */
	private static int PUERTO_HTTP;
    /**
     * Pagina principal index
     */
	private String pagina_index;

    /**
     * Contructor: obtenemos un controlador indicandole el numero de maquinas a buscar en el registro
     * y sus respectivos nombres
     * @param num_maquinas cantidad de maquinas
     * @param nombres_maquina nombres de las maquinas
     */
	public Controlador(int num_maquinas, ArrayList<String> nombres_maquina)
	{
		NUMERO_MAQUINAS=num_maquinas; 
		nombres_maquinas=nombres_maquina;
	}

    /**
     * Devuelve la pagina principal Index
     * @return la pagina html principal
     */
	public String getPagina_Index() { return pagina_index; }

    /**
     * Lee en el registro tantas veces como máquinas registradas sabemos que tenemos
     * y guarda todas esas máquinas en el array de máquinas vending
     * @return
     */
	public void buscarMaquinasRegistro()
	{
        System.out.println("Buscando maquinas en el servidor de nombres localizado con IP="+IP_RMI+" por puerto:"+PUERTO_RMI);

        for(int i=0;i<NUMERO_MAQUINAS;i++)
		{
			InterfazMaquinaVending maquinaRemota = null;
	        String servidor = "rmi://"+IP_RMI+":"+PUERTO_RMI+"/"+nombres_maquinas.get(i);
            try
	        {
                System.setSecurityManager(new RMISecurityManager());
	        	maquinaRemota = (InterfazMaquinaVending) Naming.lookup(servidor);
	        	maquinaRemota.setNombre(nombres_maquinas.get(i));
	        	maquinas.add(maquinaRemota);
	        }
	        catch(Exception ex)
	        {
	            System.out.println("Error al instanciar la maquina remota "+ex);
	            System.exit(0);
	        }
		}
	}

    /**
     * A partir de la informacion de cuantas maquinas hay,
     * sus nombres y sensores genera la pagina principal html
     */
    public void generarIndex() throws RemoteException
	{
        Calendar c1 = GregorianCalendar.getInstance();
        String fecha_index=c1.getTime().toLocaleString().replace("GMT","");

        String pagina="<html>";

        pagina+="<head>";
        pagina+="<title> Index </title>";
        pagina+="<meta charset='UTF-8'> <link href='index.css' rel='stylesheet' type='text/css'/>";
        pagina+="</head>";

		pagina+="<body>";

		pagina+="<div class='div_cabecera'>";
        pagina+="<h1> Sistemas distribuidos </br>";
        pagina+="<h7> " + fecha_index + " </h7>";
        pagina+="</h1>";
        pagina+="</div>";

		pagina+="<div class='div_maquinas'>";

		for(int i=0;i<maquinas.size();i++)
		{
			try
			{
				pagina+="<div class='div_maquina'>";

				pagina+="<h2>";
                pagina+="<a href='"+maquinas.get(i).getNombre()+maquinas.get(i).getId()+".html'>";
                pagina+=""+maquinas.get(i).getNombre()+"";
                pagina+="</a>";
                pagina+="</h2>";

                pagina+="<h5> Monedero: " + maquinas.get(i).getMonedero()  +        "</h5>";

                ArrayList<Integer> canales = new ArrayList<Integer> ();
                canales=maquinas.get(i).getStockCanales();

                pagina+="<h5> Stock: " + maquinas.get(i).getStock() + " ( ";
                for(int j=0;j<canales.size();j++)
                    pagina+=canales.get(j)+" ";
                pagina+=" ) </h5>";

                pagina+="<h5> Temperatura: " + maquinas.get(i).getTemperatura() + "º</h5>";

                if(maquinas.get(i).getEstado()==0)
                    pagina+="<h5> Estado: En servicio</h5>";
                else
                    pagina+="<h5 style='color:red'> Estado: Fuera de servicio</h5>";

                pagina+="<h5> Ultima modificación: " + maquinas.get(i).getFecha()  +"</h5>";

                if(!maquinas.get(i).display().equals(""))
                    pagina+="<h5 style='color:red'> Alertas: " + maquinas.get(i).display()  +"</h5>";
                else
                    pagina+="<h5> Funciona correctamente </h5>";

                pagina+="</div>";

				paginas_maquinas.add(generarPaginas_Maquinas(maquinas.get(i)));
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
			}
		}

		pagina+="</div>";
        pagina+="</body>";
        pagina+="</html>";

        /**
         * Guarda el html de la página principal en la variable y lo escribe
         * en un fichero para que el servidorHttp lo pueda leer index.html
         */
        pagina_index=pagina;
        escribeHtml(pagina, "index.html");

        /**
         * Obtiene cada página de las máquinas y escribe sus ficheros .html
         */
        for(int i=0;i<maquinas.size();i++)
        {
            String pag_maquina=generarPaginas_Maquinas(maquinas.get(i));
            String nombre_pag= maquinas.get(i).getNombre() + maquinas.get(i).getId() + ".html";
            escribeHtml(pag_maquina, nombre_pag);
        }
	}

    /**
     * Genera un html de una maquina con sus sensores (posibilidad de cambiar valores)
     * @param maquina Maquina de la que escribir el html
     * @return Devuelve la pagina html
     */
	public String generarPaginas_Maquinas(InterfazMaquinaVending maquina)
	{
		String pagina="";
		try 
		{
			pagina+="<html>";

            pagina+="<head>";
            pagina+="<title> " + maquina.getNombre() + "</title>";
            pagina+="<meta charset='UTF-8'>";
            pagina+="<link href='maquina.css' rel='stylesheet' type='text/css' />";
            pagina+="</head>";

			pagina+="<body>";

			pagina+="<div class='div_cabecera'>";
            pagina+="<h1> Sistemas distribuidos </h1> </br>";
            pagina+="</div>";

			pagina+="<div class='div_maquinas'>";

            pagina+="<div class='div_maquina'>";

            pagina+="<form method='get'>";
            pagina+="<h2> Maquina1 </h2>";
            pagina+="<h5> Cambiar monedero";
            pagina+="<input class='sensor' type=text NAME=setMonedero_"+maquina.getId()+">";
            pagina+="</h5>";
            pagina+="<div class='sensoresStock'>";
            pagina+="<h5> Cambiar stock";
            pagina+="<input class='sensor2' type=text NAME=setStock4_"+maquina.getId()+">";
            pagina+="<input class='sensor2' type=text NAME=setStock3_"+maquina.getId()+">";
            pagina+="<input class='sensor2' type=text NAME=setStock2_"+maquina.getId()+">";
            pagina+="<input class='sensor2' type=text NAME=setStock1_"+maquina.getId()+">";
            pagina+="</h5>";
            pagina+="</div>";
            pagina+="<h5> Cambiar temperatura";
            pagina+="<input class='sensor' type=text NAME=setTemperatura_"+maquina.getId()+">";
            pagina+="</h5>";
            pagina+="<h5> Maquina atascada (1)";
            pagina+="<input class='sensor' type=text NAME=setStockAtasco_"+maquina.getId()+">";
            pagina+="</h5>";

            if(!maquina.display().equals(""))
                pagina+="<h5 style='color:red'> Alertas: " + maquina.display()  +"</h5>";
            else
                pagina+="<h5> Funciona correctamente </h5>";

            pagina+="<INPUT ID='boton_enviar' type='submit' value='Enviar'>";
            pagina+="</form>";

            pagina+="</div>";

            pagina+="<div class='volver'>";
            pagina+="<h2> <a href='index.html'> Volver </a> </h2>";
            pagina+="</div>";

            pagina+="</div>";
		} 
		catch (RemoteException e)
		{
			e.printStackTrace();
		}

		pagina+="</body>";
        pagina+="</html>";
		
		return pagina;
	}

    /**
     * Escribe el contenido texto pasado por parámetro en un fichero con el nombre_archivo indicado
     * @param texto texto que contendrá el contenido html
     * @param nombre_archivo nombre con el que se guardara el contenido
     */
    public void escribeHtml(String texto, String nombre_archivo)
    {
        FileWriter fichero = null;
        PrintWriter pw = null;

        try
        {
            /**
             * Creamos el archivo, un FileWriter con el nombre del fichero y escribimos en el
             */
            fichero = new FileWriter(nombre_archivo);
            pw = new PrintWriter(fichero);
            pw.println(texto);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
             try
             {
                 /**
                  * En finally cerramos el fichero para asegurarnos que se cierra
                  */
                 if (null != fichero)
                    fichero.close();
             }
             catch (Exception e2)
             {
                    e2.printStackTrace();
             }
        }
    }

    /**
     * Dado un id de una maquina busca su nombre
     * @param id Id de la maquina
     * @return Nombre de la maquina
     */
    public String NombreMaquina(int id)
    {
        String nombre="";
        try
        {
            for(int i=0;i<nombres_maquinas.size();i++)
            {
                if(maquinas.get(i).getId()==id)
                    nombre=maquinas.get(i).getNombre();
            }
        }
        catch (RemoteException e)
        {
            e.toString();
        }
        return nombre;
    }

    /**
     * Se encarga de llamar a los actuadores de la máquina indicada devolviendo un html
     * especificando qué valores se han cambiado
     * @param url La url con los parametros de lo que se quiere cambiar
     * @return Devuelve la pagina html
     */
    public String cambiaValoresMaquina(String url)
    {
        ArrayList<String> metodos = new ArrayList<String>();
        ArrayList<Integer> ids = new ArrayList<Integer>();
        ArrayList<Integer> valores = new ArrayList<Integer>();
        boolean cambiado=false;

        /**
         * Se actualiza con los actuadores que hay que llamar
         */
        separaParametros(url, metodos, ids, valores);

        String pagina_html="";

        pagina_html+="<html>";
        pagina_html+="<head>";
        pagina_html+="<title> Success </title>";
        pagina_html+="<meta charset='UTF-8'> <link href='maquina.css' rel='stylesheet' type='text/css'/>";
        pagina_html+="</head>";

        pagina_html+="<body>";

        pagina_html+="<div class='div_cabecera'>";
        pagina_html+="<h1> Sistemas distribuidos </h1> </br>";
        pagina_html+="</div>";

        pagina_html+="<div class='div_maquinas'>";
        pagina_html+="<div class='div_maquina' style='height: 463px;'>";

        pagina_html+="<h2> " + NombreMaquina(ids.get(0)) + " </h2>";

        for(int i=0;i<metodos.size();i++)
        {
            cambiado=true;
            /**
             * Obtenemos la máquina de la que cambiaremos su valor con el id
             */
            try
            {
                cambiado=true;
                Calendar c1 = GregorianCalendar.getInstance();
                if (metodos.get(i).equals("setMonedero"))
                {
                    String mensaje=maquinas.get(ids.get(i)-1).setMonedero(valores.get(i));
                    pagina_html+="<h5>  " + mensaje + "      </h5>";
                }
                else if (metodos.get(i).equals("setStock1"))
                {
                    String mensaje=maquinas.get(ids.get(i)-1).setStock(0, valores.get(i));
                    pagina_html+="<h5>  " + mensaje + "      </h5>";
                }
                else if (metodos.get(i).equals("setStock2"))
                {
                    String mensaje=maquinas.get(ids.get(i)-1).setStock(1, valores.get(i));
                    pagina_html+="<h5>  " + mensaje + "      </h5>";
                }
                else if (metodos.get(i).equals("setStock3"))
                {
                    String mensaje=maquinas.get(ids.get(i)-1).setStock(2, valores.get(i));
                    pagina_html+="<h5>  " + mensaje + "      </h5>";
                }
                else if (metodos.get(i).equals("setStock4"))
                {
                    String mensaje=maquinas.get(ids.get(i)-1).setStock(3, valores.get(i));
                    pagina_html+="<h5>  " + mensaje + "      </h5>";
                }
                else if (metodos.get(i).equals("setTemperatura"))
                {
                    String mensaje=maquinas.get(ids.get(i)-1).setTemperatura(valores.get(i));
                    pagina_html+="<h5>  " + mensaje + "      </h5>";
                }
                else if (metodos.get(i).equals("setStockAtasco"))
                {
                    String mensaje=maquinas.get(ids.get(i)-1).setStockAtasco(valores.get(i));
                    pagina_html+="<h5>  " + mensaje + "      </h5>";
                }
                else
                    cambiado=false;

                if(cambiado==true)
                    maquinas.get(ids.get(i)-1).setFecha(c1.getTime().toLocaleString().replace("GMT",""));
            }
            catch (RemoteException e)
            {
                e.toString();
            }

        }

        pagina_html+="</div>";

        pagina_html+="<div class='volver'>";
        pagina_html+="<h2> <a href='index.html'> Volver </a> </h2>";
        pagina_html+="</div>";

        pagina_html+="</div>";
        pagina_html+="</body>";
        pagina_html+="</html>";

        /**
         * Si hay datos en el array de metodos es porque se han cambiado valores y actualizamos Index
         */
        try
        {
            if (cambiado == true)
            {
                generarIndex();
            }
        }
        catch(RemoteException e)
        {
            e.toString();
        }

        return pagina_html;
    }

    /**
     * Recibe una url que contiene los parametros de la peticion del navegador donde se indica
     * que actuador debe llamar, de cual maquina y con qué valor
     * @param p_Cadena Peticion http con parametros
     * @param http clase http
     */
    public void separaParametros(String url, ArrayList<String> metodos, ArrayList<Integer> ids, ArrayList<Integer> valores)
    {
        /**
         * Separamos fichero/argumentos Ej:./Maquina11.html?setMonedero-1=1&setStock-1=&setTemperatura-1=3
         */
        String[] fichero_parametros = url.split("\\?");
        String parametros = fichero_parametros[1];

        if(!parametros.equals(""))
        {
            /**
             * Separamos cada uno de los argumentos Ej:setMonedero-1=1 & setStock-1= & setTemperatura-1=3
             * En el for obtendremos cada metodo, id de la máquina y valor a cambiar
             */
            String[] parametro = parametros.split("\\&");

            for(int i=0;i<parametro.length;i++)
            {
                /**
                 * Separamos el método Ej: setMonedero
                 */
                String[] metodo_id_valor = parametro[i].split("_");
                String metodo=metodo_id_valor[0];
                /**
                 * Obtenemos el id y valor Ej: 1 = 2 guardamos el id 1
                 */
                String[] id_valor = metodo_id_valor[1].split("=");
                int id = Integer.parseInt(id_valor[0]);
                /**
                 * Obtenemos el valor al que cambiara Ej: 2
                 * Solo si hay valor a cambiar agregaremos los datos a los arrays para que se llame a ese actuador
                 */
                if(id_valor.length>1)
                {
                    int valor=Integer.parseInt(id_valor[1]);
                    metodos.add(metodo);
                    ids.add(id);
                    valores.add(valor);
                }
            }
        }
    }

    /**
     * Cada 2 segundos envian peticion RMI para actualizar valores de las maquinas remotas
     * @throws RemoteException por si hay errores al acceder al metodo remoto
     */
    public void actualizarPaginas() throws RemoteException
    {
        for(int i=0;i<maquinas.size();i++)
        {
            maquinas.get(i).controladorServicio();
            generarIndex();
        }
    }

    /**
     * Se comunica con el servidorHttp (que aqui es su cliente) enviándole la pagina html que ha generado
     * @param pagina_html Petición http del navegador (ruta, fichero pedido y argumentos)
     * @param cliente Socket para comunicarse con el controlador
     */
    public void escribeSocket(String pagina_html, Socket cliente)
    {
        System.out.println("Hilo servidor mini HTTP leyendo html del controlador por puerto "+PUERTO_HTTP);
        try
        {
            OutputStream aux = cliente.getOutputStream();
            DataOutputStream flujo= new DataOutputStream( aux );
            flujo.writeUTF(pagina_html);
        }
        catch(Exception e)
        {
            System.out.println("Error en cliente: " + e.toString());
            System.out.println("Error en cliente: " + e.getMessage().toString());
        }
    }

    /**
     * Controlador se comunica como servidor con el ServidorHttp y recibe un String que será la url
     * con los parametros de la peticion http del Navegador
     * @return Url que devuelve el servidorHttp
     */
    public String leeSocket(Socket cliente)
    {
        System.out.println("Hilo servidor mini HTTP leyendo html del controlador por puerto "+PUERTO_HTTP);
        String url="";
        try
        {
            InputStream aux2 = cliente.getInputStream();
            DataInputStream flujo2 = new DataInputStream( aux2 );
            url=flujo2.readUTF().toString();
        }
        catch(Exception e)
        {
            System.out.println("Error en cliente: " + e.getMessage().toString());
        }
        return url;
    }

	/**
     * Se abrira un socket siendo el controlador el servidor escuchando peticiones del cliente ServidorHttp
     * por si desea cambiar valores de las maquinas vending remotamente el controlador se comunicará con sus metodos
     * Cada 2 segundos actualizara la pagina index.html
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
    	int i = 0;
    	ArrayList<String> nombres_maq = new ArrayList<String>();

        if (args.length >= 5)
		{
			try
			{
                /**
                 * Direccion para comunicarse con maquinas remotas y sus nombres para buscarlas en el registro
                 */
                PUERTO_HTTP=Integer.parseInt(args[0]);
                IP_RMI=args[1];
				PUERTO_RMI=args[2];
				for(int j=4;j<args.length;j++)
					nombres_maq.add(args[j]);

                final Controlador cr = new Controlador(Integer.parseInt(args[3]), nombres_maq);
                /**
                 * Buscamos las máquinas en el registro, se guardaran en la variable maquinas y generamos Index
                 */
                cr.buscarMaquinasRegistro();
                cr.generarIndex();

                /**
                 * Clase con el codigo a ejecutar cada 2 segundos
                 */
                TimerTask timerTask = new TimerTask()
                {
                    public void run()
                    {
                        try
                        {
                            cr.actualizarPaginas();
                        }
                        catch (RemoteException e)
                        {
                            e.printStackTrace();
                        }
                    }
                };

                /**
                 * Se pone en marcha el timer cada segundo y a los 0 ms cada 2 s ejecuta el codigo de arriba
                 */
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(timerTask, 0, 2000);

                /**
                 * Socket en el que se escucharán las peticiones que llegaran del cliente que es el servidor http
                 * del navegador.
                 */
                ServerSocket servidor = new ServerSocket(PUERTO_HTTP);
                System.out.println("Controlador escuchando por el puerto "+PUERTO_HTTP+" para servir pagina html...");

                while(i==0)
                {
                    Socket cliente = servidor.accept();
                    /**
                     * Leemos url de la peticion http, llamamos al metodo que cambia los valores segun los parametros,
                     * generamos la pagina html a devolver y la escribimos al socket
                     */
                    String url= cr.leeSocket(cliente);
                    String pagina_html=cr.cambiaValoresMaquina(url);
                    cr.escribeSocket(pagina_html, cliente);
                }
		    }
	        catch(Exception e)
			{
	        	System.out.println("Error: "+e);
			}
		}
		else
		{
			System.out.println("Debe indicar la direccion del servidor (IP y PUERTO), el número de máquinas y sus nombres.");
			System.out.println("Ejemplo: Controlador <127.0.0.1> <1099> <2> <Maquina1> <Maquina2>");
		}
    }
}