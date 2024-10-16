import java.io.*;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Imagen imagenModificada = null;

        while (true) {
            System.out.println("Menú:");
            System.out.println("0. Salir");
            System.out.println("1. Generar referencias");
            System.out.println("2. Calcular hits y fallas de página");
            System.out.println("3. Esconder mensaje en una imagen");
            System.out.println("4. Recuperar mensaje de una imagen");
            System.out.print("Seleccione una opción: ");
            int opcion = scanner.nextInt();

            switch (opcion) {
                case 0:
                    System.out.println("Saliendo del programa.");
                    scanner.close();
                    System.exit(0);
                    break;
                case 1:
                    System.out.print("Ingrese el tamaño de página: ");
                    int tamanoPagina = scanner.nextInt();
                    System.out.print("Ingrese el nombre del archivo BMP con el mensaje escondido: ");
                    String archivoImagen = scanner.next();
                    imagenModificada = new Imagen(archivoImagen);

                    generarReferencias2(imagenModificada, tamanoPagina);
                    break;

                case 2:
                    System.out.print("Ingrese el número de marcos de página: ");
                    int numMarcos = scanner.nextInt();
                    System.out.print("Ingrese el nombre del archivo de referencias: ");
                    String archivoReferencias = scanner.next();
                    simularPaginacion(archivoReferencias, numMarcos);
                    break;

                case 3:
                    System.out.print("Ingrese el nombre de la imagen: ");
                    String archivoImg = scanner.next();
                    imagenModificada = new Imagen(archivoImg);

                    System.out.print("Ingrese el nombre del archivo de texto con el mensaje: ");
                    String archivoMensaje = scanner.next();

                    char[] mensaje = leerArchivoTexto(archivoMensaje);
                    imagenModificada.esconderMensaje(mensaje, mensaje.length);
                    imagenModificada.escribirImagen(archivoImg + "_modificado");

                    System.out.println("Mensaje escondido");
                    break;

                case 4:
                    System.out.print("Ingrese el nombre del archivo BMP con el mensaje escondido: ");
                    String archivo = scanner.next();
                    imagenModificada = new Imagen(archivo);

                    char[] mensajeOculto = imagenModificada.recuperarMensaje();
                    escribirArchivoTexto("Mensaje_oculto_" + archivo, mensajeOculto);

                    System.out.println("Mensaje_oculto_" + archivo + " se creó con exito");
                    break;
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
            int ref = 0;
            int paginasVirtuales = (((filas*columnas*3)+tamanoMensaje)/tamanoPagina);
    
    
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

            writer.write("TP=" + tamanoPagina + "\n");
            writer.write("NF=" + filas + "\n");
            writer.write("NC=" + columnas + "\n");
            writer.write("NR=" + ref + "\n");
            writer.write("NP=" + paginasVirtuales + "\n");

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
    
    //Metodo para leer el archivo de texto
    public static char[] leerArchivoTexto(String rutaArchivo) {
        StringBuilder mensaje = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(rutaArchivo));
            String linea;
            while ((linea = reader.readLine()) != null) {
                mensaje.append(linea);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mensaje.toString().toCharArray(); // Convertir a arreglo de char[]
    }
    
    // Método para escribir el mensaje en un archivo de texto
    public static void escribirArchivoTexto(String rutaArchivo, char[] mensaje) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(rutaArchivo));
            writer.write(mensaje);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void recuperarMensajeReferencias(Imagen imagen, int tamanoPagina) {
        try {
            FileWriter writer = new FileWriter("referencias.txt");
            int filas = imagen.getAlto();
            int columnas = imagen.getAncho();
            int tamanoImagen = filas * columnas * 3;
            int tamanoMensaje = imagen.leerLongitud();
            
            // Calcular número de páginas necesarias
            int ref = 0;
            //int paginasVirtuales = (((filas*columnas*3)+tamanoMensaje)/tamanoPagina);
            int paginasVirtuales = (int) Math.ceil((tamanoImagen)+tamanoMensaje/tamanoPagina);

            writer.write("TP=" + tamanoPagina + "\n");
            writer.write("NF=" + filas + "\n");
            writer.write("NC=" + columnas + "\n");
            writer.write("NR=" + ref + "\n");
            writer.write("NP=" + paginasVirtuales + "\n");

            
            System.out.println("Referencias generadas y guardadas en 'referencias.txt'.");
            int longitud = imagen.leerLongitud(); 
            char[] mensaje = new char[longitud];

            int bytesFila = imagen.getAncho() * 3;
            for (int posCaracter = 0; posCaracter < longitud; posCaracter++) {
                //mensaje[posCaracter] = 0;
                writer.write("Mensaje[" + String.valueOf(posCaracter)+"],"+((tamanoImagen + posCaracter)/tamanoPagina)+","+((tamanoImagen + posCaracter)%tamanoPagina)+","+"W"+ "\n");
                ref++;
                for (int i = 0; i < 8; i++) {
                    int numBytes = 16 + (posCaracter * 8) + i;
                    int fila = numBytes / bytesFila;
                    int col = (numBytes % bytesFila) / 3;
                    int color = (numBytes % bytesFila) % 3;
                    //mensaje[posCaracter] |= (char) ((imagen[fila][col][color] & 1) << i);
                    writer.write("Imagen[" + String.valueOf(posCaracter)+"],"+((tamanoImagen + posCaracter)/tamanoPagina)+","+((tamanoImagen + posCaracter)%tamanoPagina)+","+"R"+ "\n");
                    ref++;
                    writer.write("Mensaje[" + String.valueOf(posCaracter)+"],"+((tamanoImagen + posCaracter)/tamanoPagina)+","+((tamanoImagen + posCaracter)%tamanoPagina)+","+"W"+ "\n");
                    ref++;
                    }
                }
                
                writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void generarReferencias2(Imagen imagen, int tamanoPagina) {
        try {
            FileWriter writer = new FileWriter("referencias.txt");
            int filas = imagen.getAlto();
            int columnas = imagen.getAncho();
            int tamanoImagen = filas * columnas * 3;
            int tamanoMensaje = imagen.leerLongitud();
            
            // Calcular número de páginas necesarias
            int paginasVirtuales = (int) Math.ceil((tamanoImagen/tamanoPagina)+(tamanoMensaje/tamanoPagina));
            // Calcular numeor de referencias necesarias 
            int ref = (tamanoMensaje * 17 + 16);


            writer.write("TP=" + tamanoPagina + "\n");
            writer.write("NF=" + filas + "\n");
            writer.write("NC=" + columnas + "\n");
            writer.write("NR=" + ref + "\n");
            writer.write("NP=" + paginasVirtuales + "\n");

            
            System.out.println("Referencias generadas y guardadas en 'referencias.txt'.");
            int longitud = imagen.leerLongitud(); 
            char[] mensaje = new char[longitud];

            int bytesFila = imagen.getAncho() * 3;
            for (int posCaracter = 0; posCaracter < longitud; posCaracter++) {
                //mensaje[posCaracter] = 0;
                writer.write("Mensaje[" + String.valueOf(posCaracter)+"],"+((tamanoImagen + posCaracter)/tamanoPagina)+","+((tamanoImagen + posCaracter)%tamanoPagina)+","+"W"+ "\n");
                ref++;
                for (int i = 0; i < 8; i++) {
                    int numBytes = 16 + (posCaracter * 8) + i;
                    int fila = numBytes / bytesFila;
                    int col = (numBytes % bytesFila) / 3;
                    int color = (numBytes % bytesFila) % 3;
                    //mensaje[posCaracter] |= (char) ((imagen[fila][col][color] & 1) << i);
                    writer.write("Imagen[" + String.valueOf(posCaracter)+"],"+((tamanoImagen + posCaracter)/tamanoPagina)+","+((tamanoImagen + posCaracter)%tamanoPagina)+","+"R"+ "\n");
                    ref++;
                    writer.write("Mensaje[" + String.valueOf(posCaracter)+"],"+((tamanoImagen + posCaracter)/tamanoPagina)+","+((tamanoImagen + posCaracter)%tamanoPagina)+","+"W"+ "\n");
                    ref++;
                    }
                }
                
                writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}


