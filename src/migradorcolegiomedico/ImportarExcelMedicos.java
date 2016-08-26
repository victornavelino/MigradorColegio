/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migradorcolegiomedico;

import Controladores.SexoJpaController;
import Entidades.Medico.Medico;
import Entidades.Persona.Persona;
import Facades.MedicoFacade;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import jxl.*;

/**
 *
 * @author hugo
 */
public class ImportarExcelMedicos {

    private Medico medico;
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("MigradorColegioMedicoPU");
    SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");

    private void leerArchivoExcel(String archivoDestino) {

        try {
            Workbook archivoExcel = Workbook.getWorkbook(new File(archivoDestino));
            System.out.println("Número de Hojas\t"
                    + archivoExcel.getNumberOfSheets());

            Sheet hoja = archivoExcel.getSheet(0);
            int numColumnas = hoja.getColumns();
            int numFilas = hoja.getRows();
            String dato;
            System.out.println("Nombre de la Hoja\t"
                    + archivoExcel.getSheet(0).getName());
            // Recorre cada fila de la  hoja 
            for (int fila = 1; fila < numFilas; fila++) {
                medico = new Medico();
                medico.setPersona(new Persona());

                for (int columna = 0; columna < numColumnas; columna++) { // Recorre  cada                                     
// fila 
                    dato = hoja.getCell(columna, fila).getContents();
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
                            if (dato.contains("Masculino")) {
                                medico.getPersona().setSexo(new SexoJpaController(emf).findSexo(1L));
                            }
                            if (dato.contains("Femenino")) {
                                medico.getPersona().setSexo(new SexoJpaController(emf).findSexo(2L));
                            }
                            break;
                        case "3":
                            try {
                                medico.getPersona().setFechaNacimiento(formatoFecha.parse(dato.substring(0, 10)));
                            } catch (Exception e) {
                            }
                            break;
                        case "4":
                            medico.getPersona().setEstadoCivil(null);

                    }
                    System.out.print(dato + " ");

                }
                MedicoFacade.getInstance().alta(medico);
                System.out.println("\n");
            }

        } catch (Exception ioe) {
            ioe.printStackTrace();
        }

    }

    public static void main(String arg[]) {
        ImportarExcelMedicos excel = new ImportarExcelMedicos();
        excel.leerArchivoExcel("/home/nago/LEGAJOMEDICO.xls");
    }

    private void cargar() {
        Medico medico = new Medico();
        medico.setMatriculaProfesional("2044");
        MedicoFacade.getInstance().alta(medico);
    }

}
