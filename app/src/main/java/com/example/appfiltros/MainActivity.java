package com.example.appfiltros;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.OutputStream;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView txvStatus;
    private Button btnVizinho, btnGlobal, btnLocal, btnUnsharp, btnHistograma;
    private FloatingActionButton btnReset, btnSalva, btnGaleria;
    private static final int PEGAR_IMAGEM = 100;
    public static int mediaVizinhos = 0;
    Uri imageUri;

    BitmapDrawable bmpOriginal, bmpNormal; // usado para pegar o bitmap da imagem original.

    //Valores que serão utilizados para calcular as cores em um sistema ARGB (alpha, red, green, blue)
    //em um sistema ARGB, o valor máximo da cor de um pixel é 255, e o valor mínimo é 0.
    //Esses números indicam a intensidade de cada cor em um pixel.
    private final static int VALOR_MAIS_ALTO = 255;
    private final static int VALOR_MAIS_BAIXO = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Obtendo as permissões do usuário para salvar e acessar a galeria.
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);

        //iniciando variaveis da imagem
        imageView = findViewById(R.id.imageView);
        bmpOriginal = (BitmapDrawable) imageView.getDrawable();
        //bmpNormal = (BitmapDrawable) imageView.getDrawable();

        //botões para galeria
        btnGaleria = findViewById(R.id.btnGaleria);
        btnSalva = findViewById(R.id.btnSalva);

        //Eventos dos botões
        btnGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                escolherImagem();
            }
        });

        btnSalva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarPraGaleria();
            }
        });

        //função para manipular os filtros
        filters();
    }

    //abrindo a galeria
    private void escolherImagem() {
        Intent galeria = new Intent(Intent.ACTION_PICK);
        galeria.setType("image/*");
        startActivityForResult(galeria, PEGAR_IMAGEM);
    }

    private void salvarPraGaleria(){
        Uri imagem;
        ContentResolver contentResolver = getContentResolver();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            imagem = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        }else {
            imagem = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        //salvando a imagem
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis() + ".jpg"); //formato da imagem
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "images/*"); //local onde a imagem será salva
        Uri uri = contentResolver.insert(imagem, contentValues);

        try{
            bmpOriginal = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = bmpOriginal.getBitmap();

            OutputStream outputStream = contentResolver.openOutputStream(Objects.requireNonNull(uri));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            Objects.requireNonNull(outputStream);

            Toast.makeText(this, "Imagem salva com sucesso!", Toast.LENGTH_SHORT).show();
        }catch(Exception e){
            Toast.makeText(this, "Não foi possível salvar a imagem.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    //colocando a imagem que foi selecionada na galeria no ImageView
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == PEGAR_IMAGEM){
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
            bmpOriginal = (BitmapDrawable) imageView.getDrawable();
            bmpNormal = (BitmapDrawable) imageView.getDrawable();
        }
    }

    //filtros
    private void filters() {
        //botao do filtro cinza
        btnVizinho = findViewById(R.id.btnVizinho);
        btnGlobal = findViewById(R.id.btnGlobal);
        btnReset = findViewById(R.id.btnReset);
        btnLocal = findViewById(R.id.btnLocal);
        btnUnsharp = findViewById(R.id.btnUnsharp);
        btnHistograma = findViewById(R.id.btnHistograma);

        txvStatus = findViewById(R.id.txvStatus);

        btnVizinho.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //filtro vizinho
                txvStatus.setText("Aguarde");
                filter(Filter.vizinho);

            }
        });
        btnUnsharp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //filtro máscara unsharp
                txvStatus.setText("Aguarde");
                filter(Filter.unsharp);

            }
        });
        btnHistograma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //filtro histograma
                txvStatus.setText("Aguarde");
                filter(Filter.histograma);

            }
        });
        btnGlobal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //filtro global
                txvStatus.setText("Aguarde");
                filter(Filter.global);
            }
        });
        btnLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //filtro local
                txvStatus.setText("Aguarde");
                filter(Filter.local);
            }
        });
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //imagem original
                filter(Filter.original);
            }
        });
    }

    //criação e implementação dos filtros
    private void filter(String filter) {
        //Aqui estamos pegando o bitmap da imagem original (bmpOriginal) e criando uma variável que será utilizada
        //para aplicar o filtro à imagem (outputBitmap).
        Bitmap outputBitmap = bmpOriginal.getBitmap().copy(Bitmap.Config.ARGB_8888, true);

        int altura =  outputBitmap.getHeight();
        int largura = outputBitmap.getWidth();
        int windowSize = 6;
        double enhancementFactor = 0.5;
        double threshold = 0.5;



        //filtro vizinho
        if(filter.equalsIgnoreCase(Filter.vizinho)){
            for (int row = 0; row < altura; row++) {
                for (int col = 0; col < largura; col++) {
                    int pixel = outputBitmap.getPixel(col, row);
                    int redValue = Color.red(pixel);
                    int greenValue = Color.green(pixel);
                    int blueValue = Color.blue(pixel);

                    // Calcule os valores mínimo e máximo da vizinhança
                    int[] minmaxNeighbor = getMinMaxNeighborColor(outputBitmap, col, row, windowSize);
                    //int maxNeighbor = getMaxNeighborColor(outputBitmap, col, row, windowSize);

                    // Aplique a transformação de contraste adaptativo
                    int newRed = (int) (redValue + enhancementFactor * (redValue - minmaxNeighbor[0]) / (minmaxNeighbor[3] - minmaxNeighbor[0]));
                    int newGreen = (int) (greenValue + enhancementFactor * (greenValue - minmaxNeighbor[1]) / (minmaxNeighbor[4] - minmaxNeighbor[1]));
                    int newBlue = (int) (blueValue + enhancementFactor * (blueValue - minmaxNeighbor[2]) / (minmaxNeighbor[5] - minmaxNeighbor[2]));

                    // Limite o valor dentro do intervalo 0-255
                    newRed = Math.min(255, Math.max(0, newRed));
                    newGreen = Math.min(255, Math.max(0, newGreen));
                    newBlue = Math.min(255, Math.max(0, newBlue));

                    // Crie o pixel processado com a nova intensidade
                    int processedPixel = Color.rgb(newRed, newGreen, newBlue);

                    // Atualize o pixel na imagem processada
                    outputBitmap.setPixel(col, row, processedPixel);
/*
                    int[] neighborhood = getNeighborhood(outputBitmap, col, row, windowSize);
                    double stdDeviation = calculateStandardDeviation(neighborhood, mediaVizinhos);

                    int pixel = outputBitmap.getPixel(col, row);
                    int intensity = getGrayValue(pixel);
                    int enhancedIntensity = (int) (intensity + enhancementFactor * (intensity - mediaVizinhos) / stdDeviation);

                    enhancedIntensity = Math.max(0, Math.min(255, enhancedIntensity));
                    int enhancedPixel = Color.rgb(enhancedIntensity, enhancedIntensity, enhancedIntensity);

                    outputBitmap.setPixel(col, row, enhancedPixel);
*/
                }
            }
            //colocando o filtro na imagem
            //outputBitmap.setPixels(getPixels(enhancedImage), 0, largura, 0, 0, largura, altura);
            imageView.setImageBitmap(outputBitmap);
            txvStatus.setText("Finalizado");
        }
        //aplicando filtro global
        if(filter.equalsIgnoreCase(Filter.global)){

            // Obtém os valores mínimos e máximos de intensidade em cada canal de cor
            int minRed = 255, minGreen = 255, minBlue = 255;
            int maxRed = 0, maxGreen = 0, maxBlue = 0;

            // Percorre os pixels do bitmap para obter os valores mínimos e máximos
            for (int y = 0; y < altura; y++) {
                for (int x = 0; x < largura; x++) {
                    // Obtém os valores dos canais de cor do pixel atual
                    int pixel = outputBitmap.getPixel(x, y);
                    int redValue = Color.red(pixel);
                    int greenValue = Color.green(pixel);
                    int blueValue = Color.blue(pixel);

                    // Atualiza os valores mínimos e máximos de cada canal de cor
                    minRed = Math.min(minRed, redValue);
                    minGreen = Math.min(minGreen, greenValue);
                    minBlue = Math.min(minBlue, blueValue);
                    maxRed = Math.max(maxRed, redValue);
                    maxGreen = Math.max(maxGreen, greenValue);
                    maxBlue = Math.max(maxBlue, blueValue);
                }
            }

            // Percorre novamente os pixels do bitmap e aplica o contraste global
            for (int y = 0; y < altura; y++) {
                for (int x = 0; x < largura; x++) {
                    // Obtém os valores dos canais de cor do pixel atual
                    int pixel = outputBitmap.getPixel(x, y);
                    int redValue = Color.red(pixel);
                    int greenValue = Color.green(pixel);
                    int blueValue = Color.blue(pixel);

                    // Calcula os novos valores normalizados para cada canal de cor
                    int newRed = normalizeValue(redValue, minRed, maxRed);
                    int newGreen = normalizeValue(greenValue, minGreen, maxGreen);
                    int newBlue = normalizeValue(blueValue, minBlue, maxBlue);

                    // Define os novos valores dos canais de cor no bitmap de saída
                    outputBitmap.setPixel(x, y, Color.rgb(newRed, newGreen, newBlue));
                }
            }

            imageView.setImageBitmap(outputBitmap);
            txvStatus.setText("Finalizado");
        }
        //aplicando filtro local
        if(filter.equalsIgnoreCase(Filter.local)){
            // Realize o processamento da imagem
            for (int row = 0; row < altura; row++) {
                for (int col = 0; col < largura; col++) {
                    int pixel = outputBitmap.getPixel(col, row);

                    // Obtém os valores dos canais de cor do pixel atual
                    int redValue = Color.red(pixel);
                    int greenValue = Color.green(pixel);
                    int blueValue = Color.blue(pixel);

                    // Calcula o valor médio da intensidade da vizinhança do pixel atual
                    double neighborhoodIntensity[] = getNeighborhoodIntensity(outputBitmap, col, row);

                    // Calcula o novo valor do canal de cor usando o contraste local
                    int newRed = calculateNewValue(redValue, neighborhoodIntensity[0], threshold);
                    int newGreen = calculateNewValue(greenValue, neighborhoodIntensity[1], threshold);
                    int newBlue = calculateNewValue(blueValue, neighborhoodIntensity[2], threshold);

                    // Define os novos valores dos canais de cor no bitmap de saída
                    outputBitmap.setPixel(col, row, Color.rgb(newRed, newGreen, newBlue));
                }
            }

            // Atualize o ImageView com a imagem processada
            imageView.setImageBitmap(outputBitmap);
            txvStatus.setText("Finalizado");
        }
        //aplicando maskara unsharp - OK
        if(filter.equalsIgnoreCase(Filter.unsharp)){
            double amount = 0.5;
            int radius = 3;
            int limiar = 5;
            // Percorre os pixels do bitmap
            for (int y = 0; y < altura; y++) {
                for (int x = 0; x < largura; x++) {
                    // Obtém a média dos pixels na vizinhança do pixel atual para cada canal de cor
                    int[] average = getAverageIntensity(outputBitmap, x, y, radius);

                    // Obtém o valor dos canais de cor do pixel atual
                    int pixel = outputBitmap.getPixel(x, y);
                    int redValue = Color.red(pixel);
                    int greenValue = Color.green(pixel);
                    int blueValue = Color.blue(pixel);

                    // Calcula os novos valores dos canais de cor com base na média e no fator de nitidez
                    if (Math.abs(redValue - average[0]) > limiar) {
                        redValue = (int) (redValue + amount * (redValue - average[0]));
                        redValue = Math.min(255, Math.max(0, redValue));
                    }
                    if (Math.abs(greenValue - average[0]) > limiar) {
                        greenValue = (int) (greenValue + amount * (greenValue - average[1]));
                        greenValue = Math.min(255, Math.max(0, greenValue));
                    }
                    if (Math.abs(blueValue - average[0]) > limiar) {
                        blueValue = (int) (blueValue + amount * (blueValue - average[2]));
                        blueValue = Math.min(255, Math.max(0, blueValue));
                    }

                    // Define os novos valores dos canais de cor no bitmap de saída
                    outputBitmap.setPixel(x, y, Color.rgb(redValue, greenValue, blueValue));
                }
            }
            imageView.setImageBitmap(outputBitmap);
            txvStatus.setText("Finalizado");
        }
        //aplicando equalização de histograma
        if(filter.equalsIgnoreCase(Filter.histograma)){
            // Cria um histograma para a imagem em escala de cinza
            int[] histogram = new int[256];

            // Percorre os pixels da imagem e atualiza o histograma
            for (int y = 0; y < altura; y++) {
                for (int x = 0; x < largura; x++) {
                    // Obtém o valor de intensidade em escala de cinza do pixel
                    int pixel = outputBitmap.getPixel(x, y);
                    int grayValue = getGrayValue(pixel);

                    // Incrementa o valor correspondente no histograma
                    histogram[grayValue]++;
                }
            }

            // Calcula o histograma acumulado
            int[] cumulativeHistogram = new int[256];
            cumulativeHistogram[0] = histogram[0];
            for (int i = 1; i < 256; i++) {
                cumulativeHistogram[i] = cumulativeHistogram[i - 1] + histogram[i];
            }

            // Percorre os pixels da imagem e aplica a equalização de histograma
            for (int y = 0; y < altura; y++) {
                for (int x = 0; x < largura; x++) {
                    // Obtém o valor de intensidade em escala de cinza do pixel
                    int pixel = outputBitmap.getPixel(x, y);
                    int grayValue = getGrayValue(pixel);

                    // Obtém o valor normalizado do histograma acumulado
                    int normalizedValue = (int) (cumulativeHistogram[grayValue] * 255.0 / (largura * altura));

                    // Define o novo valor do pixel no bitmap de saída
                    int newPixel = Color.rgb(normalizedValue, normalizedValue, normalizedValue);
                    outputBitmap.setPixel(x, y, newPixel);
                }
            }

            // Atualize o ImageView com a imagem processada
            imageView.setImageBitmap(outputBitmap);
            txvStatus.setText("Finalizado");
        }
        /*if(filter.equalsIgnoreCase(Filter.histograma)){
            // Realize o processamento da imagem
            for (int row = 0; row < altura; row++) {
                for (int col = 0; col < largura; col++) {
                    int pixel = outputBitmap.getPixel(col, row);

                    // Extraia os componentes de cor
                    int alpha = Color.alpha(pixel);
                    int red = Color.red(pixel);
                    int green = Color.green(pixel);
                    int blue = Color.blue(pixel);

                    // Calcule o histograma adaptativo
                    int[] histogram = getAdaptiveHistogram(outputBitmap, col, row, windowSize);

                    // Calcule a função de equalização adaptativa
                    int[] equalizationMap = getAdaptiveEqualizationMap(histogram);

                    // Aplique a equalização adaptativa aos componentes de cor
                    int processedRed = equalizationMap[red];
                    int processedGreen = equalizationMap[green];
                    int processedBlue = equalizationMap[blue];

                    // Combine os componentes de cor novamente
                    int processedPixel = Color.argb(alpha, processedRed, processedGreen, processedBlue);

                    // Atualize o pixel na imagem processada
                    outputBitmap.setPixel(col, row, processedPixel);
                }
            }

            // Atualize o ImageView com a imagem processada
            imageView.setImageBitmap(outputBitmap);
            txvStatus.setText("Finalizado");
        }*/
        //restaurando a imagem ao original
        if(filter.equalsIgnoreCase(Filter.original)){
            /*for(int i = 0; i < largura; i++){
                for(int j = 0; j < altura; j++){

                    int pixelAntigo = bmpOriginal.getBitmap().getPixel(i, j);

                    int vermelhoAntes = Color.red(pixelAntigo);
                    int azulAntes = Color.blue(pixelAntigo);
                    int verdeAntes = Color.green(pixelAntigo);

                    int novoPixel = Color.rgb(vermelhoAntes, verdeAntes, azulAntes);
                    outputBitmap.setPixel(i, j, novoPixel);
                }
            }*/
            outputBitmap = bmpOriginal.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
            imageView.setImageBitmap(outputBitmap);
            txvStatus.setText("");
        }
    }

    // Função para obter o valor mínimo da vizinhança de um pixel
    private int[] getMinMaxNeighborColor(Bitmap image, int col, int row, int windowSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] minmaxValues = {255, 255, 255, 0, 0, 0};

        // Determine a janela de vizinhança para cálculo do valor mínimo
        int startCol = Math.max(0, col - windowSize / 2);
        int endCol = Math.min(width - 1, col + windowSize / 2);
        int startRow = Math.max(0, row - windowSize / 2);
        int endRow = Math.min(height - 1, row + windowSize / 2);

        // Percorra a janela de vizinhança e encontre o valor mínimo
        for (int i = startRow; i <= endRow; i++) {
            for (int j = startCol; j <= endCol; j++) {
                int pixel = image.getPixel(j, i);

                // Obtém os valores dos canais de cor do pixel
                int redValue = Color.red(pixel);
                int greenValue = Color.green(pixel);
                int blueValue = Color.blue(pixel);

                // Atualiza os valores mínimos para cada canal de cor
                minmaxValues[0] = Math.min(minmaxValues[0], redValue);
                minmaxValues[1] = Math.min(minmaxValues[1], greenValue);
                minmaxValues[2] = Math.min(minmaxValues[2], blueValue);
                minmaxValues[3] = Math.max(minmaxValues[3], redValue);
                minmaxValues[4] = Math.max(minmaxValues[4], greenValue);
                minmaxValues[5] = Math.max(minmaxValues[5], blueValue);
            }
        }

        return minmaxValues;
    }
/*
    private static int[] getNeighborhood(Bitmap image, int x, int y, int windowSize) {
        int halfWindowSize = windowSize / 2;
        int[] neighborhood = new int[windowSize * windowSize];
        int index = 0;
        int sum = 0;

        for (int j = -halfWindowSize; j <= halfWindowSize; j++) {
            for (int i = -halfWindowSize; i <= halfWindowSize; i++) {
                int posX = x + i;
                int posY = y + j;

                if (posX >= 0 && posX < image.getWidth() && posY >= 0 && posY < image.getHeight()) {
                    neighborhood[index] = image.getPixel(posX, posY);
                } else {
                    neighborhood[index] = image.getPixel(x, y);
                }
                int intensity = getGrayValue(neighborhood[index]);
                sum += intensity;

                index++;
            }
        }
        mediaVizinhos =(int) sum / neighborhood.length;
        return neighborhood;
    }

    private static double calculateStandardDeviation(int[] pixels, int mean) {
        double sum = 0;

        for (int pixel : pixels) {
            int intensity = getGrayValue(pixel);
            sum += Math.pow(intensity - mean, 2);
        }

        double variance = sum / pixels.length;
        return Math.sqrt(variance);
    }
*/
    /*private static int[] getPixels(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];

        image.getPixels(pixels, 0, width, 0, 0, width, height);

        return pixels;
    }*/

    // Função para calcular o histograma adaptativo de um pixel
    /*private int[] getAdaptiveHistogram(Bitmap image, int col, int row, int windowSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] histogram = new int[256];

        // Determine a janela de vizinhança para cálculo do histograma adaptativo
        int startCol = Math.max(0, col - windowSize / 2);
        int endCol = Math.min(width - 1, col + windowSize / 2);
        int startRow = Math.max(0, row - windowSize / 2);
        int endRow = Math.min(height - 1, row + windowSize / 2);

        // Percorra a janela de vizinhança e conte as ocorrências de cada nível de intensidade
        for (int i = startRow; i <= endRow; i++) {
            for (int j = startCol; j <= endCol; j++) {
                int pixel = image.getPixel(j, i);
                int intensity = Color.red(pixel); // Use apenas o canal de vermelho para o histograma

                histogram[intensity]++;
            }
        }

        return histogram;
    }

    // Função para calcular a função de equalização adaptativa
    private int[] getAdaptiveEqualizationMap(int[] histogram) {
        int totalPixels = 0;
        int[] equalizationMap = new int[256];

        // Calcule o número total de pixels no histograma
        for (int i = 0; i < histogram.length; i++) {
            totalPixels += histogram[i];
        }

        float sumProbabilities = 0;

        // Calcule a função de equalização adaptativa
        for (int i = 0; i < histogram.length; i++) {
            float probability = (float) histogram[i] / totalPixels;
            sumProbabilities += probability;

            // Mapeie o valor do nível de intensidade para um novo valor
            int equalizedValue = (int) (255 * sumProbabilities);
            equalizationMap[i] = equalizedValue;
        }

        return equalizationMap;
    }*/

    private static int[] getAverageIntensity(Bitmap bitmap, int x, int y, int radius) {
        int[] sum = new int[3];
        int count = 0;

        // Percorre os pixels na vizinhança do pixel atual
        for (int i = x - radius; i <= x + radius; i++) {
            for (int j = y - radius; j <= y + radius; j++) {
                // Verifica se o pixel está dentro dos limites do bitmap
                if (i >= 0 && i < bitmap.getWidth() && j >= 0 && j < bitmap.getHeight()) {
                    // Obtém os valores dos canais de cor do pixel
                    int pixel = bitmap.getPixel(i, j);
                    int redValue = Color.red(pixel);
                    int greenValue = Color.green(pixel);
                    int blueValue = Color.blue(pixel);

                    // Soma os valores dos canais de cor
                    sum[0] += redValue;
                    sum[1] += greenValue;
                    sum[2] += blueValue;
                    count++;
                }
            }
        }

        // Calcula a média dos valores dos canais de cor
        int[] average = new int[3];
        average[0] = sum[0] / count;
        average[1] = sum[1] / count;
        average[2] = sum[2] / count;

        return average;
    }

    private static double[] getNeighborhoodIntensity(Bitmap bitmap, int x, int y) {
        int[] sum = new int[3];
        int count = 0;
        int janela = 3;

        // Percorre os pixels na vizinhança do pixel atual
        for (int i = x - janela; i <= x + janela; i++) {
            for (int j = y - janela; j <= y + janela; j++) {
                // Verifica se o pixel está dentro dos limites do bitmap
                if (i >= 0 && i < bitmap.getWidth() && j >= 0 && j < bitmap.getHeight()) {
                    // Obtém os valores dos canais de cor do pixel
                    int pixel = bitmap.getPixel(i, j);
                    int redValue = Color.red(pixel);
                    int greenValue = Color.green(pixel);
                    int blueValue = Color.blue(pixel);

                    // Soma os valores dos canais de cor
                    sum[0] += redValue;
                    sum[1] += greenValue;
                    sum[2] += blueValue;
                    count++;
                }
            }
        }
        // Calcula a média dos valores dos canais de cor
        double[] average = new double[3];
        average[0] = (double) sum[0] / count;
        average[1] = (double) sum[1] / count;
        average[2] = (double) sum[2] / count;

        return average;
    }

    private static int calculateNewValue(int value, double neighborhoodIntensity, double threshold) {
        double newValue;

        // Verifica se o valor está acima ou abaixo do limiar
        if (value >= neighborhoodIntensity) {
            newValue = value + threshold * (255 - value);
        } else {
            newValue = value;// - threshold * value;
        }

        // Limita o valor entre 0 e 255
        newValue = Math.max(0, Math.min(255, newValue));

        return (int) newValue;
    }

    private static int normalizeValue(int value, int minValue, int maxValue) {
        // Realiza o cálculo da normalização do valor
        double normalizedValue = (value - minValue) * 255.0 / (maxValue - minValue);

        // Limita o valor entre 0 e 255
        int newValue = (int) Math.max(0, Math.min(255, normalizedValue));

        return newValue;
    }

    private static int getGrayValue(int pixel) {
        int red = Color.red(pixel);
        int green = Color.green(pixel);
        int blue = Color.blue(pixel);
        //return (int) (red * 0.2989 + green * 0.587 + blue * 0.114);
        return (int) ((red + green + blue )/3);
    }


}