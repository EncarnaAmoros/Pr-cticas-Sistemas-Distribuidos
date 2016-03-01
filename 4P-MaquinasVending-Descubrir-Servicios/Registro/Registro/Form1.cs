using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace Registro
{
    public partial class Form1 : Form
    {
        Seguridad.UDDISecurityService seguridad = null;
        Buscar.UDDIInquiryService buscar = null;
        Publicar.UDDIPublicationService publicar = null;

        public Form1()
        {
            InitializeComponent();
            seguridad = new Seguridad.UDDISecurityService();
            buscar = new Buscar.UDDIInquiryService();
            publicar = new Publicar.UDDIPublicationService();
        }

        private string generarclave()
        {
            Seguridad.get_authToken auth = new Seguridad.get_authToken();
            Seguridad.authToken respuesta = new Seguridad.authToken();
            auth.userID="uddi";
            auth.cred="uddi";
            respuesta=seguridad.get_authToken(auth);
            return respuesta.authInfo;
        }

        private void cambiarServidorJuddi()
        {
            //Si se ha especificado un servidor se usara ese
            if (!textBox3.Text.ToString().Equals(""))
            { 
                //http://localhost:8080/juddiv3/services/inquiry
                publicar.Url = "http://" + textBox3.Text.ToString() + ":8080/juddiv3/services/publish";
                buscar.Url = "http://" + textBox3.Text.ToString() + ":8080/juddiv3/services/inquiry";
                seguridad.Url = "http://" + textBox3.Text.ToString() + ":8080/juddiv3/services/security";

            }
        }

        //Se encarga de publicar un servicio con nombre y dirección wsdl especificados
        private void button2_Click(object sender, EventArgs e)
        {
            cambiarServidorJuddi();
            Publicar.save_tModel guardarmodelo = new Publicar.save_tModel();
            Publicar.tModel[] modelos = new Publicar.tModel[1];
            Publicar.tModel modelo = new Publicar.tModel();
            Publicar.name nombre = new Publicar.name();
            Publicar.overviewDoc[] docs = new Publicar.overviewDoc[1];
            Publicar.overviewDoc doc = new Publicar.overviewDoc();
            Publicar.overviewURL url = new Publicar.overviewURL();
            string clave = "";

            try
            {
                url.Value = textBox1.Text;
                doc.overviewURL = url;
                clave = generarclave(); 
                guardarmodelo.authInfo = clave;
                nombre.Value = this.textBox2.Text.ToString();
                modelo.name = nombre;
                docs[0] = doc;
                modelo.overviewDoc = docs;

                modelos[0] = modelo;
                guardarmodelo.tModel = modelos;
                guardarmodelo.authInfo = clave; 
            }
            catch (Exception ex)
            {
                MessageBox.Show("Error al generar auth.\nExcepcion: " + ex.Message.ToString());
            }
            try
            {
                publicar.save_tModel(guardarmodelo);
            }
            catch (Exception ex)
            {
                MessageBox.Show("Error al agregar maquina.\nExcepcion: " + ex.Message.ToString());
            }

            this.textBox1.Text = "";
            this.textBox2.Text = "";
        }

        //Botón de buscar servicios
        private void button7_Click(object sender, EventArgs e)
        {
            cambiarServidorJuddi();
            this.comboBox1.Items.Clear();
            buscarEnJuddi(this.textBox5.Text.ToString());
        }

        //Busca un servicio según nombre especificado+%
        private void buscarEnJuddi(String parametro)
        {
            Maquina.MaquinaVending m = new Maquina.MaquinaVending();
            List<String> maquinas = new List<string>();
            List<String> maquinas_key = new List<string>();
            String clave = generarclave();

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

                String[] q = new String[2];
                String[] w = new String[1];
                q[0] = "approximateMatch";
                ftm.authInfo = clave;
                ftm.findQualifiers = q;
                n.Value = parametro + "%";
                ftm.name = n;
                tml = buscar.find_tModel(ftm);

                try
                {
                    if (tml.tModelInfos != null)
                        list = tml.tModelInfos.ToList();
                    else
                        MessageBox.Show("No se ha encontrado ninguna máquina. ");

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
                                maquinas_key.Add(detalles.tModel[0].tModelKey);
                                maquinas.Add(detalles.tModel[0].overviewDoc[0].overviewURL.Value);
                                this.comboBox1.Items.Add(detalles.tModel[0].overviewDoc[0].overviewURL.Value + " ; ");
                            }
                        });
                    }
                    catch (Exception ex)
                    {
                        MessageBox.Show("Error :" + ex);
                    }
                }
                catch (Exception ex)
                {
                    MessageBox.Show("Error: \n" + ex);
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("Error al conectar con UDDI, verificar URL." + "\n" + "Excepcion: " + ex.Message);

            }

        }

    }
}