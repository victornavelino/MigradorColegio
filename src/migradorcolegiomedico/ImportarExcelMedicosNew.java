/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migradorcolegiomedico;

import Controladores.BancoJpaController;
import Controladores.CuentaJpaController;
import Controladores.DepartamentoJpaController;
import Controladores.EspecialidadJpaController;
import Controladores.EspecializacionJpaController;
import Controladores.EstadoCivilJpaController;
import Controladores.LocalidadJpaController;
import Controladores.OrganismoJpaController;
import Controladores.PagoJpaController;
import Controladores.ProvinciaJpaController;
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
import Entidades.Localidad.Departamento;
import Entidades.Localidad.Localidad;
import Entidades.Localidad.Provincia;
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
import Facades.EspecialidadFacade;
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
public class ImportarExcelMedicosNew {

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("MigradorColegioMedicoPU");
    SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
    String archivo = "ARCHIVO.xls";

    public static void main(String arg[]) {
        System.out.println("Importador de Archivos:");
        ImportarExcelMedicosNew excel = new ImportarExcelMedicosNew();
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
        excel.crearEstadosCiviles();
        excel.crearProvincias();
        excel.crearDepartamentos();
        excel.crearLocalidades();
        excel.crearUniversidades();
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

        JFileChooser fileChooser = new FileChooserTop();

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
            System.out.println("Importando Legajo Medico  ...");
            String ruta = fileChooser.getSelectedFile().getAbsolutePath() + File.separator + archivo;
            File selectedFile = new File(ruta);
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
            boolean flag = true;
            InputStream in = null;
            in = new FileInputStream(selectedFile);

            WorkbookSettings ws = new WorkbookSettings();
            ws.setEncoding("Cp1252");
            Workbook workbook = Workbook.getWorkbook(in, ws);
            Sheet sheet = workbook.getSheet(20);
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
                    UnidadFormadora unidadFormadora = new UnidadFormadora();
                    List<Telefono> telefonos = new ArrayList<>();

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
                                medico.getPersona().setFechaNacimiento(formatoFecha.parse(dato));
                            } catch (Exception e) {
                            }
                            break;
                        case "4":
                            try {
                                EstadoCivil civilBuscado = new EstadoCivilJpaController(emf).findEstadoCivil(Long.parseLong(dato));

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
                                String replace = dato.replace(".", "").replace("'", "");
                                long parseLong = Long.parseLong(replace);
                                medico.getPersona().getDocumentoIdentidad().setNumero(parseLong);
                            } catch (NumberFormatException numberFormatException) {
                            }
                            break;
                        case "7":
                            //Nacionalidad
                            break;
                        case "8":
                            //Provincia
                            medico.getPersona().setDomicilio(new Domicilio());
                            if (!dato.contains("NULL") && !dato.isEmpty()) {
                                try {
                                    Localidad localidad = new LocalidadJpaController(emf).findLocalidad(100 + Long.parseLong(dato));
                                    medico.getPersona().getDomicilio().setLocalidad(localidad);
                                } catch (Exception e) {
                                }
                            }
                            break;
                        case "9":
                            //Departamento
                            if (!dato.contains("NULL") && !dato.isEmpty()) {
                                try {
                                    Localidad localidad = new LocalidadJpaController(emf).findLocalidad(Long.parseLong(dato));
                                    medico.getPersona().getDomicilio().setLocalidad(localidad);
                                } catch (Exception e) {
                                }
                            }//le cargo la localidad que asigne a cada departamento, despues agregamos las que faltan
                            break;

                        case "10":
                            //Localidad

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
                            telefonos.add(new Telefono());
                            try {
                                TipoTelefono tipoTelefono = new TipoTelefonoJpaController(emf).findTipoTelefono(1L);
                                telefonos.get(0).setTipoTelefono(tipoTelefono);
                            } catch (Exception e) {
                            }
                            telefonos.get(0).setNumero(dato);

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
                            telefonos.add(t);
                            medico.getPersona().setTelefonos(telefonos);//falta agregar el tipo

                            break;
                        case "19":
                            //EMAIL
                            List<CorreoElectronico> ces = new ArrayList<>();

                            if (!dato.isEmpty()) {
                                ces.add(new CorreoElectronico());
                                ces.get(0).setDireccion(dato);
                            }
                            medico.getPersona().setCorreosElectronicos(ces);

                            break;
                        case "20": {
                            try {
                                //FECHA INSCRIPCION
                                if (!dato.contains("NULL") && !dato.isEmpty()) {
                                    Date parse = formatoFecha.parse(dato);
                                    medico.setFechaInscripcion(parse);
                                }
                            } catch (ParseException ex) {
                                Logger.getLogger(ImportarExcelMedicosNew.class.getName()).log(Level.SEVERE, null, ex);
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
                            break;
                        case "23": {
                            try {
                                //FECHA RECIBIDO
                                if (!dato.contains("NULL") && !dato.isEmpty()) {
                                    Date parse = formatoFecha.parse(dato);
                                    medico.setFechaRecibido(parse);
                                }
                            } catch (ParseException ex) {
                                Logger.getLogger(ImportarExcelMedicosNew.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        break;
                        case "24":
                            //UNIVERSIDAD
                            try {
                                if (!dato.contains("NULL")) {
                                    if ("0".equals(dato)) {
                                        dato = "7";
                                    }
                                    unidadFormadora = new UnidadFormadoraJpaController(emf).findUnidadFormadora(Long.parseLong(dato)); //TODO aca hay que validar por nombre
                                    if (unidadFormadora == null) {
                                        unidadFormadora = new UnidadFormadora();
                                        unidadFormadora.setId(Long.parseLong(dato));
                                        unidadFormadora.setDescripcion(dato);
                                        new UnidadFormadoraJpaController(emf).create(unidadFormadora);
                                    }
                                    medico.setUnidadFormadora(unidadFormadora);
                                }
                            } catch (NumberFormatException numberFormatException) {
                            }
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
                            break;
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
                            try {
                                if (!dato.contains("NULL") && !dato.isEmpty()) {
                                    Date parse = formatoFecha.parse(dato);
                                    medico.setFechaBaja(parse);
                                }
                            } catch (ParseException ex) {
                                Logger.getLogger(ImportarExcelMedicosNew.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            break;
                        case "32":
                            //MOTIVOBAJA
                            if (!dato.contains("NULL") && !dato.isEmpty()) {
                                medico.setMotivoBaja(dato);
                            }
                            break;
                        case "33":
                            //MATRICULANACIONAL
                            break;
                        case "34":
                            //NROINSCRIPCION
                            break;
                        case "35":
                            //TIPOSOCIO
                            try {
                                TipoMedico tipoMedico = new TipoMedicoJpaController(emf).findTipoMedico(Long.parseLong(dato));
                                medico.setTipoSocio(tipoMedico);
                            } catch (NumberFormatException numberFormatException) {
                            }
                            break;
                        case "36":
                            //ORGANISMO
                            try {
                                Organismo organismo = new OrganismoJpaController(emf).findOrganismo(Long.parseLong(dato));
                                medico.setOrganismo(organismo);
                            } catch (NumberFormatException numberFormatException) {
                            }
                            break;
                        case "37":
                            //LIBROINSCRIPCION
                            if (!dato.contains("NULL") && !dato.isEmpty()) {
                                try {
                                    medico.setLibro(Integer.valueOf(dato));
                                } catch (NumberFormatException numberFormatException) {
                                }
                            }
                            break;
                        case "38":
                            //FOLIOINSCRIPCION
                            if (!dato.contains("NULL") && !dato.isEmpty()) {
                                try {
                                    medico.setFolio(Integer.valueOf(dato));
                                } catch (NumberFormatException numberFormatException) {
                                }
                            }
                            break;
                        case "39":
                            //FECHATITULO
                            try {
                                //FECHA INSCRIPCION
                                if (!dato.contains("NULL") && !dato.isEmpty()) {
                                    Date parse = formatoFecha.parse(dato);
                                    medico.setFechaTitulo(parse);
                                }
                            } catch (ParseException ex) {
                                Logger.getLogger(ImportarExcelMedicosNew.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            break;
                    }//fin switch

                }//fin for columnas
                MedicoFacade.getInstance().alta(medico);

            } //fin for filas
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ImportarExcelMedicosNew.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ImportarExcelMedicosNew.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BiffException ex) {
            Logger.getLogger(ImportarExcelMedicosNew.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void importarEspecializacionMedica(JFileChooser fileChooser) {
        try {
            System.out.println("Importando Especialización Medica  ...");
            String ruta = fileChooser.getSelectedFile().getAbsolutePath() + File.separator + archivo;
            File selectedFile = new File(ruta);
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
            boolean flag = true;
            InputStream in = null;
            in = new FileInputStream(selectedFile);

            WorkbookSettings ws = new WorkbookSettings();
            ws.setEncoding("Cp1252");
            Workbook workbook = Workbook.getWorkbook(in, ws);
            Sheet sheet = workbook.getSheet(13);
            String dato;
            // Recorre cada fila de la  hoja
            for (int fila = 1; fila < sheet.getRows(); fila++) {
                Especializacion especializacion = new Especializacion();
                Especialidad especialidad = null;
                UnidadFormadora unidadFormadora = null;
                Medico medico;
                boolean guarda = false;

                for (int columna = 0; columna < sheet.getColumns(); columna++) { // Recorre  cada columna
                    dato = sheet.getCell(columna, fila).getContents();
                    //declar clases comunes
                    switch (String.valueOf(columna)) {
                        case "0":
                            //Matricula
                            try {
                                if (!dato.contains("NULL") && !dato.isEmpty()) {
                                    medico = MedicoFacade.getInstance().buscarPorMatricula(Integer.parseInt(dato));
                                    especializacion.setMedico(medico);
                                }
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
                                if (!dato.contains("NULL") && !dato.isEmpty() && !"0".equals(dato)) {
                                    especialidad = EspecialidadFacade.getInstance().buscarPorCodigo(Long.parseLong(dato));
                                    guarda = true;
                                }

                            } catch (Exception e) {
                            }
                            if (!dato.contains("NULL") && !dato.isEmpty()) {
                                especializacion.setEspecialidad(especialidad);
                            }
                            break;
                        case "3":
                            //MATRICULAESPECIALIDAD
                            try {
                                if (!dato.contains("NULL") && !dato.isEmpty() && !"0".equals(dato)) {
                                    especializacion.setMatriculaEspecialidad(Integer.parseInt(dato));
                                    guarda = true;
                                } else {
                                    guarda = false;
                                }
                            } catch (Exception e) {
                            }
                            break;
                        case "4":
                            //FECHAMATRICULACION
                            try {
                                if (!dato.contains("NULL") && !dato.isEmpty()) {

                                    especializacion.setFechaMatriculacion(formatoFecha.parse(dato));
                                }
                            } catch (Exception e) {
                            }
                            break;
                        case "5":
                            //LIBRO	
                            try {
                                if (!dato.contains("NULL") && !dato.isEmpty()) {
                                    especializacion.setLibro(dato);
                                }
                            } catch (Exception e) {
                            }
                            break;
                        case "6":
                            //FOLIO
                            try {
                                if (!dato.contains("NULL") && !dato.isEmpty()) {
                                    especializacion.setFolio(dato);
                                }
                            } catch (Exception e) {
                            }
                            break;
                        case "7":
                            //	UNIVERSIDAD
                            try {
                                if (!dato.contains("NULL") && !dato.isEmpty()) {
                                    if ("0".equals(dato)) {
                                        dato = "7";

                                    }
                                    unidadFormadora = new UnidadFormadoraJpaController(emf).findUnidadFormadora(Long.parseLong(dato)); //TODO aca hay que validar por nombre

                                }
                            } catch (NumberFormatException numberFormatException) {
                            }
                            try {
                                if (!dato.contains("NULL") && !dato.isEmpty()) {
                                    especializacion.setUnidadFormadora(unidadFormadora);
                                }
                            } catch (Exception e) {
                            }
                            break;

                    }//fin switch

                }//fin for columnas
                if (guarda) {
                    new EspecializacionJpaController(emf).create(especializacion);

                }
            }//fin for filas
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ImportarExcelMedicosNew.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ImportarExcelMedicosNew.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (BiffException ex) {
            Logger.getLogger(ImportarExcelMedicosNew.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ImportarExcelMedicosNew.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void importarEspecialidad(JFileChooser fileChooser) {
        try {
            System.out.println("Importando Especialidad  ...");
            String ruta = fileChooser.getSelectedFile().getAbsolutePath() + File.separator + archivo;
            File selectedFile = new File(ruta);
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
            boolean flag = true;
            InputStream in = null;
            in = new FileInputStream(selectedFile);

            WorkbookSettings ws = new WorkbookSettings();
            ws.setEncoding("Cp1252");
            Workbook workbook = Workbook.getWorkbook(in, ws);
            Sheet sheet = workbook.getSheet(12);
            String dato;
            // Recorre cada fila de la  hoja
            for (int fila = 1; fila < sheet.getRows(); fila++) {
                Especialidad especialidad = new Especialidad();
                for (int columna = 0; columna < sheet.getColumns(); columna++) { // Recorre  cada fila
                    dato = sheet.getCell(columna, fila).getContents();
                    Cell cell = sheet.getCell(columna, fila);
                    //declar clases comunes
                    switch (String.valueOf(columna)) {
                        case "0":
                            //ID	
                            try {
                                NumberCell numberCell = (NumberCell) cell;
                                double d = numberCell.getValue();
                                especialidad.setCodigoEspecilidad((long) d);
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
            Logger.getLogger(ImportarExcelMedicosNew.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ImportarExcelMedicosNew.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (BiffException ex) {
            Logger.getLogger(ImportarExcelMedicosNew.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ImportarExcelMedicosNew.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void importarPagos(JFileChooser fileChooser) {
        try {
            System.out.println("Importando pagos  ...");
            String ruta = fileChooser.getSelectedFile().getAbsolutePath() + File.separator + archivo;
            File selectedFile = new File(ruta);
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
            boolean flag = true;
            InputStream in = null;
            in = new FileInputStream(selectedFile);

            WorkbookSettings ws = new WorkbookSettings();
            ws.setEncoding("Cp1252");
            Workbook workbook = Workbook.getWorkbook(in, ws);
            Sheet sheet = workbook.getSheet(10);
            String dato;
            // Recorre cada fila de la  hoja
            for (int fila = 1; fila < sheet.getRows(); fila++) {
                Pago pago = new Pago();
                String fecha = new String();
                Medico medico = new Medico();
                for (int columna = 0; columna < sheet.getColumns(); columna++) { // Recorre  cada fila
                    dato = sheet.getCell(columna, fila).getContents();
                    Cell cell = sheet.getCell(columna, fila);
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
                                pago.setNroRecibo(dato.replace("'", ""));

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
                            break;

                        case "5":
                            //MES	
                            try {
                                if (null != dato) {
                                    pago.setMes(Integer.valueOf(dato));
                                }
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
                                    NumberCell numberCell = (NumberCell) cell;
                                    pago.setImporte(new BigDecimal(numberCell.getValue()));
                                }

                            } catch (Exception e) {
                                System.out.println("Error importando: " + dato + " , E: " + e);
                            }
                            break;
                        case "9":
                            //IDNROCOMPROBANTE	 									
                            try {
                                if (!dato.contains("NULL")) {
                                    //         pago.setNroRecibo(dato);
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
                                    fecha = dato;
                                }
                            } catch (Exception e) {
                            }
                            break;
                        case "12":
                            //	HORAREGISTRO// 											
                            try {
                                if (!dato.contains("NULL")) {
                                    if (cell.getType() == CellType.DATE) {
                                        DateCell dateCell = (DateCell) cell;
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
                                        fecha += " ";
                                        fecha += simpleDateFormat.format(dateCell.getDate());

                                        DateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                        Date date = inputFormat.parse(fecha);

                                        pago.setFechaPago(date);
                                    }
                                }
                            } catch (Exception e) {
                                System.out.println("error Parse:  " + e + " fila: " + fila + " dato: " + dato);
                            }
                            break;
                    }
                }
                new PagoJpaController(emf).create(pago);

            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ImportarExcelMedicosNew.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ImportarExcelMedicosNew.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (BiffException ex) {
            Logger.getLogger(ImportarExcelMedicosNew.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ImportarExcelMedicosNew.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void importarRecertificacion(JFileChooser fileChooser) {
        try {
            System.out.println("Importando recertificacion  ...");
            String ruta = fileChooser.getSelectedFile().getAbsolutePath() + File.separator + archivo;
            File selectedFile = new File(ruta);
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
            boolean flag = true;
            InputStream in = null;
            in = new FileInputStream(selectedFile);
            WorkbookSettings ws = new WorkbookSettings();
            ws.setEncoding("Cp1252");
            Workbook workbook = Workbook.getWorkbook(in, ws);
            Sheet sheet = workbook.getSheet(29);
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
            Logger.getLogger(ImportarExcelMedicosNew.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ImportarExcelMedicosNew.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (BiffException ex) {
            Logger.getLogger(ImportarExcelMedicosNew.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ImportarExcelMedicosNew.class
                    .getName()).log(Level.SEVERE, null, ex);
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
            tipoMedico = new TipoMedico(5L, "Inactivo".toUpperCase(), true, false, "Centros - Clínicas - Sanatorios");
            new TipoMedicoJpaController(emf).create(tipoMedico);
        }
        tipoMedico = new TipoMedicoJpaController(emf).findTipoMedico(6L);
        if (tipoMedico == null) {
            tipoMedico = new TipoMedico(6L, "RESD AF".toUpperCase(), false, false, "Baja");
            new TipoMedicoJpaController(emf).create(tipoMedico);
        }
        tipoMedico = new TipoMedicoJpaController(emf).findTipoMedico(7L);
        if (tipoMedico == null) {
            tipoMedico = new TipoMedico(7L, "TES".toUpperCase(), false, false, "Adherente");
            new TipoMedicoJpaController(emf).create(tipoMedico);
        }
        tipoMedico = new TipoMedicoJpaController(emf).findTipoMedico(8L);
        if (tipoMedico == null) {
            tipoMedico = new TipoMedico(8L, "Anulado".toUpperCase(), false, false, "Suspendido");
            new TipoMedicoJpaController(emf).create(tipoMedico);
        }
        tipoMedico = new TipoMedicoJpaController(emf).findTipoMedico(9L);
        if (tipoMedico == null) {
            tipoMedico = new TipoMedico(9L, "Fallecido".toUpperCase(), false, false, "Expulsado");
            new TipoMedicoJpaController(emf).create(tipoMedico);
        }
        tipoMedico = new TipoMedicoJpaController(emf).findTipoMedico(10L);
        if (tipoMedico == null) {
            tipoMedico = new TipoMedico(10L, "CANCE".toUpperCase(), false, false, "Fallecido");
            new TipoMedicoJpaController(emf).create(tipoMedico);
        }
        tipoMedico = new TipoMedicoJpaController(emf).findTipoMedico(11L);
        if (tipoMedico == null) {
            tipoMedico = new TipoMedico(11L, "Ninguno".toUpperCase(), true, true, "Vitalicio Pasivo (c/Caja Compnsd)");
            new TipoMedicoJpaController(emf).create(tipoMedico);
        }
        tipoMedico = new TipoMedicoJpaController(emf).findTipoMedico(12L);
        if (tipoMedico == null) {
            tipoMedico = new TipoMedico(12L, "Solicitante".toUpperCase(), false, false, "Solicitante");
            new TipoMedicoJpaController(emf).create(tipoMedico);
        }
        tipoMedico = new TipoMedicoJpaController(emf).findTipoMedico(13L);
        if (tipoMedico == null) {
            tipoMedico = new TipoMedico(13L, "No radicado".toUpperCase(), true, true, "");
            new TipoMedicoJpaController(emf).create(tipoMedico);
        }
        tipoMedico = new TipoMedicoJpaController(emf).findTipoMedico(14L);
        if (tipoMedico == null) {
            tipoMedico = new TipoMedico(14L, "Suspendido".toUpperCase(), false, false, "");
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

        crearOrganismoSolo(1L, "Colegio Médico");
        crearOrganismoSolo(2L, "Circulo Médico");
        crearOrganismoSolo(3L, "Gbo Provincial");
        crearOrganismoSolo(4L, "OSEP");
        crearOrganismoSolo(5L, "ECA");
        crearOrganismoSolo(6L, "No Radicado");
        crearOrganismoSolo(7L, "Teso");
        crearOrganismoSolo(8L, "Vital Pas");
        crearOrganismoSolo(9L, "ACO");
        crearOrganismoSolo(10L, "Debito Galicia");

    }

    private void crearOrganismoSolo(Long id, String descripcion) {
        Organismo organismo = new OrganismoJpaController(emf).findOrganismo(id);
        if (organismo == null) {
            organismo = new Organismo();
            organismo.setId(id);
            organismo.setDescripcion(descripcion.toUpperCase());
            new OrganismoJpaController(emf).create(organismo);
        }
    }

    private void crearTipoIngreso() {
        TipoDeIngreso tdi = new TipoDeIngresoJpaController(emf).findTipoDeIngreso(1L);
        if (tdi == null) {
            tdi = new TipoDeIngreso();
            tdi.setId(1L);
            tdi.setDescripcion("MATRICULA");
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
            tdi.setDescripcion("RECERTIFICACION");
            new TipoDeIngresoJpaController(emf).create(tdi);
        }
        tdi = new TipoDeIngresoJpaController(emf).findTipoDeIngreso(4L);
        if (tdi == null) {
            tdi = new TipoDeIngreso();
            tdi.setId(4L);
            tdi.setDescripcion("ESPECIALIZACION");
            new TipoDeIngresoJpaController(emf).create(tdi);
        }
        tdi = new TipoDeIngresoJpaController(emf).findTipoDeIngreso(5L);
        if (tdi == null) {
            tdi = new TipoDeIngreso();
            tdi.setId(5L);
            tdi.setDescripcion("MANTENIMIENTO");
            new TipoDeIngresoJpaController(emf).create(tdi);
        }
        tdi = new TipoDeIngresoJpaController(emf).findTipoDeIngreso(6L);
        if (tdi == null) {
            tdi = new TipoDeIngreso();
            tdi.setId(6L);
            tdi.setDescripcion("CERTIFICADO DE ETICA");
            new TipoDeIngresoJpaController(emf).create(tdi);
        }
        tdi = new TipoDeIngresoJpaController(emf).findTipoDeIngreso(7L);
        if (tdi == null) {
            tdi = new TipoDeIngreso();
            tdi.setId(7L);
            tdi.setDescripcion("OTROS");
            new TipoDeIngresoJpaController(emf).create(tdi);
        }
    }

    private void crearTipoEgreso() {
        TipoDeEgreso tde = new TipoDeEgresoJpaController(emf).findTipoDeEgreso(1L);
        if (tde == null) {
            tde = new TipoDeEgreso();
            tde.setId(1L);
            tde.setDescripcion("CHEQUES");
            new TipoDeEgresoJpaController(emf).create(tde);
        }
        tde = new TipoDeEgresoJpaController(emf).findTipoDeEgreso(2L);
        if (tde == null) {
            tde = new TipoDeEgreso();
            tde.setId(2L);
            tde.setDescripcion("BANCOS");
            new TipoDeEgresoJpaController(emf).create(tde);
        }
        tde = new TipoDeEgresoJpaController(emf).findTipoDeEgreso(3L);
        if (tde == null) {
            tde = new TipoDeEgreso();
            tde.setId(3L);
            tde.setDescripcion("SUELDOS");
            new TipoDeEgresoJpaController(emf).create(tde);
        }
        tde = new TipoDeEgresoJpaController(emf).findTipoDeEgreso(4L);
        if (tde == null) {
            tde = new TipoDeEgreso();
            tde.setId(4L);
            tde.setDescripcion("REFRIGERIOS");
            new TipoDeEgresoJpaController(emf).create(tde);
        }
        tde = new TipoDeEgresoJpaController(emf).findTipoDeEgreso(5L);
        if (tde == null) {
            tde = new TipoDeEgreso();
            tde.setId(5L);
            tde.setDescripcion("REFRIGERIOS");
            new TipoDeEgresoJpaController(emf).create(tde);
        }
        tde = new TipoDeEgresoJpaController(emf).findTipoDeEgreso(6L);
        if (tde == null) {
            tde = new TipoDeEgreso();
            tde.setId(6L);
            tde.setDescripcion("TELEFONO");
            new TipoDeEgresoJpaController(emf).create(tde);
        }
        tde = new TipoDeEgresoJpaController(emf).findTipoDeEgreso(7L);
        if (tde == null) {
            tde = new TipoDeEgreso();
            tde.setId(7L);
            tde.setDescripcion("OTROS");
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

    private void crearEstadosCiviles() {
        crearEstadoCivil(1L, "Casado/a".toUpperCase());
        crearEstadoCivil(2L, "Soltero/a".toUpperCase());
        crearEstadoCivil(3L, "Divorciado/a".toUpperCase());
        crearEstadoCivil(4L, "Viudo/a".toUpperCase());

    }

    private void crearEstadoCivil(Long id, String descripcion) {
        EstadoCivil td = new EstadoCivilJpaController(emf).findEstadoCivil(id);
        if (td == null) {
            td = new EstadoCivil();
            td.setId(id);
            td.setDescripcion(descripcion);
            new EstadoCivilJpaController(emf).create(td);
        }
    }

    private void crearProvincias() {
        crearProvincia(1L, "Catamarca");
        crearProvincia(2L, "Buenos Aires");
        crearProvincia(3L, "Capital Federal");
        crearProvincia(5L, "Chaco");
        crearProvincia(6L, "Chubut");
        crearProvincia(7L, "Cordoba");
        crearProvincia(8L, "Corrientes");
        crearProvincia(9L, "Entre Rios");
        crearProvincia(10L, "Formosa");
        crearProvincia(11L, "Jujuy");
        crearProvincia(12L, "La Pampa");
        crearProvincia(13L, "La Rioja");
        crearProvincia(14L, "Mendoza");
        crearProvincia(15L, "Misiones");
        crearProvincia(16L, "Neuquen");
        crearProvincia(18L, "Rio Negro");
        crearProvincia(19L, "Salta");
        crearProvincia(20L, "San Juan");
        crearProvincia(21L, "San Luis");
        crearProvincia(22L, "Santa Cruz");
        crearProvincia(23L, "Santa Fé");
        crearProvincia(24L, "Sgo. del Estero");
        crearProvincia(25L, "Tierra del Fuego");
        crearProvincia(26L, "Tucumán");
        crearProvincia(29L, "Ninguna");
    }

    private void crearProvincia(Long id, String descripcion) {
        Provincia td = new ProvinciaJpaController(emf).findProvincia(id);
        if (td == null) {
            td = new Provincia();
            td.setId(id);
            td.setDescripcion(descripcion.toUpperCase());
            new ProvinciaJpaController(emf).create(td);
        }
    }

    private void crearDepartamentos() {
        crearDepartamento(1L, "Ancasti", 1L);
        crearDepartamento(2L, "Capital", 1L);
        crearDepartamento(3L, "Pomán", 1L);
        crearDepartamento(4L, "Valle Viejo", 1L);
        crearDepartamento(5L, "Tinogasta", 1L);
        crearDepartamento(6L, "Santa María", 1L);
        crearDepartamento(7L, "Andalgala", 1L);
        crearDepartamento(8L, "Capayan", 1L);
        crearDepartamento(9L, "Belén", 1L);
        crearDepartamento(10L, "Paclin", 1L);
        crearDepartamento(11L, "La Paz", 1L);
        crearDepartamento(12L, "Antofagasta de la Sierra", 1L);
        crearDepartamento(13L, "Fray Mamerto Esquíu", 1L);
        crearDepartamento(14L, "Santa Rosa", 1L);
        crearDepartamento(15L, "Ambato", 1L);
        crearDepartamento(16L, "El Alto", 1L);
        crearDepartamento(17L, "Ninguno", 1L);
        //Crear Departamento de Provincias
        crearDepartamento(101L, "Catamarca", 1L);
        crearDepartamento(102L, "Buenos Aires", 2L);
        crearDepartamento(103L, "Capital Federal", 3L);
        crearDepartamento(105L, "Chaco", 5L);
        crearDepartamento(106L, "Chubut", 6L);
        crearDepartamento(107L, "Cordoba", 7L);
        crearDepartamento(108L, "Corrientes", 8L);
        crearDepartamento(109L, "Entre Rios", 9L);
        crearDepartamento(110L, "Formosa", 10L);
        crearDepartamento(111L, "Jujuy", 11L);
        crearDepartamento(112L, "La Pampa", 12L);
        crearDepartamento(113L, "La Rioja", 13L);
        crearDepartamento(114L, "Mendoza", 14L);
        crearDepartamento(115L, "Misiones", 15L);
        crearDepartamento(116L, "Neuquen", 16L);
        crearDepartamento(118L, "Rio Negro", 18L);
        crearDepartamento(119L, "Salta", 19L);
        crearDepartamento(120L, "San Juan", 20L);
        crearDepartamento(121L, "San Luis", 21L);
        crearDepartamento(122L, "Santa Cruz", 22L);
        crearDepartamento(123L, "Santa Fé", 23L);
        crearDepartamento(124L, "Sgo. del Estero", 24L);
        crearDepartamento(125L, "Tierra del Fuego", 25L);
        crearDepartamento(126L, "Tucumán", 26L);
        crearDepartamento(129L, "Ninguna", 29L);
    }

    private void crearDepartamento(Long id, String descripcion, Long provincia) {
        Departamento td = new DepartamentoJpaController(emf).findDepartamento(id);
        if (td == null) {
            td = new Departamento();
            td.setProvincia(new ProvinciaJpaController(emf).findProvincia(provincia));
            td.setId(id);
            td.setDescripcion(descripcion.toUpperCase());
            new DepartamentoJpaController(emf).create(td);
        }

    }

    private void crearLocalidades() {
        crearLocalidad(1L, "Ancasti");
        crearLocalidad(2L, "SFVC");
        crearLocalidad(3L, "Pomán");
        crearLocalidad(4L, "Valle Viejo");
        crearLocalidad(5L, "Tinogasta");
        crearLocalidad(6L, "Santa María");
        crearLocalidad(7L, "Andalgala");
        crearLocalidad(8L, "Capayan");
        crearLocalidad(9L, "Belén");
        crearLocalidad(10L, "Paclin");
        crearLocalidad(11L, "La Paz");
        crearLocalidad(12L, "Antofagasta de la Sierra");
        crearLocalidad(13L, "Fray Mamerto Esquíu");
        crearLocalidad(14L, "Santa Rosa");
        crearLocalidad(15L, "Ambato");
        crearLocalidad(16L, "El Alto");
        crearLocalidad(17L, "Ninguno");
        //Localidades de Provincias
        crearLocalidad(101L, "Catamarca");
        crearLocalidad(102L, "Buenos Aires");
        crearLocalidad(103L, "Capital Federal");
        crearLocalidad(105L, "Chaco");
        crearLocalidad(106L, "Chubut");
        crearLocalidad(107L, "Cordoba");
        crearLocalidad(108L, "Corrientes");
        crearLocalidad(109L, "Entre Rios");
        crearLocalidad(110L, "Formosa");
        crearLocalidad(111L, "Jujuy");
        crearLocalidad(112L, "La Pampa");
        crearLocalidad(113L, "La Rioja");
        crearLocalidad(114L, "Mendoza");
        crearLocalidad(115L, "Misiones");
        crearLocalidad(116L, "Neuquen");
        crearLocalidad(118L, "Rio Negro");
        crearLocalidad(119L, "Salta");
        crearLocalidad(120L, "San Juan");
        crearLocalidad(121L, "San Luis");
        crearLocalidad(122L, "Santa Cruz");
        crearLocalidad(123L, "Santa Fé");
        crearLocalidad(124L, "Sgo. del Estero");
        crearLocalidad(125L, "Tierra del Fuego");
        crearLocalidad(126L, "Tucumán");
        crearLocalidad(129L, "Ninguna");

    }

    private void crearLocalidad(Long id, String descripcion) {
        Localidad td = new LocalidadJpaController(emf).findLocalidad(id);
        if (td == null) {
            td = new Localidad();
            td.setDepartamento(new DepartamentoJpaController(emf).findDepartamento(id));
            td.setId(id);
            td.setDescripcion(descripcion.toUpperCase());
            new LocalidadJpaController(emf).create(td);
        }

    }

    private void crearUniversidades() {

        crearUniversidad(1L, "Universidad Nacional de Córdoba");
        crearUniversidad(2L, "Universidad Nacional de Tucumán");
        crearUniversidad(3L, "Universidad Nacional de Buenos Aires");
        crearUniversidad(4L, "Universidad Nacional La Plata");
        crearUniversidad(5L, "Universidad Nacional de Rosario");
        crearUniversidad(6L, "Universidad Nacional La Rioja");
        crearUniversidad(7L, "Otras");
        crearUniversidad(8L, "Universidad Privada Blas Pascal");
        crearUniversidad(9L, "Universidad Catolica de Córdoba");
        crearUniversidad(10L, "Universidad Nacional de Salta");
        crearUniversidad(11L, "UNIVERSIDAD NACIONAL DEL NORDESTE");
        crearUniversidad(12L, "UNIVERSIDAD NACIONAL DEL NOROESTE");
        crearUniversidad(13L, "UNIVERSIDAD FAVALORO");
        crearUniversidad(14L, "UNIVERSIDAD ABIERTA INTERAMERICANA");
        crearUniversidad(15L, "UNIV. DE SAN MARTIN DE PORRES  PERU");
        crearUniversidad(16L, "UNIV ITALIANA DE ROSARIO");
        crearUniversidad(17L, "FUNDACION BARCELO");
        crearUniversidad(18L, "ESC. LATINOAMERICANA DE MEDICINA");

    }

    private void crearUniversidad(Long id, String descripcion) {
        UnidadFormadora td = new UnidadFormadoraJpaController(emf).findUnidadFormadora(id);
        if (td == null) {
            td = new UnidadFormadora();
            td.setId(id);
            td.setDescripcion(descripcion.toUpperCase());
            new UnidadFormadoraJpaController(emf).create(td);
        }

    }

}
