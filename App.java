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

                    crearReferencias(imagenModificada, tamanoPagina);
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
                    imagenModificada.esconder(mensaje, mensaje.length);
                    imagenModificada.escribirImagen(archivoImg + "_modificado");

                    System.out.println("Mensaje escondido");
                    break;

                case 4:
                    System.out.print("Ingrese el nombre del archivo BMP con el mensaje escondido: ");
                    String archivo = scanner.next();
                
                    // Cargar la imagen con el archivo dado
                    imagenModificada = new Imagen(archivo);
            
                    int longitudMensaje = imagenModificada.leerLongitud();
                    char[] mensajeOculto = new char[longitudMensaje];
                
                    // Llama al método recuperar, pasándole el array y la longitud del mensaje
                    imagenModificada.recuperar(mensajeOculto, longitudMensaje);
                
                    // Escribir el archivo con el mensaje oculto recuperado
                    escribirArchivoTexto("Mensaje_oculto_" + archivo, mensajeOculto);
                
                    System.out.println("Mensaje_oculto_" + archivo + " se creó con éxito");
                    break;
            }
        }
    }

    public static void crearReferencias(Imagen img, int tamanoPagina) {
        try (BufferedWriter output = new BufferedWriter(new FileWriter("referencias.txt"))) {
            int numFilas = img.alto;
            int numColumnas = img.ancho;
            int mensajeLongitud = img.leerLongitud();
            int totalBytes = numFilas * numColumnas * 3;
    
            // Cálculo del total de referencias necesarias
            System.out.println("Longitud del mensaje: " + mensajeLongitud);
    
            int refTotal = 16 + 17 * mensajeLongitud;
            int pagsVirtuales = (totalBytes+ mensajeLongitud + tamanoPagina - 1) / tamanoPagina;
    
            // Escribimos información inicial
            output.write("P=" + tamanoPagina + "\n");
            output.write("NF=" + numFilas + "\n");
            output.write("NC=" + numColumnas + "\n");
            output.write("NR=" + refTotal + "\n");
            output.write("NP=" + pagsVirtuales + "\n");
    
            int columnaActual = 0;
            String[] coloresRGB = {"R", "G", "B"};
            int desplaz = 0;
    
            // Escribir las primeras 16 referencias
            int contador;
            for (contador = 0; contador < 16; contador++) {
                String color = coloresRGB[contador % 3];
                if (contador % 3 == 0 && contador != 0) {
                    columnaActual++;
                }
                output.write("Imagen[0][" + columnaActual + "]." + color + ",0," + desplaz + ",R\n");
                desplaz++;
            }
    
            int filaActual = 0;
            int paginaActual = 0;
            int posEnMensaje = 0;
            int pagMensaje = (3 * img.ancho * img.alto + tamanoPagina - 1) / tamanoPagina;
    
            int contadorImagen = contador;
            int indiceMensaje = 0;
            boolean continuar = false;
    
            // Escribir referencias del mensaje y colores
            while (contador < refTotal) {
                output.write("Mensaje[" + indiceMensaje + "]," + pagMensaje + "," + posEnMensaje + ",W\n");
                contador++;
                for (int i = 0; i < 16; i++) {
                    if (continuar) {
                        output.write("Mensaje[" + indiceMensaje + "]," + pagMensaje + "," + posEnMensaje + ",W\n");
                        contador++;
                        continuar = false;
                    } else {
                        String color = coloresRGB[contadorImagen % 3];
                        if (contadorImagen % 3 == 0) {
                            columnaActual++;
                            if (columnaActual >= img.ancho) {
                                filaActual++;
                                columnaActual = 0;
                            }
                        }
                        output.write("Imagen[" + filaActual + "][" + columnaActual + "]." + color + "," + paginaActual + "," + desplaz + ",R\n");
    
                        desplaz++;
                        if (desplaz >= tamanoPagina) {
                            paginaActual++;
                            desplaz = 0;
                        }
                        contador++;
                        contadorImagen++;
                        continuar = true;
                    }
                }
    
                posEnMensaje++;
                indiceMensaje++;
    
                if (posEnMensaje >= tamanoPagina) {
                    posEnMensaje = 0;
                    pagMensaje++;
                }
            }
    
            System.out.println("Referencias generadas y guardadas en 'referencias.txt'.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    public static void simularPaginacion(String archivoReferencias, int numMarcos) {
        PageTable pageTable = new PageTable(numMarcos); // Crea la tabla de páginas con marcos limitados
        FaultsCounter faultsCounter = new FaultsCounter(); // Inicializa el contador de fallos y hits
        NRUThread nruThread = new NRUThread(pageTable); // Thread para manejar bits de referencia
        //UpdaterThread updaterThread = new UpdaterThread(pageTable); // Thread para actualizar bits de referencia
        nruThread.start(); // Iniciamos el thread
        //updaterThread.start(); // Iniciamos el thread

    
        try (BufferedReader reader = new BufferedReader(new FileReader(archivoReferencias))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.startsWith("Imagen") || linea.startsWith("Mensaje")) {
                    String[] partes = linea.split(",");
                    int paginaVirtual = Integer.parseInt(partes[1]);
    
                    boolean hit = pageTable.loadPage(paginaVirtual); // Verifica si la página está en la memoria
                    if (hit) {
                        faultsCounter.countHit();
                    } else {
                        faultsCounter.countFault();
                    }
                }
            }
    
            System.out.println("Simulación completada.");
            System.out.println("Total de fallas de página: " + faultsCounter.getFaults());
            System.out.println("Total de hits: " + faultsCounter.getHits());
    
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            nruThread.interrupt(); // Detenemos el thread de NRU al final de la simulación
            try {
                nruThread.join(); // Esperamos que el thread termine correctamente antes de continuar
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
            int filas = imagen.alto;
            int columnas = imagen.ancho;
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

            int bytesFila = imagen.ancho * 3;
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


