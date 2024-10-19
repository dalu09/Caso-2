import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Imagen {

    byte[] header = new byte[54];
    byte[][][] imagen;
    int alto, ancho;
    int padding;
    String file_name;

    /**
     * *
     * Método para crear una matriz imagen a partir de un archivo.
     *
     * @param input: nombre del archivo. El formato debe ser BMP de 24 bits de bit depth
     * @pos la matriz imagen tiene los valores correspondientes a la imagen
     * almacenada en el archivo.
     *
     */
    public Imagen(String input) {
        file_name = input;
        try {
            FileInputStream fis = new FileInputStream(file_name);
            fis.read(header);

            // Extraer el ancho y alto de la imagen desde la cabecera
            // Almacenados en little endian
            ancho = ((header[21] & 0xFF) << 24) | ((header[20] & 0xFF) << 16)
                    | ((header[19] & 0xFF) << 8) | (header[18] & 0xFF);
            alto = ((header[25] & 0xFF) << 24) | ((header[24] & 0xFF) << 16)
                    | ((header[23] & 0xFF) << 8) | (header[22] & 0xFF);
            System.out.println("Ancho: " + ancho + " px, Alto: " + alto + " px");
            imagen = new byte[alto][ancho][3];
            int rowSizeSinPadding = ancho * 3;
            // El tamaño de la fila debe ser múltiplo de 4 bytes
            padding = (4 - (rowSizeSinPadding % 4)) % 4;

            // Leer y modificar los datos de los píxeles 
            // (en formato RGB, pero almacenados en orden BGR)
            byte[] pixel = new byte[3];
            for (int i = 0; i < alto; i++) {
                for (int j = 0; j < ancho; j++) {
                    // Leer los 3 bytes del píxel (B, G, R)
                    fis.read(pixel);
                    // imagen[i][j] = new Color();
                    imagen[i][j][0] = pixel[0];
                    imagen[i][j][1] = pixel[1];
                    imagen[i][j][2] = pixel[2];
                }
                fis.skip(padding);
            }
            fis.close();
        } catch (IOException e) {
            System.out.print("Error en la lectura de la imagen");
        }
    }

    /**
     * Método para esconder un valor en una matriz imagen.
     *
     * @param contador: contador de bytes escritos en la matriz
     *
     * @param valor: valor que se quiere esconder
     * @param numbits: longitud (en bits) del valor
     * @pre la matriz imagen debe haber sido inicializada con una imagen
     * @pos los bits recibidos como parámetro (en valor) están escondido en la
     * imagen.
     *
     */
    private void escribirBits(int contador, int valor, int numbits) {
        // la matriz tiene ancho pixel de ancho (num pixeles por fila)
        // Cada pixel de la matriz corresponde a 3 Bytes. 
        // En cada pixel es posible esconder 3 bits (en cada byte de los componentes se esconde un bit)
        int bytesPorFila = ancho * 3;// ancho de la imagen en bytes
        int mascara;
        for (int i = 0; i < numbits; i++) {
            // i: i-ésimo bit del valor que se va a esconder
            // 8*pos: por cada byte debemos esconder 8 bits 
            // 8*pos + i indica el byte que se debe modificar

            int fila = (8 * contador + i) / bytesPorFila;
            // Cada posición de la matriz agrupa 3 bytes (RGB).
            int col = ((8 * contador + i) % bytesPorFila) / 3;
            int color = ((8 * contador + i) % bytesPorFila) % 3;

            mascara = valor >> i;
            mascara = mascara & 1;
            imagen[fila][col][color] = (byte) ((imagen[fila][col][color] & 0xFE) | mascara);
        }
    }

    /**
     * Método para esconder un mensaje en una matriz imagen.
     *
     * @param mensaje: Mensaje a esconder
     * @param longitud: longitud del mensaje
     * @pre la matriz imagen debe haber sido inicializada con una imagen
     * @pre la longitud del mensaje en bits debe ser menor que el numero de
     * pixels de la imagen * 3
     * @pos la longitud del mensaje y el mensaje completo están escondidos en la
     * imagen
     */
    public void esconder(char[] mensaje, int longitud) {
        int contador = 0;
        byte elByte;
        escribirBits(contador, longitud, 16);
        // La longitud del mensaje se esconderá en los primeros 16 bytes. 
        // Eso es el equivalente a 2 caracteres (en necesidad de almacenamiento). 
        // El primer byte del mensaje se almacena después de la longitud (a partir del byte 17)
        contador = 2;

        for (int i = 0; i < longitud; i++) {
            elByte = (byte) mensaje[i];
            escribirBits(contador, elByte, 8);
            contador++;
            if (i % 1000 == 0) {
                System.out.println("Van " + i + " caracteres de " + longitud);
            }
        }
    }

    /**
     * Método para escribir una imagen a un archivo en formato BMP
     *
     * @param output: nombre del archivo donde se almacenará la imagen. Se
     * espera que se invoque para almacenar la imagen modificada.
     * @pre la matriz imagen debe haber sido inicializada con una imagen
     * @pos se creó el archivo en formato bmp con la información de la matriz
     * imagen
     */
    public void escribirImagen(String output) {
        byte pad = 0;
        try {
            FileOutputStream fos = new FileOutputStream(output);
            fos.write(header);
            byte[] pixel = new byte[3];
            for (int i = 0; i < alto; i++) {
                for (int j = 0; j < ancho; j++) {
                    // Leer los 3 bytes del píxel (B, G, R)
                    pixel[0] = imagen[i][j][0];
                    pixel[1] = imagen[i][j][1];
                    pixel[2] = imagen[i][j][2];
                    fos.write(pixel);
                }
                for (int k = 0; k < padding; k++) {
                    fos.write(pad);
                }
            }
            fos.close();
        } catch (IOException e) {
            System.out.print("No se escribió en la imagen");
        }
    }

    /**
     * Método para recuperar la longitud del mensaje escondido en una imagen.
     *
     * @pre la matriz imagen debe haber sido inicializada con una imagen. la
     * imagen debe esconder la longitud de un mensaje escondido, y el mensaje.
     * @return La longitud del mensaje que se esconde en la imagen.
     */
    public int leerLongitud() {
        int longitud = 0;
        // Usamos 16 bits para almacenar la longitud del mensaje. 
        // Esos 16 bits se esconden en los primeros 16 bytes de la imagen. 
        // Como cada posición de la matriz tiene 3 bytes y se esconde un bit en cada byte. 
        // Debemos leer 5 pixeles completos y 1 byte del siguiente pixel. 
        for (int i = 0; i < 16; i++) {
            // ancho es el número de pixeles en una fila
            // ancho*3 es el número de bytes en una fila
            int col = (i % (ancho * 3)) / 3;
            longitud = longitud | (imagen[0][col][((i % (ancho * 3)) % 3)] & 1) << i;
        }
        return longitud;
    }

    /**
     * Método para recuperar un mensaje escondido en una imagen
     *
     * @param cadena: vector de char, con espacio ya asignado
     * @param longitud: tamaño del mensaje escondido y tamaño del vector
     * (espacio disponible para almacenar información)
     * @pre la matriz imagen debe haber sido inicializada con una imagen. la
     * imagen debe esconder la longitud de un mensaje escondido, y el mensaje.
     * @pos cadena contiene el mensaje escondido en la imagen
     */
    public void recuperar(char[] cadena, int longitud) {
        //(imagen.ancho*3) ancho de cada fila de la matriz en bytes
        int bytesFila = ancho * 3;
        for (int posCaracter = 0; posCaracter < longitud; posCaracter++) {
            cadena[posCaracter] = 0;
            for (int i = 0; i < 8; i++) {
                // los dos primeros caracteres son la longitud. Hay que saltarlos
                int numBytes = 16 + (posCaracter * 8) + i;
                int fila = numBytes / bytesFila;
                int col = numBytes % (bytesFila) / 3;
                cadena[posCaracter]
                        = (char) (cadena[posCaracter] | (imagen[fila][col][((numBytes % bytesFila) % 3)] & 1) << i);
            }
        }
    }

    public String getFile_name() {
        return file_name;
    }
} 
