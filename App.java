import java.io.*;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Ingrese el número de marcos de página: ");
        int numMarcos = scanner.nextInt();

        PageTable pageTable = new PageTable(numMarcos); 
        FaultsCounter faultsCounter = new FaultsCounter();
        Imagen imagenModificada = null;

        while (true) {
            System.out.println("Menú:");
            System.out.println("1. Generar referencias");
            System.out.println("2. Calcular hits y fallas de página");
            System.out.println("3. Salir");
            System.out.print("Seleccione una opción: ");
            int opcion = scanner.nextInt();

            switch (opcion) {
                case 1:
                    System.out.print("Ingrese el tamaño de página: ");
                    int tamanoPagina = scanner.nextInt();
                    System.out.print("Ingrese el nombre del archivo BMP con el mensaje escondido: ");
                    String archivoImagen = scanner.next();
                    imagenModificada = new Imagen(archivoImagen);

                    generarReferencias(imagenModificada, tamanoPagina);
                    break;

                case 2:
                    System.out.print("Ingrese el nombre del archivo de referencias: ");
                    String archivoReferencias = scanner.next();
                    simularPaginacion(archivoReferencias, numMarcos);
                    break;

                case 3:
                    System.out.println("Saliendo del programa.");
                    scanner.close();
                    System.exit(0);
                    break;

                default:
                    System.out.println("Opción no válida. Intente de nuevo.");
            }
        }
    }

    // Método para generar referencias desde la imagen BMP
    public static void generarReferencias(Imagen imagen, int tamanoPagina) {
        try {
            FileWriter writer = new FileWriter("referencias.txt");
            int filas = imagen.getAlto();
            int columnas = imagen.getAncho();
            int tamanoMensaje = imagen.leerLongitud();
    
            // Calcular número de páginas necesarias
            int totalBytes = filas * columnas * 3 + tamanoMensaje;
            int paginasVirtuales = (int) Math.ceil((double) totalBytes / tamanoPagina);
    
            // Escribir cabecera del archivo de referencias
            writer.write("TP=" + tamanoPagina + "\n");
            writer.write("NF=" + filas + "\n");
            writer.write("NC=" + columnas + "\n");
            writer.write("NR=" + totalBytes + "\n");
            writer.write("NP=" + paginasVirtuales + "\n");
    
            // Escribir referencias generadas
            for (int i = 0; i < filas; i++) {
                for (int j = 0; j < columnas; j++) {
                    writer.write("Imagen[" + i + "][" + j + "].R," + (i * columnas + j) / tamanoPagina + "," + (i * columnas + j) % tamanoPagina + ",R\n");
                    writer.write("Imagen[" + i + "][" + j + "].G," + (i * columnas + j) / tamanoPagina + "," + (i * columnas + j) % tamanoPagina + ",R\n");
                    writer.write("Imagen[" + i + "][" + j + "].B," + (i * columnas + j) / tamanoPagina + "," + (i * columnas + j) % tamanoPagina + ",R\n");
                }
            }
    
            // Referencias para el mensaje
            for (int k = 0; k < tamanoMensaje; k++) {
                writer.write("Mensaje[" + k + "]," + ((filas * columnas * 3) + k) / tamanoPagina + "," + ((filas * columnas * 3) + k) % tamanoPagina + ",W\n");
            }
    
            writer.close();
            System.out.println("Referencias generadas y guardadas en 'referencias.txt'.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void simularPaginacion(String archivoReferencias, int numMarcos) {
        PageTable pageTable = new PageTable(numMarcos);
        FaultsCounter faultsCounter = new FaultsCounter();
        
        // Variables para calcular tiempos
        long tiempoTotal = 0;
        long tiempoHits = 25;  // ns
        long tiempoMisses = 10_000_000;  // 10 ms en ns
    
        try {
            BufferedReader reader = new BufferedReader(new FileReader(archivoReferencias));
            String linea;
            while ((linea = reader.readLine()) != null) {
                // Procesar cada línea del archivo de referencias
                if (linea.startsWith("Imagen") || linea.startsWith("Mensaje")) {
                    String[] partes = linea.split(",");
                    int paginaVirtual = Integer.parseInt(partes[1]);
    
                    // Intentamos cargar la página y verificamos si es un hit o un fallo de página
                    boolean hit = pageTable.loadPage(paginaVirtual);
                    if (hit) {
                        faultsCounter.countHit();
                        tiempoTotal += tiempoHits;
                    } else {
                        faultsCounter.countFault();
                        tiempoTotal += tiempoMisses;
                    }
                }
            }
    
            reader.close();
            System.out.println("Simulación completada.");
            System.out.println("Total de fallas de página: " + faultsCounter.getFaults());
            System.out.println("Total de hits: " + faultsCounter.getHits());
            System.out.println("Tiempo total de acceso: " + tiempoTotal + " nanosegundos");
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
}
