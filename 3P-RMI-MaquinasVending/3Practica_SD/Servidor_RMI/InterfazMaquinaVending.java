import java.rmi.Remote;
import java.util.ArrayList;

public interface InterfazMaquinaVending extends Remote 
{
	
	public int getMonedero()  throws java.rmi.RemoteException;
	public int getStock()  throws java.rmi.RemoteException;
	public int getTemperatura()  throws java.rmi.RemoteException;
	public int getEstado()  throws java.rmi.RemoteException;
	public int getId()  throws java.rmi.RemoteException;
	public String getNombre() throws java.rmi.RemoteException;
	public String getFecha()  throws java.rmi.RemoteException;
    public Integer getStockAtasco()  throws java.rmi.RemoteException;
    public ArrayList<Integer> getStockCanales() throws java.rmi.RemoteException;

    public String setStockAtasco(Integer valor)  throws java.rmi.RemoteException;
	public String setMonedero(int valor)  throws java.rmi.RemoteException;
	public String setStock(int pos, int valor)  throws java.rmi.RemoteException;
	public String setTemperatura(int valor)  throws java.rmi.RemoteException;
	public String setEstado(int valor)  throws java.rmi.RemoteException;
	public void setNombre(String valor) throws java.rmi.RemoteException;
	public void setFecha(String valor)  throws java.rmi.RemoteException;
	
	public String display()  throws java.rmi.RemoteException;
	
	public void alimentacion(boolean ok)  throws java.rmi.RemoteException;

	public void controladorServicio()  throws java.rmi.RemoteException;
}
