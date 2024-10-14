import java.util.ArrayList;
import java.util.HashMap;

public class PageTable {
    private HashMap<Integer, Page> pagesTable = new HashMap<>();
    private ArrayList<Integer> frames = new ArrayList<>();
    private int maxFrames;

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
                frames.add(page);
                pagesTable.put(page, new Page());
            } else {
                replacePage(page);
            }
            return false;
        }
    }

    // Método sincronizado para reemplazar una página en la memoria usando el algoritmo NRU
    public synchronized void replacePage(int page) {
        int paginaReemplazar = selectPageToReplace();
        System.out.println("Reemplazando página " + paginaReemplazar + " con " + page);
        frames.set(frames.indexOf(paginaReemplazar), page);
        pagesTable.put(page, new Page()); // Reemplazamos la página por una nueva instancia
        pagesTable.remove(paginaReemplazar);
    }

    // Método sincronizado para clasificar las páginas para el algoritmo NRU
    public synchronized void categorizePages() {
        for (Integer page : pagesTable.keySet()) {
            Page p = pagesTable.get(page);
            // Simulamos el envejecimiento reiniciando el bit R
            p.referenced = false; // El bit R se pone en false después de cada ciclo NRU
        }
    }

    // Selecciona la página a reemplazar (algoritmo simplificado de NRU)
    public synchronized int selectPageToReplace() {
        for (Integer page : frames) {
            Page p = pagesTable.get(page);
            if (!p.referenced) {
                return page;
            }
        }
        // Si no encontramos ninguna página con R = 0, reemplazamos la primera
        return frames.get(0);
    }
}
