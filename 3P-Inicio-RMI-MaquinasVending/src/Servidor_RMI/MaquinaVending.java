import java.io.Serializable;
import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class MaquinaVending extends UnicastRemoteObject implements InterfazMaquinaVending, Serializable
{
	private int id;
	private int monedero;
	private ArrayList<Integer> stock = new ArrayList<Integer>();
	private int temperatura;
	private int estado;
	private String fecha;
	private String nombre;
	private static int numeroMaquinas=0;
    private ArrayList<String> alertas = new ArrayList<String>();
    private Integer atascada;

	//Generar datos aleatorios
	public MaquinaVending(String nombre) throws RemoteException
	{	
		this.nombre=nombre;
		monedero=(int)(Math.random()*(800-1+1)+1);//800 1-800; (HASTA-DESDE+1) + DESDE

        for(int i=0;i<4;i++)
		    stock.add((int)(Math.random()*(20-10+1)+10));

		temperatura=(int)(Math.random()*(10-1+1)+1);
		estado=0;
		numeroMaquinas++;
		id=numeroMaquinas;
        atascada=0;

        Calendar c1 = GregorianCalendar.getInstance();
        fecha=c1.getTime().toLocaleString().replace("GMT","");

        for(int i=0;i<3;i++)
            alertas.add("");
	}
	
	public int getMonedero() { return monedero;	}
	public int getStock()
    {
        int suma=0;
        for(int i=0;i<stock.size();i++)
            suma+=stock.get(i);
        return suma;
    }
    public ArrayList<Integer> getStockCanales()
    {
        return stock;
    }
	public int getTemperatura() { return temperatura;	}
	public int getEstado() { return estado;	}
	public int getId() { return id;	}
	public String getNombre() { return nombre;	}
	public String getFecha() { return fecha;	}
    public Integer getStockAtasco() { return atascada; }

    public String setStockAtasco(Integer valor)
    {
        String mensaje="";
        if(valor==1)
            mensaje="Maquina " + id + " con atasco en stock";

        atascada=valor;
        if(!mensaje.equals(""))
            System.out.println(mensaje);
        return mensaje;
    }

	public String setMonedero(int valor)
    {
        String mensaje="El monedero de la maquina " + id + " ha cambiado a " + valor;
        monedero=valor;
        System.out.println(mensaje);
        return mensaje;
    }

	public String setStock(int pos, int valor)
    {
        if(valor<0)
        {
            stock.set(pos, valor);
            return setStockAtasco(1);
        }
        String mensaje="Canal " + (pos+1) + " de maquina " + id + " ha cambiado a " + valor;
        stock.set(pos, valor);
        System.out.println(mensaje);
        return mensaje;
    }

    /**
     * Es el refrigerador
     * @param valor a cambiar
     * @return mensaje de lo que se ha cambiado
     */
	public String setTemperatura(int valor)
    {
        String mensaje="La temperatura de la maquina " + id + " ha cambiado a " + valor ;
        temperatura=valor;
        System.out.println(mensaje);
        return mensaje;
    }

	public String setEstado(int valor)
    {
        String mensaje="";
        estado=valor;
        if(!mensaje.equals(""))
            System.out.println(mensaje);
        return mensaje;
    }

    public void setNombre(String valor) { nombre=valor; }

    public void setFecha(String valor) { fecha=valor;	}
	
	public String display()
	{
        if(alertas.get(0).equals("") && alertas.get(1).equals("") && alertas.get(2).equals(""))
            return "";
        else
            return alertas.get(0) + " " + alertas.get(1) + " " + alertas.get(2);
	}
	
	//Si ok es falso fuera de servicio
	public void alimentacion(boolean ok)
	{
		if(ok==true && estado==-1)
			estado=0;
		else if(ok==false && estado==0)
            estado=-1;
	}

	public void controladorServicio()
	{
        /**
         * Si se da una de estas situaciones maquina fuera de servicio
         */
		if(getStock()<10 || atascada==1 || !(monedero!=-1 && monedero<=800) || temperatura>10)
        {
            alimentacion(false);
        }
        /**
         * Si no se da ninguna, esta en servicio de nuevo
         */
        else if(estado==-1)
            alimentacion(true);

        /**
         * Segun la situacion se enviara una alerta determinada o se activara el refrigerador
         */
        if(getStock()<10)
            alertas.set(0,"Maquina sin stock");
        else
            alertas.set(0,"");
        if(atascada==1)
            alertas.set(1,"Maquina atascada");
        else
            alertas.set(1,"");
        if(!(monedero!=-1 && monedero<=800))
            alertas.set(2,"Maquina sin cambio");
        else
            alertas.set(2,"");

        /**
         * Comprobamos si algún canal está atascado
         */
        boolean atasco=false;
        for(int k=0;k<stock.size();k++)
        {
            if (stock.get(k) == -1) {
               atasco = true;
            }
        }
        if(atasco==true)
        {
            atascada=1; estado=-1;
        }
        else
        {
            atascada=0; estado=0;
        }

        if(temperatura>10)
            setEstado(-1);
	}
}
