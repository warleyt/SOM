package som;

import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.Scanner;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class Main {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        Random rd = new Random();

        Scanner in = new Scanner(System.in);
        System.out.println("Selecione:\n0 - Digitar\n1 - Porta AND\n2 - Iris\n3 - Breast Cancer Wisconsin (Original)\n");
        int op = in.nextInt();
        in.nextLine();

        File Entrada = new File("inexistente.txt");
        switch (op) {
            case 1:
                Entrada = new File("and.txt");
                break;
            case 2:
                Entrada = new File("iris.txt");
                break;
            case 3:
                Entrada = new File("bcw(original).txt");
                break;
        }
        try {
            in = new Scanner(Entrada);
        } catch (FileNotFoundException evalor) {
        }

        int nAmostras = Integer.parseInt(in.nextLine()); //Numero de amostras a serem consideradas
        int nEntradas = Integer.parseInt(in.nextLine());//Numero de elementos de uma amostra
        int nNeuronios = Integer.parseInt(in.nextLine());//Número de neurônios a serem utilizados
        int nEpocas = Integer.parseInt(in.nextLine());//Numero de epocas a serem consideradas
        double sigma0 = nNeuronios / 2;
        double ni0 = 0.1;
        double tau1 = nEpocas / Math.log(sigma0);
        double tau2 = nEpocas;
        double ni = 0;
        double sigma = 0;

        ArrayList<Amostra> matXList = new ArrayList<>();
        ArrayList<Double> classes = new ArrayList<>();
        for (int i = 0; i < nAmostras; i++) {
            String linhas = in.nextLine();
            String[] itens = linhas.split(",");
            double[] linha = new double[nEntradas];
            int j = 0;
            for (; j < nEntradas; j++) {
                linha[j] = (Double.parseDouble(itens[j]));
            }
            carregaClasses(classes, Double.parseDouble(itens[j]));
            matXList.add(new Amostra(classes.indexOf(Double.parseDouble(itens[j])), linha));
        }

        int nClasses = classes.size();

        ArrayList<Amostra> treino = new ArrayList<>();
        ArrayList<Amostra> teste = new ArrayList<>();
        int nTreino = (int) (matXList.size() * 0.75); //3/4 
        for (int i = 0; i < nTreino; i++) {
            treino.add(matXList.remove(rd.nextInt(matXList.size())));
        }
        teste.addAll(matXList);
        matXList.clear();

        SOM mapaKonohen = new SOM(nEntradas, nNeuronios);
        int erroTreino, erroTeste;
        DefaultCategoryDataset dsClass = new DefaultCategoryDataset();
        for (int epoca = 1; epoca <= nEpocas; epoca++) {
            //Calculando o sigma
            sigma = sigma0 * Math.exp(-epoca / tau1);
            //Calculando o ni
            ni = ni0 * Math.exp(-epoca - 1 / tau2);

            erroTreino = 0;
            erroTeste = 0;

            for (int linhas = 0; linhas < treino.size(); linhas++) {
                mapaKonohen.treinar(ni, treino.get(linhas).getEntradas(), sigma);
            }
            int[][] mapaSoma = new int[nNeuronios][nClasses];

            for (int linhas = 0; linhas < teste.size(); linhas++) {
                mapaKonohen.pCompetitivo(teste.get(linhas).getEntradas());//Calculando qual neurônio ganhou
                int i = mapaKonohen.indNeuronioGanhador;
                mapaSoma[i][teste.get(linhas).getClasse()]++;
            }

            int[] mapa = new int[nNeuronios];
            for (int i = 0; i < nNeuronios; i++) {
                int c = 0;
                for (int j = 1; j < nClasses; j++) {
                    if (mapaSoma[i][c] < mapaSoma[i][j]) {
                        c = j;
                    }
                }
                mapa[i] = c;
            }

            //generalização
            for (int linhas = 0; linhas < treino.size(); linhas++) {
                mapaKonohen.pCompetitivo(treino.get(linhas).getEntradas());
                int i = mapaKonohen.indNeuronioGanhador;
                if (mapa[i] != treino.get(linhas).getClasse()) {
                    erroTreino++;
                }
            }

            //especialização
            for (int linhas = 0; linhas < teste.size(); linhas++) {
                mapaKonohen.pCompetitivo(teste.get(linhas).getEntradas());
                int i = mapaKonohen.indNeuronioGanhador;
                if (mapa[i] != teste.get(linhas).getClasse()) {
                    erroTeste++;
                }
            }
            
            dsClass.addValue(((double)erroTreino / (double)nTreino), "Classificação Treino", "" + epoca + "");
            dsClass.addValue(((double)erroTeste / (double)((double)nAmostras - (double)nTreino)), "Classificação Teste", "" + epoca + "");

            System.out.print("Época:\t" + epoca + "\t\terro treino: " + erroTreino + "\t\terro teste: " + erroTeste + "\t\t");
            for (int i = 0; i < mapa.length; i++) {
                System.out.print(mapa[i] + " | ");
            }
            System.out.println("");
        }

        // cria o gráfico
        JFreeChart grafico = ChartFactory.createLineChart("Erros", "Épocas",
                "Valor", dsClass, PlotOrientation.VERTICAL, true, true, false);

        GregorianCalendar gc = new GregorianCalendar();

        String sufNomeArq = op + "-" + gc.get(Calendar.HOUR_OF_DAY) + "."
                + gc.get(Calendar.MINUTE) + "-" + gc.get(Calendar.DAY_OF_MONTH) + "." + (gc.get(Calendar.MONTH) + 1) + "." + gc.get(Calendar.YEAR) + ".png";
        try (OutputStream arquivo = new FileOutputStream("grafico" + sufNomeArq)) {
            ChartUtilities.writeChartAsPNG(arquivo, grafico, 2000, 1000);
        }

        System.out.println("\nImagem do gráfico salva na pasta do projeto!");
        try {
            File arquivo = new File("grafico" + sufNomeArq);
            Process p = Runtime.getRuntime().exec("cmd.exe /c \"" + arquivo.getAbsolutePath() + "\"");
        } catch (IOException e) {
        }
    }

    public static void carregaClasses(ArrayList<Double> classes, double saida) {
        if (classes.isEmpty()) {
            classes.add(saida);
        }
        if (!classes.contains(saida)) {
            classes.add(saida);
        }
    }
}
