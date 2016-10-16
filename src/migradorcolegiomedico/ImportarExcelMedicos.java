/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migradorcolegiomedico;

import Controladores.BancoJpaController;
import Controladores.CuentaJpaController;
import Controladores.EspecialidadJpaController;
import Controladores.EspecializacionJpaController;
import Controladores.EstadoCivilJpaController;
import Controladores.OrganismoJpaController;
import Controladores.PagoJpaController;
import Controladores.RecertificacionJpaController;
import Controladores.SexoJpaController;
import Controladores.TipoDeEgresoJpaController;
import Controladores.TipoDeIngresoJpaController;
import Controladores.TipoDocumentoJpaController;
import Controladores.TipoMedicoJpaController;
import Controladores.TipoTelefonoJpaController;
import Controladores.UnidadFormadoraJpaController;
import Controladores.UsuarioJpaController;
import Entidades.Caja.TipoDeEgreso;
import Entidades.Caja.TipoDeIngreso;
import Entidades.Medico.Especialidad;
import Entidades.Medico.Especializacion;
import Entidades.Medico.Medico;
import Entidades.Medico.Organismo;
import Entidades.Medico.Recertificacion;
import Entidades.Medico.TipoMedico;
import Entidades.Medico.UnidadFormadora;
import Entidades.Pago.Banco;
import Entidades.Pago.Cuenta;
import Entidades.Pago.Mes;
import Entidades.Pago.Pago;
import Entidades.Persona.CorreoElectronico;
import Entidades.Persona.DocumentoIdentidad;
import Entidades.Persona.Domicilio;
import Entidades.Persona.EstadoCivil;
import Entidades.Persona.Persona;
import Entidades.Persona.Sexo;
import Entidades.Persona.Telefono;
import Entidades.Persona.TipoDocumento;
import Entidades.Persona.TipoTelefono;
import Entidades.Usuario.Usuario;
import Facades.EspecializacionFacade;
import Facades.MedicoFacade;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.JFileChooser;
import jxl.*;
import jxl.read.biff.BiffException;

/**
 *
 * @author hugo
 */
public class ImportarExcelMedicos {

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("MigradorColegioMedicoPU");
    SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");

    public static void main(String arg[]) {
        System.out.println("Importador de Archivos:");
        ImportarExcelMedicos excel = new ImportarExcelMedicos();
        excel.crearUsuario();
        excel.crearOrganismo();
        excel.crearSexo();
        excel.crearTipoTelefono();
        excel.crearBanco();
        excel.crearCuenta();
        excel.crearTipoMedico();
        excel.crearTipoEgreso();
        excel.crearTipoIngreso();
        excel.crearTipoDocumento();
        excel.importar();//LEGAJOMEDICO.xls --> MEDICOS
    }

    private void crearSexo() {
        Sexo sexo = new SexoJpaController(emf).findSexo(1L);
        if (sexo == null) {
            sexo = new Sexo();
            sexo.setId(1L);
            sexo.setDescripcion("Masculino".toUpperCase());
            new SexoJpaController(emf).create(sexo);
        }
        sexo = new SexoJpaController(emf).findSexo(2L);
        if (sexo == null) {
            sexo = new Sexo();
            sexo.setId(2L);
            sexo.setDescripcion("Femenino".toUpperCase());
            new SexoJpaController(emf).create(sexo);

        }
    }

    private void crearTipoTelefono() {
        TipoTelefono tipoTelefono = new TipoTelefonoJpaController(emf).findTipoTelefono(1L);
        if (tipoTelefono == null) {
            tipoTelefono = new TipoTelefono();
            tipoTelefono.setId(1L);
            tipoTelefono.setDescripcion("Fijo".toUpperCase());
            new TipoTelefonoJpaController(emf).create(tipoTelefono);
        }
        tipoTelefono = new TipoTelefonoJpaController(emf).findTipoTelefono(2L);
        if (tipoTelefono == null) {
            tipoTelefono = new TipoTelefono();
            tipoTelefono.setId(2L);
            tipoTelefono.setDescripcion("Celular".toUpperCase());
            new TipoTelefonoJpaController(emf).create(tipoTelefono);

        }
    }

    private void importar() {

        JFileChooser fileChooser = new JFileChooser();

        fileChooser.setCurrentDirectory(new java.io.File("."));
        fileChooser.setDialogTitle("Seleccione la carpeta con los archivos xls a importar");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //
        // disable the "All files" option.
        //
        fileChooser.setAcceptAllFileFilterUsed(false);
        //    

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            importarEspecialidad(fileChooser);
            importarLegajoMedico(fileChooser);
            importarEspecializacionMedica(fileChooser);
            importarPagos(fileChooser);
            importarRecertificacion(fileChooser);
        }
    }

    private void importarLegajoMedico(JFileChooser fileChooser) {
        try {
            String archivo = "LEGAJOMEDICO.xls";
            System.out.println("Importando " + archivo + "  ...");
            String ruta = fileChooser.getSelectedFile().getAbsolutePath() + File.separator + archivo;
            File selectedFile = new File(ruta);
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
            boolean flag = true;
            InputStream in = null;
            in = new FileInputStream(selectedFile);

            Workbook workbook = Workbook.getWorkbook(in);
            Sheet sheet = workbook.getSheet(0);
            String dato;
            // Recorre cada fila de la  hoja
            for (int fila = 1; fila < sheet.getRows(); fila++) {
                Medico medico = new Medico();
                medico.setPersona(new Persona());

                for (int columna = 0; columna < sheet.getColumns(); columna++) { // Recorre  cada columna
                    dato = sheet.getCell(columna, fila).getContents();
                    //declar clases comunes
//                    Especialidad especialidadBuscada = new Especialidad();
//                    Especializacion especializacion = new Especializacion();
//                    UnidadFormadora unidadFormadora = new UnidadFormadora();
                    switch (String.valueOf(columna)) {
                        case "0":
                            try {
                                medico.setMatriculaProfesional(Integer.parseInt(dato));
                            } catch (Exception e) {
                            }
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
                                if (!dato.contains("NULL")) {
                                    Date parse = formatoFecha.parse(dato.substring(0, 10));
                                    medico.setFechaInscripcion(parse);
                                }
                            } catch (ParseException ex) {
                                Logger.getLogger(ImportarExcelMedicos.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        break;
                        case "21":
                            //TITULO
                            if (!dato.contains("NULL")) {
                                medico.setTitulo(dato);
                            }
                            break;
                        case "22":
                        //IDESPECIALIDAD
//                            try {
//                                especialidadBuscada = new EspecialidadJpaController(emf).findEspecialidad(Long.parseLong(dato));
//                                if (especialidadBuscada == null) {
//                                    especialidadBuscada = new Especialidad();
//                                    especialidadBuscada.setId(Long.parseLong(dato));
//                                    especialidadBuscada.setDescripcion(dato);
//                                    new EspecialidadJpaController(emf).create(especialidadBuscada);
//                                }
//                            } catch (NumberFormatException numberFormatException) {
//                            }
//                            especializacion.setEspecialidad(especialidadBuscada);
//                            break;
                        case "23": {
//                            try {
//                                //FECHARECIBIDO
//                                especializacion.setFechaMatriculacion(formatoFecha.parse(dato.substring(0, 10)));
//                            } catch (ParseException ex) {
//                                Logger.getLogger(ImportarExcelMedicos.class.getName()).log(Level.SEVERE, null, ex);
//                            }
                        }
                        break;
                        case "24":
                            //UNIVERSIDAD
//                            try {
//                                unidadFormadora = new UnidadFormadoraJpaController(emf).findUnidadFormadora(Long.parseLong(dato)); //TODO aca hay que validar por nombre
//                                if (unidadFormadora == null) {
//                                    unidadFormadora = new UnidadFormadora();
//                                    unidadFormadora.setId(Long.parseLong(dato));
//                                    unidadFormadora.setDescripcion(dato);
//                                    new UnidadFormadoraJpaController(emf).create(unidadFormadora);
//                                }
//                            } catch (NumberFormatException numberFormatException) {
//                            }
                            break;
                        case "25":
                        //FACULTAD
//                            Facultad facultad = new Facultad();
//                            try {
//                                facultad = new FacultadJpaController(emf).findFacultad(Long.parseLong(dato)); //TODO aca hay que validar por nombre
//                                if (facultad == null) {
//                                    facultad = new Facultad();
//                                    facultad.setId(Long.parseLong(dato));
//                                    facultad.setDescripcion(dato);
//                                    new FacultadJpaController(emf).create(facultad);
//                                }
//                            } catch (NumberFormatException numberFormatException) {
//                            }
//                            unidadFormadora.setFacultad(facultad);
                        case "26":
                            //PROVINCIARECIBIDO
//                            unidadFormadora.setLocalidad(null);
//                            especializacion.setUnidadFormadora(unidadFormadora);

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
            Logger.getLogger(ImportarExcelMedicos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ImportarExcelMedicos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BiffException ex) {
            Logger.getLogger(ImportarExcelMedicos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void importarEspecializacionMedica(JFileChooser fileChooser) {
        try {
            String archivo = "ESPECIALIZACIONMEDICA.xls";
            System.out.println("Importando " + archivo + "  ...");
            String ruta = fileChooser.getSelectedFile().getAbsolutePath() + File.separator + archivo;
            File selectedFile = new File(ruta);
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
            boolean flag = true;
            InputStream in = null;
            in = new FileInputStream(selectedFile);

            Workbook workbook = Workbook.getWorkbook(in);
            Sheet sheet = workbook.getSheet(0);
            String dato;
            // Recorre cada fila de la  hoja
            for (int fila = 1; fila < sheet.getRows(); fila++) {
                Especializacion especializacion = new Especializacion();
                Especialidad especialidad = new Especialidad();
                UnidadFormadora unidadFormadora = new UnidadFormadora();
                Medico medico = new Medico();

                for (int columna = 0; columna < sheet.getColumns(); columna++) { // Recorre  cada fila
                    dato = sheet.getCell(columna, fila).getContents();
                    //declar clases comunes
                    switch (String.valueOf(columna)) {
                        case "0":
                            //Matricula
                            try {
                                medico = MedicoFacade.getInstance().buscarPorMatricula(Integer.parseInt(dato));
                                especializacion.setMedico(medico);
                            } catch (Exception e) {
                            }
                            break;
                        case "1":
                            //IDITEMCUENTA

//                      
                            break;
                        case "2":
                            //ESPECIALIDAD	
                            try {
                                if (!dato.contains("NULL")) {
                                    especialidad = new EspecialidadJpaController(emf).findEspecialidad(Long.parseLong(dato));
                                }

                            } catch (Exception e) {
                            }
                            if (!dato.contains("NULL")) {
                                especializacion.setEspecialidad(especialidad);
                            }
                            break;
                        case "3":
                            //MATRICULAESPECIALIDAD
                            try {
                                if (!dato.contains("NULL")) {
                                    especializacion.setMatriculaEspecialidad(Integer.parseInt(dato));
                                }
                            } catch (Exception e) {
                            }
                            break;
                        case "4":
                            //FECHAMATRICULACION
                            try {
                                if (!dato.contains("NULL")) {

                                    especializacion.setFechaMatriculacion(formatoFecha.parse(dato.substring(0, 10)));
                                }
                            } catch (Exception e) {
                            }
                            break;
                        case "5":
                            //LIBRO	
                            try {
                                if (!dato.contains("NULL")) {
                                    especializacion.setLibro(dato);
                                }
                            } catch (Exception e) {
                            }
                            break;
                        case "6":
                            //FOLIO
                            try {
                                if (!dato.contains("NULL")) {
                                    especializacion.setFolio(dato);
                                }
                            } catch (Exception e) {
                            }
                            break;
                        case "7":
                            //	UNIVERSIDAD
                            try {
                                if (!dato.contains("NULL")) {
                                    unidadFormadora = new UnidadFormadoraJpaController(emf).findUnidadFormadora(Long.parseLong(dato)); //TODO aca hay que validar por nombre
                                    if (unidadFormadora == null) {
                                        unidadFormadora = new UnidadFormadora();
                                        unidadFormadora.setId(Long.parseLong(dato));
                                        unidadFormadora.setDescripcion(dato);
                                        new UnidadFormadoraJpaController(emf).create(unidadFormadora);
                                    }
                                }
                            } catch (NumberFormatException numberFormatException) {
                            }
                            try {
                                if (!dato.contains("NULL")) {
                                    especializacion.setUnidadFormadora(unidadFormadora);
                                }
                            } catch (Exception e) {
                            }
                            break;

                    }
                }
                new EspecializacionJpaController(emf).create(especializacion);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ImportarExcelMedicos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ImportarExcelMedicos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BiffException ex) {
            Logger.getLogger(ImportarExcelMedicos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ImportarExcelMedicos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void importarEspecialidad(JFileChooser fileChooser) {
        try {
            String archivo = "especialidad.xls";
            System.out.println("Importando " + archivo + "  ...");
            String ruta = fileChooser.getSelectedFile().getAbsolutePath() + File.separator + archivo;
            File selectedFile = new File(ruta);
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
            boolean flag = true;
            InputStream in = null;
            in = new FileInputStream(selectedFile);

            Workbook workbook = Workbook.getWorkbook(in);
            Sheet sheet = workbook.getSheet(0);
            String dato;
            // Recorre cada fila de la  hoja
            for (int fila = 1; fila < sheet.getRows(); fila++) {
                Especialidad especialidad = new Especialidad();
                for (int columna = 0; columna < sheet.getColumns(); columna++) { // Recorre  cada fila
                    dato = sheet.getCell(columna, fila).getContents();
                    //declar clases comunes
                    switch (String.valueOf(columna)) {
                        case "0":
                            //ID	
                            try {
                                especialidad.setId(Long.parseLong(dato));
                            } catch (NumberFormatException numberFormatException) {
                            }
                            break;
                        case "1":
                            //ESPECIALIDAD

                            try {
                                especialidad.setDescripcion(dato);
                                especialidad.setNombreEspecialidad(dato);

                            } catch (Exception e) {
                            }
                            break;
                        case "2":
                            //ESPECIALIDAD	
                            try {
                                //    especialidad.setNombreEspecialidad(dato);

                            } catch (Exception e) {
                            }
                            break;

                    }
                }
                new EspecialidadJpaController(emf).create(especialidad);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ImportarExcelMedicos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ImportarExcelMedicos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BiffException ex) {
            Logger.getLogger(ImportarExcelMedicos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ImportarExcelMedicos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void importarPagos(JFileChooser fileChooser) {
        try {
            String archivo = "DETALLEPAGOS.xls";
            System.out.println("Importando " + archivo + "  ...");
            String ruta = fileChooser.getSelectedFile().getAbsolutePath() + File.separator + archivo;
            File selectedFile = new File(ruta);
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
            boolean flag = true;
            InputStream in = null;
            in = new FileInputStream(selectedFile);

            Workbook workbook = Workbook.getWorkbook(in);
            Sheet sheet = workbook.getSheet(0);
            String dato;
            // Recorre cada fila de la  hoja
            for (int fila = 1; fila < sheet.getRows(); fila++) {
                Pago pago = new Pago();
                String fecha = new String();
                Medico medico = new Medico();
                for (int columna = 0; columna < sheet.getColumns(); columna++) { // Recorre  cada fila
                    dato = sheet.getCell(columna, fila).getContents();
                    //declar clases comunes
                    switch (String.valueOf(columna)) {
                        case "0":
                            //MATRICULAPROVINCIAL	

                            try {
                                medico = MedicoFacade.getInstance().buscarPorMatricula(Integer.parseInt(dato));
                                pago.setMedico(medico);
                            } catch (NumberFormatException numberFormatException) {
                            }
                            break;
                        case "1":
                            //FECHADESDE	

//                            try {
//                                pago.set(new CuentaJpaController(emf).findCuenta(Long.parseLong(dato)));
//                            } catch (Exception e) {
//                            }
                            break;
                        case "2":
                            //FECHAHASTA		
//                            try {
//                                Especializacion buscarPorMatricula = EspecializacionFacade.getInstance().buscarPorMatriculaEspecialidad(dato);
//                                pago.setEspecializacion(buscarPorMatricula);
//                            } catch (Exception e) {
//                            }
                            break;
                        case "3":
                            //NRORECIBO	
                            try {
                                pago.setNroRecibo(dato);

                            } catch (Exception e) {
                            }
                            break;

                        case "4":
                        //CANTIDADMESES
//                            try {
//                                pago.set(dato);
//
//                            } catch (Exception e) {
//                            }
//                            break;

                        case "5":
                            //MES	
                            try {
                                Mes mes = null;
                                if (null != dato) {
                                    switch (dato) {
                                        case "1":
                                            mes = Mes.ENERO;
                                            break;
                                        case "2":
                                            mes = Mes.FEBRERO;
                                            break;
                                        case "3":
                                            mes = Mes.MARZO;
                                            break;
                                        case "4":
                                            mes = Mes.ABRIL;
                                            break;
                                        case "5":
                                            mes = Mes.MAYO;
                                            break;
                                        case "6":
                                            mes = Mes.JUNIO;
                                            break;
                                        case "7":
                                            mes = Mes.JULIO;
                                            break;
                                        case "8":
                                            mes = Mes.AGOSTO;
                                            break;
                                        case "9":
                                            mes = Mes.SEPTIEMBRE;
                                            break;
                                        case "10":
                                            mes = Mes.OCTUBRE;
                                            break;
                                        case "11":
                                            mes = Mes.NOVIEMBRE;
                                            break;
                                        case "12":
                                            mes = Mes.DICIEMBRE;
                                            break;

                                    }
                                }

                                pago.setMes(mes);

                            } catch (Exception e) {
                            }
                            break;
                        case "6":
                            //AÑO	      	
                            try {
                                if (!dato.contains("NULL")) {
                                    pago.setAnio(Integer.valueOf(dato));
                                }

                            } catch (Exception e) {
                            }
                            break;
                        case "7":
                            //PAGADO										
//                            try {
//                                pago.set(dato);
//
//                            } catch (Exception e) {
//                            }
                            break;
                        case "8":
                            //IMPORTE	 								
                            try {
                                if (!dato.contains("NULL")) {
                                    pago.setImporte(new BigDecimal(dato));
                                }

                            } catch (Exception e) {
                            }
                            break;
                        case "9":
                            //IDNROCOMPROBANTE	 									
                            try {
                                if (!dato.contains("NULL")) {
                                    pago.setNroRecibo(dato);
                                }

                            } catch (Exception e) {
                            }
                            break;
                        case "10":
////                            //USUARIO								
//                            try {
//                                recertificacion.setNombreEspecialidad(dato);
//
//                            } catch (Exception e) {
//                            }
                            break;
                        case "11":
//                            //FECHAREGISTRO								
                            try {
                                if (!dato.contains("NULL")) {
                                    fecha = dato.split("\\s+")[0].concat(" ");
                                }
                            } catch (Exception e) {
                            }
                            break;
                        case "12":
                            //	HORAREGISTRO// 											
                            try {
                                if (!dato.contains("NULL")) {
                                    fecha += dato.split("\\s+")[1].concat(" ").concat(dato.split("\\s+")[2]).replace(".", "");
                                    DateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");
                                    Date date = inputFormat.parse(fecha);
                                    pago.setFechaPago(date);
                                }
                            } catch (Exception e) {
                                System.out.println("error Parse:  " + e + " fila: " + fila);
                            }
                            break;
                    }
                }
                new PagoJpaController(emf).create(pago);

            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ImportarExcelMedicos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ImportarExcelMedicos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BiffException ex) {
            Logger.getLogger(ImportarExcelMedicos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ImportarExcelMedicos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void importarRecertificacion(JFileChooser fileChooser) {
        try {
            String archivo = "RECERTIFICACIONES.xls";
            System.out.println("Importando " + archivo + "  ...");
            String ruta = fileChooser.getSelectedFile().getAbsolutePath() + File.separator + archivo;
            File selectedFile = new File(ruta);
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
            boolean flag = true;
            InputStream in = null;
            in = new FileInputStream(selectedFile);

            Workbook workbook = Workbook.getWorkbook(in);
            Sheet sheet = workbook.getSheet(0);
            String dato;
            // Recorre cada fila de la  hoja
            for (int fila = 1; fila < sheet.getRows(); fila++) {
                Recertificacion recertificacion = new Recertificacion();
                Medico medico = new Medico();
                for (int columna = 0; columna < sheet.getColumns(); columna++) { // Recorre  cada fila
                    dato = sheet.getCell(columna, fila).getContents();
                    //declar clases comunes
                    switch (String.valueOf(columna)) {
                        case "0":
                            //MATRICULAPROVINCIAL	
//
//                            try {
//                                medico = MedicoFacade.getInstance().buscarPorMatricula(dato);
//                                if (!dato.contains("NULL")) {
//                                    recertificacion.setMedico(medico);
//                                }
//                            } catch (NumberFormatException numberFormatException) {
//                            }
                            break;
                        case "1":
                        //IDITEMCUENTA	

//                            try {
//                                if (!dato.contains("NULL")) {
//                                    recertificacion.setCuenta(new CuentaJpaController(emf).findCuenta(Long.parseLong(dato)));
//                                }
//                            } catch (Exception e) {
//                            }
//                            break;
                        case "2":
                            //MATRICULAESPECIALIDAD		
                            try {
                                Especializacion buscarPorMatricula = EspecializacionFacade.getInstance().buscarPorMatriculaEspecialidad(Integer.parseInt(dato));

                                if (!dato.contains("NULL")) {
                                    recertificacion.setEspecializacion(buscarPorMatricula);
                                }
                            } catch (Exception e) {
                            }
                            break;
                        case "3":
                            //FECHARECERTIFICACION			
                            try {
                                if (!dato.contains("NULL")) {
                                    recertificacion.setFechaRecertificacion(formatoFecha.parse(dato.substring(0, 10)));
                                }

                            } catch (Exception e) {
                            }
                            break;

                        case "4":
                        //IDESPECIALIDAD				
//                            try {
//                                recertificacion.set(dato);
//
//                            } catch (Exception e) {
//                            }
//                            break;

                        case "5":
                            //ESPECIALIDADARECERTIFICAR	
//                            try {
//                                recertificacion.set(dato);
//
//                            } catch (Exception e) {
//                            }
                            break;
                        case "6":
                            //ACTANRO	
                            try {
                                if (!dato.contains("NULL")) {
                                    recertificacion.setNroActa(dato);
                                }

                            } catch (Exception e) {
                            }
                            break;
                        case "7":
                            //RESOLUCION									
                            try {
                                if (!dato.contains("NULL")) {
                                    recertificacion.setNroResolucion(dato);
                                }

                            } catch (Exception e) {
                            }
                            break;
                        case "8":
                            //LIBRO								
                            try {
                                if (!dato.contains("NULL")) {
                                    recertificacion.setLibro(dato);
                                }

                            } catch (Exception e) {
                            }
                            break;
                        case "9":
                            //FOLIO									
                            try {
                                if (!dato.contains("NULL")) {
                                    recertificacion.setFolio(dato);
                                }

                            } catch (Exception e) {
                            }
                            break;
                        case "10":
////                            //USUARIO									
//                            try {
//                                recertificacion.setNombreEspecialidad(dato);
//
//                            } catch (Exception e) {
//                            }
                            break;
                        case "11":
//                            //FECHA								
//                            try {
//                                recertificacion.setFechaRecertificacion(dato);
//
//                            } catch (Exception e) {
//                            }
//                            break;
                        case "12":
                        //HORA									
//                            try {
//                                recertificacion.setNombreEspecialidad(dato);
//
//                            } catch (Exception e) {
//                            }
//                            break;
                        case "13":
                            //OBSERVACIONES									
                            try {
                                if (!dato.contains("NULL")) {
                                    recertificacion.setObservaciones(dato);
                                }

                            } catch (Exception e) {
                            }
                            break;
                        case "14":
                            //FECHAVENCIMIENTO								
                            try {
                                if (!dato.contains("NULL")) {
                                    recertificacion.setFechaVencimiento(formatoFecha.parse(dato.substring(0, 10)));
                                }

                            } catch (Exception e) {
                            }
                            break;
                    }
                }
                new RecertificacionJpaController(emf).create(recertificacion);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ImportarExcelMedicos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ImportarExcelMedicos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BiffException ex) {
            Logger.getLogger(ImportarExcelMedicos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ImportarExcelMedicos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String quitarComas(String entrada) {
        return entrada.replace("" + "$" + "", "").replace(".", "").replace(",", ".").replace('"', ' ').trim();
    }

    private void crearBanco() {

        Banco banco = new BancoJpaController(emf).findBanco(3L);
        if (banco == null) {
            banco = new Banco();
            banco.setId(1L);
            banco.setDescripcion("BANCO GALICIA".toUpperCase());
            new BancoJpaController(emf).create(banco);
        }
        banco = new BancoJpaController(emf).findBanco(2L);
        if (banco == null) {
            banco = new Banco();
            banco.setId(1L);
            banco.setDescripcion("BANCO NACION".toUpperCase());
            new BancoJpaController(emf).create(banco);
        }
        banco = new BancoJpaController(emf).findBanco(1L);
        if (banco == null) {
            banco = new Banco();
            banco.setId(1L);
            banco.setDescripcion("BANCO PATAGONIA".toUpperCase());
            new BancoJpaController(emf).create(banco);
        }
        banco = new BancoJpaController(emf).findBanco(6L);
        if (banco == null) {
            banco = new Banco();
            banco.setId(1L);
            banco.setDescripcion("BANCO HBC".toUpperCase());
            new BancoJpaController(emf).create(banco);
        }
        banco = new BancoJpaController(emf).findBanco(8L);
        if (banco == null) {
            banco = new Banco();
            banco.setId(1L);
            banco.setDescripcion("CHEQUE (PESOS)".toUpperCase());
            new BancoJpaController(emf).create(banco);
        }
        banco = new BancoJpaController(emf).findBanco(4L);
        if (banco == null) {
            banco = new Banco();
            banco.setId(1L);
            banco.setDescripcion("BANCO SANTANDER RIO".toUpperCase());
            new BancoJpaController(emf).create(banco);
        }
        banco = new BancoJpaController(emf).findBanco(5L);
        if (banco == null) {
            banco = new Banco();
            banco.setId(1L);
            banco.setDescripcion("BANCO FRANCES".toUpperCase());
            new BancoJpaController(emf).create(banco);
        }
        banco = new BancoJpaController(emf).findBanco(7L);
        if (banco == null) {
            banco = new Banco();
            banco.setId(1L);
            banco.setDescripcion("BANCO HIPOTECARIO".toUpperCase());
            new BancoJpaController(emf).create(banco);
        }

    }

    private void crearCuenta() {

        Cuenta cuenta = new CuentaJpaController(emf).findCuenta(1L);
        if (cuenta == null) {
            cuenta = new Cuenta(1L, "Cuota Societaria".toUpperCase(), new BigDecimal("30"), false);
            new CuentaJpaController(emf).create(cuenta);
        }
        cuenta = new CuentaJpaController(emf).findCuenta(2L);
        if (cuenta == null) {
            cuenta = new Cuenta(2L, "MANTENIMIENTO MATRICULA 2011".toUpperCase(), new BigDecimal("20"), false);
            new CuentaJpaController(emf).create(cuenta);
        }
        cuenta = new CuentaJpaController(emf).findCuenta(9L);
        if (cuenta == null) {
            cuenta = new Cuenta(9L, "MANTENIMIENTO MATRICULA 2013".toUpperCase(), new BigDecimal("57.6"), false);
            new CuentaJpaController(emf).create(cuenta);
        }
        cuenta = new CuentaJpaController(emf).findCuenta(10L);
        if (cuenta == null) {
            cuenta = new Cuenta(10L, "Mantenimiento de matricula Junio 2013".toUpperCase(), new BigDecimal("69.12"), false);
            new CuentaJpaController(emf).create(cuenta);
        }
        cuenta = new CuentaJpaController(emf).findCuenta(7L);
        if (cuenta == null) {
            cuenta = new Cuenta(7L, "CUOTA SOCIETARIA NO RADICADO".toUpperCase(), new BigDecimal("150"), false);
            new CuentaJpaController(emf).create(cuenta);
        }
        cuenta = new CuentaJpaController(emf).findCuenta(8L);
        if (cuenta == null) {
            cuenta = new Cuenta(8L, "PAGO POR CIRCULO".toUpperCase(), new BigDecimal("120"), true);
            new CuentaJpaController(emf).create(cuenta);
        }
        cuenta = new CuentaJpaController(emf).findCuenta(11L);
        if (cuenta == null) {
            cuenta = new Cuenta(11L, "Cuota Societaria NO RADICADO Junio".toUpperCase(), new BigDecimal("345.6"), false);
            new CuentaJpaController(emf).create(cuenta);
        }
        cuenta = new CuentaJpaController(emf).findCuenta(3L);
        if (cuenta == null) {
            cuenta = new Cuenta(3L, "POR DEBITO BANCARIO".toUpperCase(), new BigDecimal("120"), true);
            new CuentaJpaController(emf).create(cuenta);
        }
        cuenta = new CuentaJpaController(emf).findCuenta(12L);
        if (cuenta == null) {
            cuenta = new Cuenta(12L, "POR DEBITO BANCARIO".toUpperCase(), new BigDecimal("120"), true);
            new CuentaJpaController(emf).create(cuenta);
        }
        cuenta = new CuentaJpaController(emf).findCuenta(6L);
        if (cuenta == null) {
            cuenta = new Cuenta(6L, "CORRECCION DE CAJA".toUpperCase(), new BigDecimal("0"), false);
            new CuentaJpaController(emf).create(cuenta);
        }

    }

    private void crearTipoMedico() {
        TipoMedico tipoMedico = new TipoMedicoJpaController(emf).findTipoMedico(1L);
        if (tipoMedico == null) {
            tipoMedico = new TipoMedico(1L, "Activo", true, true, "Activo");
            new TipoMedicoJpaController(emf).create(tipoMedico);
        }
        tipoMedico = new TipoMedicoJpaController(emf).findTipoMedico(2L);
        if (tipoMedico == null) {
            tipoMedico = new TipoMedico(2L, "Baja", false, false, "Cuota Minima/Inactivo");
            new TipoMedicoJpaController(emf).create(tipoMedico);
        }

        tipoMedico = new TipoMedicoJpaController(emf).findTipoMedico(3L);
        if (tipoMedico == null) {
            tipoMedico = new TipoMedico(3L, "Baja Solicitada", false, false, "Vitalicio Activo");
            new TipoMedicoJpaController(emf).create(tipoMedico);
        }

        tipoMedico = new TipoMedicoJpaController(emf).findTipoMedico(4L);
        if (tipoMedico == null) {
            tipoMedico = new TipoMedico(4L, "Inabilitado", false, false, "Vitalicio Pasivo (s/Caja Compnsd)");
            new TipoMedicoJpaController(emf).create(tipoMedico);
        }

        tipoMedico = new TipoMedicoJpaController(emf).findTipoMedico(5L);
        if (tipoMedico == null) {
            tipoMedico = new TipoMedico(5L, "Inactivo", true, false, "Centros - Clínicas - Sanatorios");
            new TipoMedicoJpaController(emf).create(tipoMedico);
        }
        tipoMedico = new TipoMedicoJpaController(emf).findTipoMedico(6L);
        if (tipoMedico == null) {
            tipoMedico = new TipoMedico(6L, "RESD AF", false, false, "Baja");
            new TipoMedicoJpaController(emf).create(tipoMedico);
        }
        tipoMedico = new TipoMedicoJpaController(emf).findTipoMedico(7L);
        if (tipoMedico == null) {
            tipoMedico = new TipoMedico(7L, "TES", false, false, "Adherente");
            new TipoMedicoJpaController(emf).create(tipoMedico);
        }
        tipoMedico = new TipoMedicoJpaController(emf).findTipoMedico(8L);
        if (tipoMedico == null) {
            tipoMedico = new TipoMedico(8L, "Anulado", false, false, "Suspendido");
            new TipoMedicoJpaController(emf).create(tipoMedico);
        }
        tipoMedico = new TipoMedicoJpaController(emf).findTipoMedico(9L);
        if (tipoMedico == null) {
            tipoMedico = new TipoMedico(9L, "Fallecido", false, false, "Expulsado");
            new TipoMedicoJpaController(emf).create(tipoMedico);
        }
        tipoMedico = new TipoMedicoJpaController(emf).findTipoMedico(10L);
        if (tipoMedico == null) {
            tipoMedico = new TipoMedico(10L, "CANCE", false, false, "Fallecido");
            new TipoMedicoJpaController(emf).create(tipoMedico);
        }
        tipoMedico = new TipoMedicoJpaController(emf).findTipoMedico(11L);
        if (tipoMedico == null) {
            tipoMedico = new TipoMedico(11L, "Ninguno", true, true, "Vitalicio Pasivo (c/Caja Compnsd)");
            new TipoMedicoJpaController(emf).create(tipoMedico);
        }
        tipoMedico = new TipoMedicoJpaController(emf).findTipoMedico(12L);
        if (tipoMedico == null) {
            tipoMedico = new TipoMedico(12L, "Solicitante", false, false, "Solicitante");
            new TipoMedicoJpaController(emf).create(tipoMedico);
        }
        tipoMedico = new TipoMedicoJpaController(emf).findTipoMedico(13L);
        if (tipoMedico == null) {
            tipoMedico = new TipoMedico(13L, "No radicado", true, true, "");
            new TipoMedicoJpaController(emf).create(tipoMedico);
        }
        tipoMedico = new TipoMedicoJpaController(emf).findTipoMedico(14L);
        if (tipoMedico == null) {
            tipoMedico = new TipoMedico(14L, "Suspendido", false, false, "");
            new TipoMedicoJpaController(emf).create(tipoMedico);
        }
    }

    private void crearUsuario() {
        Usuario usuario = new UsuarioJpaController(emf).findUsuario(1L);
        if (usuario == null) {
            usuario = new Usuario();
            usuario.setId(1L);
            usuario.setNombre("Administrador");
            usuario.setApellido("Administrador");
            usuario.setUsuario("admin");
            usuario.setPassword("0478721f1106c2a631a90181bac7efc77767a3903eb9220687bff8a14e940fa7");//hugo
            new UsuarioJpaController(emf).create(usuario);
        }
    }

    private void crearOrganismo() {
        Organismo organismo = new OrganismoJpaController(emf).findOrganismo(1L);
        if (organismo == null) {
            organismo = new Organismo();
            organismo.setId(1L);
            organismo.setDescripcion("COLEGIO MEDICO");
            new OrganismoJpaController(emf).create(organismo);

        }
        organismo = new OrganismoJpaController(emf).findOrganismo(2L);
        if (organismo == null) {
            organismo = new Organismo();
            organismo.setId(2L);
            organismo.setDescripcion("CIRCULO MEDICO");
            new OrganismoJpaController(emf).create(organismo);
        }
        organismo = new OrganismoJpaController(emf).findOrganismo(3L);
        if (organismo == null) {
            organismo = new Organismo();
            organismo.setId(3L);
            organismo.setDescripcion("ECA");
            new OrganismoJpaController(emf).create(organismo);
        }
        organismo = new OrganismoJpaController(emf).findOrganismo(4L);
        if (organismo == null) {
            organismo = new Organismo();
            organismo.setId(4L);
            organismo.setDescripcion("DESCUENTO POR PLANILLA");
            new OrganismoJpaController(emf).create(organismo);
        }
    }

    private void crearTipoIngreso() {
        TipoDeIngreso tdi = new TipoDeIngresoJpaController(emf).findTipoDeIngreso(1L);
        if (tdi == null) {
            tdi = new TipoDeIngreso();
            tdi.setId(1L);
            tdi.setDescripcion("PAGO");
            new TipoDeIngresoJpaController(emf).create(tdi);
        }
        tdi = new TipoDeIngresoJpaController(emf).findTipoDeIngreso(2L);
        if (tdi == null) {
            tdi = new TipoDeIngreso();
            tdi.setId(2L);
            tdi.setDescripcion("PLAN DE PAGO");
            new TipoDeIngresoJpaController(emf).create(tdi);
        }
        tdi = new TipoDeIngresoJpaController(emf).findTipoDeIngreso(3L);
        if (tdi == null) {
            tdi = new TipoDeIngreso();
            tdi.setId(3L);
            tdi.setDescripcion("MATRICULA");
            new TipoDeIngresoJpaController(emf).create(tdi);
        }
        tdi = new TipoDeIngresoJpaController(emf).findTipoDeIngreso(4L);
        if (tdi == null) {
            tdi = new TipoDeIngreso();
            tdi.setId(4L);
            tdi.setDescripcion("RECERTIFICACION");
            new TipoDeIngresoJpaController(emf).create(tdi);
        }
        tdi = new TipoDeIngresoJpaController(emf).findTipoDeIngreso(5L);
        if (tdi == null) {
            tdi = new TipoDeIngreso();
            tdi.setId(5L);
            tdi.setDescripcion("MATRICULA");
            new TipoDeIngresoJpaController(emf).create(tdi);
        }
        tdi = new TipoDeIngresoJpaController(emf).findTipoDeIngreso(6L);
        if (tdi == null) {
            tdi = new TipoDeIngreso();
            tdi.setId(6L);
            tdi.setDescripcion("OTRO");
            new TipoDeIngresoJpaController(emf).create(tdi);
        }
    }

    private void crearTipoEgreso() {
        TipoDeEgreso tde = new TipoDeEgresoJpaController(emf).findTipoDeEgreso(1L);
        if (tde == null) {
            tde = new TipoDeEgreso();
            tde.setId(1L);
            tde.setDescripcion("BANCO");
            new TipoDeEgresoJpaController(emf).create(tde);
        }
        tde = new TipoDeEgresoJpaController(emf).findTipoDeEgreso(2L);
        if (tde == null) {
            tde = new TipoDeEgreso();
            tde.setId(2L);
            tde.setDescripcion("OTRO");
            new TipoDeEgresoJpaController(emf).create(tde);
        }

    }

    private void crearTipoDocumento() {
        TipoDocumento td = new TipoDocumentoJpaController(emf).findTipoDocumento(1L);
        if (td == null) {
            td = new TipoDocumento();
            td.setId(1L);
            td.setDescripcion("DNI");
            new TipoDocumentoJpaController(emf).create(td);
        }

    }
}
