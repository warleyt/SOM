package som;

public class Amostra {
    private int classe;
    private double[] entrada;

    public Amostra(int classe, double[] entrada) {
        this.classe = classe;
        this.entrada = entrada;
    }

    public int getClasse() {
        return classe;
    }

    public void setClasse(int classe) {
        this.classe = classe;
    }

    public double[] getEntradas() {
        return entrada;
    }

    public void setEntradas(double[] entrada) {
        this.entrada = entrada;
    }
    
    
    
}
