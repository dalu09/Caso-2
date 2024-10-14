public class UpdaterThread extends Thread {
    private PageTable pageTable;

    public UpdaterThread(PageTable pageTable) {
        this.pageTable = pageTable;
    }

    public void run() {
        try {
            int[] referencias = {1, 2, 3, 4, 5, 1, 3, 6, 7, 3};  // Ejemplo de referencias
            for (int referencia : referencias) {
                pageTable.loadPage(referencia);
                Thread.sleep(2); // Esperar 2 ms antes de cargar la siguiente página
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
