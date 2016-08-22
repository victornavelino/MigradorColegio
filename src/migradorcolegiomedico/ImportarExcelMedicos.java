/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migradorcolegiomedico;

import Entidades.Medico.Medico;
import Facades.MedicoFacade;
import java.io.File;
import jxl.*; 
/**
 *
 * @author hugo
 */
public class ImportarExcelMedicos {

    private void leerArchivoExcel(String archivoDestino) {

        try {
            Workbook archivoExcel = Workbook.getWorkbook(new File(archivoDestino));
            System.out.println("Número de Hojas\t"
                    + archivoExcel.getNumberOfSheets());

                Sheet hoja = archivoExcel.getSheet(0);
                int numColumnas = hoja.getColumns();
                int numFilas = hoja.getRows();
                String data;
                System.out.println("Nombre de la Hoja\t"
                        + archivoExcel.getSheet(0).getName());
                for (int fila = 0; fila < numFilas; fila++) { // Recorre cada fila de la  hoja 
                    for (int columna = 0; columna < numColumnas; columna++) { // Recorre  cada                                     
// fila 
                        data = hoja.getCell(columna, fila).getContents();
                        System.out.print(data + " ");

                    }
                    System.out.println("\n");
                }
            
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }

    }

    public static void main(String arg[]) {
        ImportarExcelMedicos excel = new ImportarExcelMedicos();
        excel.leerArchivoExcel("/home/hugo/LEGAJOMEDICO.xls");
        excel.cargar();
    }

    private void cargar() {
       Medico medico=new Medico();
       medico.setMatriculaProfesional("2044");
        MedicoFacade.getInstance().alta(medico);
    }
    
}
