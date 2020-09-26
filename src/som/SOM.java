package som;

public class SOM {
    double [][] pesos;
    int indNeuronioGanhador;
    double[] hji;
    
    public SOM(int nEntradas, int nNeuronios) {
        this.pesos = new double[nEntradas][nNeuronios];
        
        for (int i = 0; i < nEntradas; i++) {
            for (int j = 0; j < nNeuronios; j++) {
                pesos[i][j] = (Math.random()*1.6)-0.3;
            }
        }
    }
    
    private double distAoQ(double[] p1, double [] p2){
        if(p1.length != p2.length) return -1; 
        int numDimensoes = p1.length;
        double distAoQuadrado = 0;
        for (int i = 0; i < numDimensoes; i++) {
            distAoQuadrado+=Math.pow(p1[i]-p2[i], 2);
        }
        return distAoQuadrado;
    }
    
    public void pCompetitivo(double[] entradas){
        double [] vetColuna = new double[pesos.length];
        double menorDist = Double.POSITIVE_INFINITY;
        for (int colunas = 0; colunas < pesos[0].length; colunas++) {
            for (int linhas = 0; linhas < pesos.length; linhas++) {
                vetColuna[linhas] = pesos[linhas][colunas];
            }
            if(distAoQ(entradas, vetColuna)<menorDist){
                menorDist = distAoQ(entradas, vetColuna);
                this.indNeuronioGanhador = colunas;
            }
        }
    }
    
    public void pCooperativo(double sigma){
        int nNeuronios = pesos[0].length;
        int i = this.indNeuronioGanhador;
        //dji = Distância lateral entre os neurônios
        int[] dji = new int[nNeuronios];
        for (int j = 0; j < dji.length; j++) {
            dji[j] = Math.abs(j-i);
        }
        //hij = Vizinhança topológica
        this.hji = new double[nNeuronios];
        for (int j = 0; j < this.hji.length; j++) {
            this.hji[j] = Math.pow(Math.E, -(dji[j])/(2*Math.pow(sigma, 2)) );
        }
    }
    
    public void pAdaptativo(double ni, double[] entradas){
        for (int i = 0; i < pesos.length; i++) {
            for (int j = 0; j < pesos[0].length; j++) {
                this.pesos[i][j] = this.pesos[i][j]+(ni*this.hji[j]*(entradas[i]-this.pesos[i][j]));
            }
        }
    }
    public void treinar(double ni, double[] entradas, double sigma){
        pCompetitivo(entradas);
        pCooperativo(sigma);
        pAdaptativo(ni, entradas);
    }
    
    
    
    
    
    
}
