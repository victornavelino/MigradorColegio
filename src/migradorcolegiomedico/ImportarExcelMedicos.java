/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migradorcolegiomedico;

import Controladores.DocumentoIdentidadJpaController;
import Controladores.EspecialidadJpaController;
import Controladores.EstadoCivilJpaController;
import Controladores.SexoJpaController;
import Controladores.TipoDocumentoJpaController;
import Controladores.TipoTelefonoJpaController;
import Entidades.Medico.Especialidad;
import Entidades.Medico.Medico;
import Entidades.Persona.CorreoElectronico;
import Entidades.Persona.DocumentoIdentidad;
import Entidades.Persona.Domicilio;
import Entidades.Persona.EstadoCivil;
import Entidades.Persona.Persona;
import Entidades.Persona.Sexo;
import Entidades.Persona.Telefono;
import Entidades.Persona.TipoTelefono;
import Facades.MedicoFacade;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import jxl.*;
import jxl.read.biff.BiffException;

/**
 *
 * @author hugo
 */
public class ImportarExcelMedicos {

    private Medico medico;
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("MigradorColegioMedicoPU");
    SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");

    public static void main(String arg[]) {
        System.out.println("Importador de Archivos:");
        ImportarExcelMedicos excel = new ImportarExcelMedicos();
        //excel.crearSexo();
        excel.crearTipoTelefono();
        excel.importar();//LEGAJOMEDICO.xls --> MEDICOS
    }

    private void crearSexo() {
        Sexo sexo = new SexoJpaController(emf).findSexo(1L);
        if (sexo == null) {
            sexo.setId(1L);
            sexo.setDescripcion("Masculino".toUpperCase());
            new SexoJpaController(emf).create(sexo);
        }
        sexo = new SexoJpaController(emf).findSexo(2L);
        if (sexo == null) {
            sexo = new Sexo();
            sexo.setId(1L);
            sexo.setDescripcion("Femenino".toUpperCase());
            new SexoJpaController(emf).create(sexo);

        }
    }

    private void crearTipoTelefono() {
        TipoTelefono tipoTelefono = new TipoTelefonoJpaController(emf).findTipoTelefono(1L);
        if (tipoTelefono == null) {
            tipoTelefono.setId(1L);
            tipoTelefono.setDescripcion("Fijo".toUpperCase());
            new TipoTelefonoJpaController(emf).create(tipoTelefono);
        }
        tipoTelefono = new TipoTelefonoJpaController(emf).findTipoTelefono(2L);
        if (tipoTelefono == null) {
            tipoTelefono = new TipoTelefono();
            tipoTelefono.setId(1L);
            tipoTelefono.setDescripcion("Celular".toUpperCase());
            new TipoTelefonoJpaController(emf).create(tipoTelefono);

        }
    }

    private void importar() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = fileChooser.getSelectedFile();
                System.out.println("Selected file: " + selectedFile.getAbsolutePath());
                boolean flag = true;
                InputStream in = null;
                in = new FileInputStream(selectedFile);

                Workbook workbook = Workbook.getWorkbook(in);
                Sheet sheet = workbook.getSheet(0);
                String dato;
                // Recorre cada fila de la  hoja 
                for (int fila = 1; fila < sheet.getRows(); fila++) {
                    medico = new Medico();
                    medico.setPersona(new Persona());

                    for (int columna = 0; columna < sheet.getColumns(); columna++) { // Recorre  cada fila 
                        dato = sheet.getCell(columna, fila).getContents();
                        switch (String.valueOf(columna)) {
                            case "0":
                                medico.setMatriculaProfesional(dato);
                                break;
                            case "1":
                                try {
                                    String apellidoNombreArray[];
                                    apellidoNombreArray = dato.split("\\,");
                                    medico.getPersona().setApellido(apellidoNombreArray[0]);
                                    medico.getPersona().setNombre(apellidoNombreArray[1].trim());
                                } catch (Exception e) {
                                }
                                break;
                            case "2":
                                try {
                                    if (dato.contains("Masculino")) {
                                        medico.getPersona().setSexo(new SexoJpaController(emf).findSexo(1L));
                                    } else if (dato.contains("Femenino")) {
                                        medico.getPersona().setSexo(new SexoJpaController(emf).findSexo(2L));
                                    }
                                } catch (Exception e) {
                                }
                                break;
                            case "3":
                                try {
                                    medico.getPersona().setFechaNacimiento(formatoFecha.parse(dato.substring(0, 10)));
                                } catch (Exception e) {
                                }
                                break;
                            case "4":
                                try {
                                    EstadoCivil civilBuscado = new EstadoCivilJpaController(emf).findEstadoCivil(Long.parseLong(dato));
                                    if (civilBuscado == null) {
                                        civilBuscado = new EstadoCivil();
                                        civilBuscado.setId(Long.parseLong(dato));
                                        civilBuscado.setDescripcion(dato);
                                        new EstadoCivilJpaController(emf).create(civilBuscado);
                                    }
                                    medico.getPersona().setEstadoCivil(civilBuscado);
                                } catch (NumberFormatException numberFormatException) {
                                }
                                break;
                            case "5":
                                medico.getPersona()
                                        .setDocumentoIdentidad(
                                                new DocumentoIdentidad());
                                try {
                                    if (dato.contains("DNI")) {
                                        medico.getPersona().getDocumentoIdentidad().setTipoDocumento(
                                                new TipoDocumentoJpaController(emf).findTipoDocumento(1L));
                                    }
                                } catch (Exception e) {
                                }
                                break;
                            case "6":
                                try {
                                    medico.getPersona().getDocumentoIdentidad().setNumero(Long.parseLong(dato));
                                } catch (NumberFormatException numberFormatException) {
                                }
                                break;
                            case "7":
                                //Nacionalidad
                                break;
                            case "8":
                                //Provincia
                                medico.getPersona().setDomicilio(new Domicilio());
                                // medico.getPersona().getDomicilio().setLocalidad(null);
                                break;
                            case "9":
                                //Departamento
                                // medico.getPersona().getDomicilio().setLocalidad(null);
                                break;

                            case "10":
                                //Localidad
                                // medico.getPersona().getDomicilio().setLocalidad(null);
                                break;

                            case "11":
                                //barrio
                                medico.getPersona().getDomicilio().setBarrio(dato);
                                break;
                            case "12":
                                //Calle
                                medico.getPersona().getDomicilio().setCalle(dato);
                                break;
                            case "13":
                                //numero
                                medico.getPersona().getDomicilio().setNumero(dato);
                                break;
                            case "14":
                                //piso
                                medico.getPersona().getDomicilio().setPiso(dato);
                                break;
                            case "15":
                                //dpto
                                medico.getPersona().getDomicilio().setDpto(dato);
                                break;
                            case "16":
                                //codigio postal
                                medico.getPersona().getDomicilio().setCodigoPostal(dato);
                                break;
                            case "17":
                                //Telefono
                                List<Telefono> telefonos = new ArrayList<>();
                                telefonos.add(new Telefono());
                                try {
                                    TipoTelefono tipoTelefono = new TipoTelefonoJpaController(emf).findTipoTelefono(1L);
                                    telefonos.get(0).setTipoTelefono(tipoTelefono);
                                } catch (Exception e) {
                                }
                                telefonos.get(0).setNumero(dato);

                                medico.getPersona().setTelefonos(telefonos);//falta agregar el tipo
                                break;
                            case "18":
                                //Celular
                                Telefono t = new Telefono();
                                try {
                                    TipoTelefono tipoTelefono = new TipoTelefonoJpaController(emf).findTipoTelefono(2L);
                                    t.setTipoTelefono(tipoTelefono);
                                } catch (Exception e) {
                                }
                                t.setNumero(dato);
                                medico.getPersona().getTelefonos().add(t);
                                ;//falta agregar el tipo

                                break;
                            case "19":
                                //EMAIL
                                if (!dato.isEmpty()) {
                                    List<CorreoElectronico> ces = new ArrayList<>();
                                    ces.add(new CorreoElectronico());
                                    ces.get(0).setDireccion(dato);
                                    medico.getPersona().setCorreosElectronicos(null);
                                }
                                break;
                            case "20": {
                                try {
                                    //FECHA INSCRIPCION
                                    medico.setFechaInscripcion(formatoFecha.parse(dato.substring(0, 10)));
                                } catch (ParseException ex) {
                                    Logger.getLogger(ImportarExcelMedicos.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            break;
                            case "21":
                                //TITULO
                                medico.setTitulo(dato);
                                break;
                            case "22":
                                //IDESPECIALIDAD
                                Especialidad especialidadBuscada = new EspecialidadJpaController(emf).findEspecialidad(Long.parseLong(dato));
                                if (especialidadBuscada == null) {
                                    especialidadBuscada = new Especialidad();
                                    especialidadBuscada.setId(Long.parseLong(dato));
                                    especialidadBuscada.setDescripcion(dato);
                                    new EspecialidadJpaController(emf).create(especialidadBuscada);
                                }//falta terminar
                             //   List<Es> especialidads = new ArrayList<>();
                                
                               // medico.getPersona().setEespecialidads);
                                break;
                            case "23":
                                //FECHARECIBIDO 
                                break;
                            case "24":
                                //UNIVERSIDAD
                                break;
                            case "25":
                            //FACULTAD
                            case "26":
                                //PROVINCIARECIBIDO
                                break;
                            case "27":
                                //NOMBREUSUARIO
                                break;
                            case "28":
                            //TIPOUSUARIO	
                            case "29":
                                //FECHAREGISTRO	
                                break;
                            case "30":
                                //HORAREGISTRO	
                                break;
                            case "31":
                                //FECHABAJA	
                                break;
                            case "32":
                                //MOTIVOBAJA	
                                break;
                            case "33":
                                //MATRICULANACIONAL	
                                break;
                            case "34":
                                //NROINSCRIPCION	
                                break;
                            case "35":
                                //TIPOSOCIO	
                                break;
                            case "36":
                                //ORGANISMO	
                                break;
                            case "37":
                                //LIBROINSCRIPCION	
                                break;
                            case "38":
                                //FOLIOINSCRIPCION	
                                break;
                            case "39":
                            //FECHATITULO

                        }
                    }
                    MedicoFacade.getInstance().alta(medico);
                }
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(null, "Error:" + ex);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error:" + ex);
            } catch (BiffException ex) {
                JOptionPane.showMessageDialog(null, "Error:" + ex);
            }
        }
    }

    public String quitarComas(String entrada) {
        return entrada.replace("" + "$" + "", "").replace(".", "").replace(",", ".").replace('"', ' ').trim();
    }

}
