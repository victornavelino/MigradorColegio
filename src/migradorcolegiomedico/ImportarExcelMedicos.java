/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migradorcolegiomedico;

import Controladores.DocumentoIdentidadJpaController;
import Controladores.EstadoCivilJpaController;
import Controladores.SexoJpaController;
import Controladores.TipoDocumentoJpaController;
import Entidades.Medico.Medico;
import Entidades.Persona.DocumentoIdentidad;
import Entidades.Persona.Domicilio;
import Entidades.Persona.EstadoCivil;
import Entidades.Persona.Persona;
import Entidades.Persona.Telefono;
import Facades.MedicoFacade;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
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
        excel.importar();//LEGAJOMEDICO.xls --> MEDICOS
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
                                telefonos.get(0).setNumero(dato);
                                medico.getPersona().setTelefonos(telefonos);//falta agregar el tipo
                                break;
                            case "18":
                                //Celular
                                Telefono t = new Telefono();
                                t.setNumero(dato);
                                medico.getPersona().getTelefonos().add(t);
                                ;//falta agregar el tipo

                                break;
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
