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

                    generarReferencias(imagenModificada, tamanoPagina);
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

                    System.out.println("Mensaje_oculto_" + archivo + " se creó con éxito");
                    break;
            }
        }
    }

    // Método para generar referencias desde la imagen BMP
    public static void generarReferencias(Imagen imagen, int tamanoPag) {
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter("referencias.txt"))) {
            int filasImg = imagen.getAlto();
            int colsImg = imagen.getAncho();
            int longitudMensaje = imagen.leerLongitud();
            int tamanoBytesImagen = filasImg * colsImg * 3;
    
            // Calcular la cantidad de referencias necesarias
            System.out.println("Tamaño del mensaje: " + longitudMensaje);
    
            int totalReferencias = 16 + 17 * longitudMensaje;
            int numPagsVirtuales = (tamanoBytesImagen + longitudMensaje + tamanoPag - 1) / tamanoPag;
            
            escritor.write("P=" + tamanoPag + "\n");
            escritor.write("NF=" + filasImg + "\n");
            escritor.write("NC=" + colsImg + "\n");
            escritor.write("NR=" + totalReferencias + "\n");
            escritor.write("NP=" + numPagsVirtuales + "\n");
    
            int columna = 0;
            String[] colores = {"R", "G", "B"}; 
            int offset = 0;
    
            for (int i = 0; i < 16; i++) {
                String colorActual = colores[i % 3];
                if (i % 3 == 0 && i != 0) {
                    columna++;
                }
                escritor.write("Imagen[0][" + columna + "]." + colorActual + ",0," + offset + ",R\n");
                offset++;
            }
            int fila = 0;
            int numPagina = 0;
            int posicionMensaje = 0;
            int pagMensaje = (3 * imagen.getAncho() * imagen.getAlto() + tamanoPag - 1) / tamanoPag;
            int contadorImg = 16;
            int indiceMensaje = 0;
            boolean continuacion = false;
    
            for (; contadorImg < totalReferencias;) {
                escritor.write("Mensaje[" + indiceMensaje + "]," + pagMensaje + "," + posicionMensaje + ",W\n");
                contadorImg++;
    
                for (int i = 0; i < 8; i++) {
                    String colorRef = colores[contadorImg % 3];
                    if (contadorImg % 3 == 0) {
                        columna++;
                        if (columna >= imagen.getAncho()) {
                            fila++;
                            columna = 0;
                        }
                    }
                    escritor.write("Imagen[" + fila + "][" + columna + "]." + colorRef + "," + numPagina + "," + offset + ",R\n");
                    offset++;
                    if (offset >= tamanoPag) {
                        numPagina++;
                        offset = 0;
                    }
                    contadorImg++;
                    continuacion = true;
                }
                posicionMensaje++;
                indiceMensaje++;
                if (posicionMensaje >= tamanoPag) {
                    posicionMensaje = 0;
                    pagMensaje++;
                }
            }
    
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
            int paginasVirtuales = (((filas*columnas*3)+tamanoMensaje)/tamanoPagina);

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


