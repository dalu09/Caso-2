import java.util.ArrayList;
import java.util.HashMap;

public class PageTable {
    private HashMap<Integer, Page> pagesTable = new HashMap<>(); // Páginas virtuales cargadas
    private ArrayList<Integer> frames = new ArrayList<>(); // Marcos de página disponibles
    private int maxFrames; // Número de marcos disponibles en RAM

    public PageTable(int maxFrames) {
        this.maxFrames = maxFrames;
    }

    class Page {
        boolean referenced; 
        boolean modified; 

        Page() {
            referenced = false;
            modified = false;
        }
    }

    // Método sincronizado para cargar una página en la memoria
    public synchronized boolean loadPage(int page) {
        if (pagesTable.containsKey(page)) {
            Page currentPage = pagesTable.get(page);
            currentPage.referenced = true; // Actualizamos el bit R cuando hay un hit
            System.out.println("Hit: Página " + page);
            return true;
        } else {
            System.out.println("Falló de página: " + page);
            if (frames.size() < maxFrames) {
                frames.add(page); // Agregamos la página a la memoria real
                pagesTable.put(page, new Page());
            } else {
                replacePage(page); // Reemplazamos una página si no hay espacio
            }
            return false;
        }
    }

    public synchronized void replacePage(int page) {
        int paginaReemplazar = selectPageToReplace(); // Seleccionamos la página a reemplazar usando NRU
        System.out.println("Reemplazando página " + paginaReemplazar + " con " + page);
        frames.set(frames.indexOf(paginaReemplazar), page);
        pagesTable.put(page, new Page()); // Reemplazamos la página por una nueva instancia
        pagesTable.remove(paginaReemplazar);
    }

    // Método sincronizado para clasificar las páginas para el algoritmo NRU
    public synchronized void categorizePages() {
        for (Integer page : pagesTable.keySet()) {
            Page p = pagesTable.get(page);
            p.referenced = false; // El bit R se pone en false después de cada ciclo NRU
        }
    }
    public synchronized void updateTableState() {
        for (Integer page : pagesTable.keySet()) {
            Page p = pagesTable.get(page);
            // Actualiza los bits o el estado según las reglas del algoritmo de paginación que estés usando.
            
        }
    }

    public synchronized int selectPageToReplace() {
        for (Integer page : frames) {
            Page p = pagesTable.get(page);
            if (!p.referenced) { // Selecciona la primera página que no ha sido referenciada
                return page;
            }
        }
        return frames.get(0); // Si todas las páginas fueron referenciadas, reemplazamos la primera
    }
}
