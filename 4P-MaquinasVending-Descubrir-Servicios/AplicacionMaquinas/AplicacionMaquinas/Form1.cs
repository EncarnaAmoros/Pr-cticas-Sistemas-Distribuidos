using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System;
using System.Collections;

namespace AplicacionMaquinas
{
    public partial class Form1 : Form
    {
        ArrayList nombres = new ArrayList();
        ArrayList url_leidas = new ArrayList();
        String url_maquina_escogida="";

        Seguridad.UDDISecurityService seg = null;
        Buscar.UDDIInquiryService buscar = null;
        Publicar.UDDIPublicationService publicar = null;

        public Form1()
        {
            InitializeComponent();
            seg = new Seguridad.UDDISecurityService();
            buscar = new Buscar.UDDIInquiryService();
            publicar = new Publicar.UDDIPublicationService();
        }

        private void cambiarServidorJuddi()
        {
            //Si se ha especificado un servidor se usara ese
            if (!textBox4.Text.ToString().Equals(""))
            {
                //http://localhost:8080/juddiv3/services/inquiry
                publicar.Url = "http://" + textBox4.Text.ToString() + ":8080/juddiv3/services/publish";
                buscar.Url = "http://" + textBox4.Text.ToString() + ":8080/juddiv3/services/inquiry";
                seg.Url = "http://" + textBox4.Text.ToString() + ":8080/juddiv3/services/security";

            }
        }

        private void label1_Click(object sender, EventArgs e)
        {

        }

        private void comboBox1_SelectedIndexChanged(object sender, EventArgs e)
        {
            int indice_escogido = this.comboBox1.SelectedIndex;
            this.label8.Text = this.nombres[indice_escogido].ToString();

            Maquina.MaquinaVending m = new Maquina.MaquinaVending();
            System.Console.WriteLine("URL: "+m.Url);
            String identificador_maq = this.url_leidas[indice_escogido].ToString();
            identificador_maq=identificador_maq.Remove(identificador_maq.Length - 5);
            m.Url = identificador_maq;
            m.Url += ".MaquinaVendingHttpSoap12Endpoint/";
            url_maquina_escogida = m.Url; 
            
            m.Inicializar();
            int monedero = m.getMonedero().@return;
            int stock = m.getStock().@return;
            int temperatura = m.getTemperatura().@return;
            int estado = m.getEstado().@return;
            String fecha = m.getFecha().@return;
            String nombre = m.getNombre().@return;
            this.label2.Text = monedero.ToString();
            this.label3.Text = stock.ToString();
            this.label4.Text = temperatura.ToString();
            this.label5.Text = estado.ToString();
            this.label7.Text = fecha;
            this.label8.Text = nombre;
            if (estado == 0)
                this.radioButton1.Checked = true;
            else
                this.radioButton2.Checked = true;
        }

        private void recargarDatos()
        {
            Maquina.MaquinaVending m = new Maquina.MaquinaVending();
            m.Url = url_maquina_escogida;
            m.Inicializar();
            int monedero = m.getMonedero().@return;
            int stock = m.getStock().@return;
            int temperatura = m.getTemperatura().@return;
            int estado = m.getEstado().@return;
            String fecha = m.getFecha().@return;
            String nombre = m.getNombre().@return;
            this.label2.Text = monedero.ToString();
            this.label3.Text = stock.ToString();
            this.label4.Text = temperatura.ToString();
            this.label5.Text = estado.ToString();
            this.label7.Text = fecha;
            this.label8.Text = nombre;
        }

        private void buscarEnJuddi(String parametro)
        {
            Maquina.MaquinaVending m = new Maquina.MaquinaVending();
            List<String> maquinas = new List<string>();
            List<String> maquinas_key = new List<string>();

            //Obtener clave
            Seguridad.get_authToken get_auth = new Seguridad.get_authToken();
            Seguridad.authToken auth = new Seguridad.authToken();
            get_auth.userID = "juddi";
            get_auth.cred = "juddi";
            auth = seg.get_authToken(get_auth);
            String clave = auth.authInfo;

            //Obtener wsdl
            try
            {
                Buscar.tModelList tml = new Buscar.tModelList();
                Buscar.find_tModel ftm = new Buscar.find_tModel();
                List<Buscar.tModelInfo> list = new List<Buscar.tModelInfo>();
                Buscar.name n = new Buscar.name();
           
                Buscar.get_tModelDetail getdetalles = new Buscar.get_tModelDetail();
                Buscar.tModelDetail detalles = new Buscar.tModelDetail();

        
                String url = buscar.Url;
           
                String[] q= new String[2];
                String[] w = new String[1];
                q[0] = "approximateMatch";
                ftm.authInfo=clave;//"authtoken:97baf37a‐05f3‐4912‐9bb4‐ef2d34a88900";
                ftm.findQualifiers = q;
                n.Value = parametro+"%";
                ftm.name = n;
                tml= buscar.find_tModel(ftm);

                try
                {
                    if(tml.tModelInfos!=null)
                    list= tml.tModelInfos.ToList();
                    else
                        MessageBox.Show("No se ha encontrado ninguna máquina. " );
            
                    try
                    {
                        list.ForEach(delegate(Buscar.tModelInfo info)
                        {
                            n = info.name;
                            getdetalles.authInfo = ftm.authInfo;
                            w[0] = info.tModelKey;
                            getdetalles.tModelKey = w;
                            detalles = buscar.get_tModelDetail(getdetalles);
                            if (!maquinas_key.Contains(detalles.tModel[0].tModelKey))
                            {
                                this.nombres.Add(n.Value);
                                maquinas_key.Add(detalles.tModel[0].tModelKey);
                                maquinas.Add(detalles.tModel[0].overviewDoc[0].overviewURL.Value);
                                this.comboBox1.Items.Add(detalles.tModel[0].overviewDoc[0].overviewURL.Value + " ; ");
                                //Tipo:http://192.168.56.101:8080/MaquinaVending2/services/MaquinaVending.MaquinaVendingHttpSoap11Endpoint/
                                url_leidas.Add(detalles.tModel[0].overviewDoc[0].overviewURL.Value);//".MaquinaVendingHttpSoap11Endpoint/");
                            }
                        });
                    }
                    catch (Exception ex)
                    {
                        MessageBox.Show("Error :" + ex);
                    }
                }
                catch(Exception ex)
                {
                    MessageBox.Show("Error: \n" + ex);
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("Error al conectar con UDDI, verificar URL."+ "\n"+"Excepcion: "+ex.Message);
            
            }

        }

        private void button1_Click(object sender, EventArgs e)
        {
            cambiarServidorJuddi();
            recargarDatos();            
        }

        private void cambiarFecha()
        {
            Maquina.MaquinaVending m = new Maquina.MaquinaVending();
            m.Url = url_maquina_escogida;
            m.setFecha();
            String fecha = m.getFecha().@return;
            this.label7.Text = fecha;
        }

        private void button2_Click(object sender, EventArgs e)
        {
            Maquina.MaquinaVending m = new Maquina.MaquinaVending();
            m.Url = url_maquina_escogida;
            int mon = Int32.Parse(this.textBox1.Text);
            bool test=true;
            m.setMonedero(mon, test);
            int monedero = m.getMonedero().@return;
            this.textBox1.Text = monedero.ToString();
            this.label2.Text = monedero.ToString();
            this.textBox1.Text = "";
            cambiarFecha();
        }

        private void label2_Click(object sender, EventArgs e)
        {
        }

        private void button3_Click(object sender, EventArgs e)
        {
            Maquina.MaquinaVending m = new Maquina.MaquinaVending();
            m.Url = url_maquina_escogida;
            m.setStock(Int32.Parse(this.textBox2.Text), true);
            int stock = m.getStock().@return;
            this.textBox2.Text = stock.ToString();
            this.label3.Text = stock.ToString();
            this.textBox2.Text = "";
            cambiarFecha();
        }

        private void button4_Click(object sender, EventArgs e)
        {
            Maquina.MaquinaVending m = new Maquina.MaquinaVending();
            m.Url = url_maquina_escogida;
            m.setTemperatura(Int32.Parse(this.textBox3.Text), true);
            int temperatura = m.getTemperatura().@return;
            this.textBox3.Text = temperatura.ToString();
            this.label4.Text = temperatura.ToString();
            this.textBox3.Text = "";
            cambiarFecha();
        }
        
        private void button7_Click(object sender, System.EventArgs e)
        {
            cambiarServidorJuddi();
            this.comboBox1.Items.Clear();
            buscarEnJuddi(this.textBox5.Text.ToString());
        }

        private void radioButton1_CheckedChanged_1(object sender, System.EventArgs e)
        {
                Maquina.MaquinaVending m = new Maquina.MaquinaVending();
                m.Url = url_maquina_escogida;
                int estado = 0;
                m.setEstado(estado, true);
                this.label5.Text = estado.ToString();
                cambiarFecha();
        }

        private void radioButton2_CheckedChanged(object sender, System.EventArgs e)
        {
                Maquina.MaquinaVending m = new Maquina.MaquinaVending();
                m.Url = url_maquina_escogida;
                int estado = -1;
                m.setEstado(estado, true);
                this.label5.Text = estado.ToString();
                cambiarFecha();
        }
    }
}
