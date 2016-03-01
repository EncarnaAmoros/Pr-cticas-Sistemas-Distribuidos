import java.util.ArrayList;

public class Http {
	
	private String metodo;
	private String fichero;
	private String version;

	public Http(String a, String b, String c)
	{
		metodo=a;
		fichero=b;
		version=c;		
	}
	
	public String getMetodo() { return metodo; }
	public String getFichero() { return fichero; }
	public String getVersion() { return version; }

    public void setMetodo(String valor) { metodo=valor; }
    public void setFichero(String valor) { fichero=valor; }
    public void setVersion(String valor) { version=valor; }

}
